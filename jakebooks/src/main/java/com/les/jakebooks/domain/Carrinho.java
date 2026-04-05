package com.les.jakebooks.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.les.jakebooks.model.enums.StatusCarrinho;

/**
 * Entidade que representa um carrinho de compras do sistema.
 * RF0031: Gerenciar carrinho.
 * RN0044: Carrinho é bloqueado com aviso 5 minutos antes da expiração.
 */
@Entity
@Table(name = "carrinho")
public class Carrinho {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private LocalDate dataCriacao;
    
    @Enumerated(EnumType.STRING)
    private StatusCarrinho status;
    
    private LocalDate dataExpiracao;

    private Integer tentativasReprovadas = 0;

    private LocalDateTime dataBloqueio;
    
    @OneToOne
    @JoinColumn(name = "cliente_id")
    private Cliente cliente;
    
    @OneToMany(mappedBy = "carrinho", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ItemCarrinho> itens = new ArrayList<>();

    // Construtores
    public Carrinho() {
    }

    public Carrinho(LocalDate dataCriacao, StatusCarrinho status, LocalDate dataExpiracao, Cliente cliente) {
        this.dataCriacao = dataCriacao;
        this.status = status;
        this.dataExpiracao = dataExpiracao;
        this.cliente = cliente;
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

    public StatusCarrinho getStatus() {
        return status;
    }

    public void setStatus(StatusCarrinho status) {
        this.status = status;
    }

    public LocalDate getDataExpiracao() {
        return dataExpiracao;
    }

    public void setDataExpiracao(LocalDate dataExpiracao) {
        this.dataExpiracao = dataExpiracao;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public void setCliente(Cliente cliente) {
        this.cliente = cliente;
    }

    public List<ItemCarrinho> getItens() {
        return itens;
    }

    public void setItens(List<ItemCarrinho> itens) {
        this.itens = itens;
    }

    public Integer getTentativasReprovadas() {
        return tentativasReprovadas;
    }

    public void setTentativasReprovadas(Integer tentativasReprovadas) {
        this.tentativasReprovadas = tentativasReprovadas;
    }

    public LocalDateTime getDataBloqueio() {
        return dataBloqueio;
    }

    public void setDataBloqueio(LocalDateTime dataBloqueio) {
        this.dataBloqueio = dataBloqueio;
    }
}
