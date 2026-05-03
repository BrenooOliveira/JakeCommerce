# TASK-CHK-03: Converter Carrinho em Pedido

## Metadata

| Campo | Valor |
|-------|-------|
| **ID** | TASK-CHK-03 |
| **Agente** | checkout-agent |
| **Prioridade** | Alta |
| **RF Relacionado** | RF0037 |
| **RN Relacionada** | RN0064, RN0039, RN0040 |
 
## Objetivo

Transformar um carrinho finalizado em pedido com status inicial EM_PROCESSAMENTO, convertendo todos os itens e associando endereco de entrega e informacoes de pagamento.

## Pre-Condicoes

- Carrinho validado (TASK-CHK-02 executada com sucesso)
- Endereco de entrega selecionado
- Frete calculado
- Pagamento processado com status APROVADA

## Pos-Condicoes

- Pedido criado com status EM_PROCESSAMENTO
- ItemPedido criado para cada ItemCarrinho
- Carrinho alterado para status FINALIZADO
- Baixa de estoque executada (TASK-CHK-04)

## Especificacao Tecnica

### Backend (backend-agent)

#### PedidoService
```java
package com.les.jakebooks.service;

@Service
@RequiredArgsConstructor
@Transactional
public class PedidoService {

    private final PedidoRepository pedidoRepository;
    private final ItemPedidoRepository itemPedidoRepository;
    private final CarrinhoService carrinhoService;
    private final EstoqueService estoqueService;

    /**
     * Converte carrinho em pedido apos pagamento aprovado
     * RF0037 - Finalizar compra (status inicial: EM_PROCESSAMENTO)
     */
    public Pedido converterCarrinhoEmPedido(ConversaoPedidoDTO dados) {
        Carrinho carrinho = carrinhoService.buscarPorId(dados.getCarrinhoId());

        // Validar pre-condicoes
        validarConversao(carrinho, dados);

        // Criar pedido
        Pedido pedido = criarPedido(carrinho, dados);
        pedido = pedidoRepository.save(pedido);

        // Converter itens
        List<ItemPedido> itensPedido = converterItensCarrinho(carrinho.getItens(), pedido);
        itemPedidoRepository.saveAll(itensPedido);

        // Finalizar carrinho
        carrinhoService.finalizarCarrinho(carrinho.getId());

        return pedido;
    }

    private void validarConversao(Carrinho carrinho, ConversaoPedidoDTO dados) {
        if (carrinho.getStatus() != StatusCarrinho.ABERTO) {
            throw new CarrinhoNaoDisponivelException(
                "Carrinho nao esta disponivel para conversao"
            );
        }

        if (dados.getPagamento().getStatus() != StatusPagamento.APROVADA) {
            throw new PagamentoNaoAprovadoException(
                "Conversao permitida apenas para pagamentos aprovados"
            );
        }
    }

    private Pedido criarPedido(Carrinho carrinho, ConversaoPedidoDTO dados) {
        Pedido pedido = new Pedido();
        pedido.setDataCriacao(new Date());
        pedido.setStatus(StatusPedido.EM_PROCESSAMENTO);
        pedido.setCliente(carrinho.getCliente());
        pedido.setEnderecoEntrega(dados.getEnderecoEntrega());
        pedido.setPagamento(dados.getPagamento());
        pedido.setValorFrete(dados.getValorFrete());

        // Calcular valor total: itens + frete - cupons
        BigDecimal subtotalItens = calcularSubtotalItens(carrinho.getItens());
        BigDecimal valorCupons = dados.getPagamento().getValorTotalCupons();
        BigDecimal valorTotal = subtotalItens.add(dados.getValorFrete()).subtract(valorCupons);

        // Garantir que valor total nao seja negativo
        pedido.setValorTotal(valorTotal.max(BigDecimal.ZERO));

        return pedido;
    }

    private List<ItemPedido> converterItensCarrinho(List<ItemCarrinho> itensCarrinho, Pedido pedido) {
        return itensCarrinho.stream()
            .map(itemCarrinho -> {
                ItemPedido itemPedido = new ItemPedido();
                itemPedido.setPedido(pedido);
                itemPedido.setLivro(itemCarrinho.getLivro());
                itemPedido.setQuantidade(itemCarrinho.getQuantidade());
                itemPedido.setValorUnitario(itemCarrinho.getValorUnitario());
                return itemPedido;
            })
            .collect(Collectors.toList());
    }

    private BigDecimal calcularSubtotalItens(List<ItemCarrinho> itens) {
        return itens.stream()
            .map(item -> item.getValorUnitario().multiply(
                BigDecimal.valueOf(item.getQuantidade())
            ))
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Busca pedidos por status para admin
     */
    @Transactional(readOnly = true)
    public List<PedidoDTO> listarPedidosPorStatus(StatusPedido status) {
        List<Pedido> pedidos = pedidoRepository.findByStatusOrderByDataCriacaoDesc(status);
        return pedidos.stream()
            .map(this::toDTO)
            .collect(Collectors.toList());
    }

    private PedidoDTO toDTO(Pedido pedido) {
        return PedidoDTO.builder()
            .id(pedido.getId())
            .dataCriacao(pedido.getDataCriacao())
            .status(pedido.getStatus())
            .nomeCliente(pedido.getCliente().getNome())
            .valorTotal(pedido.getValorTotal())
            .enderecoEntrega(pedido.getEnderecoEntrega().getEnderecoCompleto())
            .build();
    }
}
```

