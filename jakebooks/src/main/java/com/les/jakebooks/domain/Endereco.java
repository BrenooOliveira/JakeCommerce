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

import com.les.jakebooks.domain.enums.TipoEndereco;
import com.les.jakebooks.domain.enums.TipoResidencia;

/**
 * Entidade que representa um endereço de um cliente.
 * RN0023: Campos obrigatórios do endereço.
 * RN0021: Pelo menos um endereço de cobrança é obrigatório.
 * RN0022: Pelo menos um endereço de entrega é obrigatório.
 */
@Entity
@Table(name = "endereco")
public class Endereco {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String nomeIdentificador;
    
    @Enumerated(EnumType.STRING)
    private TipoResidencia tipoResidencia;
    
    private String logradouro;
    
    private Integer numero;
    
    private String bairro;
    
    private String cep;
    
    private String cidade;
    
    private String estado;
    
    private String pais;
    
    @Enumerated(EnumType.STRING)
    private TipoEndereco tipoEndereco;
    
    @ManyToOne
    @JoinColumn(name = "cliente_id")
    private Cliente cliente;

    // Construtores
    public Endereco() {
    }

    public Endereco(String nomeIdentificador, TipoResidencia tipoResidencia, String logradouro, 
                    Integer numero, String bairro, String cep, String cidade, String estado, 
                    String pais, TipoEndereco tipoEndereco) {
        this.nomeIdentificador = nomeIdentificador;
        this.tipoResidencia = tipoResidencia;
        this.logradouro = logradouro;
        this.numero = numero;
        this.bairro = bairro;
        this.cep = cep;
        this.cidade = cidade;
        this.estado = estado;
        this.pais = pais;
        this.tipoEndereco = tipoEndereco;
    }

    // Getters e Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNomeIdentificador() {
        return nomeIdentificador;
    }

    public void setNomeIdentificador(String nomeIdentificador) {
        this.nomeIdentificador = nomeIdentificador;
    }

    public TipoResidencia getTipoResidencia() {
        return tipoResidencia;
    }

    public void setTipoResidencia(TipoResidencia tipoResidencia) {
        this.tipoResidencia = tipoResidencia;
    }

    public String getLogradouro() {
        return logradouro;
    }

    public void setLogradouro(String logradouro) {
        this.logradouro = logradouro;
    }

    public Integer getNumero() {
        return numero;
    }

    public void setNumero(Integer numero) {
        this.numero = numero;
    }

    public String getBairro() {
        return bairro;
    }

    public void setBairro(String bairro) {
        this.bairro = bairro;
    }

    public String getCep() {
        return cep;
    }

    public void setCep(String cep) {
        this.cep = cep;
    }

    public String getCidade() {
        return cidade;
    }

    public void setCidade(String cidade) {
        this.cidade = cidade;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getPais() {
        return pais;
    }

    public void setPais(String pais) {
        this.pais = pais;
    }

    public TipoEndereco getTipoEndereco() {
        return tipoEndereco;
    }

    public void setTipoEndereco(TipoEndereco tipoEndereco) {
        this.tipoEndereco = tipoEndereco;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public void setCliente(Cliente cliente) {
        this.cliente = cliente;
    }
}
