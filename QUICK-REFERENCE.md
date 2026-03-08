# Quick Reference - Regras de Negócio JakeCommerce

## 🚀 Checklist Rápido

Use este arquivo como referência rápida ao desenvolver novo requisito.

---

## 1. Implementar Nova Regra de Negócio

### ✓ Passo 1: Criar Exceção (se necessário)
```java
// exception/MinhaException.java
public class MinhaException extends RuntimeException {
    public MinhaException(String mensagem) {
        super(mensagem);
    }
}
```

### ✓ Passo 2: Adicionar Handler (se necessário)
```java
// exception/GlobalExceptionHandler.java
@ExceptionHandler(MinhaException.class)
public ResponseEntity<ErrorResponse> handleMinha(MinhaException ex) {
    ErrorResponse erro = new ErrorResponse(
        HttpStatus.UNPROCESSABLE_ENTITY.value(),
        "Mensagem de erro",
        ex.getMessage()
    );
    return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(erro);
}
```

### ✓ Passo 3: Criar Validator (se necessário)
```java
// validator/MeuValidator.java
@Component
public class MeuValidator {
    public void validar(String campo) {
        if (condicaoErro) {
            throw new MinhaException("Mensagem de erro");
        }
    }
}
```

### ✓ Passo 4: Usar no Service
```java
@Service
@Transactional
public class MeuService {
    @Autowired private MeuValidator validator;
    
    public void meuMetodo(String campo) {
        validator.validar(campo);  // Lança exceção se inválido
        // Resto da lógica...
    }
}
```

### ✓ Passo 5: NUNCA colocar lógica no Controller
```java
@RestController
public class MeuController {
    @Autowired private MeuService service;
    
    @PostMapping
    public ResponseEntity<?> criar(@RequestBody MeuDTO dto) {
        return ResponseEntity.ok(service.meuMetodo(dto));
        // FIM - GlobalExceptionHandler cuida do resto
    }
}
```

---

## 2. Validators Disponíveis

### SenhaValidator
```java
@Autowired private SenhaValidator senhaValidator;

senhaValidator.validarSenha(senha);  // Lança SenhaInseguraException
```

### EstoqueValidator
```java
@Autowired private EstoqueValidator estoqueValidator;

estoqueValidator.validarQuantidadeDisponivel(codigo, solicitado, disponivel);
estoqueValidator.validarLimiteUnidadesPerPedido(quantidade);
estoqueValidator.validarQuantidadePositiva(quantidade);
estoqueValidator.validarCusto(custo);
```

### ClienteValidator
```java
@Autowired private ClienteValidator clienteValidator;

clienteValidator.validarDadosObrigatorios(nome, cpf, email);
clienteValidator.validarCPF(cpf);
clienteValidator.validarDadosEndereco(logradouro, numero, bairro, cep, cidade, estado);
clienteValidator.validarDadosCarta(numero, nomeImpresso, codigoSeguranca, bandeira);
clienteValidator.validarRanking(ranking);
```

### LivroValidator
```java
@Autowired private LivroValidator livroValidator;

livroValidator.validarDadosObrigatorios(codigo, titulo, isbn);
livroValidator.validarValorVenda(custoBase, percentualMargem, valorVenda);
livroValidator.validarNumeroPaginas(numeroPaginas);
livroValidator.validarAno(ano);
```

### PagamentoValidator
```java
@Autowired private PagamentoValidator pagamentoValidator;

pagamentoValidator.validarDadosPagamento(valorTotal, codigoPedido);
pagamentoValidator.validarUnicoCupomPromocional(numeroCupons);
pagamentoValidator.validarValorMinimoPedido(valorTotal, temFrete);
pagamentoValidator.validarLimiteTentativasReprovadas(tentativas);
pagamentoValidator.validarPagamentoAprovado(status, codigoPedido);
```

### TrocaValidator
```java
@Autowired private TrocaValidator trocaValidator;

trocaValidator.validarStatusPedidoParaTroca(statusPedido, codigoPedido);
trocaValidator.validarDadosObrigatorios(codigoPedido, motivo);
trocaValidator.validarStatusTroca(status);
```

### CarrinhoValidator
```java
@Autowired private CarrinhoValidator carrinhoValidator;

carrinhoValidator.validarCarrinhoNaoVazio(numeroItens);
carrinhoValidator.validarQuantidadeCarrinho(quantidade);
carrinhoValidator.validarCarrinhoParaCheckout(statusCarrinho);
carrinhoValidator.validarExpiracaoProxima(minutosFaltando);
```

---

## 3. Criptografia de Senhas

```java
import com.les.jakebooks.util.CriptografiaUtil;

// Criptografar
String hash = CriptografiaUtil.criptografar(senhaUsuario);

// Validar
boolean senhaValida = CriptografiaUtil.validar(senhaInformada, hashArmazenado);

// Gerar temporária
String senhaTemporaria = CriptografiaUtil.gerarSenhaTemporaria();
```

