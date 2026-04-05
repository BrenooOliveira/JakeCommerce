package com.les.jakebooks.exception;

/**
 * Exceção lançada quando não é encontrado estoque para um livro.
 * Código RN: RN0062
 * Requisito: Todo item deve possuir registro de estoque.
 */
public class EstoqueNaoEncontradoException extends NegocioException {

    private String tituloLivro;
    private String codigoLivro;

    public EstoqueNaoEncontradoException(String mensagem) {
        super(mensagem, "RN0062");
    }

    public EstoqueNaoEncontradoException(String mensagem, String tituloLivro, String codigoLivro) {
        super(mensagem, "RN0062");
        this.tituloLivro = tituloLivro;
        this.codigoLivro = codigoLivro;
    }

    public String getTituloLivro() {
        return tituloLivro;
    }

    public String getCodigoLivro() {
        return codigoLivro;
    }
}