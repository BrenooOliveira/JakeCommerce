package com.les.jakebooks.exception;

/**
 * Exceção lançada quando não há estoque suficiente no momento da baixa.
 * TASK-CHK-04: Coordenar Baixa de Estoque
 * Diferente de EstoqueInsuficienteException (validação), esta é para baixa real.
 * Código RN: RN0032
 * Requisito: Validar estoque antes da finalização.
 */
public class EstoqueInsuficienteParaBaixaException extends NegocioException {

    private String tituloLivro;
    private Integer quantidadeDisponivel;
    private Integer quantidadeNecessaria;

    public EstoqueInsuficienteParaBaixaException(String mensagem) {
        super(mensagem, "RN0032");
    }

    public EstoqueInsuficienteParaBaixaException(String tituloLivro,
                                                 Integer quantidadeDisponivel,
                                                 Integer quantidadeNecessaria) {
        super(String.format("Estoque insuficiente para baixa. Livro: %s, Disponível: %d, Necessário: %d",
                tituloLivro, quantidadeDisponivel, quantidadeNecessaria), "RN0032");
        this.tituloLivro = tituloLivro;
        this.quantidadeDisponivel = quantidadeDisponivel;
        this.quantidadeNecessaria = quantidadeNecessaria;
    }

    public String getTituloLivro() {
        return tituloLivro;
    }

    public Integer getQuantidadeDisponivel() {
        return quantidadeDisponivel;
    }

    public Integer getQuantidadeNecessaria() {
        return quantidadeNecessaria;
    }
}
