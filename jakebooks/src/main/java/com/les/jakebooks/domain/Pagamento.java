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
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.les.jakebooks.domain.enums.StatusPagamento;

/**
 * Entidade que representa um pagamento de um pedido.
 * RN0037: Validar pagamento.
 * RN0038: Status pagamento: APROVADA ou REPROVADA.
 * RN0035: Consumir cupons antes do cartão.
 */
@Entity
@Table(name = "pagamento")
public class Pagamento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime dataCriacao;

    @Enumerated(EnumType.STRING)
    private StatusPagamento status;

    private BigDecimal valorTotal;

    private BigDecimal valorPagoCupons;

    private BigDecimal valorPagoCartoes;

    @ManyToOne
    @JoinColumn(name = "pedido_id")
    private Pedido pedido;

    @OneToMany(mappedBy = "pagamento", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PagamentoCartao> pagamentosCartao = new ArrayList<>();

    @OneToMany(mappedBy = "pagamento", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PagamentoCupom> pagamentosCupom = new ArrayList<>();

    /**
     * Lista de cupons consumidos neste pagamento (transiente).
     * Usado para marcar cupons como inativos após aprovação.
     * RN0035: Consumir cupons antes do cartão.
     */
    @Transient
    private List<Cupom> cuponsConsumidos = new ArrayList<>();

    // Construtores
    public Pagamento() {
    }

    public Pagamento(StatusPagamento status, BigDecimal valorTotal) {
        this.status = status;
        this.valorTotal = valorTotal;
        this.dataCriacao = LocalDateTime.now();
    }

    // Getters e Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDateTime getDataCriacao() {
        return dataCriacao;
    }

    public void setDataCriacao(LocalDateTime dataCriacao) {
        this.dataCriacao = dataCriacao;
    }

    public StatusPagamento getStatus() {
        return status;
    }

    public void setStatus(StatusPagamento status) {
        this.status = status;
    }

    public BigDecimal getValorTotal() {
        return valorTotal;
    }

    public void setValorTotal(BigDecimal valorTotal) {
        this.valorTotal = valorTotal;
    }

    public BigDecimal getValorPagoCupons() {
        return valorPagoCupons;
    }

    public void setValorPagoCupons(BigDecimal valorPagoCupons) {
        this.valorPagoCupons = valorPagoCupons;
    }

    public BigDecimal getValorPagoCartoes() {
        return valorPagoCartoes;
    }

    public void setValorPagoCartoes(BigDecimal valorPagoCartoes) {
        this.valorPagoCartoes = valorPagoCartoes;
    }

    public Pedido getPedido() {
        return pedido;
    }

    public void setPedido(Pedido pedido) {
        this.pedido = pedido;
    }

    public List<PagamentoCartao> getPagamentosCartao() {
        return pagamentosCartao;
    }

    public void setPagamentosCartao(List<PagamentoCartao> pagamentosCartao) {
        this.pagamentosCartao = pagamentosCartao;
    }

    public List<PagamentoCupom> getPagamentosCupom() {
        return pagamentosCupom;
    }

    public void setPagamentosCupom(List<PagamentoCupom> pagamentosCupom) {
        this.pagamentosCupom = pagamentosCupom;
    }

    public List<Cupom> getCuponsConsumidos() {
        return cuponsConsumidos;
    }

    public void setCuponsConsumidos(List<Cupom> cuponsConsumidos) {
        this.cuponsConsumidos = cuponsConsumidos;
    }
}
