# TASK-SHP-02: Selecionar Endereco de Entrega

## Metadata

| Campo | Valor |
|-------|-------|
| **ID** | TASK-SHP-02 |
| **Agente** | shipping-agent |
| **Prioridade** | Alta |
| **RF Relacionado** | RF0035 |
| **RN Relacionada** | RN0022 |

## Objetivo

Permitir que o cliente selecione um endereco de entrega para o pedido e disparar automaticamente o calculo de frete.

## Pre-Condicoes

- TASK-SHP-01 concluida
- Cliente visualizando lista de enderecos
- Pelo menos 1 endereco de entrega disponivel

## Regras de Negocio

| RN | Regra | Validacao |
|----|-------|-----------|
| - | Endereco deve pertencer ao cliente | Verificar clienteId |
| - | Endereco deve ser do tipo ENTREGA | Verificar tipo |

## Especificacao Tecnica

### Backend (backend-agent)

#### EnderecoService (adicionar)
```java
/**
 * Seleciona endereco para entrega do checkout atual
 * @param clienteId ID do cliente
 * @param enderecoId ID do endereco selecionado
 * @return EnderecoDTO do endereco selecionado
 * @throws EnderecoNaoEncontradoException se endereco nao existe
 * @throws AcessoNegadoException se endereco nao pertence ao cliente
 */
public EnderecoDTO selecionarEnderecoEntrega(Long clienteId, Long enderecoId) {
    Endereco endereco = enderecoRepository.findById(enderecoId)
        .orElseThrow(() -> new EnderecoNaoEncontradoException(
            "Endereco nao encontrado"
        ));

    if (!endereco.getCliente().getId().equals(clienteId)) {
        throw new AcessoNegadoException(
            "Endereco nao pertence ao cliente"
        );
    }

    if (endereco.getTipo() != TipoEndereco.ENTREGA) {
        throw new TipoEnderecoInvalidoException(
            "Endereco selecionado nao e do tipo entrega"
        );
    }

    return toDTO(endereco);
}
```

#### CheckoutDTO (adicionar campo)
```java
package com.les.jakebooks.dto;

public class CheckoutDTO {
    private Long enderecoEntregaId;
    private FreteDTO frete;
    private PagamentoDTO pagamento;

    // getters e setters
}
```

### Frontend (frontend-agent)

#### CheckoutController (adicionar)
```java
@PostMapping("/checkout/endereco")
public String selecionarEndereco(
        @RequestParam Long enderecoId,
        HttpSession session,
        RedirectAttributes ra,
        Principal principal) {

    Long clienteId = getClienteId(principal);

    try {
        EnderecoDTO endereco = enderecoService.selecionarEnderecoEntrega(
            clienteId, enderecoId
        );

        // Armazenar na sessao do checkout
        CheckoutDTO checkout = getCheckoutFromSession(session);
        checkout.setEnderecoEntregaId(enderecoId);
        session.setAttribute("checkout", checkout);

        // Calcular frete automaticamente
        return "redirect:/checkout/frete";

    } catch (EnderecoNaoEncontradoException | AcessoNegadoException e) {
        ra.addFlashAttribute("erro", e.getMessage());
        return "redirect:/checkout/endereco";
    }
}
```

#### Template: checkout/endereco.html (form)
```html
<form th:action="@{/checkout/endereco}" method="post">
    <div th:each="endereco : ${enderecos}" class="card mb-2 endereco-card">
        <div class="card-body d-flex align-items-center">
            <input type="radio"
                   name="enderecoId"
                   th:value="${endereco.id}"
                   th:id="'end-' + ${endereco.id}"
                   class="form-check-input me-3"
                   required>
            <label th:for="'end-' + ${endereco.id}" class="flex-grow-1">
                <strong th:text="${endereco.logradouro + ', ' + endereco.numero}"></strong>
                <span th:if="${endereco.complemento}"
                      th:text="' - ' + ${endereco.complemento}"></span>
                <br>
                <small class="text-muted"
                       th:text="${endereco.bairro + ' - ' + endereco.cidade + '/' + endereco.estado}"></small>
                <br>
                <small class="text-muted" th:text="'CEP: ' + ${endereco.cep}"></small>
            </label>
        </div>
    </div>

    <div class="mt-3">
        <a th:href="@{/checkout/carrinho}" class="btn btn-secondary">Voltar</a>
        <button type="submit" class="btn btn-primary">Continuar</button>
    </div>
</form>
```

## Fluxo de Execucao

```
1. Cliente seleciona endereco via radio button
2. Cliente clica em "Continuar"
3. POST para /checkout/endereco com enderecoId
4. Controller valida:
   - Endereco existe
   - Endereco pertence ao cliente
   - Endereco e do tipo ENTREGA
5. SE validacao OK:
   - Armazena enderecoId na sessao do checkout
   - Redireciona para /checkout/frete (TASK-SHP-03)
6. SE validacao FALHA:
   - Adiciona mensagem de erro
   - Redireciona de volta para selecao
```

## Criterios de Aceite

- [ ] Apenas endereco do proprio cliente pode ser selecionado
- [ ] Validacao de tipo ENTREGA funciona
- [ ] Erro exibido se endereco invalido
- [ ] Endereco armazenado na sessao do checkout
- [ ] Redireciona para calculo de frete apos selecao
- [ ] Botao "Voltar" retorna ao carrinho

## Dependencias

- **Task Anterior:** TASK-SHP-01 (Listar Enderecos)
- **Proxima Task:** TASK-SHP-03 (Calcular Frete)

---

**Status:** Pendente
