# TASK-CHK-02: Validar Pre-Condicoes do Carrinho

## Metadata

| Campo | Valor |
|-------|-------|
| **ID** | TASK-CHK-02 |
| **Agente** | checkout-agent |
| **Prioridade** | Alta |
| **RF Relacionado** | RF0033 |
| **RN Relacionada** | RN0031, RN0032, RN0063 |

## Objetivo

Garantir que o carrinho atende a todas as pre-condicoes necessarias antes de iniciar o processo de checkout, validando estoque, limites e status.

## Pre-Condicoes

- Cliente autenticado
- Carrinho existente

## Validacoes a Executar

### 1. Carrinho Nao Vazio
- Verificar se existe pelo menos 1 item no carrinho
- **Excecao:** `CarrinhoVazioException`

### 2. Carrinho Nao Expirado
- Validar se carrinho tem status ABERTO
- Verificar se dataExpiracao nao foi atingida
- **Excecao:** `CarrinhoExpiradoException`

### 3. Estoque Disponivel (RN0031)
- Para cada item, validar se quantidade <= estoque disponivel
- **Excecao:** `EstoqueInsuficienteException`

### 4. Limite de Unidades por Livro (RN0063)
- Verificar se nenhum item excede 10 unidades
- **Excecao:** `LimiteItensExcedidoException`

### 5. Livros Ativos
- Validar se todos os livros do carrinho tem status ATIVO
- **Excecao:** `LivroInativoException`

## Especificacao Tecnica

### Backend (backend-agent)

#### CompraValidator
```java
package com.les.jakebooks.validator;

@Component
@RequiredArgsConstructor
public class CompraValidator {

    private final EstoqueRepository estoqueRepository;
    private final LivroRepository livroRepository;

    /**
     * Valida todas as pre-condicoes do carrinho para checkout
     * RN0031, RN0032, RN0063
     */
    public void validarCarrinhoParaCheckout(Carrinho carrinho) {
        validarCarrinhoNaoVazio(carrinho);
        validarCarrinhoNaoExpirado(carrinho);
        validarItensCarrinho(carrinho.getItens());
    }

    private void validarCarrinhoNaoVazio(Carrinho carrinho) {
        if (carrinho.getItens().isEmpty()) {
            throw new CarrinhoVazioException(
                "Carrinho vazio. Adicione pelo menos um item antes de finalizar a compra."
            );
        }
    }

    private void validarCarrinhoNaoExpirado(Carrinho carrinho) {
        if (!carrinho.getStatus().equals(StatusCarrinho.ABERTO)) {
            throw new CarrinhoExpiradoException(
                "Carrinho nao esta mais disponivel para finalizacao"
            );
        }

        if (carrinho.getDataExpiracao().before(new Date())) {
            throw new CarrinhoExpiradoException(
                "Carrinho expirou. Por favor, adicione os itens novamente"
            );
        }
    }

    private void validarItensCarrinho(List<ItemCarrinho> itens) {
        for (ItemCarrinho item : itens) {
            validarLivroAtivo(item.getLivro());
            validarEstoqueDisponivel(item);
            validarLimiteUnidades(item);
        }
    }

    private void validarLivroAtivo(Livro livro) {
        if (livro.getStatus() != StatusLivro.ATIVO) {
            throw new LivroInativoException(
                String.format("O livro '%s' nao esta mais disponivel",
                    livro.getTitulo())
            );
        }
    }

    private void validarEstoqueDisponivel(ItemCarrinho item) {
        Estoque estoque = estoqueRepository.findByLivro(item.getLivro())
            .orElseThrow(() -> new EstoqueNaoEncontradoException(
                "Estoque nao encontrado para o livro: " + item.getLivro().getTitulo()
            ));

        if (estoque.getQuantidade() < item.getQuantidade()) {
            throw new EstoqueInsuficienteException(
                String.format("Estoque insuficiente para '%s'. Disponivel: %d, Solicitado: %d",
                    item.getLivro().getTitulo(),
                    estoque.getQuantidade(),
                    item.getQuantidade())
            );
        }
    }

    private void validarLimiteUnidades(ItemCarrinho item) {
        if (item.getQuantidade() > 10) {
            throw new LimiteItensExcedidoException(
                String.format("Maximo 10 unidades por livro. Livro: '%s', Quantidade: %d",
                    item.getLivro().getTitulo(),
                    item.getQuantidade())
            );
        }
    }

    /**
     * Re-valida estoque antes da finalizacao (RN0032)
     * Executado imediatamente antes de processar o pagamento
     */
    public void revalidarEstoqueParaFinalizacao(Carrinho carrinho) {
        validarItensCarrinho(carrinho.getItens());
    }
}
```

