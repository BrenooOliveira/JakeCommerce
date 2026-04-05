package com.les.jakebooks.exception;

import java.math.BigDecimal;

/**
 * Exceção lançada quando um valor de pagamento com cartão é inferior ao mínimo.
 *
 * Código RN: RN0034
 * Requisito: Múltiplos cartões permitidos (mínimo R$10 por cartão).
 */
public class ValorMinimoCartaoException extends NegocioException {

    private static final BigDecimal VALOR_MINIMO = new BigDecimal("10.00");

    private BigDecimal valorInformado;
    private Long cartaoId;

    public ValorMinimoCartaoException(BigDecimal valorInformado) {
        super("Valor mínimo por cartão é R$ " + VALOR_MINIMO + ". Valor informado: R$ " + valorInformado, "RN0034");
        this.valorInformado = valorInformado;
    }

    public ValorMinimoCartaoException(BigDecimal valorInformado, Long cartaoId) {
        super("Valor mínimo por cartão é R$ " + VALOR_MINIMO + ". " +
              "Valor informado para cartão ID " + cartaoId + ": R$ " + valorInformado, "RN0034");
        this.valorInformado = valorInformado;
        this.cartaoId = cartaoId;
    }

    public BigDecimal getValorInformado() {
        return valorInformado;
    }

    public Long getCartaoId() {
        return cartaoId;
    }

    public static BigDecimal getValorMinimo() {
        return VALOR_MINIMO;
    }
}
