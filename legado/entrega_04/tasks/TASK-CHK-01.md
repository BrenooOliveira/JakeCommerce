# TASK-CHK-01: Orquestrar Fluxo de Checkout

## Metadata

| Campo | Valor |
|-------|-------|
| **ID** | TASK-CHK-01 |
| **Agente** | checkout-agent |
| **Prioridade** | Alta |
| **RF Relacionado** | RF0033 |
| **RN Relacionada** | - |

## Objetivo

Coordenar a sequencia completa de passos do checkout, garantindo que cada etapa seja executada na ordem correta e que as dependencias entre os agentes sejam respeitadas.

## Pre-Condicoes

- Cliente autenticado
- Carrinho com status ABERTO
- Carrinho com pelo menos 1 item

## Fluxo Principal

```
[1. Validar Carrinho]
        |
        v
[2. Selecionar Endereco] --> shipping-agent
        |
        v
[3. Calcular Frete] --> shipping-agent
        |
        v
[4. Selecionar Pagamento] --> payment-agent
        |
        v
[5. Processar Pagamento] --> payment-agent
        |
        v
[6. Finalizar Compra]
```

## Especificacao Tecnica

### Backend (backend-agent)

#### CheckoutService
```java
package com.les.jakebooks.service;

@Service
@RequiredArgsConstructor
public class CheckoutService {

    private final CarrinhoService carrinhoService;
    private final EnderecoService enderecoService;
    private final FreteService freteService;
    private final PagamentoService pagamentoService;
    private final PedidoService pedidoService;
    private final CompraValidator compraValidator;

    /**
     * Inicia o processo de checkout
     * Valida pre-condicoes e retorna dados iniciais
     */
    @Transactional(readOnly = true)
    public CheckoutInicioDTO iniciarCheckout(Long clienteId) {
        // Buscar carrinho ativo
        Carrinho carrinho = carrinhoService.buscarCarrinhoAtivo(clienteId)
            .orElseThrow(() -> new CarrinhoVazioException(
                "Nenhum carrinho ativo encontrado"));

        // Validar pre-condicoes
        compraValidator.validarCarrinhoParaCheckout(carrinho);

        // Buscar enderecos de entrega
        List<EnderecoDTO> enderecos = enderecoService
            .listarEnderecosPorTipo(clienteId, TipoEndereco.ENTREGA);

        // Buscar cupons disponiveis
        List<CupomDTO> cupons = pagamentoService.listarCuponsDisponiveis(clienteId);

        // Buscar cartoes do cliente
        List<CartaoDTO> cartoes = pagamentoService.listarCartoes(clienteId);

        return CheckoutInicioDTO.builder()
            .carrinhoId(carrinho.getId())
            .itens(toItemDTOList(carrinho.getItens()))
            .subtotal(carrinho.getSubtotal())
            .enderecos(enderecos)
            .cuponsDisponiveis(cupons)
            .cartoes(cartoes)
            .build();
    }

    /**
     * Processa selecao de endereco e calcula frete
     */
    @Transactional(readOnly = true)
    public CheckoutFreteDTO selecionarEndereco(Long carrinhoId, Long enderecoId) {
        Carrinho carrinho = carrinhoService.buscarPorId(carrinhoId);

        // Validar endereco
        Endereco endereco = enderecoService.buscarPorId(enderecoId);
        if (endereco.getTipo() != TipoEndereco.ENTREGA) {
            throw new EnderecoInvalidoException(
                "Endereco selecionado nao e do tipo ENTREGA");
        }

        // Calcular frete
        FreteDTO frete = freteService.calcularFrete(
            carrinho.getSubtotal(),
            endereco.getCep()
        );

        BigDecimal total = carrinho.getSubtotal().add(frete.getValor());

        return CheckoutFreteDTO.builder()
            .enderecoId(enderecoId)
            .enderecoFormatado(endereco.getEnderecoCompleto())
            .frete(frete)
            .subtotal(carrinho.getSubtotal())
            .total(total)
            .build();
    }

    /**
     * Monta resumo final antes da confirmacao
     */
    @Transactional(readOnly = true)
    public CheckoutResumoDTO montarResumo(CheckoutDTO checkout) {
        Carrinho carrinho = carrinhoService.buscarPorId(checkout.getCarrinhoId());
        Endereco endereco = enderecoService.buscarPorId(checkout.getEnderecoEntregaId());

        FreteDTO frete = freteService.calcularFrete(
            carrinho.getSubtotal(),
            endereco.getCep()
        );

        // Calcular descontos de cupons
        BigDecimal totalCupons = checkout.getCupomIds().stream()
            .map(id -> pagamentoService.buscarCupom(id).getValor())
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal subtotal = carrinho.getSubtotal();
        BigDecimal valorFrete = frete.getValor();
        BigDecimal totalPedido = subtotal.add(valorFrete);
        BigDecimal valorAPagar = totalPedido.subtract(totalCupons).max(BigDecimal.ZERO);

        return CheckoutResumoDTO.builder()
            .itens(toItemDTOList(carrinho.getItens()))
            .endereco(endereco.getEnderecoCompleto())
            .frete(frete)
            .subtotal(subtotal)
            .totalCupons(totalCupons)
            .totalPedido(totalPedido)
            .valorAPagar(valorAPagar)
            .build();
    }
}
```

