package com.les.jakebooks.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Entidade que representa o estoque de um livro no sistema.
 * RF0051: Entrada exige produto, quantidade, custo, fornecedor e data.
 * RN005x: Considera o maior custo para cálculo de venda.
 * RN0061: Não permitir quantidade zero.
 * RN0062: Todo item deve possuir custo.
 * RNF0064: Não permitir registro sem data.
 */
@Entity
@Table(name = "estoque")
public class Estoque {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private Integer quantidade;
    
    private BigDecimal custoAtual;
    
    private LocalDate dataEntrada;
    
    @OneToOne(mappedBy = "estoque")
    private Livro livro;

    // Construtores
    public Estoque() {
    }

    public Estoque(Integer quantidade, BigDecimal custoAtual, LocalDate dataEntrada) {
        this.quantidade = quantidade;
        this.custoAtual = custoAtual;
        this.dataEntrada = dataEntrada;
    }

    // Getters e Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getQuantidade() {
        return quantidade;
    }

    public void setQuantidade(Integer quantidade) {
        this.quantidade = quantidade;
    }

    public BigDecimal getCustoAtual() {
        return custoAtual;
    }

    public void setCustoAtual(BigDecimal custoAtual) {
        this.custoAtual = custoAtual;
    }

    public LocalDate getDataEntrada() {
        return dataEntrada;
    }

    public void setDataEntrada(LocalDate dataEntrada) {
        this.dataEntrada = dataEntrada;
    }

    public Livro getLivro() {
        return livro;
    }

    public void setLivro(Livro livro) {
        this.livro = livro;
    }
}
