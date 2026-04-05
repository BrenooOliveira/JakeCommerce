# TASK-CHK-04: Coordenar Baixa de Estoque

## Metadata

| Campo | Valor |
|-------|-------|
| **ID** | TASK-CHK-04 |
| **Agente** | checkout-agent |
| **Prioridade** | Alta |
| **RF Relacionado** | RF0053 |
| **RN Relacionada** | RN0028 |

## Objetivo

Executar a baixa de estoque de forma transacional e segura, garantindo que o estoque seja decrementado apenas apos pagamento aprovado e conversao do carrinho em pedido.

## Pre-Condicoes

- Pedido criado com status EM_PROCESSAMENTO
- Pagamento com status APROVADA (RN0028)
- Estoque validado no momento da conversao

## Pos-Condicoes

- Quantidade decrementada no estoque de cada livro
- Log da operacao registrado (RNF0012)
- Em caso de falha, reversao da operacao

## Regra de Negocio Critica

**RN0028:** Baixa estoque apenas apos pagamento APROVADO
- Operacao executada SOMENTE apos confirmacao do pagamento
- Atomicidade garantida: ou baixa tudo ou nada
- Reversao automatica em caso de falha

## Especificacao Tecnica

### Backend (backend-agent)

#### EstoqueService
```java
package com.les.jakebooks.service;

@Service
@RequiredArgsConstructor
@Transactional
public class EstoqueService {

    private final EstoqueRepository estoqueRepository;
    private final LogTransacaoService logService;
    private final CompraValidator compraValidator;

    /**
     * Executa baixa de estoque para todos os itens do pedido
     * RN0028 - Baixa estoque apenas após pagamento APROVADO
     * RF0053 - Baixa automática após venda
     */
    public void executarBaixaPorPedido(Pedido pedido) {
        // Validar pre-condicoes
        validarPreCondicoesBaixa(pedido);

        // Re-validar estoque antes da baixa (seguranca adicional)
        revalidarEstoqueDisponivel(pedido.getItens());

        // Executar baixa para cada item
        List<MovimentoEstoque> movimentos = new ArrayList<>();

        for (ItemPedido item : pedido.getItens()) {
            MovimentoEstoque movimento = executarBaixaItem(item);
            movimentos.add(movimento);
        }

        // Registrar log da operacao (RNF0012)
        logService.registrarBaixaEstoque(pedido, movimentos);
    }

    private void validarPreCondicoesBaixa(Pedido pedido) {
        if (pedido.getPagamento().getStatus() != StatusPagamento.APROVADA) {
            throw new PagamentoNaoAprovadoException(
                "Baixa de estoque permitida apenas para pagamentos aprovados"
            );
        }

        if (pedido.getStatus() != StatusPedido.EM_PROCESSAMENTO) {
            throw new StatusPedidoInvalidoException(
                "Pedido deve estar EM_PROCESSAMENTO para baixa de estoque"
            );
        }
    }

    private void revalidarEstoqueDisponivel(List<ItemPedido> itens) {
        for (ItemPedido item : itens) {
            Estoque estoque = estoqueRepository.findByLivroIdWithLock(item.getLivro().getId())
                .orElseThrow(() -> new EstoqueNaoEncontradoException(
                    "Estoque nao encontrado para livro: " + item.getLivro().getTitulo()
                ));

            if (estoque.getQuantidade() < item.getQuantidade()) {
                throw new EstoqueInsuficienteParaBaixaException(
                    String.format("Estoque insuficiente para baixa. Livro: %s, Disponivel: %d, Necessario: %d",
                        item.getLivro().getTitulo(),
                        estoque.getQuantidade(),
                        item.getQuantidade())
                );
            }
        }
    }

    private MovimentoEstoque executarBaixaItem(ItemPedido item) {
        Estoque estoque = estoqueRepository.findByLivroIdWithLock(item.getLivro().getId())
            .orElseThrow(() -> new EstoqueNaoEncontradoException(
                "Estoque nao encontrado"
            ));

        // Calcular nova quantidade
        int quantidadeAnterior = estoque.getQuantidade();
        int novaQuantidade = quantidadeAnterior - item.getQuantidade();

        // Validar se ainda tem estoque (double-check)
        if (novaQuantidade < 0) {
            throw new EstoqueInsuficienteParaBaixaException(
                String.format("Tentativa de baixa resulta em estoque negativo. Livro: %s, Atual: %d, Baixa: %d",
                    item.getLivro().getTitulo(),
                    quantidadeAnterior,
                    item.getQuantidade())
            );
        }

        // Executar baixa
        estoque.setQuantidade(novaQuantidade);
        estoque = estoqueRepository.save(estoque);

        // Retornar dados do movimento para log
        return MovimentoEstoque.builder()
            .livroId(item.getLivro().getId())
            .tituloLivro(item.getLivro().getTitulo())
            .quantidadeAnterior(quantidadeAnterior)
            .quantidadeBaixa(item.getQuantidade())
            .quantidadeNova(novaQuantidade)
            .dataMovimento(new Date())
            .build();
    }

    /**
     * Reverter baixa de estoque em caso de necessidade
     * Usado apenas em casos excepcionais de inconsistencia
     */
    public void reverterBaixaPorPedido(Pedido pedido, String motivo) {
        for (ItemPedido item : pedido.getItens()) {
            Estoque estoque = estoqueRepository.findByLivroId(item.getLivro().getId())
                .orElseThrow(() -> new EstoqueNaoEncontradoException(
                    "Estoque nao encontrado para reversao"
                ));

            // Adicionar quantidade de volta
            estoque.setQuantidade(estoque.getQuantidade() + item.getQuantidade());
            estoqueRepository.save(estoque);
        }

        // Registrar log da reversao
        logService.registrarReversaoEstoque(pedido, motivo);
    }
}
```

