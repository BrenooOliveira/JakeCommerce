# Agent: fix-livros-comprar

## Problema Identificado

1. A pagina `/livros` (listagem) nao possui botao de "comprar" ou "adicionar ao carrinho"
2. O botao de "ver" na listagem que redireciona para `/livros/{codigo}` funciona, mas a pagina de detalhe tambem nao possui opcao de compra
3. A pagina de detalhe do livro (`/livros/{codigo}`) possui apenas acoes administrativas (editar, inativar, ativar), sem possibilidade de adicionar ao carrinho

### Resultado Esperado

- Clientes autenticados devem poder adicionar livros ao carrinho diretamente da listagem
- Clientes autenticados devem poder adicionar livros ao carrinho na pagina de detalhe
- O botao deve verificar estoque e status do livro antes de exibir

## Arquivos Envolvidos

| Arquivo | Descricao |
|---------|-----------|
| `livros/lista.html` | Template de listagem - adicionar botao de comprar |
| `livros/detalhe.html` | Template de detalhe - adicionar botao de adicionar ao carrinho |
| `CarrinhoController.java` | Ja possui endpoint `/carrinho/adicionar` - nenhuma modificacao necessaria |

## Solucao Proposta

### Passo 1: Modificar livros/lista.html

Adicionar botao de "Adicionar ao Carrinho" na coluna de acoes, visivel apenas para usuarios autenticados e quando o livro esta ativo e com estoque:

```html
<!-- Dentro do loop th:each="livro : ${page.content}" na coluna de acoes -->

<!-- Botao Adicionar ao Carrinho - para clientes autenticados -->
<form th:if="${livro.status().name() == 'ATIVO'}"
      sec:authorize="isAuthenticated()"
      th:action="@{/carrinho/adicionar}"
      method="post" class="d-inline">
    <input type="hidden" name="codigoLivro" th:value="${livro.codigo()}" />
    <input type="hidden" name="quantidade" value="1" />
    <button type="submit" class="btn btn-sm btn-success" title="Adicionar ao Carrinho">
        <i class="bi bi-cart-plus"></i>
    </button>
</form>
```

### Passo 2: Modificar livros/detalhe.html

Adicionar card de "Comprar" no sidebar, visivel para usuarios autenticados:

```html
<!-- Novo Card de Compra - antes do Card de Acoes (para clientes) -->
<div class="card mb-4" sec:authorize="isAuthenticated()">
    <div class="card-header bg-success text-white">
        <h5 class="card-title mb-0">
            <i class="bi bi-cart-check"></i> Comprar
        </h5>
    </div>
    <div class="card-body">
        <!-- Verificar se livro esta ativo e com estoque -->
        <div th:if="${livro.status() == T(com.les.jakebooks.model.enums.StatusLivro).ATIVO and livro.estoque().quantidade() > 0}">
            <form th:action="@{/carrinho/adicionar}" method="post">
                <input type="hidden" name="codigoLivro" th:value="${livro.codigo()}" />

                <div class="mb-3">
                    <label for="quantidade" class="form-label">Quantidade</label>
                    <select class="form-select" id="quantidade" name="quantidade">
                        <option th:each="i : ${#numbers.sequence(1, T(java.lang.Math).min(10, livro.estoque().quantidade()))}"
                                th:value="${i}" th:text="${i}"></option>
                    </select>
                    <small class="text-muted">
                        Disponivel: <span th:text="${livro.estoque().quantidade()}"></span> un.
                    </small>
                </div>

                <div class="d-grid">
                    <button type="submit" class="btn btn-success btn-lg">
                        <i class="bi bi-cart-plus"></i> Adicionar ao Carrinho
                    </button>
                </div>
            </form>
        </div>

        <!-- Mensagem se livro inativo -->
        <div th:if="${livro.status() != T(com.les.jakebooks.model.enums.StatusLivro).ATIVO}"
             class="alert alert-warning mb-0">
            <i class="bi bi-exclamation-triangle"></i>
            Este livro nao esta disponivel para venda.
        </div>

        <!-- Mensagem se sem estoque -->
        <div th:if="${livro.status() == T(com.les.jakebooks.model.enums.StatusLivro).ATIVO and livro.estoque().quantidade() == 0}"
             class="alert alert-danger mb-0">
            <i class="bi bi-x-circle"></i>
            Produto sem estoque no momento.
        </div>
    </div>
</div>

<!-- Card de Acoes admin - modificar para mostrar apenas para admin -->
<div class="card mb-4" th:if="${isAdmin == true}">
    <!-- conteudo existente de acoes admin -->
</div>
```

### Passo 3: Adicionar link para Login (usuarios nao autenticados)

No detalhe do livro, mostrar incentivo para login:

```html
<!-- Para usuarios nao autenticados -->
<div class="card mb-4" sec:authorize="!isAuthenticated()">
    <div class="card-header">
        <h5 class="card-title mb-0">
            <i class="bi bi-cart-check"></i> Comprar
        </h5>
    </div>
    <div class="card-body text-center">
        <p>Faca login para adicionar este livro ao seu carrinho.</p>
        <a th:href="@{/login}" class="btn btn-primary">
            <i class="bi bi-box-arrow-in-right"></i> Fazer Login
        </a>
        <p class="mt-2 mb-0">
            <small>Nao tem conta? <a th:href="@{/clientes/novo}">Cadastre-se</a></small>
        </p>
    </div>
</div>
```

## Validacao

Apos implementar:

1. **Listagem** (`/livros`):
   - Usuario nao autenticado: botao de comprar NAO aparece
   - Usuario autenticado: botao de comprar aparece para livros ATIVOS
   - Clicar no botao adiciona 1 unidade ao carrinho

2. **Detalhe** (`/livros/{codigo}`):
   - Usuario nao autenticado: card de compra mostra incentivo para login
   - Usuario autenticado: card de compra mostra seletor de quantidade e botao
   - Livro INATIVO: mostra mensagem de indisponibilidade
   - Livro sem estoque: mostra mensagem de falta de estoque
   - Adicionar ao carrinho funciona corretamente

## Criterios de Conclusao

- [ ] Botao de adicionar ao carrinho na listagem (para autenticados)
- [ ] Card de compra no detalhe do livro (para autenticados)
- [ ] Card de incentivo ao login (para nao autenticados)
- [ ] Validacao de status ATIVO antes de permitir compra
- [ ] Validacao de estoque antes de permitir compra
- [ ] Seletor de quantidade limitado ao estoque disponivel (max 10)
- [ ] Integracao com endpoint existente `/carrinho/adicionar`

## Dependencias

- **REQUER:** `fix-session-auth` executado primeiro
  - Sem a sessao correta, o `/carrinho/adicionar` ira falhar

## Notas Tecnicas

- Usar `sec:authorize="isAuthenticated()"` para controle de visibilidade baseado em autenticacao
- Usar `th:if="${isAdmin == true}"` para controle de visibilidade baseado em role
- Limite de 10 unidades por item (RN0063)
- O endpoint `/carrinho/adicionar` ja existe e funciona
- Certificar-se de adicionar namespace `xmlns:sec="http://www.thymeleaf.org/extras/spring-security"` se nao existir
