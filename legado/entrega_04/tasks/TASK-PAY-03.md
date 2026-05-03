# TASK-PAY-03: Gerenciar Excedente de Cupom

## Metadata

| Campo | Valor |
|-------|-------|
| **ID** | TASK-PAY-03 |
| **Agente** | payment-agent |
| **Prioridade** | Media |
| **RF Relacionado** | RF0036 |
| **RN Relacionada** | RN0036 |

## Objetivo

Gerar um novo cupom de troca quando o valor total dos cupons aplicados excede o valor do pedido, devolvendo o excedente ao cliente.

## Pre-Condicoes

- TASK-PAY-02 concluida (cupons aplicados)
- Soma dos cupons > valor total do pedido

## Regras de Negocio

| RN | Regra | Logica |
|----|-------|--------|
| RN0036 | Gerar cupom para excedente | Se soma_cupons > valor_total, criar cupom TROCA |

### Calculo do Excedente

```
excedente = soma_valor_cupons - valor_total_pedido

SE excedente > 0:
   criar novo Cupom(tipo=TROCA, valor=excedente, cliente=clienteAtual)
```

## Especificacao Tecnica

### Backend (backend-agent)

#### CupomService (adicionar)
```java
/**
 * Gera cupom de troca para valor excedente
 * @param clienteId ID do cliente
 * @param valorExcedente Valor do excedente
 * @return DTO do novo cupom gerado
 */
@Transactional
public CupomDTO gerarCupomExcedente(Long clienteId, BigDecimal valorExcedente) {
    if (valorExcedente.compareTo(BigDecimal.ZERO) <= 0) {
        throw new IllegalArgumentException("Valor excedente deve ser positivo");
    }

    Cliente cliente = clienteRepository.findById(clienteId)
        .orElseThrow(() -> new ClienteNaoEncontradoException(clienteId));

    Cupom cupom = new Cupom();
    cupom.setCodigo(gerarCodigoCupom());
    cupom.setValor(valorExcedente);
    cupom.setTipo(TipoCupom.TROCA);
    cupom.setCliente(cliente);
    cupom.setAtivo(true);
    cupom.setDataCriacao(LocalDateTime.now());
    cupom.setOrigemPedidoId(null); // Sera preenchido apos finalizar pedido

    cupom = cupomRepository.save(cupom);

    // Log da operacao
    logService.registrar(
        TipoLog.CUPOM_GERADO,
        String.format("Cupom %s gerado para cliente %d, valor R$ %.2f (excedente)",
            cupom.getCodigo(), clienteId, valorExcedente),
        clienteId
    );

    return toDTO(cupom);
}

/**
 * Gera codigo unico para cupom
 */
private String gerarCodigoCupom() {
    String prefixo = "TROCA";
    String aleatorio = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    return prefixo + "-" + aleatorio;
}
```

#### PagamentoService (adicionar)
```java
/**
 * Processa pagamento calculando excedente se houver
 * @return ResultadoPagamentoDTO com info do cupom excedente se gerado
 */
public ResultadoPagamentoDTO processarPagamentoCupons(
        Long clienteId,
        BigDecimal valorTotal,
        List<CupomAplicadoDTO> cuponsAplicados) {

    BigDecimal valorCupons = cuponsAplicados.stream()
        .map(CupomAplicadoDTO::valor)
        .reduce(BigDecimal.ZERO, BigDecimal::add);

    BigDecimal valorRestante = valorTotal.subtract(valorCupons);

    CupomDTO cupomExcedente = null;

    // Se valor dos cupons excede o total, gerar cupom de troca
    if (valorRestante.compareTo(BigDecimal.ZERO) < 0) {
        BigDecimal excedente = valorRestante.abs();
        cupomExcedente = cupomService.gerarCupomExcedente(clienteId, excedente);
        valorRestante = BigDecimal.ZERO;
    }

    return new ResultadoPagamentoDTO(
        valorTotal,
        valorCupons,
        valorRestante,
        cupomExcedente,
        valorRestante.compareTo(BigDecimal.ZERO) == 0 // pagamentoCompleto
    );
}
```

#### ResultadoPagamentoDTO
```java
package com.les.jakebooks.dto;

public record ResultadoPagamentoDTO(
    BigDecimal valorTotal,
    BigDecimal valorPagoComCupons,
    BigDecimal valorRestante,
    CupomDTO cupomExcedenteGerado,
    boolean pagamentoCompleto
) {
    public boolean temExcedente() {
        return cupomExcedenteGerado != null;
    }
}
```

### Frontend (frontend-agent)

