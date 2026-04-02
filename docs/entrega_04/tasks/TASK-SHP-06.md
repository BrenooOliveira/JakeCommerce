# TASK-SHP-06: Listar Pedidos por Status (Admin)

## Metadata

| Campo | Valor |
|-------|-------|
| **ID** | TASK-SHP-06 |
| **Agente** | shipping-agent |
| **Prioridade** | Media |
| **RF Relacionado** | - |
| **RN Relacionada** | - |

## Objetivo

Fornecer uma visao consolidada de todos os pedidos organizados por status, permitindo ao administrador gerenciar o fluxo de entregas de forma eficiente.

## Pre-Condicoes

- Usuario logado como ADMIN
- Tasks TASK-SHP-04 e TASK-SHP-05 concluidas

## Especificacao Tecnica

### Backend (backend-agent)

#### PedidoService (adicionar)
```java
/**
 * Retorna contagem de pedidos por status
 */
public Map<StatusPedido, Long> contarPedidosPorStatus() {
    return Arrays.stream(StatusPedido.values())
        .collect(Collectors.toMap(
            status -> status,
            status -> pedidoRepository.countByStatus(status)
        ));
}

/**
 * Lista pedidos filtrados por status
 */
public Page<PedidoResumoDTO> listarPedidos(
        StatusPedido status,
        Pageable pageable) {

    Page<Pedido> pedidos;

    if (status != null) {
        pedidos = pedidoRepository.findByStatus(status, pageable);
    } else {
        pedidos = pedidoRepository.findAll(pageable);
    }

    return pedidos.map(this::toResumoDTO);
}

/**
 * Busca pedido por codigo
 */
public Optional<PedidoDetalheDTO> buscarPorCodigo(String codigo) {
    return pedidoRepository.findByCodigo(codigo)
        .map(this::toDetalheDTO);
}
```

#### PedidoRepository (adicionar)
```java
Long countByStatus(StatusPedido status);

Page<Pedido> findByStatus(StatusPedido status, Pageable pageable);

Optional<Pedido> findByCodigo(String codigo);
```

#### DashboardPedidosDTO
```java
package com.les.jakebooks.dto;

public record DashboardPedidosDTO(
    long totalPedidos,
    long emProcessamento,
    long emTransporte,
    long entregues,
    long emTroca,
    BigDecimal valorTotalVendas
) {}
```

### Frontend (frontend-agent)

#### AdminPedidoController (adicionar)
```java
@GetMapping
public String listarTodos(
        @RequestParam(required = false) StatusPedido status,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(required = false) String busca,
        Model model) {

    // Dashboard
    Map<StatusPedido, Long> contagem = pedidoService.contarPedidosPorStatus();
    model.addAttribute("contagem", contagem);

    // Lista paginada
    Pageable pageable = PageRequest.of(page, 20, Sort.by("dataCriacao").descending());
    Page<PedidoResumoDTO> pedidos = pedidoService.listarPedidos(status, pageable);
    model.addAttribute("pedidos", pedidos);
    model.addAttribute("statusFiltro", status);

    // Busca por codigo
    if (busca != null && !busca.isBlank()) {
        pedidoService.buscarPorCodigo(busca)
            .ifPresent(p -> model.addAttribute("pedidoBusca", p));
    }

    return "admin/pedidos/index";
}

@GetMapping("/{id}")
public String detalhesPedido(@PathVariable Long id, Model model) {
    PedidoDetalheDTO pedido = pedidoService.buscarPorId(id)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

    model.addAttribute("pedido", pedido);
    return "admin/pedidos/detalhes";
}
```

