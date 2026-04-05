package com.les.jakebooks.exception;

import java.math.BigDecimal;

/**
 * Excecao lancada quando a soma dos valores dos cartoes nao corresponde ao valor restante.
 * RF0036: Selecionar pagamento
 * RN0034: Soma deve ser igual ao valor restante
 */
public class ValorPagamentoInvalidoException extends ValidacaoNegocioException {

    private BigDecimal valorInformado;
    private BigDecimal valorEsperado;

    public ValorPagamentoInvalidoException(String mensagem) {
        super(mensagem);
    }

    public ValorPagamentoInvalidoException(BigDecimal valorInformado, BigDecimal valorEsperado) {
        super(String.format("Soma dos valores (R$ %.2f) diferente do valor restante (R$ %.2f)",
                valorInformado, valorEsperado));
        this.valorInformado = valorInformado;
        this.valorEsperado = valorEsperado;
    }

    public BigDecimal getValorInformado() {
        return valorInformado;
    }

    public BigDecimal getValorEsperado() {
        return valorEsperado;
    }
}
