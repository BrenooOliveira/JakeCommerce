package com.les.jakebooks.exception;

/**
 * Excecao lancada quando um cupom ja foi utilizado.
 * RF0036: Selecionar pagamento.
 */
public class CupomJaUtilizadoException extends ValidacaoNegocioException {

    private final String codigo;

    public CupomJaUtilizadoException(String mensagem) {
        super(mensagem);
        this.codigo = null;
    }

    public CupomJaUtilizadoException(String mensagem, String codigo) {
        super(mensagem);
        this.codigo = codigo;
    }

    public String getCodigo() {
        return codigo;
    }
}
