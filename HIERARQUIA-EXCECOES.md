# Hierarquia de Exceções - JakeCommerce

## 📋 Visão Geral

A hierarquia de exceções foi criada para garantir que todas as violações de regras de negócio sejam capturadas e tratadas de forma consistente e rastreável.

**Classe Base:** `NegocioException extends RuntimeException`

---

## 🌳 Estrutura Hierárquica

```
RuntimeException
    └── NegocioException (base para todas as RN)
        ├── LivroNaoEncontradoException
        ├── EstoqueInsuficienteException (RN0031, RN0032)
        ├── ValorAbaixoDaMargemException (RN0013, RN0014)
        ├── ClienteNaoEncontradoException
        ├── SenhaFracaException
        ├── CarrinhoExpiradoException (RN0044, RN0045)
        ├── PagamentoReprovadoException (RN0037, RN0038, RN0065)
        ├── TrocaNaoPermitidaException (RN0043)
        ├── CupomInvalidoException (RN0033, RN0035, RN0036)
        └── LimitePedidoException (RN0063, RN0064, RN0065)
```

---

## 🎯 Classes Criadas

### 1. **NegocioException** (classe base)

**Localização:** `com.les.jakebooks.exception.NegocioException`

**Atributos:**
- `codigoRN : String` - Código da regra de negócio violada (ex: "RN0031")

**Construtores:**
```java
NegocioException(String mensagem)
NegocioException(String mensagem, String codigoRN)
NegocioException(String mensagem, Throwable causa)
NegocioException(String mensagem, String codigoRN, Throwable causa)
```

**Métodos:**
- `getCodigoRN() : String`
- `setCodigoRN(String codigoRN)`
- `toString() : String` - Retorna mensagem + [codigoRN]

---

### 2. **LivroNaoEncontradoException**

**Código RN:** RN_LIVRO_NAO_ENCONTRADO

**Caso de uso:** Quando tentar acessar, alterar ou vender livro inexistente

**Atributos:**
- `codigoLivro : String`

**Exemplo:**
```java
throw new LivroNaoEncontradoException(
    "Livro JAVA001 não encontrado no sistema",
    "JAVA001"
);
```

---

### 3. **EstoqueInsuficienteException**

**Código RN:** RN0031, RN0032

**Caso de uso:** Quando não há estoque suficiente para:
- Adicionar ao carrinho (RN0031)
- Finalizar venda (RN0032)

**Atributos:**
- `codigoLivro : String`
- `quantidadeSolicitada : Integer`
- `quantidadeDisponivel : Integer`

**Exemplo:**
```java
throw new EstoqueInsuficienteException(
    "Estoque insuficiente para o livro JAVA001",
    "JAVA001",
    10,      // Solicitado
    3        // Disponível
);
```

---

### 4. **ValorAbaixoDaMargemException**

**Código RN:** RN0013, RN0014

**Caso de uso:** Quando o valor de venda está abaixo da margem definida

**Atributos:**
- `custoBase : Double`
- `percentualMargem : Double`
- `valorMinimo : Double`
- `valorInformado : Double`

**Exemplo:**
```java
throw new ValorAbaixoDaMargemException(
    "Valor de venda abaixo da margem. Redução exige autorização.",
    50.0,    // custoBase
    30.0,    // percentualMargem
    65.0,    // valorMinimo (50 * 1.30)
    40.0     // valorInformado
);
```

---

### 5. **ClienteNaoEncontradoException**

**Código RN:** RN_CLIENTE_NAO_ENCONTRADO

**Caso de uso:** Quando tentar acessar, alterar ou processar venda de cliente inexistente

**Atributos:**
- `codigoCliente : String`

**Exemplo:**
```java
throw new ClienteNaoEncontradoException(
    "Cliente CLI12345 não encontrado",
    "CLI12345"
);
```

---

### 6. **SenhaFracaException**

**Código RN:** RN_SENHA_FRACA

**Caso de uso:** Quando a senha não atende aos requisitos de segurança
- Mínimo 8 caracteres
- Pelo menos uma letra maiúscula
- Pelo menos uma letra minúscula
- Pelo menos um caractere especial

**Atributos:**
- `motivoRejeicao : String` - Descrição do motivo (ex: "Deve conter maiúsculas")

