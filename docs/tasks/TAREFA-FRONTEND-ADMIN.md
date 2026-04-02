# TAREFAS FRONTEND - Separação Cliente x Administrador

## 📋 CONTEXTO

A aplicação JakeCommerce precisa implementar interface granular entre **CLIENTE** (usuário comum) e **ADMINISTRADOR** (gestão do sistema). Este documento define as tarefas do **frontend-agent** para garantir UI/UX adequada com separação de perfis.

## 🚨 PROBLEMAS IDENTIFICADOS

### 1. Controllers não usam @PreAuthorize em métodos admin
**Problema:** Métodos administrativos em controllers não validam role ADMIN via annotation.
**Impacto:** Cliente comum pode executar ações administrativas via requisição direta (mesmo com SecurityConfig bloqueando rota).

### 2. Controllers não adicionam isAdmin no Model
**Problema:** Templates Thymeleaf não conseguem renderizar condicionalmente elementos admin.
**Impacto:** Sidebar e botões admin aparecem para todos os usuários.

### 3. Templates não têm lógica condicional baseada em isAdmin
**Problema:** Templates renderizam elementos admin para todos os usuários.
**Impacto:** Cliente comum vê links/botões que não deveria ver.

### 4. Fragments (navbar, sidebar) não diferenciam perfis
**Problema:** Navbar e sidebar iguais para cliente e admin.
**Impacto:** Experiência de usuário confusa e falta de separação visual.

---

## ✅ TAREFAS - FRONTEND AGENT

### TAREFA FR-01: Adicionar @PreAuthorize em Controllers

Todos os métodos que executam operações administrativas DEVEM ter annotation `@PreAuthorize("hasRole('ADMIN')")`.

**IMPORTANTE:** Adicionar `@EnableGlobalMethodSecurity(prePostEnabled = true)` no `SecurityConfig.java` para habilitar @PreAuthorize:

```java
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)  // ADICIONAR ESTA LINHA
public class SecurityConfig {
    // ...
}
```

---

#### FR-01.1: LivroController

**Arquivo:** `jakebooks/src/main/java/com/les/jakebooks/controller/LivroController.java`

**Métodos que precisam @PreAuthorize("hasRole('ADMIN')"):**

```java
// Exibir formulário de novo livro
@PreAuthorize("hasRole('ADMIN')")
@GetMapping("/livros/novo")
public String exibirFormularioNovo(...) { ... }

// Salvar novo livro
@PreAuthorize("hasRole('ADMIN')")
@PostMapping("/livros/novo")
public String salvar(...) { ... }

// Exibir formulário de edição
@PreAuthorize("hasRole('ADMIN')")
@GetMapping("/livros/{codigo}/editar")
public String exibirFormularioEdicao(...) { ... }

// Salvar edição
@PreAuthorize("hasRole('ADMIN')")
@PostMapping("/livros/{codigo}/editar")
public String atualizar(...) { ... }

// Inativar livro (RF0012, RN0015)
@PreAuthorize("hasRole('ADMIN')")
@PostMapping("/livros/{codigo}/inativar")
public String inativar(...) { ... }

// Ativar livro (RF0016, RN0017)
@PreAuthorize("hasRole('ADMIN')")
@PostMapping("/livros/{codigo}/ativar")
public String ativar(...) { ... }
```

**Critério de aceite:**
- [ ] Todos os métodos de CRUD de livro têm `@PreAuthorize("hasRole('ADMIN')")`
- [ ] Métodos de listagem/detalhes públicos NÃO têm annotation (ficam públicos)

---

#### FR-01.2: ClienteController

**Arquivo:** `jakebooks/src/main/java/com/les/jakebooks/controller/ClienteController.java`

**Métodos que precisam @PreAuthorize("hasRole('ADMIN')"):**

```java
// Listar TODOS os clientes (admin)
@PreAuthorize("hasRole('ADMIN')")
@GetMapping("/clientes")
public String listarClientes(...) { ... }

// Ver detalhe de QUALQUER cliente (admin)
@PreAuthorize("hasRole('ADMIN')")
@GetMapping("/clientes/{codigo}")
public String detalheCliente(...) { ... }

// Inativar cliente (RF0023)
@PreAuthorize("hasRole('ADMIN')")
@PostMapping("/clientes/{codigo}/inativar")
public String inativar(...) { ... }
```