---

## 4. Status HTTP Esperados

| Erro | Status | Handler |
|------|--------|---------|
| Regra de negócio violada | 422 | GlobalExceptionHandler |
| Recurso não encontrado | 404 | GlobalExceptionHandler |
| Cliente bloqueado | 403 | GlobalExceptionHandler |
| Erro inesperado | 500 | GlobalExceptionHandler |
| Sucesso | 200/201 | Ok |

---

## 5. Log Automático (RNF0012)

✅ Automático via `TransacaoInterceptor`

Registra:
- Data/hora: `2026-03-08 10:30:45.123`
- Usuário: `admin@email.com` ou `ANONIMO`
- Método: `POST`, `GET`, `PUT`, `DELETE`
- Rota: `/api/clientes`, `/api/livros`, etc.
- Parâmetros: `nome=João&email=joao@email.com` (sem senhas)
- Status: `201`, `422`, `500`, etc.
- Duração: `333ms`

**Nada a fazer - acontece automaticamente!**

---

## 6. ErrorResponse - Formato Padronizado

Sempre retorna:
```json
{
  "timestamp": "2026-03-08T10:30:45.123",
  "status": 422,
  "mensagem": "Violação de regra de negócio",
  "detalhes": "Detalhes específicos do erro",
  "campo": "campo_afetado"
}
```

---

## 7. Exemplo Completo

### Requisito
Não permitir quantidade zero em estoque

### Implementação

**1. Validator (já existe)**
```java
// EstoqueValidator.java
public void validarQuantidadePositiva(Integer quantidade) {
    if (quantidade == null || quantidade <= 0) {
        throw new IllegalArgumentException("Quantidade deve ser maior que zero");
    }
}
```

**2. Service**
```java
@Service
@Transactional
public class EstoqueService {
    @Autowired private EstoqueValidator estoqueValidator;
    
    public void entradaEmEstoque(String codigoLivro, Integer quantidade) {
        estoqueValidator.validarQuantidadePositiva(quantidade);
        // Salvar no banco...
    }
}
```

**3. Controller**
```java
@RestController
@RequestMapping("/api/estoque")
public class EstoqueController {
    @Autowired private EstoqueService estoqueService;
    
    @PostMapping("/entrada")
    public ResponseEntity<?> entrada(@RequestBody EstoqueDTO dto) {
        estoqueService.entradaEmEstoque(dto.getCodigoLivro(), dto.getQuantidade());
        return ResponseEntity.ok().build();
    }
}
```

**4. Resultado**
```
POST /api/estoque/entrada
Body: { "codigoLivro": "JAVA001", "quantidade": 0 }

GlobalExceptionHandler:
  └→ IllegalArgumentException
    └→ HTTP 500 (não é RN específica)

// OU usar uma exceção de negócio:
throw new ValidacaoNegocioException("Quantidade deve ser maior que zero");
  └→ HTTP 422 ✓ Correto
```

---

## 8. Troubleshooting

### ❌ Exceção não é capturada
- Verifica se classe está no pacote `com.les.jakebooks.exception`
- Verifica se `GlobalExceptionHandler` tem `@ControllerAdvice`
- Restart da aplicação

### ❌ Validador não é injetado
- Verifica se tem `@Component`
- Verifica se campo tem `@Autowired`
- Verifica se classe está no pacote `com.les.jakebooks.validator`

### ❌ Log não aparece
- Logs são em console (System.out.println)
- Verifica console da aplicação
- Pode adicionar arquivo rotativo (Appender do Log4j)

### ❌ Compilação falha
```bash
cd jakebooks
mvn clean compile
```

---

## 9. Documentação Completa

Para mais informações, consulte:

- **BUSINESS-RULES-GUIDE.md** - Guia completo (1500+ linhas)
- **BUSINESS-RULES-EXAMPLES.md** - 5 exemplos práticos
- **BUSINESS-RULES-IMPLEMENTATION.md** - Status da implementação
- **Comentários no código-fonte** - Documentação inline

---

## 10. Checklist Antes de Commitar

- [ ] Lógica de negócio está em Service
- [ ] Controller não tem lógica de negócio
- [ ] Uso de Validator apropriado
- [ ] Exceção específica é lançada (não genérica)
- [ ] GlobalExceptionHandler trata a exceção
- [ ] Testes unitários criam (happy path + error cases)
- [ ] Código compila sem erros críticos
- [ ] Logs aparecem no console (RNF0012)

---

**Última atualização:** 08 de março de 2026  
**Versão:** 1.0

---

## 📌 Atalhos Úteis

```bash
# Compilar
cd jakebooks && mvn clean compile

# Rodar testes
mvn test

# Executar aplicação
mvn spring-boot:run

# Gerar JAR
mvn clean package
```

---

🚀 **Pronto para desenvolver com confiança!**
