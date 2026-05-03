# TASK-CHK-05: Gerenciar Estado da Transacao

## Metadata

| Campo | Valor |
|-------|-------|
| **ID** | TASK-CHK-05 |
| **Agente** | checkout-agent |
| **Prioridade** | Media |
| **RF Relacionado** | RF0033, RF0037 |
| **RN Relacionada** | RN0065 |

## Objetivo

Garantir atomicidade e consistencia durante todo o processo de checkout, implementando controle transacional robusto com rollback automatico em caso de falhas e gerenciamento de tentativas reprovadas.

## Pre-Condicoes

- Todas as tasks anteriores (CHK-01 a CHK-04) implementadas
- Mecanismo de transacao configurado no Spring
- Sistema de log operacional

## Pos-Condicoes

- Transacao atomica em todo o fluxo de checkout
- Estado consistente independente de falhas
- Historico de tentativas controlado (RN0065)
- Rollback automatico quando necessario

## Aspectos de Gestao

### 1. Atomicidade Transacional
- Todo o checkout executado em uma unica transacao
- Rollback automatico em qualquer falha
- Estado sempre consistente

### 2. Controle de Tentativas Reprovadas (RN0065)
- Rastreamento de pagamentos reprovados consecutivos  - Bloqueio automatico apos 3 tentativas reprovadas
- Reset do contador apos pagamento aprovado

### 3. Recuperacao de Estado
- Capacidade de recuperar transacoes parciais
- Limpeza de dados inconsistentes
- Manutencao da integridade referencial

## Especificacao Tecnica

### Backend (backend-agent)

#### TransacaoCheckoutService
```java
package com.les.jakebooks.service;

@Service
@RequiredArgsConstructor
public class TransacaoCheckoutService {

    private final CompraService compraService;
    private final CarrinhoService carrinhoService;
    private final LogTransacaoService logService;

    /**
     * Executa checkout completo com controle transacional
     * Garante atomicidade de todo o processo
     */
    @Transactional
    public ResultadoCheckoutDTO executarCheckoutCompleto(CheckoutTransacaoDTO dados) {
        String transacaoId = gerarTransacaoId();

        logService.iniciarTransacao(transacaoId, dados.getCarrinhoId());

        try {
            // 1. Validar pre-condicoes
            validarEstadoTransacao(dados);

            // 2. Processar pagamento
            ResultadoPagamentoDTO resultadoPagamento = processarPagamentoComControle(dados, transacaoId);

            if (resultadoPagamento.getStatus() == StatusPagamento.REPROVADA) {
                return tratarPagamentoReprovado(dados, resultadoPagamento, transacaoId);
            }

            // 3. Finalizar compra (conversao + baixa estoque)
            ResultadoCompraDTO resultadoCompra = compraService.finalizarCompra(
                FinalizacaoCompraDTO.builder()
                    .carrinhoId(dados.getCarrinhoId())
                    .enderecoEntrega(dados.getEnderecoEntrega())
                    .pagamento(resultadoPagamento.getPagamento())
                    .valorFrete(dados.getValorFrete())
                    .build()
            );

            // 4. Reset contador de tentativas (sucesso)
            resetarTentativasReprovadas(dados.getCarrinhoId());

            logService.finalizarTransacao(transacaoId, StatusResultado.SUCESSO);

            return ResultadoCheckoutDTO.builder()
                .transacaoId(transacaoId)
                .status(StatusResultado.SUCESSO)
                .pedidoId(resultadoCompra.getPedidoId())
                .mensagem("Compra finalizada com sucesso")
                .build();

        } catch (Exception e) {
            logService.finalizarTransacao(transacaoId, StatusResultado.FALHA, e.getMessage());
            throw new TransacaoCheckoutException(
                String.format("Falha no checkout [Transacao: %s]: %s", transacaoId, e.getMessage())
            );
        }
    }

    private void validarEstadoTransacao(CheckoutTransacaoDTO dados) {
        Carrinho carrinho = carrinhoService.buscarPorId(dados.getCarrinhoId());

        if (carrinho.getStatus() != StatusCarrinho.ABERTO) {
            throw new CarrinhoIndisponivelException(
                "Carrinho nao esta disponivel para checkout"
            );
        }

        // Verificar se carrinho esta bloqueado por tentativas reprovadas
        if (isCarrinhoBloqueado(carrinho.getId())) {
            throw new CarrinhoBloqueadoPagamentoException(
                "Carrinho bloqueado devido a multiplas tentativas de pagamento reprovadas"
            );
        }
    }

    private ResultadoCheckoutDTO tratarPagamentoReprovado(
            CheckoutTransacaoDTO dados,
            ResultadoPagamentoDTO resultadoPagamento,
            String transacaoId) {

        // Incrementar contador de tentativas
        incrementarTentativasReprovadas(dados.getCarrinhoId());

        logService.registrarTentativaReprovada(transacaoId, dados.getCarrinhoId());

        int totalTentativas = contarTentativasReprovadas(dados.getCarrinhoId());

        // Verificar limite de tentativas (RN0065)
        if (totalTentativas >= 3) {
            bloquearCarrinho(dados.getCarrinhoId());
            logService.registrarBloqueioCarrinho(dados.getCarrinhoId(), totalTentativas);

            return ResultadoCheckoutDTO.builder()
                .transacaoId(transacaoId)
                .status(StatusResultado.BLOQUEADO)
                .mensagem("Carrinho bloqueado. Entre em contato com o suporte.")
                .tentativasRestantes(0)
                .build();
        }

        return ResultadoCheckoutDTO.builder()
            .transacaoId(transacaoId)
            .status(StatusResultado.PAGAMENTO_REPROVADO)
            .mensagem(resultadoPagamento.getMensagem())
            .tentativasRestantes(3 - totalTentativas)
            .build();
    }

    private String gerarTransacaoId() {
        return "TXN_" + System.currentTimeMillis() + "_" +
               ThreadLocalRandom.current().nextInt(1000, 9999);
    }
}
```