**Métodos autenticados (próprio cliente pode acessar) - SEM @PreAuthorize:**

```java
// Ver próprio perfil (autenticado)
@GetMapping("/clientes/perfil")
public String perfil(...) { ... }

// Editar próprios dados (autenticado)
@GetMapping("/clientes/perfil/editar")
public String editarPerfil(...) { ... }

// Salvar edição de próprios dados (autenticado)
@PostMapping("/clientes/perfil/editar")
public String salvarEdicao(...) { ... }

// Alterar própria senha (autenticado)
@GetMapping("/clientes/alterar-senha")
public String exibirFormularioAlterarSenha(...) { ... }

@PostMapping("/clientes/alterar-senha")
public String alterarSenha(...) { ... }
```

**IMPORTANTE:** Nos métodos de perfil próprio, adicionar validação no service para garantir que cliente só altera próprios dados:

```java
// No service:
public void atualizar(String codigo, ClienteDTO dto) {
    String emailLogado = SecurityUtil.getEmailUsuarioLogado();
    Cliente cliente = buscarPorCodigo(codigo);

    // Cliente só pode alterar próprios dados (exceto admin)
    if (!SecurityUtil.isAdmin() && !cliente.getEmail().equals(emailLogado)) {
        throw new ValidacaoNegocioException("Você não tem permissão para alterar dados de outro cliente");
    }

    // ... resto da lógica ...
}
```

**Critério de aceite:**
- [ ] Listagem e detalhes de todos os clientes protegidos com `@PreAuthorize("hasRole('ADMIN')")`
- [ ] Inativação protegida com `@PreAuthorize("hasRole('ADMIN')")`
- [ ] Perfil próprio acessível para autenticados (sem @PreAuthorize)
- [ ] Service valida que cliente só altera próprios dados

---

#### FR-01.3: EstoqueController

**Arquivo:** `jakebooks/src/main/java/com/les/jakebooks/controller/EstoqueController.java`

**TODOS os métodos precisam @PreAuthorize("hasRole('ADMIN')"):**

```java
// Listar estoque (RF0051)
@PreAuthorize("hasRole('ADMIN')")
@GetMapping("/estoque")
public String listar(...) { ... }

// Formulário entrada de estoque
@PreAuthorize("hasRole('ADMIN')")
@GetMapping("/estoque/entrada")
public String exibirFormularioEntrada(...) { ... }

// Salvar entrada de estoque (RF0051)
@PreAuthorize("hasRole('ADMIN')")
@PostMapping("/estoque/entrada")
public String registrarEntrada(...) { ... }
```

**Critério de aceite:**
- [ ] TODOS os métodos de EstoqueController têm `@PreAuthorize("hasRole('ADMIN')")`

---

#### FR-01.4: PedidoController

**Arquivo:** `jakebooks/src/main/java/com/les/jakebooks/controller/PedidoController.java`

**Métodos admin:**

```java
// Listar TODOS os pedidos (admin)
@PreAuthorize("hasRole('ADMIN')")
@GetMapping("/admin/pedidos")
public String listarTodosPedidos(...) { ... }

// Despachar pedido (RF0038)
@PreAuthorize("hasRole('ADMIN')")
@PostMapping("/admin/pedidos/{id}/despachar")
public String despachar(...) { ... }

// Confirmar entrega (RF0039)
@PreAuthorize("hasRole('ADMIN')")
@PostMapping("/admin/pedidos/{id}/confirmar-entrega")
public String confirmarEntrega(...) { ... }
```

**Métodos autenticados (cliente pode ver próprios pedidos):**

```java
// Listar próprios pedidos (autenticado)
@GetMapping("/pedidos")
public String listarMeusPedidos(...) {
    // Filtrar apenas pedidos do cliente logado
    String email = SecurityUtil.getEmailUsuarioLogado();
    // ...
}

// Detalhe de próprio pedido (autenticado)
@GetMapping("/pedidos/{id}")
public String detalhe(...) {
    // Validar que pedido pertence ao cliente logado
    // ...
}
```

