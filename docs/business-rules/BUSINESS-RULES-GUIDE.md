# Infraestrutura de Regras de Negócio - JakeCommerce

Este documento descreve a infraestrutura implementada para garantir que todas as regras de negócio sejam protegidas e aplicadas corretamente na aplicação.

## 📋 Estrutura Criada

### 1. **Exceções Customizadas** (`com.les.jakebooks.exception`)

Todas as violações de regras de negócio lançam exceções específicas (nunca `RuntimeException` genérica):

- **`ValidacaoNegocioException`**: Violação genérica de regra de negócio
- **`RecursoNaoEncontradoException`**: Recurso não encontrado (HTTP 404)
- **`EstoqueInsuficienteException`**: Estoque insuficiente (RN0031, RN0032)
- **`PagamentoReprovadoException`**: Pagamento reprovado (RN0037, RN0038, RN0065)
- **`CarrinhoExpiradoException`**: Carrinho expirado (RN0044, RN0045)
- **`ClienteBloqueadoException`**: Cliente bloqueado (StatusCliente.BLOQUEADO)
- **`TrocaNaoPermitidaException`**: Troca não permitida (RN0043)
- **`SenhaInseguraException`**: Senha não atende aos requisitos

### 2. **GlobalExceptionHandler** (`com.les.jakebooks.exception`)

Manipulador global de exceções com status HTTP apropriados:

- **HTTP 404**: `RecursoNaoEncontradoException`
- **HTTP 422**: Todas as violações de regras de negócio
- **HTTP 403**: `ClienteBloqueadoException`
- **HTTP 500**: Exceções genéricas

**Resposta padronizada:**
```json
{
  "timestamp": "2026-03-08T10:30:45.123",
  "status": 422,
  "mensagem": "Violação de regra de negócio",
  "detalhes": "Mensagem específica do erro",
  "campo": "campo_afetado"
}
```

### 3. **Validators Customizados** (`com.les.jakebooks.validator`)

Componentes `@Component` para validar regras de negócio específicas:

#### **SenhaValidator**
- Valida requisitos: 8+ caracteres, maiúscula, minúscula, caractere especial
- Lança `SenhaInseguraException` com detalhes do erro

```java
@Autowired
private SenhaValidator senhaValidator;

public void cadastrarCliente(String senha) {
    senhaValidator.validarSenha(senha); // Lança exceção se inválido
    // ... resto da lógica
}
```

#### **EstoqueValidator**
- RN0031, RN0032: Validar estoque no carrinho e antes de finalizar
- RN0061-0062: Quantidade e custo obrigatórios
- RN0063: Máximo 10 unidades por livro por pedido

```java
@Autowired
private EstoqueValidator estoqueValidator;

public void adicionarAoCarrinho(String codigoLivro, Integer quantidade, Integer quantidadeDisponivel) {
    estoqueValidator.validarQuantidadeDisponivel(codigoLivro, quantidade, quantidadeDisponivel);
    estoqueValidator.validarLimiteUnidadesPerPedido(quantidade);
}
```

#### **ClienteValidator**
- RN0021-0027: Validar dados, endereços, cartões
- Validar CPF, email, bandeira de cartão

```java
@Autowired
private ClienteValidator clienteValidator;

public void cadastrarCliente(String nome, String cpf, String email) {
    clienteValidator.validarDadosObrigatorios(nome, cpf, email);
    clienteValidator.validarCPF(cpf);
}
```

#### **LivroValidator**
- RN0011: Dados obrigatórios
- RN0013-0014: Validar valor de venda baseado em margem

```java
@Autowired
private LivroValidator livroValidator;

public void cadastrarLivro(String codigo, String titulo, String isbn, Double custoBase, Double percentualMargem, Double valorVenda) {
    livroValidator.validarDadosObrigatorios(codigo, titulo, isbn);
    livroValidator.validarValorVenda(custoBase, percentualMargem, valorVenda);
}
```

#### **PagamentoValidator**
- RN0033-0038: Validar cupons, cartões, valores mínimos
- RN0065: Bloqueio por tentativas reprovadas

```java
@Autowired
private PagamentoValidator pagamentoValidator;

public void processarPagamento(Double valorTotal, String codigoPedido, Integer numeroCupons) {
    pagamentoValidator.validarDadosPagamento(valorTotal, codigoPedido);
    pagamentoValidator.validarUnicoCupomPromocional(numeroCupons);
    pagamentoValidator.validarValorMinimoPedido(valorTotal, false);
}
```

#### **TrocaValidator**
- RN0043: Apenas pedidos ENTREGUES podem solicitar troca

```java
@Autowired
private TrocaValidator trocaValidator;

public void solicitarTroca(String codigoPedido, String statusPedido, String motivo) {
    trocaValidator.validarStatusPedidoParaTroca(statusPedido, codigoPedido);
    trocaValidator.validarDadosObrigatorios(codigoPedido, motivo);
}
```