#### ControleReprovasService
```java
package com.les.jakebooks.service;

@Service
@RequiredArgsConstructor
public class ControleReprovasService {

    private final HistoricoReprovaRepository historicoReprovaRepository;
    private final CarrinhoService carrinhoService;

    /**
     * Implementa RN0065: 3 pagamentos REPROVADOS consecutivos bloqueiam carrinho
     */
    public void incrementarTentativasReprovadas(Long carrinhoId) {
        HistoricoReprova historico = historicoReprovaRepository
            .findByCarrinhoIdAndAtivo(carrinhoId, true)
            .orElse(new HistoricoReprova(carrinhoId));

        historico.incrementarTentativa();
        historicoReprovaRepository.save(historico);
    }

    public int contarTentativasReprovadas(Long carrinhoId) {
        return historicoReprovaRepository
            .findByCarrinhoIdAndAtivo(carrinhoId, true)
            .map(HistoricoReprova::getTentativas)
            .orElse(0);
    }

    public boolean isCarrinhoBloqueado(Long carrinhoId) {
        return contarTentativasReprovadas(carrinhoId) >= 3;
    }

    public void resetarTentativasReprovadas(Long carrinhoId) {
        historicoReprovaRepository.findByCarrinhoIdAndAtivo(carrinhoId, true)
            .ifPresent(historico -> {
                historico.setAtivo(false);
                historico.setDataReset(new Date());
                historicoReprovaRepository.save(historico);
            });
    }

    public void bloquearCarrinho(Long carrinhoId) {
        carrinhoService.alterarStatusCarrinho(carrinhoId, StatusCarrinho.BLOQUEADO);
    }
}
```

#### HistoricoReprova (Entidade para controle)
```java
package com.les.jakebooks.domain;

@Entity
@Table(name = "historico_reprova")
@Data
public class HistoricoReprova {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long carrinhoId;

    @Column(nullable = false)
    private Integer tentativas = 0;

    @Column(nullable = false)
    private Boolean ativo = true;

    @Column(nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date dataInicio;

    @Temporal(TemporalType.TIMESTAMP)
    private Date dataReset;

    public HistoricoReprova() {
        this.dataInicio = new Date();
    }

    public HistoricoReprova(Long carrinhoId) {
        this();
        this.carrinhoId = carrinhoId;
    }

    public void incrementarTentativa() {
        this.tentativas++;
    }
}
```

### Status de Carrinho Atualizado

#### StatusCarrinho (Enum atualizado)
```java
package com.les.jakebooks.model.enums;

public enum StatusCarrinho {
    ABERTO,
    EXPIRADO,
    FINALIZADO,
    BLOQUEADO  // Novo status para RN0065
}
```

### Recuperacao de Transacoes