### Business Rules (business-rules-agent)

#### Excecoes
```java
package com.les.jakebooks.exception;

public class CarrinhoVazioException extends RuntimeException {
    public CarrinhoVazioException(String message) {
        super(message);
    }
}

public class CarrinhoExpiradoException extends RuntimeException {
    public CarrinhoExpiradoException(String message) {
        super(message);
    }
}

public class EstoqueInsuficienteException extends RuntimeException {
    public EstoqueInsuficienteException(String message) {
        super(message);
    }
}

public class LimiteItensExcedidoException extends RuntimeException {
    public LimiteItensExcedidoException(String message) {
        super(message);
    }
}

public class LivroInativoException extends RuntimeException {
    public LivroInativoException(String message) {
        super(message);
    }
}

public class EstoqueNaoEncontradoException extends RuntimeException {
    public EstoqueNaoEncontradoException(String message) {
        super(message);
    }
}
```

### Frontend (frontend-agent)

#### Error Handling no CheckoutController
```java
@ExceptionHandler(CarrinhoVazioException.class)
public String handleCarrinhoVazio(CarrinhoVazioException e, RedirectAttributes ra) {
    ra.addFlashAttribute("erro", e.getMessage());
    return "redirect:/carrinho";
}

@ExceptionHandler(CarrinhoExpiradoException.class)
public String handleCarrinhoExpirado(CarrinhoExpiradoException e, RedirectAttributes ra) {
    ra.addFlashAttribute("erro", e.getMessage());
    return "redirect:/carrinho";
}

@ExceptionHandler({EstoqueInsuficienteException.class, LimiteItensExcedidoException.class})
public String handleValidacaoItem(RuntimeException e, RedirectAttributes ra) {
    ra.addFlashAttribute("erro", e.getMessage());
    return "redirect:/carrinho?action=revisar";
}

@ExceptionHandler(LivroInativoException.class)
public String handleLivroInativo(LivroInativoException e, RedirectAttributes ra) {
    ra.addFlashAttribute("erro", e.getMessage());
    return "redirect:/carrinho?action=remover-inativos";
}
```

## Criterios de Aceite

- [ ] Sistema rejeita carrinho vazio com mensagem clara
- [ ] Sistema valida status ABERTO do carrinho
- [ ] Sistema verifica expiracao do carrinho
- [ ] Sistema valida estoque disponivel para cada item (RN0031)
- [ ] Sistema impede mais de 10 unidades do mesmo livro (RN0063)
- [ ] Sistema rejeita livros com status INATIVO
- [ ] Re-validacao ocorre antes da finalizacao (RN0032)
- [ ] Mensagens de erro sao claras e direcionais
- [ ] Cliente e redirecionado adequadamente em caso de erro

## Dependencias

- **business-rules-agent:** Criar excecoes listadas
- **backend-agent:** Implementar CompraValidator
- **TASK-CHK-01:** Integrar validacao no fluxo principal

## Fluxo de Integracao

```
CheckoutService.iniciarCheckout()
        |
        v
CompraValidator.validarCarrinhoParaCheckout()
        |
    SUCESSO --> Continuar fluxo
        |
    FALHA --> Lancar excecao --> Redirecionar usuario
```

---

**Status:** Pendente