**IMPORTANTE:** Métodos de pedidos próprios devem filtrar por cliente logado:

```java
// No controller:
String email = SecurityUtil.getEmailUsuarioLogado();
List<PedidoDTO> pedidos = pedidoService.listarPorCliente(email);
```

**Critério de aceite:**
- [ ] Listar todos os pedidos protegido com `@PreAuthorize("hasRole('ADMIN')")`
- [ ] Despachar e confirmar entrega protegidos com `@PreAuthorize("hasRole('ADMIN')")`
- [ ] Listagem de próprios pedidos filtra por cliente logado
- [ ] Detalhe de pedido valida que pertence ao cliente logado

---

#### FR-01.5: TrocaController

**Arquivo:** `jakebooks/src/main/java/com/les/jakebooks/controller/TrocaController.java`

**Métodos admin:**

```java
// Listar TODAS as trocas (RF0042)
@PreAuthorize("hasRole('ADMIN')")
@GetMapping("/trocas")
public String listarTodas(...) { ... }

// Ver detalhe de QUALQUER troca (admin)
@PreAuthorize("hasRole('ADMIN')")
@GetMapping("/trocas/{id}/admin")
public String detalheAdmin(...) { ... }

// Autorizar troca (RF0041)
@PreAuthorize("hasRole('ADMIN')")
@PostMapping("/trocas/{id}/autorizar")
public String autorizar(...) { ... }

// Recusar troca
@PreAuthorize("hasRole('ADMIN')")
@PostMapping("/trocas/{id}/recusar")
public String recusar(...) { ... }

// Confirmar recebimento (RF0043)
@PreAuthorize("hasRole('ADMIN')")
@PostMapping("/trocas/{id}/confirmar-recebimento")
public String confirmarRecebimento(...) { ... }
```

**Métodos autenticados (cliente pode solicitar próprias trocas):**

```java
// Solicitar troca (RF0040)
@GetMapping("/trocas/solicitar")
public String exibirFormularioSolicitacao(...) { ... }

@PostMapping("/trocas/solicitar")
public String solicitar(...) { ... }

// Ver próprias trocas
@GetMapping("/minhas-trocas")
public String minhasTrocas(...) {
    String email = SecurityUtil.getEmailUsuarioLogado();
    // ...filtrar apenas trocas do cliente logado
}
```

**Critério de aceite:**
- [ ] Listar todas, autorizar, recusar e confirmar recebimento protegidos com `@PreAuthorize("hasRole('ADMIN')")`
- [ ] Solicitar troca acessível para autenticados
- [ ] Listagem de próprias trocas filtra por cliente logado

---

#### FR-01.6: AnaliseController

**Arquivo:** `jakebooks/src/main/java/com/les/jakebooks/controller/AnaliseController.java`

**TODOS os métodos precisam @PreAuthorize("hasRole('ADMIN')"):**

```java
// Dashboard de análise (RF0055)
@PreAuthorize("hasRole('ADMIN')")
@GetMapping("/analise")
public String dashboard(...) { ... }

// Gerar gráficos de análise
@PreAuthorize("hasRole('ADMIN')")
@GetMapping("/analise/grafico")
public String grafico(...) { ... }

// Analisar histórico por período (RF0055)
@PreAuthorize("hasRole('ADMIN')")
@GetMapping("/analise/historico")
public String analisarHistorico(...) { ... }
```

**Critério de aceite:**
- [ ] TODOS os métodos de AnaliseController têm `@PreAuthorize("hasRole('ADMIN')")`

---

### TAREFA FR-02: Adicionar isAdmin no Model de todos os Controllers

Todos os controllers devem adicionar atributo `isAdmin` no Model usando `SecurityUtil.isAdmin()`.

**Padrão a seguir:**

```java
import com.les.jakebooks.util.SecurityUtil;

@Controller
public class ExemploController {

    @GetMapping("/rota")
    public String metodo(Model model) {
        // Adicionar isAdmin em TODOS os métodos que retornam view
        model.addAttribute("isAdmin", SecurityUtil.isAdmin());

        // ... resto da lógica ...

        return "template";
    }
}
```

