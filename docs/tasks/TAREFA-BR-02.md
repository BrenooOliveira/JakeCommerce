# TAREFA BR-02: GlobalExceptionHandler Aprimorado ✅

## Status: CONCLUÍDO

**Data:** 9 de março de 2026  
**Compilação:** ✅ BUILD SUCCESS (115 arquivos compilados)  
**Arquivo:** [GlobalExceptionHandler.java](src/main/java/com/les/jakebooks/exception/GlobalExceptionHandler.java)

---

## 📋 Resumo da Implementação

O GlobalExceptionHandler foi totalmente reescrito para suportar **dual-mode operation**:

1. **Mode REST/API** → Retorna JSON com ResponseEntity
2. **Mode MVC/View** → Redireciona com RedirectAttributes (Thymeleaf)

### Detecção de Tipo de Requisição

```java
private boolean aceitaJson(HttpServletRequest request) {
    String accept = request.getHeader("Accept");
    return accept != null && accept.contains(MediaType.APPLICATION_JSON_VALUE);
}
```

Verifica o header `Accept` para determinar se cliente aceita JSON ou HTML.

---

## 🔄 Fluxo de Tratamento por Exception

### 1. **NegocioException** (Classe Base)
- **JSON Request:** HTTP 422 + ErrorResponse com código RN
- **HTML Request:** Redireciona com flash attributes

```java
@ExceptionHandler(NegocioException.class)
public Object handleNegocioException(NegocioException ex, 
                                    HttpServletRequest request,
                                    RedirectAttributes redirectAttributes)
```

**Exemplos de subclasses capturadas:**
- LivroNaoEncontradoException → `/livros`
- EstoqueInsuficienteException → `/carrinho`
- PagamentoReprovadoException → `/carrinho/checkout`
- TrocaNaoPermitidaException → `/pedidos`
- ClienteNaoEncontradoException → `/clientes`

### 2. **MethodArgumentNotValidException** (@Valid)
Consolida erros de validação de formulários:

```java
@ExceptionHandler(MethodArgumentNotValidException.class)
public Object handleMethodArgumentNotValid(...)
```

**Estratégia:**

| Tipo | Resposta |
|------|----------|
| API JSON | `{ "erro": {...}, "erros": { "campo": "mensagem" } }` |
| HTML Form | Redireciona para `/referer` com flash attributes |

### 3. **Exception Genérica**
Captura erros inesperados:

```java
@ExceptionHandler(Exception.class)
public Object handleGenericException(...)
```

| Tipo | Resposta |
|------|----------|
| API JSON | HTTP 500 + JSON |
| HTML Request | Redireciona para `/error/500` |

---

## 📊 Handlers Específicos Implementados

| Exception | HTTP | Request JSON | Request HTML |
|-----------|------|:----:|:----:|
| **LivroNaoEncontradoException** | 404 | JSON | → `/livros` |
| **ClienteNaoEncontradoException** | 404 | JSON | → `/clientes` |
| **EstoqueInsuficienteException** | 422 | JSON | → `/carrinho` |
| **PagamentoReprovadoException** | 422 | JSON | → `/carrinho/checkout` |
| **CarrinhoExpiradoException** | 422 | JSON | → `/carrinho` |
| **TrocaNaoPermitidaException** | 422 | JSON | → `/pedidos` |
| **CupomInvalidoException** | 422 | JSON | → `/carrinho/checkout` |
| **LimitePedidoException** | 422 | JSON | → `/carrinho` |
| **ValorAbaixoDaMargemException** | 422 | JSON | → `/livros` |
| **SenhaFracaException** | 422 | JSON | → `/clientes/cadastro` |
| **MethodArgumentNotValidException** | 400 | JSON (map) | → referer |
| **Exception (genérica)** | 500 | JSON | → `/error/500` |

---

## 💡 Exemplos de Uso

### Exemplo 1: API Request (JSON)

```bash
curl -X GET \
  "http://localhost:8080/api/livros/999" \
  -H "Accept: application/json"
```

**Resposta:**
```json
{
  "status": 404,
  "mensagem": "Livro não encontrado",
  "detalhes": "999"
}
```

---

### Exemplo 2: Browser Request (HTML - Thymeleaf)

```html
<!-- User clica em "Deletar Livro" com código 999 -->
<!-- Flash attributes -->
erro: "Livro não encontrado: 999"
codigoRN: "RN0011"

<!-- Redireciona para POST → /livros -->
<!-- Thymeleaf renderiza com mensagem de erro -->
```

---

### Exemplo 3: Form Validation (@Valid)

