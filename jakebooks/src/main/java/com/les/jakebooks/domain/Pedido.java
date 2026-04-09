package com.les.jakebooks.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.les.jakebooks.domain.enums.StatusPedido;

/**
 * Entidade que representa um pedido do cliente.
 * RF0037: Finalizar compra (status inicial: EM_PROCESSAMENTO).
 * RF0038: Despachar produtos (EM_TRANSPORTE).
 * RF0039: Confirmar entrega (ENTREGUE).
 * RN0032: Validar estoque antes da finalização.
 * RN0064: Pedido mínimo 20 sem frete.
 */
@Entity
@Table(name = "pedido")
public class Pedido {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private LocalDate dataCriacao;

    private LocalDateTime dataDespacho;

    private LocalDateTime dataEntrega;

    private Boolean trocaHabilitada = false;

    @Enumerated(EnumType.STRING)
    private StatusPedido status;
    
    private BigDecimal valorTotal;
    
    private BigDecimal valorFrete;
    
    @ManyToOne
    @JoinColumn(name = "cliente_id")
    private Cliente cliente;
    
    @ManyToOne
    @JoinColumn(name = "endereco_id")
    private Endereco endereco;
    
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "pagamento_id")
    private Pagamento pagamento;
    
    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ItemPedido> itens = new ArrayList<>();
    
    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Troca> trocas = new ArrayList<>();

    // Construtores
    public Pedido() {
    }

    public Pedido(LocalDate dataCriacao, StatusPedido status, BigDecimal valorTotal, 
                  BigDecimal valorFrete, Cliente cliente, Endereco endereco) {
        this.dataCriacao = dataCriacao;
        this.status = status;
        this.valorTotal = valorTotal;
        this.valorFrete = valorFrete;
        this.cliente = cliente;
        this.endereco = endereco;
    }

    // Getters e Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDate getDataCriacao() {
        return dataCriacao;
    }

    public void setDataCriacao(LocalDate dataCriacao) {
        this.dataCriacao = dataCriacao;
    }

    public LocalDateTime getDataDespacho() {
        return dataDespacho;
    }

    public void setDataDespacho(LocalDateTime dataDespacho) {
        this.dataDespacho = dataDespacho;
    }

    public LocalDateTime getDataEntrega() {
        return dataEntrega;
    }

    public void setDataEntrega(LocalDateTime dataEntrega) {
        this.dataEntrega = dataEntrega;
    }

    public Boolean getTrocaHabilitada() {
        return trocaHabilitada;
    }

    public void setTrocaHabilitada(Boolean trocaHabilitada) {
        this.trocaHabilitada = trocaHabilitada;
    }

    public StatusPedido getStatus() {
        return status;
    }

    public void setStatus(StatusPedido status) {
        this.status = status;
    }

    public BigDecimal getValorTotal() {
        return valorTotal;
    }

    public void setValorTotal(BigDecimal valorTotal) {
        this.valorTotal = valorTotal;
    }

    public BigDecimal getValorFrete() {
        return valorFrete;
    }

    public void setValorFrete(BigDecimal valorFrete) {
        this.valorFrete = valorFrete;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public void setCliente(Cliente cliente) {
        this.cliente = cliente;
    }

    public Endereco getEndereco() {
        return endereco;
    }

    public void setEndereco(Endereco endereco) {
        this.endereco = endereco;
    }

    public Pagamento getPagamento() {
        return pagamento;
    }

    public void setPagamento(Pagamento pagamento) {
        this.pagamento = pagamento;
    }

    public List<ItemPedido> getItens() {
        return itens;
    }

    public void setItens(List<ItemPedido> itens) {
        this.itens = itens;
    }

    public List<Troca> getTrocas() {
        return trocas;
    }

    public void setTrocas(List<Troca> trocas) {
        this.trocas = trocas;
    }
}
