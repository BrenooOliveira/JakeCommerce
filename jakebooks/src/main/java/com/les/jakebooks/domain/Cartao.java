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

import com.les.jakebooks.model.enums.BandeiraCartao;

/**
 * Entidade que representa um cartão de crédito de um cliente.
 * RN0024: Campos obrigatórios do cartão.
 * RN0025: Bandeira deve estar cadastrada.
 * RN0034: Múltiplos cartões permitidos (mínimo 10 por cartão).
 */
@Entity
@Table(name = "cartao")
public class Cartao {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String numero;
    
    private String nomeImpresso;
    
    @Enumerated(EnumType.STRING)
    private BandeiraCartao bandeira;
    
    private String codigoSeguranca;
    
    private Boolean preferencial;
    
    @ManyToOne
    @JoinColumn(name = "cliente_id")
    private Cliente cliente;

    // Construtores
    public Cartao() {
    }

    public Cartao(String numero, String nomeImpresso, BandeiraCartao bandeira, 
                  String codigoSeguranca, Boolean preferencial) {
        this.numero = numero;
        this.nomeImpresso = nomeImpresso;
        this.bandeira = bandeira;
        this.codigoSeguranca = codigoSeguranca;
        this.preferencial = preferencial;
    }

    // Getters e Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNumero() {
        return numero;
    }

    public void setNumero(String numero) {
        this.numero = numero;
    }

    public String getNomeImpresso() {
        return nomeImpresso;
    }

    public void setNomeImpresso(String nomeImpresso) {
        this.nomeImpresso = nomeImpresso;
    }

    public BandeiraCartao getBandeira() {
        return bandeira;
    }

    public void setBandeira(BandeiraCartao bandeira) {
        this.bandeira = bandeira;
    }

    public String getCodigoSeguranca() {
        return codigoSeguranca;
    }

    public void setCodigoSeguranca(String codigoSeguranca) {
        this.codigoSeguranca = codigoSeguranca;
    }

    public Boolean getPreferencial() {
        return preferencial;
    }

    public void setPreferencial(Boolean preferencial) {
        this.preferencial = preferencial;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public void setCliente(Cliente cliente) {
        this.cliente = cliente;
    }
}
