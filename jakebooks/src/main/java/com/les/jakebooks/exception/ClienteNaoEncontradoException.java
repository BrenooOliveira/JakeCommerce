package com.les.jakebooks.exception;

/**
 * Exceção lançada quando um cliente solicitado não é encontrado.
 * 
 * Código RN: Consulta de clientes
 * Aplicável: Quando tentar acessar, alterar ou processar venda de cliente inexistente
 */
public class ClienteNaoEncontradoException extends NegocioException {

    private String codigoCliente;

    public ClienteNaoEncontradoException(String mensagem) {
        super(mensagem, "RN_CLIENTE_NAO_ENCONTRADO");
    }

    public ClienteNaoEncontradoException(String mensagem, String codigoCliente) {
        super(mensagem, "RN_CLIENTE_NAO_ENCONTRADO");
        this.codigoCliente = codigoCliente;
    }

    public ClienteNaoEncontradoException(String mensagem, String codigoCliente, Throwable causa) {
        super(mensagem, "RN_CLIENTE_NAO_ENCONTRADO", causa);
        this.codigoCliente = codigoCliente;
    }

    public String getCodigoCliente() {
        return codigoCliente;
    }
}
