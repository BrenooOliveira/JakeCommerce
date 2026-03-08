package com.les.jakebooks.exception;

/**
 * Exceção lançada quando a operação é bloqueada para o cliente.
 * Corresponde aos requisitos de StatusCliente: BLOQUEADO e RN0065.
 */
public class ClienteBloqueadoException extends RuntimeException {

    private String codigoCliente;
    private String motivo;

    public ClienteBloqueadoException(String mensagem) {
        super(mensagem);
    }

    public ClienteBloqueadoException(String mensagem, String codigoCliente, String motivo) {
        super(mensagem);
        this.codigoCliente = codigoCliente;
        this.motivo = motivo;
    }

    public String getCodigoCliente() {
        return codigoCliente;
    }

    public String getMotivo() {
        return motivo;
    }
}
