# TASK-PAY-05: Processar Pagamento

## Metadata

| Campo | Valor |
|-------|-------|
| **ID** | TASK-PAY-05 |
| **Agente** | payment-agent |
| **Prioridade** | Alta |
| **RF Relacionado** | RF0036 |
| **RN Relacionada** | RN0037, RN0038 |

## Objetivo

Processar o pagamento completo (cupons + cartoes), registrar transacoes e retornar status APROVADA ou REPROVADA.

## Pre-Condicoes

- TASK-PAY-02 concluida (cupons aplicados)
- TASK-PAY-04 concluida (cartoes distribuidos, se aplicavel)
- Todas as validacoes passaram

## Regras de Negocio

| RN | Regra | Logica |
|----|-------|--------|
| RN0037 | Validar pagamento | Verificar dados antes de processar |
| RN0038 | Status: APROVADA ou REPROVADA | Resultado do processamento |

## Especificacao Tecnica

### Backend (backend-agent)

#### PagamentoService (adicionar)
```java
/**
 * Processa pagamento completo
 * @return Pagamento com status APROVADA ou REPROVADA
 */
@Transactional
public Pagamento processarPagamento(ProcessarPagamentoDTO dto, Long clienteId) {
    // 1. Criar entidade Pagamento
    Pagamento pagamento = new Pagamento();
    pagamento.setDataCriacao(LocalDateTime.now());
    pagamento.setValorTotal(dto.getValorTotal());
    pagamento.setStatus(StatusPagamento.PENDENTE);

    pagamento = pagamentoRepository.save(pagamento);

    // 2. Registrar pagamentos com cupons
    BigDecimal valorPagoCupons = BigDecimal.ZERO;

    for (CupomAplicadoDTO cupomDto : dto.getCuponsAplicados()) {
        Cupom cupom = cupomRepository.findById(cupomDto.id())
            .orElseThrow(() -> new CupomNaoEncontradoException(cupomDto.id()));

        PagamentoCupom pc = new PagamentoCupom();
        pc.setPagamento(pagamento);
        pc.setCupom(cupom);
        pc.setValor(cupomDto.valor());

        pagamentoCupomRepository.save(pc);

        // Marcar cupom como utilizado
        cupom.setAtivo(false);
        cupom.setDataUtilizacao(LocalDateTime.now());
        cupomRepository.save(cupom);

        valorPagoCupons = valorPagoCupons.add(cupomDto.valor());
    }

    pagamento.setValorPagoCupons(valorPagoCupons);

    // 3. Processar pagamentos com cartoes (se houver)
    BigDecimal valorPagoCartoes = BigDecimal.ZERO;
    boolean todosCartoesAprovados = true;

    if (dto.getCartoesValores() != null && !dto.getCartoesValores().isEmpty()) {
        for (Map.Entry<Long, BigDecimal> entry : dto.getCartoesValores().entrySet()) {
            Long cartaoId = entry.getKey();
            BigDecimal valor = entry.getValue();

            Cartao cartao = cartaoRepository.findById(cartaoId)
                .orElseThrow(() -> new CartaoNaoEncontradoException(cartaoId));

            PagamentoCartao pc = new PagamentoCartao();
            pc.setPagamento(pagamento);
            pc.setCartao(cartao);
            pc.setValor(valor);

            // Simular processamento com gateway
            StatusPagamentoCartao statusCartao = simularGateway(cartao, valor);
            pc.setStatus(statusCartao);

            pagamentoCartaoRepository.save(pc);

            if (statusCartao == StatusPagamentoCartao.APROVADO) {
                valorPagoCartoes = valorPagoCartoes.add(valor);
            } else {
                todosCartoesAprovados = false;
            }
        }
    }

    pagamento.setValorPagoCartoes(valorPagoCartoes);

    // 4. Definir status final do pagamento
    boolean pagamentoCompleto =
        valorPagoCupons.add(valorPagoCartoes).compareTo(dto.getValorTotal()) >= 0;

    if (pagamentoCompleto && todosCartoesAprovados) {
        pagamento.setStatus(StatusPagamento.APROVADA);
    } else {
        pagamento.setStatus(StatusPagamento.REPROVADA);

        // Reverter cupons se pagamento reprovado
        reverterCuponsUtilizados(dto.getCuponsAplicados());
    }

    pagamento = pagamentoRepository.save(pagamento);

    // 5. Log da operacao
    logService.registrar(
        pagamento.getStatus() == StatusPagamento.APROVADA ?
            TipoLog.PAGAMENTO_APROVADO : TipoLog.PAGAMENTO_REPROVADO,
        String.format("Pagamento %d - Status: %s - Valor: R$ %.2f",
            pagamento.getId(), pagamento.getStatus(), dto.getValorTotal()),
        clienteId
    );

    return pagamento;
}

/**
 * Simula gateway de pagamento (ambiente academico)
 * Em producao, integrar com gateway real
 */
private StatusPagamentoCartao simularGateway(Cartao cartao, BigDecimal valor) {
    // Simulacao: 90% de aprovacao
    // Cartoes com final par: sempre aprovam
    // Cartoes com final impar: 80% aprovacao

    String ultimoDigito = cartao.getNumero().substring(cartao.getNumero().length() - 1);
    int digito = Integer.parseInt(ultimoDigito);

    if (digito % 2 == 0) {
        return StatusPagamentoCartao.APROVADO;
    }

    // 80% de chance de aprovar
    return Math.random() < 0.8 ?
        StatusPagamentoCartao.APROVADO :
        StatusPagamentoCartao.REPROVADO;
}

/**
 * Reverte cupons utilizados em caso de falha
 */
private void reverterCuponsUtilizados(List<CupomAplicadoDTO> cupons) {
    for (CupomAplicadoDTO cupomDto : cupons) {
        Cupom cupom = cupomRepository.findById(cupomDto.id()).orElse(null);
        if (cupom != null) {
            cupom.setAtivo(true);
            cupom.setDataUtilizacao(null);
            cupomRepository.save(cupom);
        }
    }
}
```