#### **CarrinhoValidator**
- RN0044-0045: Validar expiração e remoção de itens
- RN0063: Máximo 10 unidades por produto

```java
@Autowired
private CarrinhoValidator carrinhoValidator;

public void validarCarrinho(Integer numeroItens, String statusCarrinho) {
    carrinhoValidator.validarCarrinhoNaoVazio(numeroItens);
    carrinhoValidator.validarCarrinhoParaCheckout(statusCarrinho);
}
```

### 4. **Configuração de Segurança** (`com.les.jakebooks.config`)

#### **SecurityConfig**
- Configurar BCryptPasswordEncoder com força 12
- Criptografia de senhas

#### **WebMvcConfig**
- Registrar `TransacaoInterceptor`
- Configurar rotas interceptadas

### 5. **Interceptor de Transações** (`com.les.jakebooks.interceptor`)

**TransacaoInterceptor** registra:
- ✅ Data/hora (RNF0012)
- ✅ Usuário autenticado
- ✅ Operação (método HTTP + rota)
- ✅ Dados enviados (sem senhas)
- ✅ Status da resposta
- ✅ Tempo de processamento

Exemplo de log:
```
[2026-03-08 10:30:45.123] TRANSACAO INICIADA | Usuario: cliente@email.com | Método: POST | Rota: /pedidos | Dados: ...
[2026-03-08 10:30:45.456] TRANSACAO CONCLUÍDA | Usuario: cliente@email.com | Método: POST | Rota: /pedidos | Dados: Status: 201 | Duração: 333ms
```

### 6. **Utilitários** (`com.les.jakebooks.util`)

#### **CriptografiaUtil**
- `criptografar(senha)`: Criptografa senha com BCrypt
- `validar(senhaTextoPlano, senhaHash)`: Valida senha contra hash
- `gerarSenhaTemporaria()`: Gera senha temporária aleatória

```java
String senhaCriptografada = CriptografiaUtil.criptografar(senhaUsuario);
boolean senhaValida = CriptografiaUtil.validar(senhaInformada, senhaCriptografada);
```

---

## 🎯 Padrão de Uso - Service Layer

**Regra obrigatória #1: Nunca deixar regra de negócio no Controller**

Toda a lógica de negócio deve estar na camada Service:

```java
@Service
@Transactional
public class ClienteService {

    @Autowired
    private ClienteRepository clienteRepository;
    
    @Autowired
    private SenhaValidator senhaValidator;
    
    @Autowired
    private ClienteValidator clienteValidator;

    public Cliente cadastrarCliente(ClienteDTO dto) {
        // 1. Validar dados obrigatórios
        clienteValidator.validarDadosObrigatorios(dto.getNome(), dto.getCpf(), dto.getEmail());
        clienteValidator.validarCPF(dto.getCpf());
        
        // 2. Validar senha
        senhaValidator.validarSenha(dto.getSenha());
        
        // 3. Verificar se cliente já existe
        if (clienteRepository.findByCpf(dto.getCpf()).isPresent()) {
            throw new ValidacaoNegocioException("Cliente com CPF já cadastrado");
        }
        
        // 4. Criptografar senha
        String senhaCriptografada = CriptografiaUtil.criptografar(dto.getSenha());
        
        // 5. Criar cliente
        Cliente cliente = new Cliente();
        cliente.setNome(dto.getNome());
        cliente.setCpf(dto.getCpf());
        cliente.setEmail(dto.getEmail());
        cliente.setSenhaCriptografada(senhaCriptografada);
        cliente.setStatus(StatusCliente.ATIVO);
        
        return clienteRepository.save(cliente);
    }
}
```

**Controller (ZERO lógica de negócio):**

```java
@RestController
@RequestMapping("/api/clientes")
public class ClienteController {

    @Autowired
    private ClienteService clienteService;

    @PostMapping
    public ResponseEntity<ClienteDTO> criar(@RequestBody ClienteDTO dto) {
        Cliente cliente = clienteService.cadastrarCliente(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(new ClienteDTO(cliente));
    }
}
```

**GlobalExceptionHandler** captura automaticamente:
```
POST /api/clientes
400 -> Validação de dados JSON do Spring
422 -> ValidacaoNegocioException ou SenhaInseguraException
500 -> Exceções inesperadas
```

---

## 🔒 Mapeamento de HTTP Status

| Exceção | HTTP | Significado |
|---------|------|------------|
| `ValidacaoNegocioException` | 422 | Regra de negócio violada |
| `EstoqueInsuficienteException` | 422 | Sem estoque |
| `PagamentoReprovadoException` | 422 | Pagamento recusado |
| `CarrinhoExpiradoException` | 422 | Carrinho expirou |
| `ClienteBloqueadoException` | 403 | Cliente bloqueado |
| `TrocaNaoPermitidaException` | 422 | Troca não permitida |
| `SenhaInseguraException` | 422 | Senha fraca |
| `RecursoNaoEncontradoException` | 404 | Recurso não existe |
| Exceção genérica | 500 | Erro interno |

