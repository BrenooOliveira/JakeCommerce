package com.les.jakebooks.dto;

import java.time.LocalDateTime;

/**
 * DTO para registrar movimentação de estoque (entrada ou baixa).
 * TASK-CHK-04: Coordenar Baixa de Estoque
 * RNF0012: Log de transações com data, hora e dados alterados.
 *
 * Usado para logging detalhado de operações de estoque.
 */
public class MovimentoEstoque {

    private Long livroId;
    private String tituloLivro;
    private Integer quantidadeAnterior;
    private Integer quantidadeBaixa;
    private Integer quantidadeNova;
    private LocalDateTime dataMovimento;

    // Construtor vazio
    public MovimentoEstoque() {
    }

    // Construtor completo
    public MovimentoEstoque(Long livroId, String tituloLivro, Integer quantidadeAnterior,
                            Integer quantidadeBaixa, Integer quantidadeNova, LocalDateTime dataMovimento) {
        this.livroId = livroId;
        this.tituloLivro = tituloLivro;
        this.quantidadeAnterior = quantidadeAnterior;
        this.quantidadeBaixa = quantidadeBaixa;
        this.quantidadeNova = quantidadeNova;
        this.dataMovimento = dataMovimento;
    }

    // Builder para facilitar construção
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long livroId;
        private String tituloLivro;
        private Integer quantidadeAnterior;
        private Integer quantidadeBaixa;
        private Integer quantidadeNova;
        private LocalDateTime dataMovimento;

        public Builder livroId(Long livroId) {
            this.livroId = livroId;
            return this;
        }

        public Builder tituloLivro(String tituloLivro) {
            this.tituloLivro = tituloLivro;
            return this;
        }

        public Builder quantidadeAnterior(Integer quantidadeAnterior) {
            this.quantidadeAnterior = quantidadeAnterior;
            return this;
        }

        public Builder quantidadeBaixa(Integer quantidadeBaixa) {
            this.quantidadeBaixa = quantidadeBaixa;
            return this;
        }

        public Builder quantidadeNova(Integer quantidadeNova) {
            this.quantidadeNova = quantidadeNova;
            return this;
        }

        public Builder dataMovimento(LocalDateTime dataMovimento) {
            this.dataMovimento = dataMovimento;
            return this;
        }

        public MovimentoEstoque build() {
            return new MovimentoEstoque(livroId, tituloLivro, quantidadeAnterior,
                    quantidadeBaixa, quantidadeNova, dataMovimento);
        }
    }

    // Getters e Setters
    public Long getLivroId() {
        return livroId;
    }

    public void setLivroId(Long livroId) {
        this.livroId = livroId;
    }

    public String getTituloLivro() {
        return tituloLivro;
    }

    public void setTituloLivro(String tituloLivro) {
        this.tituloLivro = tituloLivro;
    }

    public Integer getQuantidadeAnterior() {
        return quantidadeAnterior;
    }

    public void setQuantidadeAnterior(Integer quantidadeAnterior) {
        this.quantidadeAnterior = quantidadeAnterior;
    }

    public Integer getQuantidadeBaixa() {
        return quantidadeBaixa;
    }

    public void setQuantidadeBaixa(Integer quantidadeBaixa) {
        this.quantidadeBaixa = quantidadeBaixa;
    }

    public Integer getQuantidadeNova() {
        return quantidadeNova;
    }

    public void setQuantidadeNova(Integer quantidadeNova) {
        this.quantidadeNova = quantidadeNova;
    }

    public LocalDateTime getDataMovimento() {
        return dataMovimento;
    }

    public void setDataMovimento(LocalDateTime dataMovimento) {
        this.dataMovimento = dataMovimento;
    }
}