**IMPORTANTE:**
- Adicionar `import com.les.jakebooks.util.SecurityUtil;` no topo de cada controller
- Adicionar `model.addAttribute("isAdmin", SecurityUtil.isAdmin());` em TODOS os métodos GET que retornam view
- **NUNCA** usar `model.addAttribute("isAdmin", false);` (hardcode)

---

#### FR-02.1: HomeController

**Arquivo:** `jakebooks/src/main/java/com/les/jakebooks/controller/HomeController.java`

```java
@GetMapping("/")
public String index(Model model) {
    model.addAttribute("isAdmin", SecurityUtil.isAdmin());
    // ...
    return "index";
}
```

---

#### FR-02.2: LivroController

**Arquivo:** `jakebooks/src/main/java/com/les/jakebooks/controller/LivroController.java`

**Adicionar em TODOS os métodos GET:**

```java
@GetMapping("/livros")
public String listar(Model model) {
    model.addAttribute("isAdmin", SecurityUtil.isAdmin());
    // ...
}

@GetMapping("/livros/{codigo}")
public String detalhe(@PathVariable String codigo, Model model) {
    model.addAttribute("isAdmin", SecurityUtil.isAdmin());
    // ...
}

@PreAuthorize("hasRole('ADMIN')")
@GetMapping("/livros/novo")
public String exibirFormularioNovo(Model model) {
    model.addAttribute("isAdmin", SecurityUtil.isAdmin());
    // ...
}

// ... e assim por diante em TODOS os métodos GET
```

---

#### FR-02.3: ClienteController

**Adicionar em todos os métodos GET.**

---

#### FR-02.4: CarrinhoController, PedidoController, TrocaController, EstoqueController, AnaliseController

**Adicionar em TODOS os métodos GET de TODOS os controllers.**

---

**Critério de aceite FR-02:**
- [ ] Todos os controllers importam `SecurityUtil`
- [ ] Todos os métodos GET adicionam `model.addAttribute("isAdmin", SecurityUtil.isAdmin())`
- [ ] Nenhum controller tem `isAdmin` hardcoded como `false`

---

### TAREFA FR-03: Atualizar Templates Thymeleaf

Todos os templates devem usar `isAdmin` para renderizar condicionalmente elementos administrativos.

---

#### FR-03.1: Layout Base

**Arquivo:** `jakebooks/src/main/resources/templates/fragments/layout.html`

**Atualizar estrutura da sidebar:**

```html
<!DOCTYPE html>
<html lang="pt-BR" xmlns:th="http://www.thymeleaf.org">
<head>
    <!-- ... meta, css, etc ... -->
</head>
<body>
    <!-- Navbar -->
    <div th:replace="~{fragments/navbar :: navbar}"></div>

    <div class="container-fluid">
        <div class="row">
            <!-- Sidebar (APENAS para admin) -->
            <div th:if="${isAdmin == true}" class="col-md-3 col-lg-2 d-md-block bg-light sidebar">
                <div th:replace="~{fragments/sidebar :: sidebar}"></div>
            </div>

            <!-- Conteúdo principal -->
            <main th:class="${isAdmin == true} ? 'col-md-9 ms-sm-auto col-lg-10' : 'col-12'">
                <div th:replace="~{fragments/messages :: messages}"></div>

                <div layout:fragment="content">
                    <!-- Conteúdo da página -->
                </div>
            </main>
        </div>
    </div>

    <!-- Footer -->
    <div th:replace="~{fragments/footer :: footer}"></div>
</body>
</html>
```

**Explicação:**
- Sidebar só renderiza quando `isAdmin == true`
- Classe do `<main>` ajusta largura: se admin, usa `col-md-9`, se cliente, usa `col-12` (largura total)

**Critério de aceite:**
- [ ] Sidebar só aparece quando `isAdmin == true`
- [ ] Largura do conteúdo ajusta dinamicamente

---

#### FR-03.2: Navbar

**Arquivo:** `jakebooks/src/main/resources/templates/fragments/navbar.html`

**Atualizar menu de navegação:**

