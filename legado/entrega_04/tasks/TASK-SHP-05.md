# TASK-SHP-05: Confirmar Entrega (Admin)

## Metadata

| Campo | Valor |
|-------|-------|
| **ID** | TASK-SHP-05 |
| **Agente** | shipping-agent |
| **Prioridade** | Media |
| **RF Relacionado** | RF0039 |
| **RN Relacionada** | RN0040 |

## Objetivo

Permitir que o administrador confirme a entrega de pedidos em transporte, alterando o status de EM_TRANSPORTE para ENTREGUE.

## Pre-Condicoes

- Usuario logado como ADMIN
- Pedido com status EM_TRANSPORTE
- TASK-SHP-04 concluida

## Regras de Negocio

| RN | Regra | Validacao |
|----|-------|-----------|
| RN0040 | Status entrega: ENTREGUE | Transicao valida apenas de EM_TRANSPORTE |

### Transicoes de Status Validas

```
EM_TRANSPORTE --> ENTREGUE (unica transicao permitida nesta task)
```

## Especificacao Tecnica

### Backend (backend-agent)

#### PedidoService (adicionar)
```java
/**
 * Confirma entrega alterando status para ENTREGUE
 * @param pedidoId ID do pedido
 * @throws PedidoNaoEncontradoException se pedido nao existe
 * @throws TransicaoStatusInvalidaException se status atual nao e EM_TRANSPORTE
 */
@Transactional
public PedidoDTO confirmarEntrega(Long pedidoId) {
    Pedido pedido = pedidoRepository.findById(pedidoId)
        .orElseThrow(() -> new PedidoNaoEncontradoException(
            "Pedido nao encontrado: " + pedidoId
        ));

    if (pedido.getStatus() != StatusPedido.EM_TRANSPORTE) {
        throw new TransicaoStatusInvalidaException(
            "Pedido deve estar EM_TRANSPORTE para confirmar entrega. " +
            "Status atual: " + pedido.getStatus()
        );
    }

    pedido.setStatus(StatusPedido.ENTREGUE);
    pedido.setDataEntrega(LocalDateTime.now());

    // Habilitar opcao de troca para o cliente
    pedido.setTrocaHabilitada(true);

    // Log da operacao (RNF0012)
    logService.registrar(
        TipoLog.PEDIDO_ENTREGUE,
        "Pedido " + pedidoId + " entregue",
        pedido.getCliente().getId()
    );

    return toDTO(pedidoRepository.save(pedido));
}

/**
 * Lista pedidos em transporte aguardando confirmacao de entrega
 */
public List<PedidoResumoDTO> listarPedidosEmTransporte() {
    return pedidoRepository
        .findByStatusOrderByDataDespachoAsc(StatusPedido.EM_TRANSPORTE)
        .stream()
        .map(this::toResumoDTO)
        .collect(Collectors.toList());
}
```

#### PedidoRepository (adicionar)
```java
List<Pedido> findByStatusOrderByDataDespachoAsc(StatusPedido status);
```

#### Pedido Entity (adicionar campos)
```java
@Column(name = "data_entrega")
private LocalDateTime dataEntrega;

@Column(name = "troca_habilitada")
private Boolean trocaHabilitada = false;
```

### Frontend (frontend-agent)

#### AdminPedidoController (adicionar)
```java
@GetMapping("/transporte")
public String listarEmTransporte(Model model) {
    List<PedidoResumoDTO> pedidos = pedidoService.listarPedidosEmTransporte();
    model.addAttribute("pedidos", pedidos);
    return "admin/pedidos/transporte";
}

@PostMapping("/{id}/confirmar-entrega")
public String confirmarEntrega(
        @PathVariable Long id,
        RedirectAttributes ra) {
    try {
        pedidoService.confirmarEntrega(id);
        ra.addFlashAttribute("sucesso", "Entrega confirmada com sucesso!");
    } catch (TransicaoStatusInvalidaException e) {
        ra.addFlashAttribute("erro", e.getMessage());
    }
    return "redirect:/admin/pedidos/transporte";
}
```

#### Template: admin/pedidos/transporte.html
```html
<div class="container-fluid">
    <h2>Pedidos em Transporte</h2>

    <div th:if="${pedidos.empty}" class="alert alert-info">
        Nenhum pedido em transporte.
    </div>

    <table th:unless="${pedidos.empty}" class="table table-striped">
        <thead>
            <tr>
                <th>Codigo</th>
                <th>Data Despacho</th>
                <th>Cliente</th>
                <th>Endereco</th>
                <th>Total</th>
                <th>Dias em Transporte</th>
                <th>Acoes</th>
            </tr>
        </thead>
        <tbody>
            <tr th:each="pedido : ${pedidos}">
                <td th:text="${pedido.codigoPedido}"></td>
                <td th:text="${#temporals.format(pedido.dataDespacho, 'dd/MM/yyyy')}"></td>
                <td th:text="${pedido.nomeCliente}"></td>
                <td th:text="${pedido.enderecoEntrega}"></td>
                <td th:text="${#numbers.formatCurrency(pedido.valorTotal)}"></td>
                <td>
                    <span th:text="${pedido.diasEmTransporte}"></span>
                    <span th:if="${pedido.diasEmTransporte > 15}"
                          class="badge bg-warning">Atrasado</span>
                </td>
                <td>
                    <form th:action="@{/admin/pedidos/{id}/confirmar-entrega(id=${pedido.id})}"
                          method="post"
                          style="display:inline;">
                        <button type="submit" class="btn btn-sm btn-success"
                                onclick="return confirm('Confirmar entrega deste pedido?')">
                            Confirmar Entrega
                        </button>
                    </form>
                    <a th:href="@{/admin/pedidos/{id}(id=${pedido.id})}"
                       class="btn btn-sm btn-secondary">
                        Detalhes
                    </a>
                </td>
            </tr>
        </tbody>
    </table>
</div>
```

## Fluxo de Execucao

```
1. Admin acessa /admin/pedidos/transporte
2. Sistema lista pedidos com status EM_TRANSPORTE
3. Admin clica em "Confirmar Entrega" em um pedido
4. Confirmacao via JavaScript
5. POST para /admin/pedidos/{id}/confirmar-entrega
6. PedidoService.confirmarEntrega():
   - Valida status atual = EM_TRANSPORTE
   - Altera status para ENTREGUE
   - Registra data de entrega
   - Habilita opcao de troca para cliente
   - Registra log
7. Redireciona com mensagem de sucesso/erro
```

## Criterios de Aceite

- [ ] Apenas pedidos EM_TRANSPORTE aparecem na lista
- [ ] Botao "Confirmar Entrega" altera status para ENTREGUE
- [ ] Data de entrega registrada automaticamente
- [ ] Troca habilitada apos confirmacao
- [ ] Indicador de atraso para pedidos > 15 dias
- [ ] Confirmacao antes de confirmar entrega
- [ ] Mensagem de sucesso apos confirmacao
- [ ] Erro se status invalido
- [ ] Log da operacao registrado

## Dependencias

- **Task Anterior:** TASK-SHP-04 (Despachar Pedido)
- **Proxima Task:** TASK-SHP-06 (Listar Pedidos por Status)
- **Futuro:** Modulo de Trocas/Devolucoes

---

**Status:** Pendente
