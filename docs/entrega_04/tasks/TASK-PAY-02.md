# TASK-PAY-02: Aplicar Cupons

## Metadata

| Campo | Valor |
|-------|-------|
| **ID** | TASK-PAY-02 |
| **Agente** | payment-agent |
| **Prioridade** | Alta |
| **RF Relacionado** | RF0036 |
| **RN Relacionada** | RN0033, RN0035 |

## Objetivo

Validar e aplicar cupons de troca e promocionais ao pagamento, garantindo as regras de limite e ordem de consumo.

## Pre-Condicoes

- TASK-PAY-01 em andamento
- Cliente selecionou cupons para aplicar
- Valor total do pedido conhecido

## Regras de Negocio

| RN | Regra | Validacao |
|----|-------|-----------|
| RN0033 | Apenas um cupom promocional por compra | Contar cupons tipo=PROMOCIONAL, max 1 |
| RN0035 | Consumir cupons antes do cartao | Ordem de aplicacao garantida |

### Tipos de Cupom

| Tipo | Descricao | Limite |
|------|-----------|--------|
| TROCA | Gerado por devolucao | Sem limite por compra |
| PROMOCIONAL | Desconto da loja | Maximo 1 por compra |

## Especificacao Tecnica

### Backend (backend-agent)

#### CupomService
```java
package com.les.jakebooks.service;

@Service
public class CupomService {

    /**
     * Lista cupons de troca ativos do cliente
     */
    public List<CupomDTO> listarCuponsTrocaAtivos(Long clienteId) {
        return cupomRepository
            .findByClienteIdAndTipoAndAtivoTrue(clienteId, TipoCupom.TROCA)
            .stream()
            .filter(c -> c.getDataValidade() == null ||
                        c.getDataValidade().isAfter(LocalDate.now()))
            .map(this::toDTO)
            .collect(Collectors.toList());
    }

    /**
     * Valida cupom promocional por codigo
     * @throws CupomInvalidoException se cupom nao existe ou expirado
     * @throws CupomJaUtilizadoException se cupom ja foi usado
     */
    public CupomDTO validarCupomPromocional(String codigo) {
        Cupom cupom = cupomRepository.findByCodigo(codigo)
            .orElseThrow(() -> new CupomInvalidoException(
                "Cupom nao encontrado: " + codigo
            ));

        if (cupom.getTipo() != TipoCupom.PROMOCIONAL) {
            throw new CupomInvalidoException(
                "Este codigo nao e de um cupom promocional"
            );
        }

        if (!cupom.isAtivo()) {
            throw new CupomJaUtilizadoException(
                "Este cupom ja foi utilizado"
            );
        }

        if (cupom.getDataValidade() != null &&
            cupom.getDataValidade().isBefore(LocalDate.now())) {
            throw new CupomInvalidoException(
                "Este cupom esta expirado"
            );
        }

        return toDTO(cupom);
    }

    /**
     * Aplica cupons ao pagamento
     * @param cuponsTrocaIds IDs dos cupons de troca selecionados
     * @param codigoPromocional Codigo do cupom promocional (pode ser null)
     * @param clienteId ID do cliente
     * @return Lista de cupons aplicados com valores
     * @throws CupomPromocionalDuplicadoException se mais de 1 promocional
     */
    public List<CupomAplicadoDTO> aplicarCupons(
            List<Long> cuponsTrocaIds,
            String codigoPromocional,
            Long clienteId) {

        List<CupomAplicadoDTO> cuponsAplicados = new ArrayList<>();

        // 1. Validar e aplicar cupons de troca
        for (Long cupomId : cuponsTrocaIds) {
            Cupom cupom = cupomRepository.findById(cupomId)
                .orElseThrow(() -> new CupomNaoEncontradoException(cupomId));

            // Validar que pertence ao cliente
            if (!cupom.getCliente().getId().equals(clienteId)) {
                throw new AcessoNegadoException(
                    "Cupom nao pertence ao cliente"
                );
            }

            // Validar que e do tipo TROCA
            if (cupom.getTipo() != TipoCupom.TROCA) {
                throw new CupomInvalidoException(
                    "Cupom " + cupom.getCodigo() + " nao e do tipo troca"
                );
            }

            // Validar que esta ativo
            if (!cupom.isAtivo()) {
                throw new CupomJaUtilizadoException(
                    "Cupom " + cupom.getCodigo() + " ja foi utilizado"
                );
            }

            cuponsAplicados.add(new CupomAplicadoDTO(
                cupom.getId(),
                cupom.getCodigo(),
                cupom.getValor(),
                TipoCupom.TROCA
            ));
        }

        // 2. Validar e aplicar cupom promocional (se informado)
        if (codigoPromocional != null && !codigoPromocional.isBlank()) {
            CupomDTO promocional = validarCupomPromocional(codigoPromocional);

            cuponsAplicados.add(new CupomAplicadoDTO(
                promocional.id(),
                promocional.codigo(),
                promocional.valor(),
                TipoCupom.PROMOCIONAL
            ));
        }

        // 3. Validar limite de 1 cupom promocional
        long qtdPromocionais = cuponsAplicados.stream()
            .filter(c -> c.tipo() == TipoCupom.PROMOCIONAL)
            .count();

        if (qtdPromocionais > 1) {
            throw new CupomPromocionalDuplicadoException(
                "Apenas um cupom promocional permitido por compra"
            );
        }

        return cuponsAplicados;
    }
}
```

#### CupomAplicadoDTO
```java
package com.les.jakebooks.dto;

public record CupomAplicadoDTO(
    Long id,
    String codigo,
    BigDecimal valor,
    TipoCupom tipo
) {}
```