#### ConversaoPedidoDTO
```java
package com.les.jakebooks.dto;

@Data
@Builder
public class ConversaoPedidoDTO {
    private Long carrinhoId;
    private Endereco enderecoEntrega;
    private Pagamento pagamento;
    private BigDecimal valorFrete;
}
```

#### CompraService (Orquestrador)
```java
package com.les.jakebooks.service;

@Service
@RequiredArgsConstructor
@Transactional
public class CompraService {

    private final PedidoService pedidoService;
    private final EstoqueService estoqueService;
    private final LogTransacaoService logService;

    /**
     * Executa conversao completa: carrinho -> pedido -> baixa estoque
     */
    public ResultadoCompraDTO finalizarCompra(FinalizacaoCompraDTO dados) {
        try {
            // 1. Converter carrinho em pedido
            ConversaoPedidoDTO conversaoDTO = ConversaoPedidoDTO.builder()
                .carrinhoId(dados.getCarrinhoId())
                .enderecoEntrega(dados.getEnderecoEntrega())
                .pagamento(dados.getPagamento())
                .valorFrete(dados.getValorFrete())
                .build();

            Pedido pedido = pedidoService.converterCarrinhoEmPedido(conversaoDTO);

            // 2. Executar baixa de estoque (RN0028)
            estoqueService.executarBaixaPorPedido(pedido);

            // 3. Registrar log da transacao (RNF0012)
            logService.registrarFinalizacaoCompra(pedido);

            return ResultadoCompraDTO.builder()
                .pedidoId(pedido.getId())
                .status(StatusResultado.SUCESSO)
                .mensagem("Compra finalizada com sucesso")
                .build();

        } catch (Exception e) {
            logService.registrarFalhaCompra(dados.getCarrinhoId(), e.getMessage());
            throw new FalhaFinalizacaoException("Erro ao finalizar compra: " + e.getMessage());
        }
    }
}
```

### Domain (domain-agent)

#### Entidades JPA

##### Pedido
```java
package com.les.jakebooks.domain;

@Entity
@Table(name = "pedido")
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Pedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date dataCriacao;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private StatusPedido status;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal valorTotal;

    @Column(nullable = false, precision = 8, scale = 2)
    private BigDecimal valorFrete;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "endereco_entrega_id", nullable = false)
    private Endereco enderecoEntrega;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "pagamento_id", nullable = false)
    private Pagamento pagamento;

    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ItemPedido> itens = new ArrayList<>();
}
```