---

## 📊 Diagrama de Fluxo

```
Request HTTP
    ↓
TransacaoInterceptor.preHandle() [Log início]
    ↓
Controller (Recebe, chama Service, retorna view)
    ↓
Service (Validators + Lógica de Negócio)
    ↓
Exception?
    ├── Sim → GlobalExceptionHandler → ErrorResponse (JSON)
    └── Não → Sucesso
    ↓
TransacaoInterceptor.afterCompletion() [Log fim]
    ↓
Response HTTP
```

---

## ✅ Checklist de Implementação para Novos Requisitos

Ao implementar novo requisito com regra de negócio:

- [ ] Criar exceção customizada em `exception/` se necessário
- [ ] Implementar validator em `validator/` se necessário
- [ ] Adicionar manejador em `GlobalExceptionHandler`
- [ ] Implementar lógica de negócio em Service (com `@Transactional` se necessário)
- [ ] Chamar validators no Service (NUNCA no Controller)
- [ ] Registrar em log (automaticamente via `TransacaoInterceptor`)
- [ ] Testar com casos que violem a regra

---

## 🧪 Exemplo Completo: Cadastro de Livro

### Requisito de Negócio
- RN0011: Dados obrigatórios
- RN0013: Valor de venda baseado em margem
- RN0014: Redução abaixo da margem exige autorização

### Implementação

**Entity:**
```java
@Entity
public class Livro {
    @Id
    private String codigo;
    private String titulo;
    private String isbn;
    private Double valorVenda;
    // ...
}
```

**Service:**
```java
@Service
@Transactional
public class LivroService {

    @Autowired
    private LivroRepository livroRepository;
    
    @Autowired
    private LivroValidator livroValidator;

    public Livro cadastrарLivro(LivroDTO dto, Double custoBase, Double percentualMargem) {
        // Validar dados obrigatórios
        livroValidator.validarDadosObrigatorios(dto.getCodigo(), dto.getTitulo(), dto.getIsbn());
        
        // Validar valor de venda
        livroValidator.validarValorVenda(custoBase, percentualMargem, dto.getValorVenda());
        
        // Se chegou aqui, passou por todas as regras
        Livro livro = new Livro();
        livro.setCodigo(dto.getCodigo());
        livro.setTitulo(dto.getTitulo());
        livro.setIsbn(dto.getIsbn());
        livro.setValorVenda(dto.getValorVenda());
        livro.setStatus(StatusLivro.ATIVO);
        
        return livroRepository.save(livro);
    }
}
```

**Controller:**
```java
@RestController
@RequestMapping("/api/livros")
public class LivroController {

    @Autowired
    private LivroService livroService;

    @PostMapping
    public ResponseEntity<LivroDTO> criar(@RequestBody LivroDTO dto) {
        Livro livro = livroService.cadastrarLivro(dto, dto.getCustoBase(), dto.getPercentualMargem());
        return ResponseEntity.status(HttpStatus.CREATED).body(new LivroDTO(livro));
    }
}
```

**Teste:**
```
POST /api/livros
{
  "codigo": "JAVA001",
  "titulo": "Java Avançado",
  "isbn": "978-1-234567-89-0",
  "valorVenda": 10.00  // Abaixo da margem
}

Response:
HTTP 422 Unprocessable Entity
{
  "timestamp": "2026-03-08T10:30:45.123",
  "status": 422,
  "mensagem": "Violação de regra de negócio",
  "detalhes": "Valor de venda está abaixo da margem. Redução exige autorização.",
  "campo": "valorVenda"
}
```

---

## 📝 RNF0012: Log de Transações

Todos os logs são automaticamente registrados pelo `TransacaoInterceptor`:

**Console Output:**
```
[2026-03-08 10:30:45.123] TRANSACAO INICIADA | Usuario: admin | Método: POST | Rota: /api/clientes | Dados: nome=João&email=joao@email.com&cpf=123456789
[2026-03-08 10:30:45.456] TRANSACAO CONCLUÍDA | Usuario: admin | Método: POST | Rota: /api/clientes | Dados: Status: 201 | Duração: 333ms
```

**Para persistir logs em banco de dados:**
1. Criar tabela `AUDITORIA` com campos: `id`, `timestamp`, `usuario`, `operacao`, `rota`, `metodo`, `status`, `duracao_ms`
2. Estender `TransacaoInterceptor` com repositório
3. Salvar logs no banco após `afterCompletion()`

---

## 🚀 Próximos Passos

1. **Integrar com Spring Security completo** para autenticação/autorização
2. **Implementar AuditingEntityListener** para rastrear mudanças nas entidades
3. **Adicionar @Validated** nas DTOs
4. **Criar testes unitários** para cada validator
5. **Implementar rate limiting** para APIs
6. **Adicionar criptografia de dados sensíveis** em repouso

---

**Última atualização:** 08 de março de 2026  
**Versão:** 1.0
