package com.les.jakebooks.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;

import com.les.jakebooks.model.enums.TipoCupom;

/**
 * Entidade que representa um cupom de desconto ou troca no sistema.
 * RN0033: Apenas um cupom promocional por compra.
 * RN0036: Cupom de troca é gerado automaticamente para valor excedente.
 * RF0044: Cupom de troca é gerado ao concluir uma troca.
 */
@Entity
@Table(name = "cupom")
public class Cupom {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String codigo;
    
    private BigDecimal valor;
    
    @Enumerated(EnumType.STRING)
    private TipoCupom tipo;
    
    private Boolean ativo;

    // Construtores
    public Cupom() {
    }

    public Cupom(String codigo, BigDecimal valor, TipoCupom tipo, Boolean ativo) {
        this.codigo = codigo;
        this.valor = valor;
        this.tipo = tipo;
        this.ativo = ativo;
    }

    // Getters e Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public BigDecimal getValor() {
        return valor;
    }

    public void setValor(BigDecimal valor) {
        this.valor = valor;
    }

    public TipoCupom getTipo() {
        return tipo;
    }

    public void setTipo(TipoCupom tipo) {
        this.tipo = tipo;
    }

    public Boolean getAtivo() {
        return ativo;
    }

    public void setAtivo(Boolean ativo) {
        this.ativo = ativo;
    }
}
