# TASK-PAY-06: Controlar Tentativas Reprovadas

## Metadata

| Campo | Valor |
|-------|-------|
| **ID** | TASK-PAY-06 |
| **Agente** | payment-agent |
| **Prioridade** | Media |
| **RF Relacionado** | RF0036 |
| **RN Relacionada** | RN0065 |

## Objetivo

Controlar tentativas de pagamento reprovadas consecutivas e bloquear o carrinho apos 3 reprovacoes, protegendo contra fraudes.

## Pre-Condicoes

- TASK-PAY-05 concluida com status REPROVADA
- Carrinho ainda ativo

## Regras de Negocio

| RN | Regra | Logica |
|----|-------|--------|
| RN0065 | 3 pagamentos REPROVADOS consecutivos bloqueiam carrinho | contador >= 3 → bloquear |

### Logica de Controle

```
SE pagamento.status == REPROVADA:
    carrinho.tentativasReprovadas++

    SE carrinho.tentativasReprovadas >= 3:
        carrinho.status = BLOQUEADO
        Lancar CarrinhoBloqueadoException

SE pagamento.status == APROVADA:
    carrinho.tentativasReprovadas = 0
```

## Especificacao Tecnica

### Backend (backend-agent)

#### CarrinhoService (adicionar)
```java
private static final int MAX_TENTATIVAS_REPROVADAS = 3;

/**
 * Incrementa contador de tentativas reprovadas
 * @throws CarrinhoBloqueadoException se atingir limite
 */
@Transactional
public void registrarTentativaReprovada(Long carrinhoId) {
    Carrinho carrinho = carrinhoRepository.findById(carrinhoId)
        .orElseThrow(() -> new CarrinhoNaoEncontradoException(carrinhoId));

    if (carrinho.getStatus() == StatusCarrinho.BLOQUEADO) {
        throw new CarrinhoBloqueadoException(
            "Este carrinho foi bloqueado devido a multiplas tentativas de pagamento reprovadas. " +
            "Entre em contato com o suporte."
        );
    }

    int tentativas = carrinho.getTentativasReprovadas() + 1;
    carrinho.setTentativasReprovadas(tentativas);

    if (tentativas >= MAX_TENTATIVAS_REPROVADAS) {
        carrinho.setStatus(StatusCarrinho.BLOQUEADO);
        carrinho.setDataBloqueio(LocalDateTime.now());
        carrinhoRepository.save(carrinho);

        // Log de seguranca
        logService.registrar(
            TipoLog.CARRINHO_BLOQUEADO,
            String.format("Carrinho %d bloqueado - %d tentativas reprovadas",
                carrinhoId, tentativas),
            carrinho.getCliente().getId()
        );

        throw new CarrinhoBloqueadoException(
            "Carrinho bloqueado apos " + MAX_TENTATIVAS_REPROVADAS +
            " tentativas de pagamento reprovadas. Entre em contato com o suporte."
        );
    }

    carrinhoRepository.save(carrinho);

    // Log de alerta
    logService.registrar(
        TipoLog.PAGAMENTO_REPROVADO,
        String.format("Tentativa %d/3 reprovada - Carrinho %d",
            tentativas, carrinhoId),
        carrinho.getCliente().getId()
    );
}

/**
 * Reseta contador apos pagamento aprovado
 */
@Transactional
public void resetarTentativasReprovadas(Long carrinhoId) {
    Carrinho carrinho = carrinhoRepository.findById(carrinhoId)
        .orElseThrow(() -> new CarrinhoNaoEncontradoException(carrinhoId));

    carrinho.setTentativasReprovadas(0);
    carrinhoRepository.save(carrinho);
}

/**
 * Verifica se carrinho esta bloqueado
 */
public void verificarCarrinhoBloqueado(Long carrinhoId) {
    Carrinho carrinho = carrinhoRepository.findById(carrinhoId)
        .orElseThrow(() -> new CarrinhoNaoEncontradoException(carrinhoId));

    if (carrinho.getStatus() == StatusCarrinho.BLOQUEADO) {
        throw new CarrinhoBloqueadoException(
            "Este carrinho esta bloqueado. Entre em contato com o suporte."
        );
    }
}

/**
 * Retorna tentativas restantes
 */
public int getTentativasRestantes(Long carrinhoId) {
    Carrinho carrinho = carrinhoRepository.findById(carrinhoId)
        .orElseThrow(() -> new CarrinhoNaoEncontradoException(carrinhoId));

    return MAX_TENTATIVAS_REPROVADAS - carrinho.getTentativasReprovadas();
}
```

#### Carrinho Entity (adicionar campos)
```java
@Column(name = "tentativas_reprovadas")
private Integer tentativasReprovadas = 0;

@Column(name = "data_bloqueio")
private LocalDateTime dataBloqueio;
```

#### StatusCarrinho Enum (adicionar)
```java
public enum StatusCarrinho {
    ABERTO("Aberto"),
    FINALIZADO("Finalizado"),
    BLOQUEADO("Bloqueado");

    private final String descricao;

    StatusCarrinho(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}
```

### Business Rules (business-rules-agent)