```html
<nav th:fragment="navbar" class="navbar navbar-expand-lg navbar-dark bg-primary">
    <div class="container-fluid">
        <a class="navbar-brand" href="/">JakeBooks</a>

        <button class="navbar-toggler" type="button" data-bs-toggle="collapse" data-bs-target="#navbarNav">
            <span class="navbar-toggler-icon"></span>
        </button>

        <div class="collapse navbar-collapse" id="navbarNav">
            <ul class="navbar-nav me-auto">
                <!-- Links públicos -->
                <li class="nav-item">
                    <a class="nav-link" href="/">Home</a>
                </li>
                <li class="nav-item">
                    <a class="nav-link" href="/livros">Livros</a>
                </li>

                <!-- Links autenticados -->
                <li class="nav-item" sec:authorize="isAuthenticated()">
                    <a class="nav-link" href="/carrinho">
                        <i class="bi bi-cart"></i> Carrinho
                    </a>
                </li>
                <li class="nav-item" sec:authorize="isAuthenticated()">
                    <a class="nav-link" href="/pedidos">Meus Pedidos</a>
                </li>

                <!-- Links admin (APENAS para admin) -->
                <li class="nav-item" th:if="${isAdmin == true}">
                    <a class="nav-link" href="/admin/pedidos">
                        <i class="bi bi-box-seam"></i> Gerenciar Pedidos
                    </a>
                </li>
                <li class="nav-item" th:if="${isAdmin == true}">
                    <a class="nav-link" href="/estoque">
                        <i class="bi bi-boxes"></i> Estoque
                    </a>
                </li>
                <li class="nav-item" th:if="${isAdmin == true}">
                    <a class="nav-link" href="/trocas">
                        <i class="bi bi-arrow-left-right"></i> Trocas
                    </a>
                </li>
                <li class="nav-item" th:if="${isAdmin == true}">
                    <a class="nav-link" href="/analise">
                        <i class="bi bi-graph-up"></i> Análises
                    </a>
                </li>
            </ul>

            <!-- Menu do usuário -->
            <ul class="navbar-nav">
                <li class="nav-item dropdown" sec:authorize="isAuthenticated()">
                    <a class="nav-link dropdown-toggle" href="#" role="button" data-bs-toggle="dropdown">
                        <i class="bi bi-person-circle"></i>
                        <span sec:authentication="name"></span>
                    </a>
                    <ul class="dropdown-menu dropdown-menu-end">
                        <li><a class="dropdown-item" href="/clientes/perfil">Meu Perfil</a></li>
                        <li><a class="dropdown-item" href="/clientes/alterar-senha">Alterar Senha</a></li>
                        <li><hr class="dropdown-divider"></li>
                        <li><a class="dropdown-item" href="/logout">Sair</a></li>
                    </ul>
                </li>

                <li class="nav-item" sec:authorize="!isAuthenticated()">
                    <a class="nav-link" href="/login">Login</a>
                </li>
                <li class="nav-item" sec:authorize="!isAuthenticated()">
                    <a class="nav-link" href="/clientes/novo">Cadastre-se</a>
                </li>
            </ul>
        </div>
    </div>
</nav>
```

**Importante:**
- Adicionar dependência Thymeleaf Security Extras no `pom.xml`:

```xml
<dependency>
    <groupId>org.thymeleaf.extras</groupId>
    <artifactId>thymeleaf-extras-springsecurity6</artifactId>
</dependency>
```

- Links admin APENAS aparecem quando `isAdmin == true`
- Usar `sec:authorize` para verificar autenticação

**Critério de aceite:**
- [ ] Links admin só aparecem quando `isAdmin == true`
- [ ] Links autenticados aparecem quando usuário está logado
- [ ] Links públicos sempre visíveis

---

#### FR-03.3: Sidebar

**Arquivo:** `jakebooks/src/main/resources/templates/fragments/sidebar.html`

**Criar sidebar administrativo:**