**Exemplo:**
```java
throw new SenhaFracaException(
    "Senha não atende aos requisitos de segurança",
    "Senha deve ter no mínimo 8 caracteres"
);
```

---

### 7. **CarrinhoExpiradoException**

**Código RN:** RN0044, RN0045

**Caso de uso:** Quando o carrinho expira antes da finalização da compra

**Atributos:**
- `carrinhoId : String`

**Exemplo:**
```java
throw new CarrinhoExpiradoException(
    "Seu carrinho expirou. Adicione itens para renovar a sessão.",
    "CARR12345"
);
```

---

### 8. **PagamentoReprovadoException**

**Código RN:** RN0037, RN0038, RN0065

**Caso de uso:** Quando:
- Pagamento é reprovado (RN0037, RN0038)
- 3 pagamentos reprovados consecutivos bloqueiam o carrinho (RN0065)

**Atributos:**
- `codigoPedido : String`
- `tentativasConsecutivas : Integer`
- `motivoRejeicao : String`

**Exemplo:**
```java
throw new PagamentoReprovadoException(
    "Pagamento reprovado. Tente novamente.",
    "PED001",
    2  // 2 tentativas falhadas
);
```

---

### 9. **TrocaNaoPermitidaException**

**Código RN:** RN0043

**Caso de uso:** Quando uma troca não pode ser realizada
- Apenas pedidos com status "ENTREGUE" podem solicitar troca

**Atributos:**
- `codigoPedido : String`
- `statusPedidoAtual : String`
- `motivoRejeicao : String`

**Exemplo:**
```java
throw new TrocaNaoPermitidaException(
    "Pedido PED001 não pode ser trocado",
    "PED001",
    "Pedido não está entregue"
);
```

---

### 10. **CupomInvalidoException**

**Código RN:** RN0033, RN0035, RN0036

**Caso de uso:** Quando um cupom é:
- Inválido
- Expirado
- Já foi utilizado
- Ou viola a regra de "um cupom promocional por compra"

**Atributos:**
- `codigoCupom : String`
- `motivoInvalid : String` - Descrição do motivo (ex: "Expirado em 01/01/2026")

**Exemplo:**
```java
throw new CupomInvalidoException(
    "Cupom PROMO10 é inválido",
    "PROMO10",
    "Cupom expirou em 01/01/2026"
);
```

---

### 11. **LimitePedidoException**

**Código RN:** RN0063, RN0064, RN0065

**Caso de uso:** Quando quantidade ou valor do pedido viola limites:
- Máximo 10 unidades do mesmo livro por pedido (RN0063)
- Pedido mínimo R$ 20,00 sem frete (RN0064)
- Bloqueio após 3 pagamentos reprovados (RN0065)

**Atributos:**
- `quantidadeAtual : Integer`
- `limiteMinimo : Integer`
- `limiteMaximo : Integer`
- `valorAtual : Double`
- `valorMinimo : Double`

**Exemplo - Uso 1 (Quantidade):**
```java
throw new LimitePedidoException(
    "Máximo 10 unidades do mesmo livro por pedido",
    15,   // quantidadeAtual
    10    // limiteMaximo
);
```

**Exemplo - Uso 2 (Valor mínimo):**
```java
throw new LimitePedidoException(
    "Valor mínimo do pedido sem frete é R$ 20,00",
    15.50,   // valorAtual
    20.0,    // valorMinimo
    "RN0064"
);
```

---

## 💾 Status de Compilação

```
✅ BUILD SUCCESS
✅ 115 arquivos compilados
⏱  Tempo: 7.748 segundos
```

---

## 📊 Mapeamento RN → Exceção

| Regra | Exceção | Localização |
|-------|---------|-------------|
| RN0013, RN0014 | ValorAbaixoDaMargemException | exception/ |
| RN0031, RN0032 | EstoqueInsuficienteException | exception/ |
| RN0033, RN0035, RN0036 | CupomInvalidoException | exception/ |
| RN0037, RN0038 | PagamentoReprovadoException | exception/ |
| RN0043 | TrocaNaoPermitidaException | exception/ |
| RN0044, RN0045 | CarrinhoExpiradoException | exception/ |
| RN0063, RN0064, RN0065 | LimitePedidoException | exception/ |
| Cadastro Cliente (Senha) | SenhaFracaException | exception/ |
| Consulta Livro | LivroNaoEncontradoException | exception/ |
| Consulta Cliente | ClienteNaoEncontradoException | exception/ |

