package com.les.jakebooks.exception;

/**
 * Exceção lançada quando um cupom é inválido, expirado ou já foi utilizado.
 * 
 * Código RN: RN0033, RN0035, RN0036
 * Requisito: Apenas um cupom promocional por compra
 *            Consumir cupons antes do cartão
 *            Gerar cupom para excedente
 */
public class CupomInvalidoException extends NegocioException {

    private String codigoCupom;
    private String motivoInvalid;

    public CupomInvalidoException(String mensagem) {
        super(mensagem, "RN0033");
    }

    public CupomInvalidoException(String mensagem, String codigoCupom) {
        super(mensagem, "RN0033");
        this.codigoCupom = codigoCupom;
    }

    public CupomInvalidoException(String mensagem, String codigoCupom, String motivoInvalid) {
        super(mensagem, "RN0033");
        this.codigoCupom = codigoCupom;
        this.motivoInvalid = motivoInvalid;
    }

    public CupomInvalidoException(String mensagem, String codigoRN, Throwable causa) {
        super(mensagem, codigoRN, causa);
    }

    public String getCodigoCupom() {
        return codigoCupom;
    }

    public String getMotivoInvalid() {
        return motivoInvalid;
    }
}