#### Template: admin/pedidos/index.html
```html
<div class="container-fluid">
    <h2>Gestao de Pedidos</h2>

    <!-- Dashboard Cards -->
    <div class="row mb-4">
        <div class="col-md-3">
            <div class="card bg-warning text-dark">
                <div class="card-body">
                    <h5 class="card-title">Em Processamento</h5>
                    <h2 th:text="${contagem['EM_PROCESSAMENTO']}">0</h2>
                    <a th:href="@{/admin/pedidos(status='EM_PROCESSAMENTO')}"
                       class="stretched-link"></a>
                </div>
            </div>
        </div>
        <div class="col-md-3">
            <div class="card bg-info text-white">
                <div class="card-body">
                    <h5 class="card-title">Em Transporte</h5>
                    <h2 th:text="${contagem['EM_TRANSPORTE']}">0</h2>
                    <a th:href="@{/admin/pedidos(status='EM_TRANSPORTE')}"
                       class="stretched-link"></a>
                </div>
            </div>
        </div>
        <div class="col-md-3">
            <div class="card bg-success text-white">
                <div class="card-body">
                    <h5 class="card-title">Entregues</h5>
                    <h2 th:text="${contagem['ENTREGUE']}">0</h2>
                    <a th:href="@{/admin/pedidos(status='ENTREGUE')}"
                       class="stretched-link"></a>
                </div>
            </div>
        </div>
        <div class="col-md-3">
            <div class="card bg-secondary text-white">
                <div class="card-body">
                    <h5 class="card-title">Em Troca</h5>
                    <h2 th:text="${contagem['EM_TROCA']}">0</h2>
                    <a th:href="@{/admin/pedidos(status='EM_TROCA')}"
                       class="stretched-link"></a>
                </div>
            </div>
        </div>
    </div>

    <!-- Busca -->
    <div class="card mb-4">
        <div class="card-body">
            <form th:action="@{/admin/pedidos}" method="get" class="row g-3">
                <div class="col-md-4">
                    <input type="text" name="busca" class="form-control"
                           placeholder="Buscar por codigo do pedido..."
                           th:value="${param.busca}">
                </div>
                <div class="col-md-3">
                    <select name="status" class="form-select">
                        <option value="">Todos os status</option>
                        <option th:each="s : ${T(com.les.jakebooks.domain.enums.StatusPedido).values()}"
                                th:value="${s}"
                                th:text="${s.descricao}"
                                th:selected="${s == statusFiltro}"></option>
                    </select>
                </div>
                <div class="col-md-2">
                    <button type="submit" class="btn btn-primary">Filtrar</button>
                </div>
            </form>
        </div>
    </div>

    <!-- Lista de Pedidos -->
    <div class="card">
        <div class="card-body">
            <table class="table table-hover">
                <thead>
                    <tr>
                        <th>Codigo</th>
                        <th>Data</th>
                        <th>Cliente</th>
                        <th>Total</th>
                        <th>Status</th>
                        <th>Acoes</th>
                    </tr>
                </thead>
                <tbody>
                    <tr th:each="pedido : ${pedidos.content}">
                        <td th:text="${pedido.codigoPedido}"></td>
                        <td th:text="${#temporals.format(pedido.dataCriacao, 'dd/MM/yyyy HH:mm')}"></td>
                        <td th:text="${pedido.nomeCliente}"></td>
                        <td th:text="${#numbers.formatCurrency(pedido.valorTotal)}"></td>
                        <td>
                            <span class="badge"
                                  th:classappend="${pedido.status.corBadge}"
                                  th:text="${pedido.status.descricao}"></span>
                        </td>
                        <td>
                            <a th:href="@{/admin/pedidos/{id}(id=${pedido.id})}"
                               class="btn btn-sm btn-outline-primary">
                                Ver Detalhes
                            </a>
                        </td>
                    </tr>
                </tbody>
            </table>

            <!-- Paginacao -->
            <nav th:if="${pedidos.totalPages > 1}">
                <ul class="pagination justify-content-center">
                    <li class="page-item" th:classappend="${pedidos.first ? 'disabled' : ''}">
                        <a class="page-link"
                           th:href="@{/admin/pedidos(page=${pedidos.number - 1}, status=${statusFiltro})}">
                            Anterior
                        </a>
                    </li>
                    <li class="page-item" th:each="i : ${#numbers.sequence(0, pedidos.totalPages - 1)}"
                        th:classappend="${i == pedidos.number ? 'active' : ''}">
                        <a class="page-link"
                           th:href="@{/admin/pedidos(page=${i}, status=${statusFiltro})}"
                           th:text="${i + 1}"></a>
                    </li>
                    <li class="page-item" th:classappend="${pedidos.last ? 'disabled' : ''}">
                        <a class="page-link"
                           th:href="@{/admin/pedidos(page=${pedidos.number + 1}, status=${statusFiltro})}">
                            Proximo
                        </a>
                    </li>
                </ul>
            </nav>
        </div>
    </div>
</div>
```

## Fluxo de Execucao

```
1. Admin acessa /admin/pedidos
2. Sistema carrega:
   - Contagem por status (dashboard)
   - Lista paginada de pedidos
3. Admin pode:
   - Clicar em card do dashboard para filtrar
   - Buscar por codigo
   - Filtrar por status
   - Paginar resultados
4. Admin clica em "Ver Detalhes"
5. Sistema exibe detalhes completos do pedido
```

## Criterios de Aceite

- [ ] Dashboard exibe contagem por status
- [ ] Cards do dashboard sao clicaveis e filtram
- [ ] Busca por codigo funciona
- [ ] Filtro por status funciona
- [ ] Paginacao funciona corretamente
- [ ] Badge de status com cores distintas
- [ ] Link para detalhes do pedido
- [ ] Ordenacao por data decrescente

## Dependencias

- **Tasks Anteriores:** TASK-SHP-04, TASK-SHP-05
- **Enum:** StatusPedido com descricao e corBadge

---

**Status:** Pendente
