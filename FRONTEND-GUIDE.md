# 📖 Guia Frontend - AgenteLayout & Fragmentos

## 📋 Sumário
1. [Layout Base](#layout-base)
2. [Fragmentos Principais](#fragmentos-principais)
3. [Exemplos de Uso](#exemplos-de-uso)
4. [Convenções](#convenções)

---

## 🎨 Layout Base

### Localizações
- **Layout principal**: `fragments/layout.html` - Define navbar, sidebar, footer e área de conteúdo
- **CSS customizado**: `static/css/style.css`
- **JS customizado**: `static/js/main.js`

### Estrutura do Layout
```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org"
      xmlns:layout="http://www.ultraq.net.nz/thymeleaf/layout"
      layout:decorate="~{fragments/layout}"
      lang="pt-BR">
<head>
    <title>Seu Título</title>
</head>
<body>
    <div layout:fragment="content">
        <!-- Seu conteúdo aqui -->
    </div>
</body>
</html>
```

### Controlar Sidebar Admin
Na sua view ou no Model do controller, adicione:
```java
model.addAttribute("isAdmin", true);  // mostra sidebar
model.addAttribute("isAdmin", false); // oculta sidebar
```

---

## 📚 Fragmentos Principais

### 1. **messages.html** - Exibição de Mensagens Flash

#### Fragmento de Inserção
```html
<div th:insert="~{fragments/messages :: messages}"></div>
```

#### Tipos de Mensagens Suportadas
- `mensagemSucesso` → Alert verde
- `mensagemErro` → Alert vermelho
- `mensagemAviso` → Alert amarelo
- `mensagemInfo` → Alert azul

#### Uso no Controller (via RedirectAttributes)
```java
@PostMapping("/livros/novo")
public String criar(@ModelAttribute LivroFormDTO dto, RedirectAttributes attrs) {
    try {
        livroService.criar(dto);
        attrs.addFlashAttribute("mensagemSucesso", "Livro cadastrado com sucesso!");
    } catch (ValidacaoException e) {
        attrs.addFlashAttribute("mensagemErro", e.getMessage());
    }
    return "redirect:/livros";
}
```

---

### 2. **navbar.html** - Barra de Navegação

#### Fragmento de Inserção
```html
<nav th:insert="~{fragments/navbar :: navbar}"></nav>
```

#### Links Padrão
- `/livros` - Área de Livros
- `/clientes` - Área de Clientes
- `/pedidos` - Área de Pedidos
- `/estoque` - Controle de Estoque
- `/analise` - Análise e Relatórios

---

### 3. **sidebar.html** - Sidebar Administrativa

#### Fragmento de Inserção
```html
<aside th:insert="~{fragments/sidebar :: sidebar}"></aside>
```

#### Seções
- **Livros**: Novo, Listar, Inativos
- **Clientes**: Novo, Listar, Bloqueados
- **Pedidos**: Todos, Em Processamento, Em Transporte, Trocas
- **Estoque**: Entrada, Consultar
- **Configuração**: Grupos, Cupons
- **Análise**: Dashboards, Relatórios

---

### 4. **form-errors.html** - Fragmentos de Formulário

#### a) Validação de Campo Único
```html
<div th:insert="~{fragments/form-errors :: field-error(field='titulo', errors=${#fields.errors('titulo')})}"></div>
```

#### b) Campo de Texto
```html
<div th:insert="~{fragments/form-errors :: text-field(name='titulo', label='Título do Livro', required=true)}"></div>
```

#### c) Campo Textarea
```html
<div th:insert="~{fragments/form-errors :: textarea-field(name='sinopse', label='Sinopse', required=true, rows='4')}"></div>
```

#### d) Campo Select
```html
<div th:insert="~{fragments/form-errors :: select-field(name='categoria', label='Categoria', required=true)}"></div>
```

#### e) Campo Checkbox
```html
<div th:insert="~{fragments/form-errors :: checkbox-field(name='ativo', label='Ativo')}"></div>
```

#### f) Campo Data
```html
<div th:insert="~{fragments/form-errors :: date-field(name='dataCadastro', label='Data', required=true)}"></div>
```

#### g) Campo Número
```html
<div th:insert="~{fragments/form-errors :: number-field(name='quantidade', label='Quantidade', required=true, min='1')}"></div>
```

#### h) Campo Email
```html
<div th:insert="~{fragments/form-errors :: email-field(name='email', label='Email', required=true)}"></div>
```

#### i) Campo Senha
```html
<div th:insert="~{fragments/form-errors :: password-field(name='senha', label='Senha', required=true)}"></div>
```

---

### 5. **pagination.html** - Paginação

#### Uso Simples
```html
<div th:insert="~{fragments/pagination :: pagination(page=${page})}"></div>
```
Espera atributo `pageLink` no Model:
```java
model.addAttribute("pageLink", "/livros");
model.addAttribute("page", livros);
```

#### Uso Avançado (com tamanho de página)
```html
<div th:insert="~{fragments/pagination :: pagination-with-size(page=${page}, pageLink='/livros')}"></div>
```

---

### 6. **footer.html** - Rodapé

#### Fragmento de Inserção
```html
<footer th:insert="~{fragments/footer :: footer}"></footer>
```

---

## 💻 Exemplos de Uso

### Exemplo 1: Listagem com Paginação

**Template: `livros/lista.html`**
```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org"
      xmlns:layout="http://www.ultraq.net.nz/thymeleaf/layout"
      layout:decorate="~{fragments/layout}"
      lang="pt-BR">
<head>
    <title>Livros</title>
</head>
<body>
    <div layout:fragment="content">
        <div class="container-fluid">
            <div class="row mb-4">
                <div class="col">
                    <h1 class="display-6">
                        <i class="bi bi-book-half"></i> Livros
                    </h1>
                </div>
                <div class="col-auto">
                    <a href="/admin/livros/novo" class="btn btn-primary">
                        <i class="bi bi-plus-circle"></i> Novo Livro
                    </a>
                </div>
            </div>

            <!-- Listagem -->
            <div class="card">
                <div class="table-responsive">
                    <table class="table table-hover mb-0">
                        <thead>
                            <tr>
                                <th>Código</th>
                                <th>Título</th>
                                <th>Autor</th>
                                <th>Status</th>
                                <th>Ações</th>
                            </tr>
                        </thead>
                        <tbody>
                            <tr th:each="livro : ${page.content}">
                                <td th:text="${livro.codigo}"></td>
                                <td th:text="${livro.titulo}"></td>
                                <td th:text="${livro.autor.nome}"></td>
                                <td>
                                    <span class="badge bg-success" 
                                          th:if="${livro.status == 'ATIVO'}">
                                        Ativo
                                    </span>
                                    <span class="badge bg-danger" 
                                          th:if="${livro.status == 'INATIVO'}">
                                        Inativo
                                    </span>
                                </td>
                                <td>
                                    <a href="#" th:href="@{/admin/livros/{id}(id=${livro.id})}" 
                                       class="btn btn-sm btn-outline-primary">
                                        <i class="bi bi-pencil"></i> Editar
                                    </a>
                                    <a href="#" th:href="@{/admin/livros/{id}/delete(id=${livro.id})}" 
                                       class="btn btn-sm btn-outline-danger"
                                       onclick="return confirmAction('Tem certeza?')">
                                        <i class="bi bi-trash"></i> Deletar
                                    </a>
                                </td>
                            </tr>
                        </tbody>
                    </table>
                </div>
            </div>

            <!-- Paginação -->
            <div th:insert="~{fragments/pagination :: pagination(page=${page})}"></div>
        </div>
    </div>
</body>
</html>
```

**Controller:**
```java
@Controller
@RequestMapping("/admin/livros")
public class LivroAdminController {
    
    @Autowired
    private LivroService livroService;
    
    @GetMapping
    public String listar(
        @RequestParam(defaultValue = "0") int page,
        Model model) {
        
        Page<LivroDetalheDTO> livros = livroService.listar(PageRequest.of(page, 10));
        
        model.addAttribute("page", livros);
        model.addAttribute("pageLink", "/admin/livros");
        model.addAttribute("isAdmin", true);
        
        return "livros/lista";
    }
}
```

---

### Exemplo 2: Formulário com Validação

**Template: `livros/form.html`**
```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org"
      xmlns:layout="http://www.ultraq.net.nz/thymeleaf/layout"
      layout:decorate="~{fragments/layout}"
      lang="pt-BR">
<head>
    <title>Novo Livro</title>
</head>
<body>
    <div layout:fragment="content">
        <div class="container-fluid">
            <div class="row mb-4">
                <div class="col">
                    <h1 class="display-6">
                        <i class="bi bi-book-plus"></i> Novo Livro
                    </h1>
                </div>
            </div>

            <div class="row">
                <div class="col-lg-8">
                    <form th:action="@{/admin/livros}" th:object="${livroForm}" method="post" novalidate>
                        <!-- Código -->
                        <div th:insert="~{fragments/form-errors :: text-field(name='codigo', label='Código', required=true)}"></div>

                        <!-- Título -->
                        <div th:insert="~{fragments/form-errors :: text-field(name='titulo', label='Título', required=true)}"></div>

                        <!-- Sinopse -->
                        <div th:insert="~{fragments/form-errors :: textarea-field(name='sinopse', label='Sinopse', required=true, rows='4')}"></div>

                        <!-- Ano -->
                        <div th:insert="~{fragments/form-errors :: number-field(name='ano', label='Ano', required=true, min='1900')}"></div>

                        <!-- Botões -->
                        <div class="d-flex gap-2 mt-4">
                            <button type="submit" class="btn btn-primary">
                                <i class="bi bi-check"></i> Salvar
                            </button>
                            <a href="/admin/livros" class="btn btn-secondary">
                                <i class="bi bi-x"></i> Cancelar
                            </a>
                        </div>
                    </form>
                </div>
            </div>
        </div>
    </div>
</body>
</html>
```

**Controller:**
```java
@Controller
@RequestMapping("/admin/livros")
public class LivroAdminController {
    
    @Autowired
    private LivroService livroService;
    
    @GetMapping("/novo")
    public String formulario(Model model) {
        model.addAttribute("livroForm", new LivroFormDTO());
        model.addAttribute("isAdmin", true);
        return "livros/form";
    }
    
    @PostMapping
    public String criar(
        @Valid @ModelAttribute LivroFormDTO dto,
        BindingResult result,
        RedirectAttributes attrs) {
        
        if (result.hasErrors()) {
            attrs.addFlashAttribute("mensagemErro", "Verifique os erros abaixo");
            return "redirect:/admin/livros/novo";
        }
        
        try {
            livroService.criar(dto);
            attrs.addFlashAttribute("mensagemSucesso", "Livro cadastrado com sucesso!");
            return "redirect:/admin/livros";
        } catch (ValidacaoException e) {
            attrs.addFlashAttribute("mensagemErro", e.getMessage());
            return "redirect:/admin/livros/novo";
        }
    }
}
```

---

## 📝 Convenções

### 1. **PRG Pattern (Post-Redirect-Get)**
Sempre use para POST:
```java
// POST - recebe dados
@PostMapping("/criar")
public String criar(...) {
    // processa
    return "redirect:/listar"; // redireciona
}

// GET - exibe resultado
@GetMapping("/listar")
public String listar(Model model) {
    // carrega dados
    return "listar"; // renderiza template
}
```

### 2. **Nomes de Atributos Flash**
Sempre use `mensagem*` para flash attributes:
- `mensagemSucesso` (verde)
- `mensagemErro` (vermelho)
- `mensagemAviso` (amarelo)
- `mensagemInfo` (azul)

### 3. **Estrutura de Diretórios de Templates**
```
templates/
├── fragments/
│   ├── layout.html          → Layout base
│   ├── navbar.html          → Navegação
│   ├── sidebar.html         → Menu admin
│   ├── footer.html          → Rodapé
│   ├── messages.html        → Alertas
│   ├── form-errors.html     → Campos de form
│   └── pagination.html      → Paginação
├── livros/
│   ├── lista.html           → Listagem
│   ├── form.html            → Formulário
│   └── detalhe.html         → Visualização
├── clientes/
│   ├── lista.html
│   ├── form.html
│   └── detalhe.html
├── index.html               → Home
└── error/
    ├── 404.html
    └── 500.html
```

### 4. **Máscaras de Entrada (JS)**
Use data-attributes para aplicar máscaras automaticamente:
```html
<!-- CPF -->
<input type="text" class="form-control" data-mask="cpf">

<!-- Telefone -->
<input type="text" class="form-control" data-mask="phone">

<!-- CEP -->
<input type="text" class="form-control" data-mask="cep">
```

---

## 🚀 Checklist para Nova Página

- [ ] Create `templates/modulo/pagina.html` usando `layout:decorate`
- [ ] Add `layout:fragment="content"` com seu HTML
- [ ] Create `@Controller` no pacote correto
- [ ] Add método `@GetMapping` carregando dados
- [ ] Add método `@PostMapping` processando dados
- [ ] Use `RedirectAttributes` para mensagens flash
- [ ] Add `isAdmin` ao model conforme necessário
