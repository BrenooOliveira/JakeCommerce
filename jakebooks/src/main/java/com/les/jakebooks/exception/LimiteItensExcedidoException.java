package com.les.jakebooks.exception;

/**
 * Exceção lançada quando um item do carrinho excede o limite de 10 unidades.
 * Código RN: RN0063
 * Requisito: Máximo 10 unidades do mesmo livro por pedido.
 */
public class LimiteItensExcedidoException extends NegocioException {

    private String tituloLivro;
    private Integer quantidadeSolicitada;
    private static final Integer LIMITE_MAXIMO = 10;

    public LimiteItensExcedidoException(String mensagem) {
        super(mensagem, "RN0063");
    }

    public LimiteItensExcedidoException(String mensagem, String tituloLivro, Integer quantidadeSolicitada) {
        super(mensagem, "RN0063");
        this.tituloLivro = tituloLivro;
        this.quantidadeSolicitada = quantidadeSolicitada;
    }

    public String getTituloLivro() {
        return tituloLivro;
    }

    public Integer getQuantidadeSolicitada() {
        return quantidadeSolicitada;
    }

    public Integer getLimiteMaximo() {
        return LIMITE_MAXIMO;
    }
}