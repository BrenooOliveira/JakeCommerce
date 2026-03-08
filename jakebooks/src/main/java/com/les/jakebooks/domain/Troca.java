package com.les.jakebooks.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDate;

import com.les.jakebooks.model.enums.StatusTroca;

/**
 * Entidade que representa uma solicitação de troca de um pedido.
 * RF0040: Solicitar troca.
 * RF0041: Autorizar troca.
 * RF0043: Confirmar recebimento de troca.
 * RF0044: Gerar cupom de troca.
 * RN0043: Apenas pedidos ENTREGUES podem solicitar troca.
 */
@Entity
@Table(name = "troca")
public class Troca {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private LocalDate dataSolicitacao;
    
    @Enumerated(EnumType.STRING)
    private StatusTroca status;
    
    private String motivo;
    
    @ManyToOne
    @JoinColumn(name = "pedido_id")
    private Pedido pedido;

    // Construtores
    public Troca() {
    }

    public Troca(LocalDate dataSolicitacao, StatusTroca status, String motivo, Pedido pedido) {
        this.dataSolicitacao = dataSolicitacao;
        this.status = status;
        this.motivo = motivo;
        this.pedido = pedido;
    }

    // Getters e Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDate getDataSolicitacao() {
        return dataSolicitacao;
    }

    public void setDataSolicitacao(LocalDate dataSolicitacao) {
        this.dataSolicitacao = dataSolicitacao;
    }

    public StatusTroca getStatus() {
        return status;
    }

    public void setStatus(StatusTroca status) {
        this.status = status;
    }

    public String getMotivo() {
        return motivo;
    }

    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }

    public Pedido getPedido() {
        return pedido;
    }

    public void setPedido(Pedido pedido) {
        this.pedido = pedido;
    }
}
