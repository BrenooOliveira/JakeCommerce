package com.les.jakebooks.exception;

/**
 * Exceção lançada quando o valor de venda está abaixo da margem definida.
 * 
 * Código RN: RN0013, RN0014
 * Requisito: Valor de venda baseado na margem do grupo.
 *            Redução abaixo da margem exige autorização.
 */
public class ValorAbaixoDaMargemException extends NegocioException {

    private Double custoBase;
    private Double percentualMargem;
    private Double valorMinimo;
    private Double valorInformado;

    public ValorAbaixoDaMargemException(String mensagem) {
        super(mensagem, "RN0013");
    }

    public ValorAbaixoDaMargemException(String mensagem, Double custoBase, Double percentualMargem, Double valorMinimo, Double valorInformado) {
        super(mensagem, "RN0014");
        this.custoBase = custoBase;
        this.percentualMargem = percentualMargem;
        this.valorMinimo = valorMinimo;
        this.valorInformado = valorInformado;
    }

    public ValorAbaixoDaMargemException(String mensagem, String codigoRN, Throwable causa) {
        super(mensagem, codigoRN, causa);
    }

    public Double getCustoBase() {
        return custoBase;
    }

    public Double getPercentualMargem() {
        return percentualMargem;
    }

    public Double getValorMinimo() {
        return valorMinimo;
    }

    public Double getValorInformado() {
        return valorInformado;
    }
}