#### Entidade Pagamento
```java
package com.les.jakebooks.domain;

@Entity
@Table(name = "pagamentos")
public class Pagamento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "data_criacao")
    private LocalDateTime dataCriacao;

    @Column(name = "valor_total", precision = 10, scale = 2)
    private BigDecimal valorTotal;

    @Column(name = "valor_pago_cupons", precision = 10, scale = 2)
    private BigDecimal valorPagoCupons;

    @Column(name = "valor_pago_cartoes", precision = 10, scale = 2)
    private BigDecimal valorPagoCartoes;

    @Enumerated(EnumType.STRING)
    private StatusPagamento status;

    @OneToMany(mappedBy = "pagamento", cascade = CascadeType.ALL)
    private List<PagamentoCupom> pagamentosCupom;

    @OneToMany(mappedBy = "pagamento", cascade = CascadeType.ALL)
    private List<PagamentoCartao> pagamentosCartao;

    // getters e setters
}
```

#### Entidade PagamentoCartao
```java
package com.les.jakebooks.domain;

@Entity
@Table(name = "pagamentos_cartao")
public class PagamentoCartao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "pagamento_id")
    private Pagamento pagamento;

    @ManyToOne
    @JoinColumn(name = "cartao_id")
    private Cartao cartao;

    @Column(precision = 10, scale = 2)
    private BigDecimal valor;

    @Enumerated(EnumType.STRING)
    private StatusPagamentoCartao status;

    // getters e setters
}
```

#### Entidade PagamentoCupom
```java
package com.les.jakebooks.domain;

@Entity
@Table(name = "pagamentos_cupom")
public class PagamentoCupom {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "pagamento_id")
    private Pagamento pagamento;

    @ManyToOne
    @JoinColumn(name = "cupom_id")
    private Cupom cupom;

    @Column(precision = 10, scale = 2)
    private BigDecimal valor;

    // getters e setters
}
```

### Frontend (frontend-agent)