---

## 🔍 Como Usar

### Lançar exceção no Service:

```java
@Service
public class LivroService {
    
    @Autowired
    private LivroRepository livroRepository;
    
    public Livro buscarLivro(String codigoLivro) {
        return livroRepository.findById(codigoLivro)
            .orElseThrow(() -> new LivroNaoEncontradoException(
                "Livro " + codigoLivro + " não encontrado",
                codigoLivro
            ));
    }
}
```

### Captura automática no GlobalExceptionHandler:

```java
@ControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(LivroNaoEncontradoException.class)
    public ResponseEntity<ErrorResponse> handleLivroNaoEncontrado(
        LivroNaoEncontradoException ex) {
        
        ErrorResponse erro = new ErrorResponse(
            HttpStatus.NOT_FOUND.value(),
            "Livro não encontrado",
            ex.getMessage(),
            ex.getCodigoLivro()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(erro);
    }
}
```

### No Controller (ZERO lógica de negócio):

```java
@RestController
@RequestMapping("/api/livros")
public class LivroController {
    
    @Autowired
    private LivroService livroService;
    
    @GetMapping("/{codigo}")
    public ResponseEntity<LivroDTO> obter(@PathVariable String codigo) {
        Livro livro = livroService.buscarLivro(codigo);
        return ResponseEntity.ok(new LivroDTO(livro));
    }
    // GlobalExceptionHandler cuida de LivroNaoEncontradoException automaticamente
}
```

---

## 🎯 Benefícios

✅ **Rastreabilidade:** Cada exceção contém código da RN violada  
✅ **Tratamento Específico:** GlobalExceptionHandler trata cada tipo diferentemente  
✅ **Informações Contextuais:** Atributos específicos para debug  
✅ **Hierarquia Clara:** Fácil catch de grupos de exceções  
✅ **Mensagens Consistentes:** Formato padrão em toda aplicação  
✅ **Não é genérica:** Nunca `catch(RuntimeException)` aleatório

---

## 📝 Exemplo Completo - Adicionar ao Carrinho

```java
@Service
@Transactional
public class CarrinhoService {
    
    @Autowired
    private LivroRepository livroRepository;
    
    @Autowired
    private EstoqueRepository estoqueRepository;
    
    public void adicionarAoCarrinho(String codigoLivro, Integer quantidade) {
        // 1. Verificar se livro existe
        Livro livro = livroRepository.findById(codigoLivro)
            .orElseThrow(() -> new LivroNaoEncontradoException(
                "Livro não encontrado",
                codigoLivro
            ));
        
        // 2. Verificar estoque
        Estoque estoque = estoqueRepository.findByLivro(livro);
        if (estoque.getQuantidade() < quantidade) {
            throw new EstoqueInsuficienteException(
                "Estoque insuficiente",
                codigoLivro,
                quantidade,
                estoque.getQuantidade()
            );
        }
        
        // 3. Validar quantidade (máximo 10)
        if (quantidade > 10) {
            throw new LimitePedidoException(
                "Máximo 10 unidades por livro",
                quantidade,
                10
            );
        }
        
        // 4. Adicionar ao carrinho (sem erros)
        // ...
    }
}
```

**Resposta de erro (HTTP 422):**
```json
{
  "timestamp": "2026-03-08T16:00:00.000",
  "status": 422,
  "mensagem": "Estoque insuficiente",
  "detalhes": "Livro: JAVA001 | Solicitado: 15 | Disponível: 3",
  "campo": "JAVA001"
}
```

---

## ✅ Checklist de Uso

- [ ] Sempre herdar de `NegocioException` para RN
- [ ] Incluir código RN na construção da exceção
- [ ] Adicionar atributos contextuais úteis para debug
- [ ] Lançar no Service, nunca no Controller
- [ ] GlobalExceptionHandler trata automaticamente
- [ ] Testes devem cobrir casos que lançam exceções
- [ ] Documentar exceção no comentário da classe

---

**Última atualização:** 08 de março de 2026  
**Versão:** 1.0  
**Status:** ✅ Pronto para uso em produção