#### CheckoutInicioDTO
```java
package com.les.jakebooks.dto;

@Data
@Builder
public class CheckoutInicioDTO {
    private Long carrinhoId;
    private List<ItemCarrinhoDTO> itens;
    private BigDecimal subtotal;
    private List<EnderecoDTO> enderecos;
    private List<CupomDTO> cuponsDisponiveis;
    private List<CartaoDTO> cartoes;
}
```

#### CheckoutFreteDTO
```java
package com.les.jakebooks.dto;

@Data
@Builder
public class CheckoutFreteDTO {
    private Long enderecoId;
    private String enderecoFormatado;
    private FreteDTO frete;
    private BigDecimal subtotal;
    private BigDecimal total;
}
```

#### CheckoutResumoDTO
```java
package com.les.jakebooks.dto;

@Data
@Builder
public class CheckoutResumoDTO {
    private List<ItemCarrinhoDTO> itens;
    private String endereco;
    private FreteDTO frete;
    private BigDecimal subtotal;
    private BigDecimal totalCupons;
    private BigDecimal totalPedido;
    private BigDecimal valorAPagar;
}
```

### Frontend (frontend-agent)

#### CheckoutController
```java
package com.les.jakebooks.controller;

@Controller
@RequestMapping("/checkout")
@RequiredArgsConstructor
public class CheckoutController {

    private final CheckoutService checkoutService;

    @GetMapping
    public String iniciarCheckout(Model model, Principal principal, HttpSession session) {
        Long clienteId = getClienteId(principal);

        try {
            CheckoutInicioDTO checkout = checkoutService.iniciarCheckout(clienteId);
            model.addAttribute("checkout", checkout);

            // Salvar na sessao para persistir entre steps
            session.setAttribute("checkoutData", new CheckoutSessionDTO(checkout.getCarrinhoId()));

            return "checkout/index";
        } catch (CarrinhoVazioException e) {
            return "redirect:/carrinho?erro=vazio";
        }
    }

    @GetMapping("/endereco")
    public String selecionarEndereco(Model model, HttpSession session, Principal principal) {
        CheckoutSessionDTO sessionData = getSessionData(session);
        Long clienteId = getClienteId(principal);

        List<EnderecoDTO> enderecos = enderecoService.listarEnderecosPorTipo(
            clienteId, TipoEndereco.ENTREGA);

        model.addAttribute("enderecos", enderecos);
        model.addAttribute("step", 1);

        return "checkout/endereco";
    }

    @PostMapping("/endereco")
    public String confirmarEndereco(
            @RequestParam Long enderecoId,
            HttpSession session,
            RedirectAttributes ra) {

        CheckoutSessionDTO sessionData = getSessionData(session);

        try {
            CheckoutFreteDTO resultado = checkoutService.selecionarEndereco(
                sessionData.getCarrinhoId(), enderecoId);

            sessionData.setEnderecoId(enderecoId);
            sessionData.setFrete(resultado.getFrete());
            session.setAttribute("checkoutData", sessionData);

            return "redirect:/checkout/pagamento";
        } catch (Exception e) {
            ra.addFlashAttribute("erro", e.getMessage());
            return "redirect:/checkout/endereco";
        }
    }

    @GetMapping("/resumo")
    public String exibirResumo(Model model, HttpSession session) {
        CheckoutSessionDTO sessionData = getSessionData(session);

        CheckoutDTO checkout = sessionData.toCheckoutDTO();
        CheckoutResumoDTO resumo = checkoutService.montarResumo(checkout);

        model.addAttribute("resumo", resumo);
        model.addAttribute("step", 3);

        return "checkout/resumo";
    }
}
```

