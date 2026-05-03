# TASK-SHP-03: Calcular Frete

## Metadata

| Campo | Valor |
|-------|-------|
| **ID** | TASK-SHP-03 |
| **Agente** | shipping-agent |
| **Prioridade** | Alta |
| **RF Relacionado** | RF0034 |
| **RN Relacionada** | RN0064 |

## Objetivo

Calcular o valor do frete baseado no endereco de entrega selecionado e no valor total do pedido, aplicando frete gratis quando aplicavel.

## Pre-Condicoes

- TASK-SHP-02 concluida
- Endereco de entrega selecionado
- Valor total do carrinho calculado

## Regras de Negocio

| RN | Regra | Logica |
|----|-------|--------|
| RN0064 | Pedido minimo R$20 para frete gratis | valorPedido >= 20.00 ? frete = 0 |

### Tabela de Frete (Simulacao Academica)

| Regiao | Condicao | Valor | Prazo |
|--------|----------|-------|-------|
| Frete Gratis | valorPedido >= R$20 | R$ 0,00 | Prazo normal |
| Mesma Cidade | CEP mesmo prefixo | R$ 5,00 | 2-3 dias |
| Mesmo Estado | CEP mesmo estado | R$ 10,00 | 5-7 dias |
| Outro Estado | Demais casos | R$ 15,00 | 10-15 dias |

## Especificacao Tecnica

### Backend (backend-agent)

#### FreteService
```java
package com.les.jakebooks.service;

@Service
public class FreteService {

    private static final BigDecimal VALOR_MINIMO_FRETE_GRATIS = new BigDecimal("20.00");
    private static final BigDecimal FRETE_MESMA_CIDADE = new BigDecimal("5.00");
    private static final BigDecimal FRETE_MESMO_ESTADO = new BigDecimal("10.00");
    private static final BigDecimal FRETE_OUTRO_ESTADO = new BigDecimal("15.00");

    // CEP de origem da loja (simulacao)
    private static final String CEP_ORIGEM = "01310100"; // Sao Paulo - SP

    /**
     * Calcula frete baseado no endereco e valor do pedido
     * @param enderecoId ID do endereco de entrega
     * @param valorPedido Valor total do pedido
     * @return FreteDTO com valor, descricao e prazo
     */
    public FreteDTO calcularFrete(Long enderecoId, BigDecimal valorPedido) {
        Endereco endereco = enderecoRepository.findById(enderecoId)
            .orElseThrow(() -> new EnderecoNaoEncontradoException(
                "Endereco nao encontrado"
            ));

        // Frete gratis para pedidos >= R$20
        if (valorPedido.compareTo(VALOR_MINIMO_FRETE_GRATIS) >= 0) {
            return new FreteDTO(
                BigDecimal.ZERO,
                "Frete Gratis",
                calcularPrazo(endereco.getCep()),
                true
            );
        }

        // Calcular por regiao
        RegiaoFrete regiao = identificarRegiao(endereco.getCep());

        return switch (regiao) {
            case MESMA_CIDADE -> new FreteDTO(
                FRETE_MESMA_CIDADE,
                "Entrega Local",
                3,
                false
            );
            case MESMO_ESTADO -> new FreteDTO(
                FRETE_MESMO_ESTADO,
                "Entrega Estadual",
                7,
                false
            );
            case OUTRO_ESTADO -> new FreteDTO(
                FRETE_OUTRO_ESTADO,
                "Entrega Nacional",
                15,
                false
            );
        };
    }

    private RegiaoFrete identificarRegiao(String cepDestino) {
        String prefixoOrigem = CEP_ORIGEM.substring(0, 3);
        String prefixoDestino = cepDestino.replaceAll("\\D", "").substring(0, 3);

        // Mesma cidade: mesmo prefixo de 3 digitos
        if (prefixoOrigem.equals(prefixoDestino)) {
            return RegiaoFrete.MESMA_CIDADE;
        }

        // Mesmo estado: primeiro digito igual (simplificacao)
        if (CEP_ORIGEM.charAt(0) == cepDestino.charAt(0)) {
            return RegiaoFrete.MESMO_ESTADO;
        }

        return RegiaoFrete.OUTRO_ESTADO;
    }

    private int calcularPrazo(String cep) {
        RegiaoFrete regiao = identificarRegiao(cep);
        return switch (regiao) {
            case MESMA_CIDADE -> 3;
            case MESMO_ESTADO -> 7;
            case OUTRO_ESTADO -> 15;
        };
    }
}
```

#### FreteDTO
```java
package com.les.jakebooks.dto;

public record FreteDTO(
    BigDecimal valor,
    String descricao,
    int prazoEstimadoDias,
    boolean gratis
) {
    public String getDescricaoCompleta() {
        if (gratis) {
            return String.format("Frete Gratis - Entrega em ate %d dias uteis", prazoEstimadoDias);
        }
        return String.format("%s - R$ %.2f - Entrega em ate %d dias uteis",
            descricao, valor, prazoEstimadoDias);
    }
}
```

