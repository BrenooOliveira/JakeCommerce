package com.les.jakebooks.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;

/**
 * Entidade que representa uma parcela de pagamento feita com cupom.
 * RN0033: Apenas um cupom promocional por compra.
 * RN0036: Gerar cupom de troca para valor excedente.
 */
@Entity
@Table(name = "pagamento_cupom")
public class PagamentoCupom {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private BigDecimal valor;
    
    @ManyToOne
    @JoinColumn(name = "pagamento_id")
    private Pagamento pagamento;
    
    @ManyToOne
    @JoinColumn(name = "cupom_id")
    private Cupom cupom;

    // Construtores
    public PagamentoCupom() {
    }

    public PagamentoCupom(BigDecimal valor, Cupom cupom) {
        this.valor = valor;
        this.cupom = cupom;
    }

    // Getters e Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public BigDecimal getValor() {
        return valor;
    }

    public void setValor(BigDecimal valor) {
        this.valor = valor;
    }

    public Pagamento getPagamento() {
        return pagamento;
    }

    public void setPagamento(Pagamento pagamento) {
        this.pagamento = pagamento;
    }

    public Cupom getCupom() {
        return cupom;
    }

    public void setCupom(Cupom cupom) {
        this.cupom = cupom;
    }
}
