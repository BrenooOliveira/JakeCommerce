# TASK-PAY-01: Orquestrar Selecao de Pagamento

## Metadata

| Campo | Valor |
|-------|-------|
| **ID** | TASK-PAY-01 |
| **Agente** | payment-agent |
| **Prioridade** | Alta |
| **RF Relacionado** | RF0036 |
| **RN Relacionada** | RN0035 |

## Objetivo

Coordenar o fluxo completo de selecao de pagamento, apresentando cupons disponiveis e permitindo a selecao de cartoes, garantindo a ordem correta de aplicacao.

## Pre-Condicoes

- TASK-SHP-03 concluida (frete calculado)
- Cliente na etapa de pagamento do checkout
- Valor total do pedido (produtos + frete) calculado

## Regras de Negocio

| RN | Regra | Logica |
|----|-------|--------|
| RN0035 | Consumir cupons antes do cartao | Aplicar cupons primeiro, cartao paga restante |

### Ordem de Pagamento

```
1. Cupons de Troca (valor deduzido do total)
2. Cupom Promocional (valor deduzido do total)
3. Cartao(s) (paga o restante)
```

## Especificacao Tecnica

### Backend (backend-agent)

#### PagamentoService
```java
package com.les.jakebooks.service;

@Service
public class PagamentoService {

    /**
     * Monta opcoes de pagamento para o cliente
     * @param clienteId ID do cliente
     * @param valorTotal Valor total a pagar (produtos + frete)
     * @return DTO com cupons disponiveis e cartoes do cliente
     */
    public OpcoesPagamentoDTO montarOpcoesPagamento(Long clienteId, BigDecimal valorTotal) {
        // Buscar cupons de troca disponiveis
        List<CupomDTO> cuponsTroca = cupomService.listarCuponsTrocaAtivos(clienteId);

        // Calcular saldo total em cupons de troca
        BigDecimal saldoCuponsTroca = cuponsTroca.stream()
            .map(CupomDTO::valor)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Buscar cartoes do cliente
        List<CartaoDTO> cartoes = cartaoService.listarCartoesAtivos(clienteId);

        return new OpcoesPagamentoDTO(
            valorTotal,
            cuponsTroca,
            saldoCuponsTroca,
            cartoes,
            null  // cupom promocional sera inserido pelo usuario
        );
    }

    /**
     * Calcula valor restante apos aplicacao de cupons
     */
    public BigDecimal calcularValorRestante(
            BigDecimal valorTotal,
            List<Long> cuponsIds,
            String codigoCupomPromocional) {

        BigDecimal valorCupons = BigDecimal.ZERO;

        // Somar cupons de troca
        for (Long cupomId : cuponsIds) {
            Cupom cupom = cupomRepository.findById(cupomId)
                .orElseThrow(() -> new CupomNaoEncontradoException(cupomId));
            valorCupons = valorCupons.add(cupom.getValor());
        }

        // Adicionar cupom promocional se informado
        if (codigoCupomPromocional != null && !codigoCupomPromocional.isBlank()) {
            Cupom promocional = cupomRepository.findByCodigo(codigoCupomPromocional)
                .orElseThrow(() -> new CupomInvalidoException("Cupom promocional invalido"));
            valorCupons = valorCupons.add(promocional.getValor());
        }

        // Calcular restante (pode ser negativo = excedente)
        return valorTotal.subtract(valorCupons);
    }
}
```

#### OpcoesPagamentoDTO
```java
package com.les.jakebooks.dto;

public record OpcoesPagamentoDTO(
    BigDecimal valorTotal,
    List<CupomDTO> cuponsTroca,
    BigDecimal saldoCuponsTroca,
    List<CartaoDTO> cartoes,
    CupomDTO cupomPromocional
) {
    public boolean temSaldoSuficienteEmCupons() {
        return saldoCuponsTroca.compareTo(valorTotal) >= 0;
    }
}
```

#### CupomDTO
```java
package com.les.jakebooks.dto;

public record CupomDTO(
    Long id,
    String codigo,
    BigDecimal valor,
    TipoCupom tipo,
    LocalDate dataValidade,
    boolean ativo
) {}
```

### Frontend (frontend-agent)

#### CheckoutController (adicionar)
```java
@GetMapping("/checkout/pagamento")
public String exibirPagamento(HttpSession session, Model model, Principal principal) {
    Long clienteId = getClienteId(principal);
    CheckoutDTO checkout = getCheckoutFromSession(session);

    // Calcular valor total (produtos + frete)
    BigDecimal valorProdutos = carrinhoService.calcularTotal(clienteId);
    BigDecimal valorFrete = checkout.getFrete().valor();
    BigDecimal valorTotal = valorProdutos.add(valorFrete);

    // Montar opcoes de pagamento
    OpcoesPagamentoDTO opcoes = pagamentoService.montarOpcoesPagamento(clienteId, valorTotal);

    model.addAttribute("opcoes", opcoes);
    model.addAttribute("valorProdutos", valorProdutos);
    model.addAttribute("valorFrete", valorFrete);
    model.addAttribute("valorTotal", valorTotal);

    return "checkout/pagamento";
}
```

