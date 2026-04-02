# TASK-SHP-01: Listar Enderecos de Entrega

## Metadata

| Campo | Valor |
|-------|-------|
| **ID** | TASK-SHP-01 |
| **Agente** | shipping-agent |
| **Prioridade** | Alta |
| **RF Relacionado** | RF0035 |
| **RN Relacionada** | RN0022 |

## Objetivo

Apresentar ao cliente todos os enderecos disponiveis para entrega durante o processo de checkout.

## Pre-Condicoes

- Cliente autenticado no sistema
- Carrinho com status ABERTO
- Cliente na etapa de selecao de endereco do checkout

## Regras de Negocio

| RN | Regra | Validacao |
|----|-------|-----------|
| RN0022 | Cliente deve ter pelo menos um endereco de entrega | Lancar excecao se nenhum endereco com tipo=ENTREGA |

## Especificacao Tecnica

### Backend (backend-agent)

#### EnderecoService
```java
package com.les.jakebooks.service;

@Service
public class EnderecoService {

    /**
     * Lista todos os enderecos de entrega do cliente
     * @param clienteId ID do cliente
     * @return Lista de EnderecoDTO ordenada por preferencia
     * @throws EnderecoEntregaNaoEncontradoException se nenhum endereco de entrega
     */
    public List<EnderecoDTO> listarEnderecosEntrega(Long clienteId) {
        List<Endereco> enderecos = enderecoRepository
            .findByClienteIdAndTipo(clienteId, TipoEndereco.ENTREGA);

        if (enderecos.isEmpty()) {
            throw new EnderecoEntregaNaoEncontradoException(
                "Cliente deve ter pelo menos um endereco de entrega cadastrado"
            );
        }

        return enderecos.stream()
            .map(this::toDTO)
            .collect(Collectors.toList());
    }
}
```

#### EnderecoRepository
```java
package com.les.jakebooks.repository;

public interface EnderecoRepository extends JpaRepository<Endereco, Long> {

    List<Endereco> findByClienteIdAndTipo(Long clienteId, TipoEndereco tipo);

    Optional<Endereco> findByIdAndClienteId(Long id, Long clienteId);
}
```

#### EnderecoDTO
```java
package com.les.jakebooks.dto;

public record EnderecoDTO(
    Long id,
    String logradouro,
    String numero,
    String complemento,
    String bairro,
    String cidade,
    String estado,
    String cep,
    String enderecoFormatado  // Para exibicao
) {}
```

### Business Rules (business-rules-agent)

#### Excecao
```java
package com.les.jakebooks.exception;

public class EnderecoEntregaNaoEncontradoException extends ValidacaoNegocioException {

    public EnderecoEntregaNaoEncontradoException(String mensagem) {
        super(mensagem);
    }
}
```

### Frontend (frontend-agent)

#### CheckoutController (parcial)
```java
@GetMapping("/checkout/endereco")
public String exibirSelecaoEndereco(Model model, Principal principal) {
    Long clienteId = getClienteId(principal);

    try {
        List<EnderecoDTO> enderecos = enderecoService.listarEnderecosEntrega(clienteId);
        model.addAttribute("enderecos", enderecos);
        return "checkout/endereco";
    } catch (EnderecoEntregaNaoEncontradoException e) {
        model.addAttribute("erro", e.getMessage());
        model.addAttribute("redirecionarCadastro", true);
        return "checkout/endereco";
    }
}
```

#### Template: checkout/endereco.html
```html
<!-- Estrutura basica -->
<div th:fragment="lista-enderecos">
    <div th:if="${enderecos.empty}" class="alert alert-warning">
        <p>Voce nao possui enderecos de entrega cadastrados.</p>
        <a th:href="@{/cliente/enderecos/novo}" class="btn btn-primary">
            Cadastrar Endereco
        </a>
    </div>

    <div th:unless="${enderecos.empty}">
        <div th:each="endereco : ${enderecos}" class="card mb-2">
            <div class="card-body">
                <input type="radio" name="enderecoId"
                       th:value="${endereco.id}"
                       th:id="'end-' + ${endereco.id}">
                <label th:for="'end-' + ${endereco.id}">
                    <span th:text="${endereco.enderecoFormatado}"></span>
                </label>
            </div>
        </div>
    </div>
</div>
```

## Fluxo de Execucao

```
1. Cliente acessa /checkout/endereco
2. Controller chama EnderecoService.listarEnderecosEntrega()
3. Service busca enderecos com tipo=ENTREGA
4. SE lista vazia:
   - Lancar EnderecoEntregaNaoEncontradoException
   - Exibir mensagem e link para cadastro
5. SE lista nao vazia:
   - Retornar lista de EnderecoDTO
   - Exibir cards com opcoes de selecao
```

## Criterios de Aceite

- [ ] Lista apenas enderecos com tipo ENTREGA
- [ ] Exibe mensagem amigavel se nenhum endereco
- [ ] Oferece link para cadastrar novo endereco
- [ ] Enderecos exibidos em formato legivel
- [ ] Radio buttons permitem selecao unica
- [ ] Funciona corretamente com 1 ou mais enderecos

## Dependencias

- **Entidade:** Endereco (ja existente)
- **Enum:** TipoEndereco com valor ENTREGA
- **Proxima Task:** TASK-SHP-02 (Selecionar Endereco)

---

**Status:** Pendente