#### CheckoutController (adicionar)
```java
@PostMapping("/checkout/pagamento/calcular")
@ResponseBody
public ResponseEntity<ResultadoPagamentoDTO> calcularPagamento(
        @RequestBody PagamentoRequestDTO request,
        HttpSession session,
        Principal principal) {

    Long clienteId = getClienteId(principal);
    CheckoutDTO checkout = getCheckoutFromSession(session);

    BigDecimal valorTotal = checkout.getValorTotal();

    // Aplicar cupons
    List<CupomAplicadoDTO> cupons = cupomService.aplicarCupons(
        request.getCuponsTrocaIds(),
        request.getCodigoCupomPromocional(),
        clienteId
    );

    // Calcular resultado (inclui excedente se houver)
    ResultadoPagamentoDTO resultado = pagamentoService.processarPagamentoCupons(
        clienteId, valorTotal, cupons
    );

    // Armazenar na sessao
    checkout.setCuponsAplicados(cupons);
    checkout.setResultadoPagamento(resultado);
    session.setAttribute("checkout", checkout);

    return ResponseEntity.ok(resultado);
}
```

#### Template: Mensagem de Excedente
```html
<!-- Exibir quando cupom excedente for gerado -->
<div th:if="${resultado.temExcedente()}" class="alert alert-info">
    <h5>Cupom de Troca Gerado!</h5>
    <p>
        O valor dos seus cupons excedeu o total da compra.
        Foi gerado um novo cupom de troca para voce:
    </p>
    <div class="card bg-light">
        <div class="card-body text-center">
            <strong>Codigo:</strong>
            <span class="badge bg-primary fs-5" th:text="${resultado.cupomExcedenteGerado.codigo}"></span>
            <br>
            <strong>Valor:</strong>
            <span class="text-success fs-5"
                  th:text="${#numbers.formatCurrency(resultado.cupomExcedenteGerado.valor)}"></span>
        </div>
    </div>
    <small class="text-muted">
        Este cupom ficara disponivel em sua conta para uso em compras futuras.
    </small>
</div>
```

#### JavaScript (calcular ao selecionar cupons)
```javascript
async function calcularPagamento() {
    const cuponsTrocaIds = Array.from(
        document.querySelectorAll('.cupom-troca:checked')
    ).map(cb => parseInt(cb.value));

    const codigoCupomPromocional =
        document.querySelector('input[name="codigoCupomPromocional"]').value || null;

    const response = await fetch('/checkout/pagamento/calcular', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
            cuponsTrocaIds,
            codigoCupomPromocional
        })
    });

    const resultado = await response.json();

    // Atualizar UI
    document.getElementById('valorRestante').textContent =
        'R$ ' + resultado.valorRestante.toFixed(2).replace('.', ',');

    // Mostrar/esconder secao de cartoes
    document.getElementById('secaoCartoes').style.display =
        resultado.pagamentoCompleto ? 'none' : 'block';

    // Mostrar cupom excedente se gerado
    if (resultado.cupomExcedenteGerado) {
        exibirCupomExcedente(resultado.cupomExcedenteGerado);
    }
}

function exibirCupomExcedente(cupom) {
    const html = `
        <div class="alert alert-info">
            <h5>Cupom de Troca Gerado!</h5>
            <p>Foi gerado um cupom de troca com o valor excedente:</p>
            <div class="text-center">
                <span class="badge bg-primary fs-4">${cupom.codigo}</span>
                <br>
                <span class="text-success fs-4">R$ ${cupom.valor.toFixed(2).replace('.', ',')}</span>
            </div>
        </div>
    `;
    document.getElementById('cupomExcedente').innerHTML = html;
}
```

## Fluxo de Execucao

```
1. Cliente seleciona cupons de troca
2. Cliente aplica cupom promocional (opcional)
3. Sistema calcula:
   - soma_cupons = valor de todos os cupons
   - restante = valor_total - soma_cupons
4. SE restante < 0 (excedente):
   - excedente = |restante|
   - Criar novo Cupom(tipo=TROCA, valor=excedente)
   - Associar ao cliente
   - Exibir codigo e valor do novo cupom
   - Definir restante = 0
5. SE restante >= 0:
   - Nao gerar cupom
   - Prosseguir para selecao de cartoes (se restante > 0)
6. Cupom excedente fica disponivel para proximas compras
```

## Criterios de Aceite

- [ ] Sistema detecta quando cupons excedem valor total
- [ ] Cupom de troca gerado com codigo unico
- [ ] Cupom associado ao cliente correto
- [ ] Valor do cupom = valor excedente exato
- [ ] Cupom exibido na tela com destaque
- [ ] Cupom ativo e disponivel imediatamente
- [ ] Log da geracao registrado
- [ ] Secao de cartoes escondida quando pagamento completo

## Dependencias

- **Task Anterior:** TASK-PAY-02 (Aplicar Cupons)
- **Proxima Task:** TASK-PAY-05 (Processar Pagamento)

---

**Status:** Pendente
