você é o paymente-agent e vamos implementar de maneira simulada o fluxo de pagamento. Basicamente: Definir forma de pagamento (pagamento em diferentes cartões, uso de cupom de troca e promocional);

Revise os códigos atuais para irmos para a PAY-04# TASK-PAY-04: Validar e Distribuir Pagamento em Cartoes

## Metadata

| Campo | Valor |
|-------|-------|
| **ID** | TASK-PAY-04 |
| **Agente** | payment-agent |
| **Prioridade** | Alta |
| **RF Relacionado** | RF0036 |
| **RN Relacionada** | RN0034 |

## Objetivo

Permitir que o cliente selecione multiplos cartoes para pagar o valor restante (apos cupons), distribuindo os valores e garantindo o minimo de R$10 por cartao.

## Pre-Condicoes

- TASK-PAY-01, TASK-PAY-02 em andamento
- Valor restante > 0 (apos aplicacao de cupons)
- Cliente possui cartoes cadastrados

## Regras de Negocio

| RN | Regra | Validacao |
|----|-------|-----------|
| RN0034 | Multiplos cartoes permitidos | Sem limite de quantidade |
| RN0034 | Minimo R$10 por cartao | valor_por_cartao >= 10.00 |
| - | Soma deve ser igual ao restante | soma_cartoes == valor_restante |

## Especificacao Tecnica

### Backend (backend-agent)

#### CartaoService
```java
package com.les.jakebooks.service;

@Service
public class CartaoService {

    /**
     * Lista cartoes ativos do cliente
     */
    public List<CartaoDTO> listarCartoesAtivos(Long clienteId) {
        return cartaoRepository.findByClienteIdAndAtivoTrue(clienteId)
            .stream()
            .map(this::toDTO)
            .collect(Collectors.toList());
    }
}
```

#### PagamentoService (adicionar)
```java
private static final BigDecimal VALOR_MINIMO_POR_CARTAO = new BigDecimal("10.00");

/**
 * Valida distribuicao de pagamento entre cartoes
 * @param cartoesValores Map de cartaoId -> valor a cobrar
 * @param valorRestante Valor total a ser pago com cartoes
 * @param clienteId ID do cliente
 * @throws ValorMinimoCartaoException se algum valor < R$10
 * @throws ValorPagamentoInvalidoException se soma != valorRestante
 */
public void validarDistribuicaoCartoes(
        Map<Long, BigDecimal> cartoesValores,
        BigDecimal valorRestante,
        Long clienteId) {

    if (cartoesValores.isEmpty()) {
        throw new CartaoNaoSelecionadoException(
            "Selecione pelo menos um cartao para pagamento"
        );
    }

    BigDecimal somaValores = BigDecimal.ZERO;

    for (Map.Entry<Long, BigDecimal> entry : cartoesValores.entrySet()) {
        Long cartaoId = entry.getKey();
        BigDecimal valor = entry.getValue();

        // Validar que cartao pertence ao cliente
        Cartao cartao = cartaoRepository.findById(cartaoId)
            .orElseThrow(() -> new CartaoNaoEncontradoException(cartaoId));

        if (!cartao.getCliente().getId().equals(clienteId)) {
            throw new AcessoNegadoException("Cartao nao pertence ao cliente");
        }

        // Validar valor minimo por cartao
        if (valor.compareTo(VALOR_MINIMO_POR_CARTAO) < 0) {
            throw new ValorMinimoCartaoException(
                String.format(
                    "Valor minimo por cartao e R$ 10,00. Cartao %s com R$ %.2f",
                    cartao.getNumeroMascarado(),
                    valor
                )
            );
        }

        somaValores = somaValores.add(valor);
    }

    // Validar que soma dos valores = valor restante
    if (somaValores.compareTo(valorRestante) != 0) {
        throw new ValorPagamentoInvalidoException(
            String.format(
                "Soma dos valores (R$ %.2f) diferente do valor restante (R$ %.2f)",
                somaValores, valorRestante
            )
        );
    }
}

/**
 * Sugere distribuicao automatica entre cartoes
 */
public Map<Long, BigDecimal> sugerirDistribuicao(
        List<Long> cartoesIds,
        BigDecimal valorRestante) {

    if (cartoesIds.isEmpty()) {
        return Map.of();
    }

    Map<Long, BigDecimal> distribuicao = new HashMap<>();

    if (cartoesIds.size() == 1) {
        // Um cartao: valor total
        distribuicao.put(cartoesIds.get(0), valorRestante);
    } else {
        // Multiplos cartoes: dividir igualmente
        BigDecimal valorPorCartao = valorRestante.divide(
            new BigDecimal(cartoesIds.size()),
            2,
            RoundingMode.DOWN
        );

        // Se valor por cartao < 10, ajustar
        if (valorPorCartao.compareTo(VALOR_MINIMO_POR_CARTAO) < 0) {
            // Usar apenas cartoes suficientes
            int maxCartoes = valorRestante.divide(VALOR_MINIMO_POR_CARTAO, 0, RoundingMode.DOWN).intValue();
            valorPorCartao = valorRestante.divide(new BigDecimal(maxCartoes), 2, RoundingMode.DOWN);

            for (int i = 0; i < maxCartoes; i++) {
                distribuicao.put(cartoesIds.get(i), valorPorCartao);
            }
        } else {
            for (Long cartaoId : cartoesIds) {
                distribuicao.put(cartaoId, valorPorCartao);
            }
        }

        // Ajustar centavos no primeiro cartao
        BigDecimal soma = distribuicao.values().stream()
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal diferenca = valorRestante.subtract(soma);
        if (diferenca.compareTo(BigDecimal.ZERO) != 0) {
            Long primeiroCartao = cartoesIds.get(0);
            distribuicao.put(primeiroCartao,
                distribuicao.get(primeiroCartao).add(diferenca));
        }
    }

    return distribuicao;
}
```