#### RecuperacaoTransacaoService
```java
package com.les.jakebooks.service;

@Service
@RequiredArgsConstructor
public class RecuperacaoTransacaoService {

    private final CarrinhoRepository carrinhoRepository;
    private final PedidoRepository pedidoRepository;

    /**
     * Limpa transacoes inconsistentes
     * Executado por job agendado
     */
    @Scheduled(fixedDelay = 3600000) // 1 hora
    @Transactional
    public void limparTransacoesPendentes() {
        Date limiteTempo = DateUtils.addMinutes(new Date(), -30);

        // Buscar carrinhos em estados inconsistentes
        List<Carrinho> carrinhosInconsistentes = carrinhoRepository
            .findCarrinhosInconsistentes(limiteTempo);

        for (Carrinho carrinho : carrinhosInconsistentes) {
            recuperarCarrinho(carrinho);
        }
    }

    private void recuperarCarrinho(Carrinho carrinho) {
        // Verificar se existe pedido criado mas nao finalizado
        Optional<Pedido> pedidoPendente = pedidoRepository
            .findByCarrinhoIdAndStatus(carrinho.getId(), StatusPedido.EM_PROCESSAMENTO);

        if (pedidoPendente.isPresent() && carrinho.getStatus() != StatusCarrinho.FINALIZADO) {
            // Sincronizar estados
            carrinhoService.finalizarCarrinho(carrinho.getId());
        } else if (carrinho.getStatus() == StatusCarrinho.FINALIZADO && pedidoPendente.isEmpty()) {
            // Reverter carrinho para ABERTO se nao ha pedido
            carrinhoService.alterarStatusCarrinho(carrinho.getId(), StatusCarrinho.ABERTO);
        }
    }
}
```

### Frontend (frontend-agent)

#### Tratamento de Estados de Transacao

Template: `checkout/bloqueado.html`
```html
<div class="container py-4">
    <div class="alert alert-warning text-center">
        <h4>Carrinho Temporariamente Bloqueado</h4>
        <p>Devido a múltiplas tentativas de pagamento reprovadas, seu carrinho foi temporariamente bloqueado.</p>
        <p>Para continuar com sua compra, entre em contato com nosso suporte:</p>

        <div class="mt-3">
            <p><strong>Email:</strong> suporte@jakebooks.com</p>
            <p><strong>Telefone:</strong> (11) 1234-5678</p>
        </div>
    </div>

    <div class="text-center">
        <a href="/livros" class="btn btn-primary">Continuar Comprando</a>
        <a href="/help/contact" class="btn btn-outline-secondary">Falar com Suporte</a>
    </div>
</div>
```

#### JavaScript para Controle de Sessao
```javascript
// checkout-transaction.js
class CheckoutTransaction {
    constructor(transacaoId) {
        this.transacaoId = transacaoId;
        this.startTime = Date.now();
    }

    // Monitora tempo de transacao ativa
    monitorarTimeout() {
        const timeout = 10 * 60 * 1000; // 10 minutos

        setTimeout(() => {
            if (this.isActive()) {
                this.alertarTimeout();
            }
        }, timeout);
    }

    alertarTimeout() {
        alert('Sua sessão de checkout está prestes a expirar. Complete a compra em breve.');
    }

    isActive() {
        return Date.now() - this.startTime < 15 * 60 * 1000; // 15 min max
    }
}
```

## Criterios de Aceite

- [ ] Todo checkout executado em transacao atomica
- [ ] Rollback automatico em caso de falha
- [ ] Contador de tentativas reprovadas funcional (RN0065)
- [ ] Bloqueio automatico apos 3 tentativas consecutivas
- [ ] Reset do contador apos pagamento aprovado
- [ ] Recovery automatico de transacoes pendentes
- [ ] Log completo de todas as operacoes transacionais
- [ ] Estados sempre consistentes entre carrinho e pedido
- [ ] Mensagens claras para usuario em cada situacao

## Dependencias

- **Todas as tasks anteriores:** CHK-01 a CHK-04 devem estar funcionais
- **backend-agent:** Implementar services de controle transacional
- **business-rules-agent:** Criar excecoes de transacao
- **domain-agent:** Adicionar entidade HistoricoReprova e status BLOQUEADO

## Fluxo de Integracao

```
[INICIAR TRANSACAO]
        |
        v
[Validar Estado] --> BLOQUEADO? --> Rejeitar
        |
        v
[Processar Pagamento]
        |
    APROVADA? --> SIM --> [Finalizar Compra] --> [Reset Tentativas] --> SUCESSO
        |
      NAO --> [Incrementar Tentativas]
        |
        v
   >= 3 TENTATIVAS? --> SIM --> [Bloquear Carrinho] --> BLOQUEADO
        |
      NAO --> PAGAMENTO_REPROVADO (permitir retry)
        |
        v
[Log Transacao] --> FIM
```

---

**Status:** Pendente