```html
<nav th:fragment="sidebar" class="sidebar">
    <div class="position-sticky pt-3">
        <h5 class="text-uppercase text-muted px-3 mb-3">Painel Administrativo</h5>

        <ul class="nav flex-column">
            <li class="nav-item">
                <a class="nav-link" href="/admin/dashboard">
                    <i class="bi bi-speedometer2"></i> Dashboard
                </a>
            </li>

            <li class="nav-item">
                <a class="nav-link" href="/livros/novo">
                    <i class="bi bi-book"></i> Novo Livro
                </a>
            </li>

            <li class="nav-item">
                <a class="nav-link" href="/clientes">
                    <i class="bi bi-people"></i> Clientes
                </a>
            </li>

            <li class="nav-item">
                <a class="nav-link" href="/admin/pedidos">
                    <i class="bi bi-box-seam"></i> Pedidos
                </a>
            </li>

            <li class="nav-item">
                <a class="nav-link" href="/trocas">
                    <i class="bi bi-arrow-left-right"></i> Trocas
                </a>
            </li>

            <li class="nav-item">
                <a class="nav-link" href="/estoque">
                    <i class="bi bi-boxes"></i> Estoque
                </a>
            </li>

            <li class="nav-item">
                <a class="nav-link" href="/analise">
                    <i class="bi bi-graph-up"></i> Análises
                </a>
            </li>
        </ul>
    </div>
</nav>

<style>
.sidebar {
    position: fixed;
    top: 56px; /* Altura do navbar */
    bottom: 0;
    left: 0;
    z-index: 100;
    padding: 0;
    overflow-x: hidden;
    overflow-y: auto;
    border-right: 1px solid #dee2e6;
}

.sidebar .nav-link {
    font-weight: 500;
    color: #333;
    padding: 0.75rem 1rem;
}

.sidebar .nav-link:hover {
    color: #0d6efd;
    background-color: #f8f9fa;
}

.sidebar .nav-link i {
    margin-right: 0.5rem;
}
</style>
```

**Critério de aceite:**
- [ ] Sidebar contém links para todas as funcionalidades admin
- [ ] Sidebar só renderiza quando `isAdmin == true` (controlado no layout.html)

---

#### FR-03.4: Listagem de Livros

**Arquivo:** `jakebooks/src/main/resources/templates/livros/lista.html`

**Adicionar botões admin condicionais:**

```html
<div layout:decorate="~{fragments/layout}">
    <div layout:fragment="content">
        <div class="d-flex justify-content-between align-items-center mb-4">
            <h2>Livros</h2>

            <!-- Botão "Novo Livro" APENAS para admin -->
            <a th:if="${isAdmin == true}" href="/livros/novo" class="btn btn-primary">
                <i class="bi bi-plus-circle"></i> Novo Livro
            </a>
        </div>

        <table class="table table-striped">
            <thead>
                <tr>
                    <th>Código</th>
                    <th>Título</th>
                    <th>Autor</th>
                    <th>Preço</th>
                    <th>Status</th>
                    <th>Ações</th>
                </tr>
            </thead>
            <tbody>
                <tr th:each="livro : ${livros}">
                    <td th:text="${livro.codigo}"></td>
                    <td th:text="${livro.titulo}"></td>
                    <td th:text="${livro.nomeAutor}"></td>
                    <td th:text="${#numbers.formatCurrency(livro.valorVenda)}"></td>
                    <td>
                        <span th:class="${livro.status == 'ATIVO'} ? 'badge bg-success' : 'badge bg-danger'"
                              th:text="${livro.status}"></span>
                    </td>
                    <td>
                        <a th:href="@{/livros/{codigo}(codigo=${livro.codigo})}" class="btn btn-sm btn-info">
                            <i class="bi bi-eye"></i> Ver
                        </a>

                        <!-- Botões admin APENAS para admin -->
                        <a th:if="${isAdmin == true}"
                           th:href="@{/livros/{codigo}/editar(codigo=${livro.codigo})}"
                           class="btn btn-sm btn-warning">
                            <i class="bi bi-pencil"></i> Editar
                        </a>

                        <form th:if="${isAdmin == true && livro.status == 'ATIVO'}"
                              th:action="@{/livros/{codigo}/inativar(codigo=${livro.codigo})}"
                              method="post" style="display: inline;">
                            <button type="submit" class="btn btn-sm btn-danger">
                                <i class="bi bi-x-circle"></i> Inativar
                            </button>
                        </form>

                        <form th:if="${isAdmin == true && livro.status == 'INATIVO'}"
                              th:action="@{/livros/{codigo}/ativar(codigo=${livro.codigo})}"
                              method="post" style="display: inline;">
                            <button type="submit" class="btn btn-sm btn-success">
                                <i class="bi bi-check-circle"></i> Ativar
                            </button>
                        </form>
                    </td>
                </tr>
            </tbody>
        </table>
    </div>
</div>
```