#### CartaoDTO
```java
package com.les.jakebooks.dto;

public record CartaoDTO(
    Long id,
    String nomeTitular,
    String numeroMascarado,  // **** **** **** 1234
    String bandeira,
    String validade,
    boolean preferencial
) {}
```

#### PagamentoCartaoDTO
```java
package com.les.jakebooks.dto;

public record PagamentoCartaoDTO(
    Long cartaoId,
    BigDecimal valor
) {}
```

### Business Rules (business-rules-agent)

#### Excecoes
```java
package com.les.jakebooks.exception;

public class ValorMinimoCartaoException extends ValidacaoNegocioException {
    public ValorMinimoCartaoException(String mensagem) {
        super(mensagem);
    }
}

public class ValorPagamentoInvalidoException extends ValidacaoNegocioException {
    public ValorPagamentoInvalidoException(String mensagem) {
        super(mensagem);
    }
}

public class CartaoNaoSelecionadoException extends ValidacaoNegocioException {
    public CartaoNaoSelecionadoException(String mensagem) {
        super(mensagem);
    }
}
```

### Frontend (frontend-agent)

#### Template: checkout/pagamento.html (secao cartoes)
```html
<!-- Secao de Cartoes -->
<div id="secaoCartoes" class="card mb-3" th:if="${opcoes.valorRestante > 0}">
    <div class="card-header">
        Pagamento com Cartao
        <span class="float-end">
            Valor a pagar: <strong id="valorCartoes" th:text="${#numbers.formatCurrency(opcoes.valorRestante)}"></strong>
        </span>
    </div>
    <div class="card-body">
        <div th:if="${opcoes.cartoes.empty}" class="alert alert-warning">
            Voce nao possui cartoes cadastrados.
            <a th:href="@{/cliente/cartoes/novo}" class="btn btn-primary btn-sm">
                Cadastrar Cartao
            </a>
        </div>

        <div th:unless="${opcoes.cartoes.empty}">
            <p class="text-muted">
                Selecione os cartoes e distribua o valor (minimo R$ 10,00 por cartao)
            </p>

            <div th:each="cartao : ${opcoes.cartoes}" class="card mb-2 cartao-item">
                <div class="card-body">
                    <div class="row align-items-center">
                        <div class="col-auto">
                            <input type="checkbox"
                                   class="form-check-input cartao-check"
                                   th:id="'cartao-' + ${cartao.id}"
                                   th:data-cartao-id="${cartao.id}"
                                   onchange="toggleCartao(this)">
                        </div>
                        <div class="col">
                            <label th:for="'cartao-' + ${cartao.id}">
                                <strong th:text="${cartao.bandeira}"></strong>
                                <span th:text="${cartao.numeroMascarado}"></span>
                                <br>
                                <small class="text-muted" th:text="${cartao.nomeTitular}"></small>
                            </label>
                        </div>
                        <div class="col-3">
                            <div class="input-group input-group-sm">
                                <span class="input-group-text">R$</span>
                                <input type="number"
                                       class="form-control valor-cartao"
                                       th:name="'cartoes[' + ${cartao.id} + ']'"
                                       th:id="'valor-' + ${cartao.id}"
                                       step="0.01"
                                       min="10.00"
                                       placeholder="0,00"
                                       disabled
                                       onchange="validarDistribuicao()">
                            </div>
                        </div>
                    </div>
                </div>
            </div>

            <div class="mt-3">
                <button type="button" class="btn btn-outline-secondary btn-sm"
                        onclick="distribuirIgualmente()">
                    Distribuir Igualmente
                </button>
            </div>

            <!-- Validacao -->
            <div id="validacaoCartoes" class="mt-2"></div>
        </div>
    </div>
</div>
```