#### Template: checkout/pagamento.html
```html
<div class="container">
    <h2>Forma de Pagamento</h2>

    <form th:action="@{/checkout/pagamento}" method="post" id="formPagamento">
        <!-- Resumo do Valor -->
        <div class="card mb-3">
            <div class="card-header">Resumo</div>
            <div class="card-body">
                <div class="d-flex justify-content-between">
                    <span>Produtos:</span>
                    <span th:text="${#numbers.formatCurrency(valorProdutos)}"></span>
                </div>
                <div class="d-flex justify-content-between">
                    <span>Frete:</span>
                    <span th:text="${opcoes.frete.gratis} ? 'Gratis' : ${#numbers.formatCurrency(valorFrete)}"></span>
                </div>
                <hr>
                <div class="d-flex justify-content-between fw-bold">
                    <span>Total:</span>
                    <span id="valorTotal" th:text="${#numbers.formatCurrency(valorTotal)}"></span>
                </div>
            </div>
        </div>

        <!-- Cupons de Troca -->
        <div class="card mb-3" th:if="${!opcoes.cuponsTroca.empty}">
            <div class="card-header">
                Cupons de Troca Disponiveis
                <span class="badge bg-success"
                      th:text="${#numbers.formatCurrency(opcoes.saldoCuponsTroca)}"></span>
            </div>
            <div class="card-body">
                <div th:each="cupom : ${opcoes.cuponsTroca}" class="form-check">
                    <input type="checkbox"
                           class="form-check-input cupom-troca"
                           name="cuponsIds"
                           th:value="${cupom.id}"
                           th:data-valor="${cupom.valor}">
                    <label class="form-check-label">
                        <span th:text="${cupom.codigo}"></span> -
                        <strong th:text="${#numbers.formatCurrency(cupom.valor)}"></strong>
                    </label>
                </div>
            </div>
        </div>

        <!-- Cupom Promocional -->
        <div class="card mb-3">
            <div class="card-header">Cupom Promocional</div>
            <div class="card-body">
                <div class="input-group">
                    <input type="text" name="codigoCupomPromocional"
                           class="form-control" placeholder="Digite o codigo do cupom">
                    <button type="button" class="btn btn-outline-secondary"
                            onclick="validarCupom()">Aplicar</button>
                </div>
                <div id="msgCupom" class="mt-2"></div>
            </div>
        </div>

        <!-- Valor Restante -->
        <div class="card mb-3">
            <div class="card-body">
                <div class="d-flex justify-content-between fw-bold">
                    <span>Valor a pagar com cartao:</span>
                    <span id="valorRestante" th:text="${#numbers.formatCurrency(valorTotal)}"></span>
                </div>
            </div>
        </div>

        <!-- Cartoes (exibir apenas se valor restante > 0) -->
        <div id="secaoCartoes" class="card mb-3">
            <div class="card-header">Selecione os Cartoes</div>
            <div class="card-body">
                <!-- Conteudo via TASK-PAY-04 -->
            </div>
        </div>

        <!-- Acoes -->
        <div class="mt-3">
            <a th:href="@{/checkout/frete}" class="btn btn-secondary">Voltar</a>
            <button type="submit" class="btn btn-primary">Finalizar Pagamento</button>
        </div>
    </form>
</div>

<script>
// Atualizar valor restante ao selecionar cupons
document.querySelectorAll('.cupom-troca').forEach(cb => {
    cb.addEventListener('change', atualizarValorRestante);
});

function atualizarValorRestante() {
    let valorTotal = [[${valorTotal}]];
    let desconto = 0;

    document.querySelectorAll('.cupom-troca:checked').forEach(cb => {
        desconto += parseFloat(cb.dataset.valor);
    });

    // TODO: adicionar cupom promocional se validado

    let restante = Math.max(0, valorTotal - desconto);
    document.getElementById('valorRestante').textContent =
        'R$ ' + restante.toFixed(2).replace('.', ',');

    // Esconder cartoes se restante = 0
    document.getElementById('secaoCartoes').style.display =
        restante > 0 ? 'block' : 'none';
}
</script>
```

## Fluxo de Execucao

```
1. Cliente acessa /checkout/pagamento
2. Sistema carrega:
   - Valor total (produtos + frete)
   - Cupons de troca do cliente
   - Cartoes do cliente
3. Cliente seleciona cupons de troca (checkboxes)
4. Sistema atualiza valor restante em tempo real
5. Cliente insere codigo de cupom promocional (opcional)
6. Sistema valida e aplica cupom promocional
7. SE valor restante > 0:
   - Exibe secao de cartoes (TASK-PAY-04)
8. SE valor restante <= 0:
   - Esconde secao de cartoes
   - Gerar cupom excedente se necessario (TASK-PAY-03)
9. Cliente clica em "Finalizar Pagamento"
10. Fluxo continua em TASK-PAY-05
```

## Criterios de Aceite

- [ ] Exibe cupons de troca disponiveis com valores
- [ ] Permite selecionar multiplos cupons de troca
- [ ] Campo para inserir cupom promocional
- [ ] Validacao de cupom promocional via AJAX
- [ ] Valor restante atualiza em tempo real
- [ ] Secao de cartoes aparece apenas se restante > 0
- [ ] Resumo claro de produtos + frete + descontos

## Dependencias

- **Task Anterior:** TASK-SHP-03 (Calcular Frete)
- **Tasks Relacionadas:**
  - TASK-PAY-02 (Aplicar Cupons)
  - TASK-PAY-03 (Gerenciar Excedente)
  - TASK-PAY-04 (Selecionar Cartoes)

---

**Status:** Pendente
