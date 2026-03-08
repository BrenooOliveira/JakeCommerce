package com.les.jakebooks.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;

/**
 * Entidade que representa um grupo de precificação de produtos.
 * RN0013: O valor de venda do livro é baseado na margem do grupo.
 * RN0014: Redução abaixo da margem exige autorização.
 */
@Entity
@Table(name = "grupo_precificacao")
public class GrupoPrecificacao {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String nome;
    
    private BigDecimal percentualMargem;

    // Construtores
    public GrupoPrecificacao() {
    }

    public GrupoPrecificacao(String nome, BigDecimal percentualMargem) {
        this.nome = nome;
        this.percentualMargem = percentualMargem;
    }

    // Getters e Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public BigDecimal getPercentualMargem() {
        return percentualMargem;
    }

    public void setPercentualMargem(BigDecimal percentualMargem) {
        this.percentualMargem = percentualMargem;
    }
}