#### JavaScript
```javascript
let valorRestante = [[${opcoes.valorRestante}]];

function toggleCartao(checkbox) {
    const cartaoId = checkbox.dataset.cartaoId;
    const inputValor = document.getElementById('valor-' + cartaoId);

    if (checkbox.checked) {
        inputValor.disabled = false;
        inputValor.focus();
    } else {
        inputValor.disabled = true;
        inputValor.value = '';
    }

    validarDistribuicao();
}

function distribuirIgualmente() {
    const checkboxes = document.querySelectorAll('.cartao-check:checked');
    if (checkboxes.length === 0) {
        alert('Selecione pelo menos um cartao');
        return;
    }

    const valorPorCartao = (valorRestante / checkboxes.length).toFixed(2);

    // Verificar minimo
    if (parseFloat(valorPorCartao) < 10) {
        alert('Valor por cartao seria menor que R$ 10,00. Selecione menos cartoes.');
        return;
    }

    let soma = 0;
    checkboxes.forEach((cb, index) => {
        const cartaoId = cb.dataset.cartaoId;
        const input = document.getElementById('valor-' + cartaoId);

        if (index === checkboxes.length - 1) {
            // Ultimo cartao pega a diferenca (centavos)
            input.value = (valorRestante - soma).toFixed(2);
        } else {
            input.value = valorPorCartao;
            soma += parseFloat(valorPorCartao);
        }
    });

    validarDistribuicao();
}

function validarDistribuicao() {
    const inputs = document.querySelectorAll('.valor-cartao:not([disabled])');
    const msgDiv = document.getElementById('validacaoCartoes');

    let soma = 0;
    let temErro = false;
    let mensagens = [];

    inputs.forEach(input => {
        const valor = parseFloat(input.value) || 0;
        soma += valor;

        if (valor > 0 && valor < 10) {
            temErro = true;
            mensagens.push('Valor minimo por cartao e R$ 10,00');
        }
    });

    const diferenca = Math.abs(soma - valorRestante);

    if (soma > 0 && diferenca > 0.01) {
        temErro = true;
        if (soma < valorRestante) {
            mensagens.push(`Faltam R$ ${(valorRestante - soma).toFixed(2)}`);
        } else {
            mensagens.push(`Valor excede em R$ ${(soma - valorRestante).toFixed(2)}`);
        }
    }

    if (temErro) {
        msgDiv.innerHTML = `<div class="alert alert-danger">${[...new Set(mensagens)].join('<br>')}</div>`;
    } else if (soma > 0 && diferenca <= 0.01) {
        msgDiv.innerHTML = '<div class="alert alert-success">Distribuicao OK</div>';
    } else {
        msgDiv.innerHTML = '';
    }

    // Habilitar/desabilitar botao submit
    const btnSubmit = document.querySelector('button[type="submit"]');
    btnSubmit.disabled = temErro || (valorRestante > 0 && soma === 0);
}
```

## Fluxo de Execucao

```
1. Sistema exibe cartoes do cliente
2. Cliente marca checkbox de cartoes a usar
3. Campo de valor e habilitado para cartoes selecionados
4. Cliente pode:
   a. Digitar valores manualmente
   b. Clicar "Distribuir Igualmente"
5. Sistema valida em tempo real:
   - Minimo R$10 por cartao
   - Soma = valor restante
6. SE validacao OK:
   - Botao "Finalizar" habilitado
7. SE validacao FALHA:
   - Exibir mensagens de erro
   - Botao "Finalizar" desabilitado
```

## Criterios de Aceite

- [ ] Lista cartoes ativos do cliente
- [ ] Checkbox habilita/desabilita campo de valor
- [ ] Validacao de minimo R$10 funciona
- [ ] Validacao de soma = restante funciona
- [ ] Botao "Distribuir Igualmente" funciona
- [ ] Mensagens de erro claras
- [ ] Botao submit desabilitado se validacao falhar
- [ ] Link para cadastrar cartao se nenhum disponivel

## Dependencias

- **Task Anterior:** TASK-PAY-01, TASK-PAY-02
- **Proxima Task:** TASK-PAY-05 (Processar Pagamento)

---

**Status:** Pendente