```java
@PostMapping("/clientes")
public String criarCliente(@Valid ClienteDTO dto, 
                          BindingResult result,
                          RedirectAttributes attr) {
    // MethodArgumentNotValidException lançada automaticamente
}
```

**Resposta JSON (API):**
```json
{
  "erro": {
    "status": 400,
    "mensagem": "Erro de validação de formulário",
    "detalhes": "Verifique os campos obrigatórios"
  },
  "erros": {
    "email": "Email inválido",
    "cpf": "CPF obrigatório"
  }
}
```

**Resposta HTML (Browser):**
```
Redireciona de volta ao formulário com:
- erros (Map<String, String>)
- erro (mensagem geral)
```

---

## 🎯 Regras Implementadas

### RN Mapeadas

| Código | Descrição | Exception |
|--------|-----------|-----------|
| RN0011-RN0016 | Livro | LivroNaoEncontradoException, ValorAbaixoDaMargemException |
| RN0021-RN0028 | Cliente | ClienteNaoEncontradoException, SenhaFracaException |
| RN0031-RN0045 | Venda | EstoqueInsuficienteException, CarrinhoExpiradoException, etc. |
| RN0051-RN0064 | Estoque | EstoqueInsuficienteException, LimitePedidoException |

### RNF Implementadas

- **RNF0012:** Log de exceção com `ex.printStackTrace()` (melhorar com logger)

---

## 🔧 Métodos Auxiliares

### `obterMensagemPorTipo(NegocioException)`
Retorna título amigável da exceção para JSON:
```java
LivroNaoEncontradoException → "Livro não encontrado"
EstoqueInsuficienteException → "Estoque insuficiente"
// ...
```

### `obterUrlRedirecionamento(NegocioException)`
Retorna URL de redirecionamento apropriada:
```java
LivroNaoEncontradoException → /livros
EstoqueInsuficienteException → /carrinho
CarrinhoExpiradoException → /carrinho
// ...
```

### `construirErrorResponse(HttpStatus, String, String, String)`
Factory method para criar ErrorResponse padronizado.

---

## 📝 Próximos Passos

### Tasks Recomendadas

1. **Criar templates de erro:**
   - `error/500.html` (página de erro genérica)
   - `fragments/flash-messages.html` (exibir flash attributes)

2. **Melhorar logging:**
   - Substituir `ex.printStackTrace()` por logger SLF4J
   - Log de transações com usuário (RNF0012)

3. **Testes de integração:**
   - Testar NegocioException com Accept: application/json
   - Testar NegocioException com Accept: text/html
   - Testar MethodArgumentNotValidException em formulários

4. **Documentação:**
   - Adicionar exemplos de curl para cada exception
   - Documentar headers requeridos para cada tipo de resposta

---

## ✅ Checklist de Validação

- [x] GlobalExceptionHandler compila sem erros
- [x] 115 arquivos compilados com sucesso
- [x] Todos os 10 tipos de NegocioException capturados
- [x] MethodArgumentNotValidException consolida erros de campo
- [x] Content negotiation (JSON vs HTML) implementada
- [x] Redirecionamentos configurados por tipo de exception
- [x] Métodos auxiliares para mensagens e URLs
- [x] Flash attributes para Thymeleaf dados
- [x] HTTP status codes apropriados (404, 422, 500)
- [ ] Testes de integração (próximo step)
- [ ] Logging com SLF4J (próximo step)
- [ ] Templates de erro HTML (próximo step)

---

## 📦 Código-Chave

```java
// Detectar se é request JSON ou HTML
if (aceitaJson(request)) {
    // Retornar JSON com ResponseEntity
    return ResponseEntity.status(...).body(erro);
} else {
    // Redirecionar com flash attributes para Thymeleaf
    redirectAttributes.addFlashAttribute("erro", mensagem);
    return "redirect:/pagina-apropriada";
}
```

---

## 🚀 Como Usar em Controllers

### Exemplo: Controller de Livros

```java
@PostMapping
public String criar(@Valid LivroDTO dto, BindingResult result) {
    // MethodArgumentNotValidException capturada se validação falhar
    
    // Seu código lança exceção
    if (livro.getValorVenda() < margem) {
        throw new ValorAbaixoDaMargemException(...); // HTTP 422
    }
    
    // GlobalExceptionHandler detecta Accept header e:
    // - JSON request → ResponseEntity 422 + JSON
    // - HTML request → Redirect /livros com flash error
}
```

---

**Implementado por:** GitHub Copilot  
**Versão:** 1.0  
**Dependências:** Spring Boot 3.x, Thymeleaf, Spring Security Crypto  
**Compatibilidade:** Java 21+