#### Excecao
```java
package com.les.jakebooks.exception;

public class CarrinhoBloqueadoException extends ValidacaoNegocioException {

    public CarrinhoBloqueadoException(String mensagem) {
        super(mensagem);
    }
}
```

### Frontend (frontend-agent)

#### CheckoutController (modificar processarPagamento)
```java
@PostMapping("/checkout/pagamento")
public String processarPagamento(
        @ModelAttribute PagamentoFormDTO form,
        HttpSession session,
        RedirectAttributes ra,
        Principal principal) {

    Long clienteId = getClienteId(principal);
    CheckoutDTO checkout = getCheckoutFromSession(session);

    try {
        // Verificar se carrinho esta bloqueado
        carrinhoService.verificarCarrinhoBloqueado(checkout.getCarrinhoId());

        // Processar pagamento...
        Pagamento pagamento = pagamentoService.processarPagamento(dto, clienteId);

        if (pagamento.getStatus() == StatusPagamento.APROVADA) {
            // Resetar tentativas
            carrinhoService.resetarTentativasReprovadas(checkout.getCarrinhoId());
            return "redirect:/checkout/finalizar";
        } else {
            // Incrementar tentativas
            try {
                carrinhoService.registrarTentativaReprovada(checkout.getCarrinhoId());
                int restantes = carrinhoService.getTentativasRestantes(checkout.getCarrinhoId());
                ra.addFlashAttribute("erro",
                    "Pagamento reprovado. Tentativas restantes: " + restantes);
                ra.addFlashAttribute("tentativasRestantes", restantes);
            } catch (CarrinhoBloqueadoException e) {
                ra.addFlashAttribute("erroBloqueio", e.getMessage());
                return "redirect:/checkout/bloqueado";
            }
            return "redirect:/checkout/pagamento";
        }

    } catch (CarrinhoBloqueadoException e) {
        ra.addFlashAttribute("erroBloqueio", e.getMessage());
        return "redirect:/checkout/bloqueado";
    } catch (Exception e) {
        ra.addFlashAttribute("erro", e.getMessage());
        return "redirect:/checkout/pagamento";
    }
}
```

#### Template: checkout/bloqueado.html
```html
<div class="container py-5">
    <div class="row justify-content-center">
        <div class="col-md-6">
            <div class="card border-danger">
                <div class="card-body text-center">
                    <div class="text-danger mb-4">
                        <i class="bi bi-shield-exclamation" style="font-size: 5rem;"></i>
                    </div>

                    <h2 class="text-danger">Carrinho Bloqueado</h2>

                    <p class="lead" th:text="${erroBloqueio}">
                        Este carrinho foi bloqueado devido a multiplas tentativas
                        de pagamento reprovadas.
                    </p>

                    <hr>

                    <p class="text-muted">
                        Por motivos de seguranca, seu carrinho foi temporariamente bloqueado.
                        Para desbloquear, entre em contato com nosso suporte.
                    </p>

                    <div class="d-grid gap-2 mt-4">
                        <a href="mailto:suporte@jakebooks.com" class="btn btn-outline-primary">
                            <i class="bi bi-envelope"></i> Contatar Suporte
                        </a>
                        <a th:href="@{/}" class="btn btn-secondary">
                            Voltar para Home
                        </a>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>
```

#### Template: checkout/pagamento.html (adicionar alertas)
```html
<!-- Alerta de tentativas restantes -->
<div th:if="${tentativasRestantes != null}" class="alert alert-warning">
    <i class="bi bi-exclamation-triangle"></i>
    <strong>Atencao!</strong>
    Voce tem <strong th:text="${tentativasRestantes}"></strong> tentativa(s) restante(s)
    antes do carrinho ser bloqueado.
</div>

<!-- Alerta de ultima tentativa -->
<div th:if="${tentativasRestantes == 1}" class="alert alert-danger">
    <i class="bi bi-shield-exclamation"></i>
    <strong>Ultima tentativa!</strong>
    Se este pagamento for reprovado, seu carrinho sera bloqueado.
    Verifique os dados com atencao.
</div>
```

## Fluxo de Execucao

```
1. Pagamento processado com status REPROVADA
2. Sistema incrementa tentativas:
   - tentativas = tentativas + 1
3. SE tentativas >= 3:
   - carrinho.status = BLOQUEADO
   - Registrar log de seguranca
   - Redirecionar para /checkout/bloqueado
4. SE tentativas < 3:
   - Exibir mensagem com tentativas restantes
   - Permitir nova tentativa
5. SE proximo pagamento APROVADA:
   - tentativas = 0 (reset)
```

## Criterios de Aceite

- [ ] Contador incrementa a cada reprovacao
- [ ] Contador reseta apos aprovacao
- [ ] Carrinho bloqueado apos 3 reprovacoes
- [ ] Mensagem clara de tentativas restantes
- [ ] Alerta especial na ultima tentativa
- [ ] Pagina de bloqueio com orientacoes
- [ ] Link para suporte na pagina de bloqueio
- [ ] Log de seguranca registrado
- [ ] Carrinho bloqueado nao permite nova tentativa

## Dependencias

- **Task Anterior:** TASK-PAY-05 (Processar Pagamento)
- **Modulo Futuro:** Admin pode desbloquear carrinhos

---

**Status:** Pendente
