package com.les.jakebooks.exception;

/**
 * Exceção lançada quando não há estoque suficiente para realizar a operação.
 * Código RN: RN0031, RN0032
 * Requisito: Validar estoque no carrinho e antes da finalização da venda.
 */
public class EstoqueInsuficienteException extends NegocioException {

    private String codigoLivro;
    private Integer quantidadeSolicitada;
    private Integer quantidadeDisponivel;

    public EstoqueInsuficienteException(String mensagem) {
        super(mensagem, "RN0031");
    }

    public EstoqueInsuficienteException(String mensagem, String codigoLivro, Integer quantidadeSolicitada, Integer quantidadeDisponivel) {
        super(mensagem, "RN0031");
        this.codigoLivro = codigoLivro;
        this.quantidadeSolicitada = quantidadeSolicitada;
        this.quantidadeDisponivel = quantidadeDisponivel;
    }

    public String getCodigoLivro() {
        return codigoLivro;
    }

    public Integer getQuantidadeSolicitada() {
        return quantidadeSolicitada;
    }

    public Integer getQuantidadeDisponivel() {
        return quantidadeDisponivel;
    }
}