#### RegiaoFrete (Enum)
```java
package com.les.jakebooks.domain.enums;

public enum RegiaoFrete {
    MESMA_CIDADE,
    MESMO_ESTADO,
    OUTRO_ESTADO
}
```

### Frontend (frontend-agent)

#### CheckoutController (adicionar)
```java
@GetMapping("/checkout/frete")
public String exibirFrete(HttpSession session, Model model, Principal principal) {
    CheckoutDTO checkout = getCheckoutFromSession(session);
    Long clienteId = getClienteId(principal);

    // Buscar endereco selecionado
    EnderecoDTO endereco = enderecoService.buscarPorId(checkout.getEnderecoEntregaId());

    // Calcular valor do carrinho
    BigDecimal valorCarrinho = carrinhoService.calcularTotal(clienteId);

    // Calcular frete
    FreteDTO frete = freteService.calcularFrete(
        checkout.getEnderecoEntregaId(),
        valorCarrinho
    );

    // Armazenar na sessao
    checkout.setFrete(frete);
    session.setAttribute("checkout", checkout);

    model.addAttribute("endereco", endereco);
    model.addAttribute("frete", frete);
    model.addAttribute("valorCarrinho", valorCarrinho);
    model.addAttribute("valorTotal", valorCarrinho.add(frete.valor()));

    return "checkout/frete";
}

@PostMapping("/checkout/frete")
public String confirmarFrete(HttpSession session) {
    // Frete ja calculado, apenas prosseguir
    return "redirect:/checkout/pagamento";
}
```

#### Template: checkout/frete.html
```html
<div class="container">
    <h2>Frete e Entrega</h2>

    <!-- Endereco Selecionado -->
    <div class="card mb-3">
        <div class="card-header">Endereco de Entrega</div>
        <div class="card-body">
            <p th:text="${endereco.enderecoFormatado}"></p>
            <a th:href="@{/checkout/endereco}" class="btn btn-sm btn-link">Alterar</a>
        </div>
    </div>

    <!-- Frete Calculado -->
    <div class="card mb-3">
        <div class="card-header">Opcao de Frete</div>
        <div class="card-body">
            <div class="d-flex justify-content-between align-items-center">
                <div>
                    <strong th:text="${frete.descricao}"></strong>
                    <br>
                    <small class="text-muted"
                           th:text="'Entrega em ate ' + ${frete.prazoEstimadoDias} + ' dias uteis'"></small>
                </div>
                <div>
                    <span th:if="${frete.gratis}" class="badge bg-success fs-5">GRATIS</span>
                    <span th:unless="${frete.gratis}"
                          class="fs-5"
                          th:text="${#numbers.formatCurrency(frete.valor)}"></span>
                </div>
            </div>
        </div>
    </div>

    <!-- Resumo -->
    <div class="card mb-3">
        <div class="card-body">
            <div class="d-flex justify-content-between">
                <span>Subtotal:</span>
                <span th:text="${#numbers.formatCurrency(valorCarrinho)}"></span>
            </div>
            <div class="d-flex justify-content-between">
                <span>Frete:</span>
                <span th:if="${frete.gratis}" class="text-success">Gratis</span>
                <span th:unless="${frete.gratis}"
                      th:text="${#numbers.formatCurrency(frete.valor)}"></span>
            </div>
            <hr>
            <div class="d-flex justify-content-between fw-bold">
                <span>Total:</span>
                <span th:text="${#numbers.formatCurrency(valorTotal)}"></span>
            </div>
        </div>
    </div>

    <!-- Acoes -->
    <form th:action="@{/checkout/frete}" method="post">
        <a th:href="@{/checkout/endereco}" class="btn btn-secondary">Voltar</a>
        <button type="submit" class="btn btn-primary">Continuar para Pagamento</button>
    </form>
</div>
```

## Fluxo de Execucao

```
1. Cliente e redirecionado para /checkout/frete
2. Controller busca:
   - Endereco selecionado da sessao
   - Valor total do carrinho
3. FreteService.calcularFrete():
   - SE valorPedido >= R$20: frete gratis
   - SENAO: calcular por regiao (CEP)
4. Controller armazena frete na sessao
5. Template exibe:
   - Endereco de entrega
   - Valor e prazo do frete
   - Resumo com subtotal + frete
6. Cliente clica em "Continuar"
7. Redireciona para /checkout/pagamento
```

## Criterios de Aceite

- [ ] Frete gratis para pedidos >= R$20
- [ ] Calculo correto por regiao (mesma cidade, estado, outro estado)
- [ ] Prazo estimado exibido corretamente
- [ ] Badge "GRATIS" quando aplicavel
- [ ] Resumo com subtotal + frete = total
- [ ] Opcao de alterar endereco
- [ ] Frete armazenado na sessao do checkout

## Dependencias

- **Task Anterior:** TASK-SHP-02 (Selecionar Endereco)
- **Proxima Task:** TASK-PAY-01 (Pagamento)

---

**Status:** Pendente