#### Template: checkout/index.html (Wizard principal)
```html
<div class="container py-4">
    <h2>Finalizar Compra</h2>

    <!-- Progress Steps -->
    <div class="checkout-steps mb-4">
        <div class="step" th:classappend="${step >= 1 ? 'active' : ''}">
            <span class="step-number">1</span>
            <span class="step-label">Endereco</span>
        </div>
        <div class="step-connector"></div>
        <div class="step" th:classappend="${step >= 2 ? 'active' : ''}">
            <span class="step-number">2</span>
            <span class="step-label">Pagamento</span>
        </div>
        <div class="step-connector"></div>
        <div class="step" th:classappend="${step >= 3 ? 'active' : ''}">
            <span class="step-number">3</span>
            <span class="step-label">Confirmacao</span>
        </div>
    </div>

    <div class="row">
        <!-- Conteudo do Step -->
        <div class="col-md-8">
            <div th:replace="~{checkout/fragments :: stepContent}"></div>
        </div>

        <!-- Resumo Lateral -->
        <div class="col-md-4">
            <div class="card">
                <div class="card-header">
                    <h5>Resumo do Pedido</h5>
                </div>
                <div class="card-body">
                    <div th:each="item : ${checkout.itens}" class="d-flex justify-content-between mb-2">
                        <span th:text="${item.quantidade + 'x ' + item.tituloLivro}"></span>
                        <span th:text="${#numbers.formatCurrency(item.subtotal)}"></span>
                    </div>
                    <hr>
                    <div class="d-flex justify-content-between">
                        <strong>Subtotal:</strong>
                        <span th:text="${#numbers.formatCurrency(checkout.subtotal)}"></span>
                    </div>
                    <div th:if="${sessionData?.frete}" class="d-flex justify-content-between">
                        <span>Frete:</span>
                        <span th:text="${sessionData.frete.gratis ? 'GRATIS' : #numbers.formatCurrency(sessionData.frete.valor)}"></span>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>
```

## Criterios de Aceite

- [ ] Cliente inicia checkout a partir do carrinho
- [ ] Sistema valida carrinho antes de iniciar
- [ ] Steps do wizard funcionam sequencialmente
- [ ] Dados persistem entre steps via sessao
- [ ] Resumo lateral atualiza em cada step
- [ ] Validacoes exibem mensagens claras
- [ ] Cliente pode voltar a steps anteriores

## Dependencias

- **TASK-CHK-02:** Validar pre-condicoes do carrinho
- **shipping-agent:** TASK-SHP-01, SHP-02, SHP-03
- **payment-agent:** TASK-PAY-01

---

**Status:** Pendente