##### ItemPedido
```java
package com.les.jakebooks.domain;

@Entity
@Table(name = "item_pedido")
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class ItemPedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(nullable = false)
    private Integer quantidade;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal valorUnitario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pedido_id", nullable = false)
    private Pedido pedido;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "livro_id", nullable = false)
    private Livro livro;

    @Transient
    public BigDecimal getSubtotal() {
        return valorUnitario.multiply(BigDecimal.valueOf(quantidade));
    }
}
```

### Repository

#### PedidoRepository
```java
package com.les.jakebooks.repository;

@Repository
public interface PedidoRepository extends JpaRepository<Pedido, Long> {

    List<Pedido> findByStatusOrderByDataCriacaoDesc(StatusPedido status);

    List<Pedido> findByClienteIdOrderByDataCriacaoDesc(Long clienteId);

    @Query("SELECT p FROM Pedido p WHERE p.status IN :statuses ORDER BY p.dataCriacao DESC")
    List<Pedido> findByStatusInOrderByDataCriacaoDesc(@Param("statuses") List<StatusPedido> statuses);
}
```

### Frontend (frontend-agent)

#### Sucesso da Conversao
Template: `checkout/sucesso.html`
```html
<div class="container py-4">
    <div class="alert alert-success text-center">
        <h4>Compra Finalizada com Sucesso!</h4>
        <p>Seu pedido foi criado e está sendo processado.</p>
    </div>

    <div class="card">
        <div class="card-header">
            <h5>Detalhes do Pedido</h5>
        </div>
        <div class="card-body">
            <p><strong>Número do Pedido:</strong> <span th:text="${pedido.id}"></span></p>
            <p><strong>Data:</strong> <span th:text="${#dates.format(pedido.dataCriacao, 'dd/MM/yyyy HH:mm')}"></span></p>
            <p><strong>Status:</strong> <span class="badge bg-primary" th:text="${pedido.status}"></span></p>
            <p><strong>Valor Total:</strong> <span th:text="${#numbers.formatCurrency(pedido.valorTotal)}"></span></p>
            <p><strong>Endereco de Entrega:</strong> <span th:text="${pedido.enderecoEntrega}"></span></p>
        </div>
        <div class="card-footer">
            <a href="/pedidos" class="btn btn-primary">Ver Meus Pedidos</a>
            <a href="/livros" class="btn btn-secondary">Continuar Comprando</a>
        </div>
    </div>
</div>
```

## Criterios de Aceite

- [ ] Pedido criado com status EM_PROCESSAMENTO (RF0037)
- [ ] Todos os itens do carrinho convertidos para ItemPedido
- [ ] Endereco de entrega associado corretamente
- [ ] Valor total calculado: itens + frete - cupons
- [ ] Carrinho alterado para status FINALIZADO
- [ ] Conversao executada apenas para pagamentos APROVADOS
- [ ] Log da transacao registrado (RNF0012)
- [ ] Cliente visualiza confirmacao da compra
- [ ] Em caso de falha, carrinho permanece ABERTO

## Dependencias

- **TASK-CHK-02:** Pre-validacoes devem estar implementadas
- **domain-agent:** Criar entidades Pedido e ItemPedido
- **backend-agent:** Implementar PedidoService e CompraService
- **payment-agent:** Pagamento deve estar processado
- **TASK-CHK-04:** Baixa de estoque sera chamada apos conversao

## Fluxo de Integracao

```
PagamentoService.processarPagamento() --> Status APROVADA
                |
                v
CompraService.finalizarCompra()
                |
                v
PedidoService.converterCarrinhoEmPedido()
                |
                v
[Pedido.EM_PROCESSAMENTO] + [Carrinho.FINALIZADO]
                |
                v
EstoqueService.executarBaixaPorPedido() (TASK-CHK-04)
```

---

**Status:** Pendente