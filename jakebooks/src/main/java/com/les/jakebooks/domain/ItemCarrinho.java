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
 * Entidade que representa um item dentro de um carrinho de compras.
 * RF0032: Definir quantidade no carrinho.
 * RN0031: Validar estoque no carrinho.
 * RN0063: Máximo 10 unidades do mesmo livro por pedido.
 */
@Entity
@Table(name = "item_carrinho")
public class ItemCarrinho {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private Integer quantidade;
    
    private BigDecimal valorUnitario;
    
    @ManyToOne
    @JoinColumn(name = "carrinho_id")
    private Carrinho carrinho;
    
    @ManyToOne
    @JoinColumn(name = "livro_id")
    private Livro livro;

    // Construtores
    public ItemCarrinho() {
    }

    public ItemCarrinho(Integer quantidade, BigDecimal valorUnitario, Carrinho carrinho, Livro livro) {
        this.quantidade = quantidade;
        this.valorUnitario = valorUnitario;
        this.carrinho = carrinho;
        this.livro = livro;
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

    public BigDecimal getValorUnitario() {
        return valorUnitario;
    }

    public void setValorUnitario(BigDecimal valorUnitario) {
        this.valorUnitario = valorUnitario;
    }

    public Carrinho getCarrinho() {
        return carrinho;
    }

    public void setCarrinho(Carrinho carrinho) {
        this.carrinho = carrinho;
    }

    public Livro getLivro() {
        return livro;
    }

    public void setLivro(Livro livro) {
        this.livro = livro;
    }
}
