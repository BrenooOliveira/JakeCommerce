package com.les.jakebooks.exception;

/**
 * Exceção lançada quando o cliente tenta usar mais de um cupom promocional.
 *
 * Código RN: RN0033
 * Requisito: Apenas um cupom promocional por compra.
 */
public class CupomPromocionalDuplicadoException extends NegocioException {

    private String codigoCupomExistente;
    private String codigoCupomNovo;

    public CupomPromocionalDuplicadoException() {
        super("Apenas um cupom promocional é permitido por compra", "RN0033");
    }

    public CupomPromocionalDuplicadoException(String codigoCupomExistente, String codigoCupomNovo) {
        super("Apenas um cupom promocional é permitido por compra. " +
              "Cupom '" + codigoCupomExistente + "' já aplicado.", "RN0033");
        this.codigoCupomExistente = codigoCupomExistente;
        this.codigoCupomNovo = codigoCupomNovo;
    }

    public String getCodigoCupomExistente() {
        return codigoCupomExistente;
    }

    public String getCodigoCupomNovo() {
        return codigoCupomNovo;
    }
}
