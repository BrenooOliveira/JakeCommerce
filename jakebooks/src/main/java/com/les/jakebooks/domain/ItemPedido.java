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
 * Entidade que representa um item dentro de um pedido.
 * RF0053: Baixa automática após venda.
 * RN0028: Baixa estoque apenas após pagamento aprovado.
 */
@Entity
@Table(name = "item_pedido")
public class ItemPedido {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private Integer quantidade;
    
    private BigDecimal valorUnitario;
    
    @ManyToOne
    @JoinColumn(name = "pedido_id")
    private Pedido pedido;
    
    @ManyToOne
    @JoinColumn(name = "livro_id")
    private Livro livro;

    // Construtores
    public ItemPedido() {
    }

    public ItemPedido(Integer quantidade, BigDecimal valorUnitario, Pedido pedido, Livro livro) {
        this.quantidade = quantidade;
        this.valorUnitario = valorUnitario;
        this.pedido = pedido;
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

    public Pedido getPedido() {
        return pedido;
    }

    public void setPedido(Pedido pedido) {
        this.pedido = pedido;
    }

    public Livro getLivro() {
        return livro;
    }

    public void setLivro(Livro livro) {
        this.livro = livro;
    }
}
