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
 * Entidade que representa uma parcela de pagamento feita com cartão.
 * RN0034: Múltiplos cartões permitidos (mínimo 10 por transação).
 * RN0065: 3 pagamentos REPROVADOS consecutivos bloqueiam carrinho.
 */
@Entity
@Table(name = "pagamento_cartao")
public class PagamentoCartao {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private BigDecimal valor;
    
    @ManyToOne
    @JoinColumn(name = "pagamento_id")
    private Pagamento pagamento;
    
    @ManyToOne
    @JoinColumn(name = "cartao_id")
    private Cartao cartao;

    // Construtores
    public PagamentoCartao() {
    }

    public PagamentoCartao(BigDecimal valor, Cartao cartao) {
        this.valor = valor;
        this.cartao = cartao;
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

    public Cartao getCartao() {
        return cartao;
    }

    public void setCartao(Cartao cartao) {
        this.cartao = cartao;
    }
}
