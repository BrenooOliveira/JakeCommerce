package com.les.jakebooks.exception;

/**
 * Exceção lançada quando um livro solicitado não é encontrado.
 * 
 * Código RN: Consulta de livros
 * Aplicável: Quando tentar acessar, alterar ou vender livro inexistente
 */
public class LivroNaoEncontradoException extends NegocioException {

    private String codigoLivro;

    public LivroNaoEncontradoException(String mensagem) {
        super(mensagem, "RN_LIVRO_NAO_ENCONTRADO");
    }

    public LivroNaoEncontradoException(String mensagem, String codigoLivro) {
        super(mensagem, "RN_LIVRO_NAO_ENCONTRADO");
        this.codigoLivro = codigoLivro;
    }

    public LivroNaoEncontradoException(String mensagem, String codigoLivro, Throwable causa) {
        super(mensagem, "RN_LIVRO_NAO_ENCONTRADO", causa);
        this.codigoLivro = codigoLivro;
    }

    public String getCodigoLivro() {
        return codigoLivro;
    }
}