### Business Rules (business-rules-agent)

#### Excecoes
```java
package com.les.jakebooks.exception;

public class CupomInvalidoException extends ValidacaoNegocioException {
    public CupomInvalidoException(String mensagem) {
        super(mensagem);
    }
}

public class CupomJaUtilizadoException extends ValidacaoNegocioException {
    public CupomJaUtilizadoException(String mensagem) {
        super(mensagem);
    }
}

public class CupomPromocionalDuplicadoException extends ValidacaoNegocioException {
    public CupomPromocionalDuplicadoException(String mensagem) {
        super(mensagem);
    }
}

public class CupomNaoEncontradoException extends ValidacaoNegocioException {
    public CupomNaoEncontradoException(Long id) {
        super("Cupom nao encontrado: " + id);
    }
}
```

### Frontend (frontend-agent)

#### CheckoutController (adicionar endpoint AJAX)
```java
@PostMapping("/checkout/pagamento/validar-cupom")
@ResponseBody
public ResponseEntity<?> validarCupomPromocional(
        @RequestParam String codigo,
        HttpSession session) {
    try {
        CupomDTO cupom = cupomService.validarCupomPromocional(codigo);

        // Verificar se ja tem promocional na sessao
        CheckoutDTO checkout = getCheckoutFromSession(session);
        if (checkout.getCupomPromocionalId() != null) {
            return ResponseEntity.badRequest()
                .body(Map.of("erro", "Ja existe um cupom promocional aplicado"));
        }

        return ResponseEntity.ok(cupom);
    } catch (CupomInvalidoException | CupomJaUtilizadoException e) {
        return ResponseEntity.badRequest()
            .body(Map.of("erro", e.getMessage()));
    }
}

@PostMapping("/checkout/pagamento/aplicar-cupom")
@ResponseBody
public ResponseEntity<?> aplicarCupomPromocional(
        @RequestParam String codigo,
        HttpSession session) {
    try {
        CupomDTO cupom = cupomService.validarCupomPromocional(codigo);

        CheckoutDTO checkout = getCheckoutFromSession(session);
        checkout.setCupomPromocionalId(cupom.id());
        checkout.setValorCupomPromocional(cupom.valor());
        session.setAttribute("checkout", checkout);

        return ResponseEntity.ok(Map.of(
            "sucesso", true,
            "cupom", cupom
        ));
    } catch (Exception e) {
        return ResponseEntity.badRequest()
            .body(Map.of("erro", e.getMessage()));
    }
}
```

#### JavaScript (checkout/pagamento.html)
```javascript
async function validarCupom() {
    const codigo = document.querySelector('input[name="codigoCupomPromocional"]').value;
    const msgDiv = document.getElementById('msgCupom');

    if (!codigo.trim()) {
        msgDiv.innerHTML = '<span class="text-danger">Digite um codigo</span>';
        return;
    }

    try {
        const response = await fetch('/checkout/pagamento/validar-cupom?codigo=' + codigo, {
            method: 'POST'
        });

        const data = await response.json();

        if (response.ok) {
            msgDiv.innerHTML = `
                <span class="text-success">
                    Cupom valido! Desconto de R$ ${data.valor.toFixed(2).replace('.', ',')}
                </span>
                <button type="button" class="btn btn-sm btn-success ms-2"
                        onclick="aplicarCupom('${codigo}')">
                    Aplicar
                </button>
            `;
        } else {
            msgDiv.innerHTML = `<span class="text-danger">${data.erro}</span>`;
        }
    } catch (error) {
        msgDiv.innerHTML = '<span class="text-danger">Erro ao validar cupom</span>';
    }
}

async function aplicarCupom(codigo) {
    const response = await fetch('/checkout/pagamento/aplicar-cupom?codigo=' + codigo, {
        method: 'POST'
    });

    const data = await response.json();

    if (response.ok) {
        document.getElementById('msgCupom').innerHTML = `
            <span class="text-success">Cupom aplicado com sucesso!</span>
        `;
        // Atualizar valor restante
        atualizarValorRestante();
    } else {
        document.getElementById('msgCupom').innerHTML =
            `<span class="text-danger">${data.erro}</span>`;
    }
}
```

## Fluxo de Execucao

```
1. Cliente seleciona cupons de troca (checkboxes)
2. Sistema atualiza valor restante localmente

3. Cliente insere codigo de cupom promocional
4. Cliente clica "Aplicar"
5. Sistema valida via AJAX:
   - Cupom existe
   - Cupom ativo
   - Cupom nao expirado
   - Nao ha outro promocional aplicado
6. SE valido:
   - Exibe mensagem de sucesso
   - Armazena na sessao
   - Atualiza valor restante
7. SE invalido:
   - Exibe mensagem de erro

8. No submit do form:
   - Backend recebe cuponsTrocaIds + codigoPromocional
   - Valida todas as regras novamente
   - Aplicar cupons ao pagamento
```

## Criterios de Aceite

- [ ] Cupons de troca listados corretamente
- [ ] Multiplos cupons de troca podem ser selecionados
- [ ] Validacao de cupom promocional funciona via AJAX
- [ ] Apenas 1 cupom promocional permitido
- [ ] Mensagens de erro claras para cada caso
- [ ] Cupons expirados nao sao aceitos
- [ ] Cupons ja utilizados nao sao aceitos
- [ ] Valor restante atualiza ao aplicar cupom

## Dependencias

- **Task Anterior:** TASK-PAY-01 (Montar Opcoes)
- **Proxima Task:** TASK-PAY-03 (Excedente) ou TASK-PAY-04 (Cartoes)

---

**Status:** Pendente
