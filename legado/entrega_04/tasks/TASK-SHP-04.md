# TASK-SHP-04: Despachar Pedido (Admin)

## Metadata

| Campo | Valor |
|-------|-------|
| **ID** | TASK-SHP-04 |
| **Agente** | shipping-agent |
| **Prioridade** | Media |
| **RF Relacionado** | RF0038 |
| **RN Relacionada** | RN0039 |

## Objetivo

Permitir que o administrador despache pedidos aprovados, alterando o status de EM_PROCESSAMENTO para EM_TRANSPORTE.

## Pre-Condicoes

- Usuario logado como ADMIN
- Pedido com status EM_PROCESSAMENTO
- Pagamento com status APROVADA

## Regras de Negocio

| RN | Regra | Validacao |
|----|-------|-----------|
| RN0039 | Status transporte: EM_TRANSPORTE | Transicao valida apenas de EM_PROCESSAMENTO |

### Transicoes de Status Validas

```
EM_PROCESSAMENTO --> EM_TRANSPORTE (unica transicao permitida nesta task)
```

## Especificacao Tecnica

### Backend (backend-agent)

#### PedidoService (adicionar)
```java
/**
 * Despacha pedido alterando status para EM_TRANSPORTE
 * @param pedidoId ID do pedido
 * @throws PedidoNaoEncontradoException se pedido nao existe
 * @throws TransicaoStatusInvalidaException se status atual nao e EM_PROCESSAMENTO
 */
@Transactional
public PedidoDTO despacharPedido(Long pedidoId) {
    Pedido pedido = pedidoRepository.findById(pedidoId)
        .orElseThrow(() -> new PedidoNaoEncontradoException(
            "Pedido nao encontrado: " + pedidoId
        ));

    if (pedido.getStatus() != StatusPedido.EM_PROCESSAMENTO) {
        throw new TransicaoStatusInvalidaException(
            "Pedido deve estar EM_PROCESSAMENTO para ser despachado. " +
            "Status atual: " + pedido.getStatus()
        );
    }

    pedido.setStatus(StatusPedido.EM_TRANSPORTE);
    pedido.setDataDespacho(LocalDateTime.now());

    // Log da operacao (RNF0012)
    logService.registrar(
        TipoLog.PEDIDO_DESPACHADO,
        "Pedido " + pedidoId + " despachado",
        pedido.getCliente().getId()
    );

    return toDTO(pedidoRepository.save(pedido));
}

/**
 * Lista pedidos aguardando despacho
 */
public List<PedidoResumoDTO> listarPedidosParaDespacho() {
    return pedidoRepository
        .findByStatusOrderByDataCriacaoAsc(StatusPedido.EM_PROCESSAMENTO)
        .stream()
        .map(this::toResumoDTO)
        .collect(Collectors.toList());
}
```

#### PedidoRepository (adicionar)
```java
List<Pedido> findByStatusOrderByDataCriacaoAsc(StatusPedido status);
```

#### PedidoResumoDTO
```java
package com.les.jakebooks.dto;

public record PedidoResumoDTO(
    Long id,
    String codigoPedido,
    LocalDateTime dataCriacao,
    String nomeCliente,
    String enderecoEntrega,
    BigDecimal valorTotal,
    StatusPedido status,
    int quantidadeItens
) {}
```

### Business Rules (business-rules-agent)

#### Excecao
```java
package com.les.jakebooks.exception;

public class TransicaoStatusInvalidaException extends ValidacaoNegocioException {

    public TransicaoStatusInvalidaException(String mensagem) {
        super(mensagem);
    }
}
```

### Frontend (frontend-agent)

#### AdminPedidoController
```java
package com.les.jakebooks.controller;

@Controller
@RequestMapping("/admin/pedidos")
public class AdminPedidoController {

    @GetMapping("/despacho")
    public String listarParaDespacho(Model model) {
        List<PedidoResumoDTO> pedidos = pedidoService.listarPedidosParaDespacho();
        model.addAttribute("pedidos", pedidos);
        return "admin/pedidos/despacho";
    }

    @PostMapping("/{id}/despachar")
    public String despachar(
            @PathVariable Long id,
            RedirectAttributes ra) {
        try {
            pedidoService.despacharPedido(id);
            ra.addFlashAttribute("sucesso", "Pedido despachado com sucesso!");
        } catch (TransicaoStatusInvalidaException e) {
            ra.addFlashAttribute("erro", e.getMessage());
        }
        return "redirect:/admin/pedidos/despacho";
    }
}
```

#### Template: admin/pedidos/despacho.html
```html
<div class="container-fluid">
    <h2>Pedidos para Despacho</h2>

    <div th:if="${pedidos.empty}" class="alert alert-info">
        Nenhum pedido aguardando despacho.
    </div>

    <table th:unless="${pedidos.empty}" class="table table-striped">
        <thead>
            <tr>
                <th>Codigo</th>
                <th>Data</th>
                <th>Cliente</th>
                <th>Endereco</th>
                <th>Itens</th>
                <th>Total</th>
                <th>Acoes</th>
            </tr>
        </thead>
        <tbody>
            <tr th:each="pedido : ${pedidos}">
                <td th:text="${pedido.codigoPedido}"></td>
                <td th:text="${#temporals.format(pedido.dataCriacao, 'dd/MM/yyyy HH:mm')}"></td>
                <td th:text="${pedido.nomeCliente}"></td>
                <td th:text="${pedido.enderecoEntrega}"></td>
                <td th:text="${pedido.quantidadeItens}"></td>
                <td th:text="${#numbers.formatCurrency(pedido.valorTotal)}"></td>
                <td>
                    <form th:action="@{/admin/pedidos/{id}/despachar(id=${pedido.id})}"
                          method="post"
                          style="display:inline;">
                        <button type="submit" class="btn btn-sm btn-primary"
                                onclick="return confirm('Confirma o despacho deste pedido?')">
                            Despachar
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
1. Admin acessa /admin/pedidos/despacho
2. Sistema lista pedidos com status EM_PROCESSAMENTO
3. Admin clica em "Despachar" em um pedido
4. Confirmacao via JavaScript
5. POST para /admin/pedidos/{id}/despachar
6. PedidoService.despacharPedido():
   - Valida status atual = EM_PROCESSAMENTO
   - Altera status para EM_TRANSPORTE
   - Registra data de despacho
   - Registra log
7. Redireciona com mensagem de sucesso/erro
```

## Criterios de Aceite

- [ ] Apenas pedidos EM_PROCESSAMENTO aparecem na lista
- [ ] Botao "Despachar" altera status para EM_TRANSPORTE
- [ ] Data de despacho registrada automaticamente
- [ ] Confirmacao antes de despachar
- [ ] Mensagem de sucesso apos despacho
- [ ] Erro se status invalido para despacho
- [ ] Log da operacao registrado

## Dependencias

- **Entidade:** Pedido com campo dataDespacho
- **Enum:** StatusPedido com EM_TRANSPORTE
- **Proxima Task:** TASK-SHP-05 (Confirmar Entrega)

---

**Status:** Pendente