**Critério de aceite:**
- [ ] Botão "Novo Livro" só aparece para admin
- [ ] Botões "Editar", "Inativar", "Ativar" só aparecem para admin
- [ ] Botão "Ver" (detalhes) aparece para todos

---

#### FR-03.5: Listagem de Clientes

**Arquivo:** `jakebooks/src/main/resources/templates/clientes/lista.html`

**Página INTEIRA só acessível para admin:**

```html
<div layout:decorate="~{fragments/layout}">
    <div layout:fragment="content">
        <h2>Gerenciar Clientes</h2>

        <table class="table table-striped">
            <thead>
                <tr>
                    <th>Código</th>
                    <th>Nome</th>
                    <th>Email</th>
                    <th>Status</th>
                    <th>Admin</th>
                    <th>Ações</th>
                </tr>
            </thead>
            <tbody>
                <tr th:each="cliente : ${clientes}">
                    <td th:text="${cliente.codigo}"></td>
                    <td th:text="${cliente.nome}"></td>
                    <td th:text="${cliente.email}"></td>
                    <td>
                        <span th:class="${cliente.status == 'ATIVO'} ? 'badge bg-success' : 'badge bg-danger'"
                              th:text="${cliente.status}"></span>
                    </td>
                    <td>
                        <span th:if="${cliente.isAdmin == true}" class="badge bg-warning">
                            <i class="bi bi-shield-check"></i> Admin
                        </span>
                    </td>
                    <td>
                        <a th:href="@{/clientes/{codigo}(codigo=${cliente.codigo})}" class="btn btn-sm btn-info">
                            <i class="bi bi-eye"></i> Ver
                        </a>

                        <form th:if="${cliente.status == 'ATIVO'}"
                              th:action="@{/clientes/{codigo}/inativar(codigo=${cliente.codigo})}"
                              method="post" style="display: inline;">
                            <button type="submit" class="btn btn-sm btn-danger">
                                <i class="bi bi-x-circle"></i> Inativar
                            </button>
                        </form>
                    </td>
                </tr>
            </tbody>
        </table>
    </div>
</div>
```

**Critério de aceite:**
- [ ] Página lista todos os clientes (sem filtro)
- [ ] Exibe badge "Admin" para clientes com `isAdmin == true`
- [ ] Permite inativar clientes (apenas admin acessa essa página)

---

#### FR-03.6: Outras páginas

**Aplicar padrão semelhante em:**

- `pedidos/lista.html` - Botões de despachar/confirmar entrega apenas para admin
- `trocas/lista.html` - Botões de autorizar/recusar/confirmar apenas para admin
- `estoque/lista.html` - Página inteira só para admin
- `analise/dashboard.html` - Página inteira só para admin

---

### TAREFA FR-04: Criar página 403 Forbidden personalizada

**Arquivo:** `jakebooks/src/main/resources/templates/error/403.html`

```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Acesso Negado - JakeBooks</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.0/font/bootstrap-icons.css">
</head>
<body>
    <div class="container mt-5">
        <div class="row justify-content-center">
            <div class="col-md-6 text-center">
                <i class="bi bi-shield-exclamation text-danger" style="font-size: 5rem;"></i>
                <h1 class="display-4 mt-3">403 - Acesso Negado</h1>
                <p class="lead">Você não tem permissão para acessar esta página.</p>
                <p class="text-muted">Esta área é restrita a administradores do sistema.</p>

                <div class="mt-4">
                    <a href="/" class="btn btn-primary">
                        <i class="bi bi-house"></i> Voltar para Home
                    </a>
                    <a href="/livros" class="btn btn-secondary">
                        <i class="bi bi-book"></i> Ver Livros
                    </a>
                </div>
            </div>
        </div>
    </div>
</body>
</html>
```