#### CheckoutController (adicionar)
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
        // Montar DTO de processamento
        ProcessarPagamentoDTO dto = new ProcessarPagamentoDTO();
        dto.setValorTotal(checkout.getValorTotal());
        dto.setCuponsAplicados(checkout.getCuponsAplicados());
        dto.setCartoesValores(form.getCartoesValores());

        // Processar pagamento
        Pagamento pagamento = pagamentoService.processarPagamento(dto, clienteId);

        checkout.setPagamentoId(pagamento.getId());
        checkout.setStatusPagamento(pagamento.getStatus());
        session.setAttribute("checkout", checkout);

        if (pagamento.getStatus() == StatusPagamento.APROVADA) {
            // Prosseguir para finalizacao (checkout-agent)
            return "redirect:/checkout/finalizar";
        } else {
            // Incrementar tentativas e mostrar erro (TASK-PAY-06)
            ra.addFlashAttribute("erro", "Pagamento reprovado. Tente novamente.");
            return "redirect:/checkout/pagamento";
        }

    } catch (Exception e) {
        ra.addFlashAttribute("erro", e.getMessage());
        return "redirect:/checkout/pagamento";
    }
}
```

#### Template: checkout/pagamento-resultado.html (modal ou redirect)
```html
<!-- Modal de Resultado -->
<div class="modal" id="modalResultado" tabindex="-1">
    <div class="modal-dialog modal-dialog-centered">
        <div class="modal-content">
            <!-- Aprovado -->
            <div th:if="${pagamento.status.name() == 'APROVADA'}" class="text-center p-4">
                <div class="text-success mb-3">
                    <i class="bi bi-check-circle" style="font-size: 4rem;"></i>
                </div>
                <h4>Pagamento Aprovado!</h4>
                <p>Seu pedido esta sendo processado.</p>
                <a th:href="@{/checkout/finalizar}" class="btn btn-success">
                    Continuar
                </a>
            </div>

            <!-- Reprovado -->
            <div th:if="${pagamento.status.name() == 'REPROVADA'}" class="text-center p-4">
                <div class="text-danger mb-3">
                    <i class="bi bi-x-circle" style="font-size: 4rem;"></i>
                </div>
                <h4>Pagamento Reprovado</h4>
                <p>Verifique os dados do cartao e tente novamente.</p>
                <p th:if="${tentativasRestantes > 0}"
                   class="text-muted">
                    Tentativas restantes: <span th:text="${tentativasRestantes}"></span>
                </p>
                <a th:href="@{/checkout/pagamento}" class="btn btn-primary">
                    Tentar Novamente
                </a>
            </div>
        </div>
    </div>
</div>
```

## Fluxo de Execucao

```
1. Cliente clica "Finalizar Pagamento"
2. Controller monta ProcessarPagamentoDTO
3. PagamentoService.processarPagamento():
   a. Criar entidade Pagamento
   b. Para cada cupom:
      - Criar PagamentoCupom
      - Marcar cupom como utilizado
   c. Para cada cartao:
      - Criar PagamentoCartao
      - Simular gateway
      - Registrar status individual
   d. Definir status final:
      - APROVADA se todos cartoes OK
      - REPROVADA se algum cartao falhou
   e. Se REPROVADA: reverter cupons
4. Registrar log
5. Retornar resultado
6. SE APROVADA: redirect para finalizacao
7. SE REPROVADA: redirect para pagamento com erro
```

## Criterios de Aceite

- [ ] Pagamento com apenas cupons funciona (sem cartoes)
- [ ] Pagamento com apenas cartoes funciona
- [ ] Pagamento misto (cupons + cartoes) funciona
- [ ] Cupons marcados como utilizados apos aprovacao
- [ ] Cupons revertidos se pagamento reprovado
- [ ] Status individual de cada cartao registrado
- [ ] Status final correto (APROVADA/REPROVADA)
- [ ] Log de todas as operacoes
- [ ] Redirect correto baseado no resultado

## Dependencias

- **Task Anterior:** TASK-PAY-02, TASK-PAY-04
- **Proxima Task:**
  - APROVADA: TASK-CHK-03 (Criar Pedido)
  - REPROVADA: TASK-PAY-06 (Controlar Tentativas)

---

**Status:** Pendente
