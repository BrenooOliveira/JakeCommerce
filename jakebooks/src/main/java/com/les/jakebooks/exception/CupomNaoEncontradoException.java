package com.les.jakebooks.exception;

/**
 * Excecao lancada quando um cupom nao e encontrado.
 * RF0036: Selecionar pagamento.
 */
public class CupomNaoEncontradoException extends ValidacaoNegocioException {

    private final Long id;
    private final String codigo;

    public CupomNaoEncontradoException(Long id) {
        super("Cupom nao encontrado: ID " + id);
        this.id = id;
        this.codigo = null;
    }

    public CupomNaoEncontradoException(String codigo) {
        super("Cupom nao encontrado: " + codigo);
        this.id = null;
        this.codigo = codigo;
    }

    public Long getId() {
        return id;
    }

    public String getCodigo() {
        return codigo;
    }
}