#### MovimentoEstoque (DTO para log)
```java
package com.les.jakebooks.dto;

@Data
@Builder
public class MovimentoEstoque {
    private Long livroId;
    private String tituloLivro;
    private Integer quantidadeAnterior;
    private Integer quantidadeBaixa;
    private Integer quantidadeNova;
    private Date dataMovimento;
}
```

#### LogTransacaoService
```java
package com.les.jakebooks.service;

@Service
@RequiredArgsConstructor
public class LogTransacaoService {

    private final Logger logger = LoggerFactory.getLogger(LogTransacaoService.class);

    /**
     * Registra log da baixa de estoque conforme RNF0012
     * Log deve conter: data, hora, usuario, dados alterados
     */
    public void registrarBaixaEstoque(Pedido pedido, List<MovimentoEstoque> movimentos) {
        String logMessage = String.format(
            "[BAIXA_ESTOQUE] Pedido ID: %d | Cliente: %s | Data: %s | Itens: %d",
            pedido.getId(),
            pedido.getCliente().getNome(),
            formatarData(new Date()),
            movimentos.size()
        );

        logger.info(logMessage);

        // Log detalhado para cada movimento
        for (MovimentoEstoque mov : movimentos) {
            String detalhe = String.format(
                "[MOVIMENTO] Livro: %s | ID: %d | Antes: %d | Baixa: %d | Depois: %d",
                mov.getTituloLivro(),
                mov.getLivroId(),
                mov.getQuantidadeAnterior(),
                mov.getQuantidadeBaixa(),
                mov.getQuantidadeNova()
            );
            logger.info(detalhe);
        }
    }

    public void registrarReversaoEstoque(Pedido pedido, String motivo) {
        String logMessage = String.format(
            "[REVERSAO_ESTOQUE] Pedido ID: %d | Motivo: %s | Data: %s",
            pedido.getId(),
            motivo,
            formatarData(new Date())
        );
        logger.warn(logMessage);
    }

    private String formatarData(Date data) {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(data);
    }
}
```

### Repository

