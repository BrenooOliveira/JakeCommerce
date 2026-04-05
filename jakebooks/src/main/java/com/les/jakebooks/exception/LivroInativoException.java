package com.les.jakebooks.exception;

/**
 * Exceção lançada quando um livro no carrinho está com status INATIVO.
 * Código RN: RN0016, RN0017
 * Requisito: Validar se todos os livros do carrinho têm status ATIVO.
 */
public class LivroInativoException extends NegocioException {

    private String tituloLivro;
    private String codigoLivro;

    public LivroInativoException(String mensagem) {
        super(mensagem, "RN0016");
    }

    public LivroInativoException(String mensagem, String tituloLivro, String codigoLivro) {
        super(mensagem, "RN0016");
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