package com.les.jakebooks.exception;

import java.math.BigDecimal;

/**
 * Exceção lançada quando o valor total de pagamento é insuficiente para cobrir o pedido.
 *
 * Código RN: RN0037
 * Requisito: Validar pagamento.
 */
public class ValorPagamentoInsuficienteException extends NegocioException {

    private BigDecimal valorTotal;
    private BigDecimal valorInformado;
    private BigDecimal valorFaltante;

    public ValorPagamentoInsuficienteException(BigDecimal valorTotal, BigDecimal valorInformado) {
        super("Valor de pagamento insuficiente. Total: R$ " + valorTotal +
              ". Informado: R$ " + valorInformado +
              ". Faltam: R$ " + valorTotal.subtract(valorInformado), "RN0037");
        this.valorTotal = valorTotal;
        this.valorInformado = valorInformado;
        this.valorFaltante = valorTotal.subtract(valorInformado);
    }

    public BigDecimal getValorTotal() {
        return valorTotal;
    }

    public BigDecimal getValorInformado() {
        return valorInformado;
    }

    public BigDecimal getValorFaltante() {
        return valorFaltante;
    }
}
