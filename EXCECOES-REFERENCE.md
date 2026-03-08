# Quick Reference - Hierarquia de Exceções

## 🚀 Uso Rápido

### Importar e Lançar

```java
import com.les.jakebooks.exception.*;

// Livro não encontrado
throw new LivroNaoEncontradoException("Livro JAVA001 não encontrado", "JAVA001");

// Estoque insuficiente
throw new EstoqueInsuficienteException("Sem estoque", "JAVA001", 10, 3);

// Valor abaixo da margem
throw new ValorAbaixoDaMargemException(
    "Valor abaixo da margem",
    50.0,    // custoBase
    30.0,    // percentualMargem
    65.0,    // valorMinimo
    40.0     // valorInformado
);

// Cliente não encontrado
throw new ClienteNaoEncontradoException("Cliente não existe", "CLI123");

// Senha fraca
throw new SenhaFracaException("Senha fraca", "Deve conter maiúsculas");

// Carrinho expirado
throw new CarrinhoExpiradoException("Carrinho expirou", "CARR123");

// Pagamento reprovado
throw new PagamentoReprovadoException("Pagamento recusado", "PED001", 2);

// Troca não permitida
throw new TrocaNaoPermitidaException("Troca não permitida", "PED001", "Pedido em transporte");

// Cupom inválido
throw new CupomInvalidoException("Cupom inválido", "PROMO10", "Expirado");

// Limite pedido violado
throw new LimitePedidoException("Quantidade acima do limite", 15, 10);
```

---

## 📊 Mapeamento RN → Exceção

| RN | Exceção | Quando Lançar |
|----|---------|--------------|
| RN_LIVRO_NAO_ENCONTRADO | LivroNaoEncontradoException | Livro não existe |
| RN0031, RN0032 | EstoqueInsuficienteException | Sem estoque suficiente |
| RN0013, RN0014 | ValorAbaixoDaMargemException | Valor < margem |
| RN_CLIENTE_NAO_ENCONTRADO | ClienteNaoEncontradoException | Cliente não existe |
| RN_SENHA_FRACA | SenhaFracaException | Senha não atende requisitos |
| RN0044, RN0045 | CarrinhoExpiradoException | Carrinho expirou |
| RN0037, RN0038, RN0065 | PagamentoReprovadoException | Pagamento reprovado |
| RN0043 | TrocaNaoPermitidaException | Pedido não ENTREGUE |
| RN0033, RN0035, RN0036 | CupomInvalidoException | Cupom inválido/expirado |
| RN0063, RN0064, RN0065 | LimitePedidoException | Quantidade/valor fora do limite |

---

## 🎯 Todos as Exceções Herdam de NegocioException

**Padrão de uso:**

```java
try {
    livroService.buscarLivro(codigo);
} catch (NegocioException ex) {
    // Captura TODAS as 10 exceções de negócio
    String codigoRN = ex.getCodigoRN();  // Ex: "RN0031"
    String mensagem = ex.getMessage();
}
```

---

## 📝 Checklist de Uso no Service

- [ ] ✅ Herdar de `NegocioException`
- [ ] ✅ Lançar com código RN
- [ ] ✅ Adicionar atributos contextuais
- [ ] ✅ GlobalExceptionHandler cuida do resto
- [ ] ✅ Não precisa try/catch no Service
- [ ] ✅ Controller não faz nada (apenas chama Service)

---

**Última atualização:** 08 de março de 2026