#### EstoqueRepository (Atualizado)
```java
package com.les.jakebooks.repository;

@Repository
public interface EstoqueRepository extends JpaRepository<Estoque, Long> {

    Optional<Estoque> findByLivroId(Long livroId);

    Optional<Estoque> findByLivro(Livro livro);

    /**
     * Busca estoque com lock pessimista para operacoes de baixa
     * Evita condicoes de corrida em alta concorrencia
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT e FROM Estoque e WHERE e.livro.id = :livroId")
    Optional<Estoque> findByLivroIdWithLock(@Param("livroId") Long livroId);

    /**
     * Busca estoques baixos para alertas
     */
    @Query("SELECT e FROM Estoque e WHERE e.quantidade <= :limite")
    List<Estoque> findEstoqueBaixo(@Param("limite") Integer limite);
}
```

### Business Rules (business-rules-agent)

#### Excecoes
```java
package com.les.jakebooks.exception;

public class PagamentoNaoAprovadoException extends RuntimeException {
    public PagamentoNaoAprovadoException(String message) {
        super(message);
    }
}

public class StatusPedidoInvalidoException extends RuntimeException {
    public StatusPedidoInvalidoException(String message) {
        super(message);
    }
}

public class EstoqueInsuficienteParaBaixaException extends RuntimeException {
    public EstoqueInsuficienteParaBaixaException(String message) {
        super(message);
    }
}

public class EstoqueNaoEncontradoException extends RuntimeException {
    public EstoqueNaoEncontradoException(String message) {
        super(message);
    }
}
```

### Frontend (frontend-agent)

#### Tratamento de Erro na Baixa
```java
// No CheckoutController ou CompraController

@ExceptionHandler(EstoqueInsuficienteParaBaixaException.class)
public String handleEstoqueInsuficienteBaixa(
        EstoqueInsuficienteParaBaixaException e,
        RedirectAttributes ra) {

    // Log do erro
    logger.error("Falha na baixa de estoque: {}", e.getMessage());

    // Redirecionar com mensagem de erro
    ra.addFlashAttribute("erro",
        "Erro ao finalizar compra: estoque insuficiente. Entre em contato com o suporte."
    );

    return "redirect:/help/contact";
}
```

Template: `error/estoque-falha.html`
```html
<div class="container py-4">
    <div class="alert alert-danger">
        <h4>Erro na Finalizacao da Compra</h4>
        <p>Ocorreu um problema com o estoque durante a finalizacao. Nossa equipe foi notificada.</p>
        <p>Por favor, entre em contato conosco pelo email: <strong>suporte@jakebooks.com</strong></p>
    </div>

    <div class="text-center">
        <a href="/carrinho" class="btn btn-primary">Revisar Carrinho</a>
        <a href="/livros" class="btn btn-secondary">Continuar Comprando</a>
    </div>
</div>
```

## Criterios de Aceite

- [ ] Baixa executada apenas apos pagamento APROVADO (RN0028)
- [ ] Estoque re-validado antes da baixa (seguranca)
- [ ] Lock pessimista aplicado para prevenir corrida
- [ ] Quantidade decrementada corretamente para cada item
- [ ] Log detalhado da operacao registrado (RNF0012)
- [ ] Excecoes tratadas adequadamente
- [ ] Reversao disponivel para casos excepcionais
- [ ] Operacao atomica (tudo ou nada)
- [ ] Mensagens de erro claras para o cliente

## Dependencias

- **TASK-CHK-03:** Pedido deve estar criado
- **backend-agent:** Implementar EstoqueService e LogTransacaoService
- **business-rules-agent:** Criar excecoes listadas
- **payment-agent:** Pagamento deve estar APROVADO

## Fluxo de Integracao

```
Pedido.EM_PROCESSAMENTO + Pagamento.APROVADA
                |
                v
EstoqueService.executarBaixaPorPedido()
                |
                +-- validarPreCondicoesBaixa()
                |
                +-- revalidarEstoqueDisponivel()
                |
                +-- Para cada ItemPedido:
                |   |-- findByLivroIdWithLock() (lock pessimista)
                |   |-- decrementar quantidade
                |   +-- salvar estoque
                |
                +-- registrarBaixaEstoque() (log)
                |
                v
            BAIXA CONCLUIDA
```

---

**Status:** Pendente