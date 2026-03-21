# TAREFA BR-03: Validators Implementados ✅

## Status: CONCLUÍDO

**Data:** 9 de março de 2026  
**Compilação:** ✅ BUILD SUCCESS (117 arquivos compilados)  
**Pacote:** `com.les.jakebooks.validator`

---

## 📋 Resumo de Implementação

Implementados **4 validators** essenciais com padrão consistente:

- ✅ **SenhaValidator** (ATUALIZADO) - Força de senha
- ✅ **CpfValidator** (NOVO) - Validação de CPF com dígitos verificadores
- ✅ **CartaoValidator** (NOVO) - Validação completa de cartão de crédito
- ✅ **EstoqueValidator** (APRIMORADO) - Quantidade e custo de estoque

---

## 🔐 1. SenhaValidator

**Arquivo:** [SenhaValidator.java](src/main/java/com/les/jakebooks/validator/SenhaValidator.java)

### Funcionalidades

```java
@Component
public class SenhaValidator {
    public void validarSenha(String senha) throws SenhaFracaException
}
```

### Requisitos Validados

| Critério | Detalhe | Motivo |
|----------|---------|--------|
| Tamanho mínimo | 8 caracteres | Segurança básica |
| Maiúscula | Pelo menos 1 (A-Z) | Complexidade aumentada |
| Minúscula | Pelo menos 1 (a-z) | Complexidade aumentada |
| Especial | Pelo menos 1 (!@#$%...) | Complexidade aumentada |

### Exceção Lançada
- **SenhaFracaException** (NegocioException child)
- Inclui `motivoRejeicao` detalhado (ex: "Falta letra maiúscula")

### Exemplos

```java
@Autowired
private SenhaValidator senhaValidator;

// Válido
senhaValidator.validarSenha("SenhaForte@123");

// Inválido - lança SenhaFracaException
senhaValidator.validarSenha("abc123");  // Falta maiúscula e especial
senhaValidator.validarSenha("ABCDEF123"); // Falta minúscula
senhaValidator.validarSenha("Abc");  // Menor que 8 caracteres
```

---

## 🆔 2. CpfValidator

**Arquivo:** [CpfValidator.java](src/main/java/com/les/jakebooks/validator/CpfValidator.java)

**Novo componente! Implementação completa com algoritmo da Receita Federal.**

### Funcionalidades

```java
@Component
public class CpfValidator {
    public void validarCpf(String cpf) throws ValidacaoNegocioException
    public boolean ehValido(String cpf)  // Retorna true/false
}
```

### Validações Implementadas

1. **Formato**
   - Aceita com ou sem formatação (XXX.XXX.XXX-XX)
   - Remove automaticamente hífen e pontos
   - Valida tamanho exato: 11 dígitos

2. **Dígitos Verificadores**
   - Algoritmo oficial Receita Federal
   - Primeiro dígito verificador (posição 10)
   - Segundo dígito verificador (posição 11)

3. **Casos Especiais**
   - Rejeita CPF com todos dígitos iguais (000.000.000-00, 111.111.111-11, etc)

### Exceção Lançada
- **ValidacaoNegocioException** com mensagem específica
- Motivos: tamanho inválido, dígitos iguais, verificador incorreto

### Exemplos

```java
@Autowired
private CpfValidator cpfValidator;

// Válido
cpfValidator.validarCpf("123.456.789-09");  // Com formatação
cpfValidator.validarCpf("12345678909");     // Sem formatação

// Inválido - lança ValidacaoNegocioException
cpfValidator.validarCpf("111.111.111-11");  // Todos dígitos iguais
cpfValidator.validarCpf("123.456.789-00");  // Dígito verificador errado
cpfValidator.validarCpf("12345");            // Poucos dígitos

// Verificação sem exceção
if (cpfValidator.ehValido("123.456.789-09")) {
    // Prosseguir com cadastro
}
```

### Algoritmo de Validação

```
1. Remove formatação (pontos e hífen)
2. Valida tamanho = 11
3. Valida se não são todos dígitos iguais
4. Calcula primeiro dígito verificador:
   - Multiplica dígitos 0-8 por 10, 9, 8, 7, 6, 5, 4, 3, 2
   - Soma resultados
   - resto = soma % 11
   - digito = resto < 2 ? 0 : 11 - resto
5. Valida se posição 9 = digito calculado
6. Calcula segundo dígito verificador:
   - Similar ao primeiro, usando dígitos 0-9
   - Multiplicadores: 11, 10, 9, 8, 7, 6, 5, 4, 3, 2
7. Valida se posição 10 = digito calculado
```

---

## 💳 3. CartaoValidator

**Arquivo:** [CartaoValidator.java](src/main/java/com/les/jakebooks/validator/CartaoValidator.java)

**Novo componente! Validação completa de cartão de crédito com algoritmo de Luhn.**

### Funcionalidades

```java
@Component
public class CartaoValidator {
    public void validarNumero(String numero) throws ValidacaoNegocioException
    public void validarBandeira(String bandeira) throws ValidacaoNegocioException
    public void validarVencimento(Integer mes, Integer ano) throws ValidacaoNegocioException
    public void validarCvv(String cvv, String bandeira) throws ValidacaoNegocioException
    public void validarNomeImpresso(String nomeImpresso) throws ValidacaoNegocioException
    public void validarCartaoCompleto(...) throws ValidacaoNegocioException
}
```

### Bandeiras Suportadas
- **VISA** (4 primeiros dígitos)
- **MASTERCARD** (5 primeiros dígitos)
- **ELO** (6 primeiros dígitos)
- **AMEX** (alternativamente)

RN0025: Bandeira deve estar cadastrada

### Validações Implementadas

| Campo | Validação | Detalhes |
|-------|-----------|----------|
| **Número** | Algoritmo de Luhn | 13-19 dígitos, checksum válido |
| **Bandeira** | Cadastrada | VISA, MASTERCARD, ELO, AMEX |
| **Vencimento** | Não expirado | Mês 1-12, ano válido, data futura |
| **CVV** | Formato | 3 dígitos (4 para AMEX) |
| **Nome** | Comprimento e caracteres | 3-30 caracteres, maiúsculas + espaço/hífen/apóstrofo |

### Exceção Lançada
- **ValidacaoNegocioException** com mensagem específica

### Exemplos

```java
@Autowired
private CartaoValidator cartaoValidator;

// Validar número (com algoritmo de Luhn)
cartaoValidator.validarNumero("4532015112830366");  // VISA válido

// Validar bandeira
cartaoValidator.validarBandeira("VISA");
cartaoValidator.validarBandeira("MASTERCARD");
// Inválido:
cartaoValidator.validarBandeira("DINERS");  // Lança exceção

// Validar vencimento
cartaoValidator.validarVencimento(12, 2026);  // Válido (futuro)
cartaoValidator.validarVencimento(1, 2024);   // Inválido (expirado)

// Validar CVV
cartaoValidator.validarCvv("123", "VISA");       // Válido (3 dígitos)
cartaoValidator.validarCvv("1234", "AMEX");      // Válido (4 dígitos para AMEX)
cartaoValidator.validarCvv("12", "VISA");        // Inválido (poucos dígitos)

// Validar nome
cartaoValidator.validarNomeImpresso("JOAO SILVA");  // Válido
cartaoValidator.validarNomeImpresso("JO-AO SILVA");  // Válido (com hífen)

// Validação completa em uma chamada
cartaoValidator.validarCartaoCompleto(
    "4532015112830366",     // número
    "JOAO SILVA",           // nomeImpresso
    "VISA",                 // bandeira
    12, 2026,              // mes, ano
    "123"                  // cvv
);
```

### Algoritmo de Luhn

```
1. Começa pela direita do número
2. Dobra cada segundo dígito
3. Se resultado > 9, subtrai 9
4. Soma todos os dígitos
5. Se soma % 10 == 0, cartão é válido
```

---

## 📦 4. EstoqueValidator

**Arquivo:** [EstoqueValidator.java](src/main/java/com/les/jakebooks/validator/EstoqueValidator.java)

**Aprimorado! Agora usa exceções específicas da hierarquia NegocioException.**

### Funcionalidades

```java
@Component
public class EstoqueValidator {
    public void validarQuantidadeDisponivel(String codigoLivro, Integer solicitada, Integer disponivel)
    public void validarQuantidadePositiva(Integer quantidade)
    public void validarCusto(Double custoAtual)
    public void validarDataEntrada(LocalDate dataEntrada)
    public void validarLimiteUnidadesPerPedido(String codigoLivro, Integer quantidade)
}
```

### Validações Implementadas

| Validação | RN | Exceção | Detalhes |
|-----------|-----|---------|----------|
| Quantidade > 0 | RN0061 | IllegalArgumentException | Não permitir zero |
| Custo > 0 | RN0062 | IllegalArgumentException | Todo item deve ter custo |
| Data obrigatória | RNF0064 | IllegalArgumentException | Registro sem data não é permitido |
| Estoque suficiente | RN0031/RN0032 | EstoqueInsuficienteException | Valida carrinho e finalização |
| Máximo 10 por pedido | RN0063 | LimitePedidoException | Por livro por pedido |

### Exceções Lançadas
- **EstoqueInsuficienteException** (NegocioException child)
- **LimitePedidoException** (NegocioException child)
- **IllegalArgumentException** (validações técnicas)

### Exemplos

```java
@Autowired
private EstoqueValidator estoqueValidator;

// Validar quantidade disponível (RN0031, RN0032)
estoqueValidator.validarQuantidadeDisponivel("LIV-001", 5, 10);  // ✓ Válido
estoqueValidator.validarQuantidadeDisponivel("LIV-001", 15, 10); // ✗ Excessão

// Validar quantidade positiva (RN0061)
estoqueValidator.validarQuantidadePositiva(5);   // ✓ Válido
estoqueValidator.validarQuantidadePositiva(0);   // ✗ Exceção
estoqueValidator.validarQuantidadePositiva(-1);  // ✗ Exceção

// Validar custo (RN0062)
estoqueValidator.validarCusto(29.99);  // ✓ Válido
estoqueValidator.validarCusto(0.0);    // ✗ Exceção
estoqueValidator.validarCusto(null);   // ✗ Exceção

// Validar data (RNF0064)
estoqueValidator.validarDataEntrada(LocalDate.now());  // ✓ Válido
estoqueValidator.validarDataEntrada(null);              // ✗ Exceção

// Validar limite por pedido (RN0063)
estoqueValidator.validarLimiteUnidadesPerPedido("LIV-001", 10);  // ✓ OK
estoqueValidator.validarLimiteUnidadesPerPedido("LIV-001", 11);  // ✗ Limite excedido
```

---

## 🏗️ Arquitetura Comum

Todos os validators seguem padrão consistente:

```java
@Component  // ← Registrado como Spring Bean
public class XxxValidator {
    
    /**
     * Método principal que valida e lança exceção específica.
     * Nome padrão: validarXxx()
     * Exceção: Subclass de NegocioException ou ValidacaoNegocioException
     */
    public void validarXxx(String valor) throws NegocioException {
        if (invalido) {
            throw new XxxException("Mensagem de erro");
        }
    }
}
```

### Injeção em Services

```java
@Service
public class ClienteService {
    
    @Autowired
    private SenhaValidator senhaValidator;
    
    @Autowired
    private CpfValidator cpfValidator;
    
    @Autowired
    private CartaoValidator cartaoValidator;
    
    @Autowired
    private EstoqueValidator estoqueValidator;
    
    public void criarCliente(ClienteDTO dto) {
        // Validações ocorrem aqui
        senhaValidator.validarSenha(dto.getSenha());
        cpfValidator.validarCpf(dto.getCpf());
        
        // Se alguma falhar, NegocioException sobe para GlobalExceptionHandler
    }
}
```

---

## 📊 Mapeamento de Regras de Negócio

| RN | Descrição | Validator | Exceção |
|-----|-----------|-----------|---------|
| RN0026 | Dados obrigatórios do cliente | CpfValidator | ValidacaoNegocioException |
| RN0025 | Bandeira deve estar cadastrada | CartaoValidator | ValidacaoNegocioException |
| RN0024 | Campos obrigatórios do cartão | CartaoValidator | ValidacaoNegocioException |
| RN0031 | Validar estoque no carrinho | EstoqueValidator | EstoqueInsuficienteException |
| RN0032 | Validar estoque antes finalização | EstoqueValidator | EstoqueInsuficienteException |
| RN0061 | Não permitir quantidade zero | EstoqueValidator | IllegalArgumentException |
| RN0062 | Todo item deve possuir custo | EstoqueValidator | IllegalArgumentException |
| RN0063 | Máximo 10 unidades por pedido | EstoqueValidator | LimitePedidoException |
| RNF0064 | Não permitir registro sem data | EstoqueValidator | IllegalArgumentException |

---

## ✅ Validação Técnica

### Build Status
```
✅ 117 arquivos compilados com sucesso
✅ BUILD SUCCESS
✅ Sem erros ou warnings
```

### Arquivos Modificados/Criados
- ✅ `SenhaValidator.java` - ATUALIZADO (SenhaInseguraException → SenhaFracaException)
- ✅ `CpfValidator.java` - NOVO (Algoritmo Receita Federal)
- ✅ `CartaoValidator.java` - NOVO (Algoritmo Luhn + validações)
- ✅ `EstoqueValidator.java` - APRIMORADO (usa NegocioException)

---

## 🎯 Testes Recomendados

### Próximas Tarefas

```java
// 1. Testes unitários para cada validator
@Test
public void testSenhaValida() { ... }
@Test
public void testCpfValido() { ... }
@Test
public void testCartaoValido() { ... }

// 2. Integração com Services
@Test
@Transactional
public void testCriarClienteComValidacoes() { ... }

// 3. Testes E2E com formulários
@Test
public void testFormularioCadastroClienteComErrosValidacao() { ... }
```

---

## 📝 Como Usar em Controllers

### Exemplo: Cadastro de Cliente

```java
@Controller
@RequestMapping("/clientes")
public class ClienteController {
    
    @Autowired
    private ClienteService service;
    
    @PostMapping
    public String criar(@Valid @ModelAttribute ClienteDTO dto, 
                       BindingResult result,
                       RedirectAttributes attr) {
        
        if (result.hasErrors()) {
            // GlobalExceptionHandler captura MethodArgumentNotValidException
            // Redireciona com erros de validação
            return "clientes/form-cadastro";
        }
        
        try {
            service.criar(dto);  // Valida com validators aqui
            attr.addFlashAttribute("sucesso", "Cliente criado com sucesso!");
            return "redirect:/clientes";
        } catch (NegocioException e) {
            // GlobalExceptionHandler captura e redireciona com erro
            attr.addFlashAttribute("erro", e.getMessage());
            return "redirect:/clientes/cadastro";
        }
    }
}
```

---

## 🚀 Recursos Futuros

1. **Banco de Dados de Bandeiras**
   - Carregar bandeiras do banco em vez de hardcoded
   - Permitir adição de novas bandeiras

2. **Validação Remota de CPF**
   - Integrar com API da Receita Federal
   - Validar se CPF já não foi cadastrado

3. **Validação Remota de Cartão**
   - Integrar com gateway de pagamento para validação real
   - Tokenização de cartão

4. **Customização de Regras**
   - Permitir configurar requisitos de senha por perfil
   - Limite de unidades configurável

---

**Implementado por:** GitHub Copilot  
**Versão:** 1.0  
**Data:** 9 de março de 2026  
**Dependências:** Spring Boot 3.x, Java 21+  
**Status:** ✅ PRONTO PARA USO