**Critério de aceite:**
- [ ] Página 403 personalizada criada
- [ ] Apresenta mensagem clara de acesso negado
- [ ] Oferece links para voltar à navegação

---

## 📝 CHECKLIST FINAL - FRONTEND AGENT

Antes de considerar as tarefas concluídas, verificar:

### Controllers
- [ ] **FR-01**: `@EnableMethodSecurity(prePostEnabled = true)` adicionado no SecurityConfig
- [ ] **FR-01.1**: LivroController - métodos admin têm `@PreAuthorize("hasRole('ADMIN')")`
- [ ] **FR-01.2**: ClienteController - listagem/inativação protegidos, perfil próprio acessível
- [ ] **FR-01.3**: EstoqueController - TODOS os métodos protegidos
- [ ] **FR-01.4**: PedidoController - despachar/confirmar protegidos, próprios pedidos filtrados
- [ ] **FR-01.5**: TrocaController - autorizar/recusar/confirmar protegidos
- [ ] **FR-01.6**: AnaliseController - TODOS os métodos protegidos

### Model Attributes
- [ ] **FR-02**: Todos os controllers importam `SecurityUtil`
- [ ] **FR-02**: Todos os métodos GET adicionam `isAdmin` via `SecurityUtil.isAdmin()`
- [ ] **FR-02**: Nenhum controller tem `isAdmin` hardcoded

### Templates
- [ ] **FR-03.1**: Layout - sidebar só renderiza para admin, largura ajusta
- [ ] **FR-03.2**: Navbar - links admin condicionais
- [ ] **FR-03.3**: Sidebar - criado menu administrativo
- [ ] **FR-03.4**: Livros - botões admin condicionais
- [ ] **FR-03.5**: Clientes - exibe badge admin
- [ ] **FR-03.6**: Demais páginas atualizado
- [ ] **FR-04**: Página 403 personalizada criada

### Testes
- [ ] Login como cliente → sidebar NÃO aparece
- [ ] Login como cliente → botões admin NÃO aparecem
- [ ] Login como cliente → acesso a `/admin/pedidos` retorna 403
- [ ] Login como admin → sidebar aparece
- [ ] Login como admin → botões admin aparecem
- [ ] Login como admin → acesso a `/admin/pedidos` bem-sucedido

---

## 🧪 TESTES DE VALIDAÇÃO

### Teste 1: Login como Cliente Comum
1. Fazer login com cliente comum (NÃO admin)
2. **Verificar:**
   - [ ] Sidebar NÃO aparece
   - [ ] Navbar NÃO exibe links admin (Gerenciar Pedidos, Estoque, Trocas, Análises)
   - [ ] Listagem de livros NÃO exibe botões "Editar", "Inativar", "Ativar"
   - [ ] Acesso direto a `/admin/pedidos` retorna página 403
   - [ ] Acesso direto a `/estoque` retorna página 403

### Teste 2: Login como Admin
1. Fazer login com admin (email: `admin@jakebooks.com`)
2. **Verificar:**
   - [ ] Sidebar aparece no lado esquerdo
   - [ ] Navbar exibe links admin
   - [ ] Listagem de livros exibe botões "Editar", "Inativar", "Ativar"
   - [ ] Acesso a `/admin/pedidos` bem-sucedido
   - [ ] Acesso a `/estoque` bem-sucedido
   - [ ] Botões "Despachar", "Confirmar Entrega" aparecem em pedidos

### Teste 3: Tentativas de Bypass
1. Como cliente comum, tentar:
   - [ ] POST `/livros/LIVRO001/inativar` → retorna 403
   - [ ] POST `/clientes/CLI001/inativar` → retorna 403
   - [ ] POST `/trocas/1/autorizar` → retorna 403
   - [ ] GET `/analise` → retorna 403

---

## 📚 REFERÊNCIAS

- **AGENTS.md**: Especificação completa do sistema
- **review-agent.md**: Checklist de revisão e validação
- **TAREFA-BACKEND-ADMIN.md**: Tarefas do backend-agent (dependência)
- Spring Security Documentation: https://docs.spring.io/spring-security/reference/
- Thymeleaf Security Extras: https://github.com/thymeleaf/thymeleaf-extras-springsecurity
