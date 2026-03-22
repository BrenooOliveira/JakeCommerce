package com.les.jakebooks.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

import com.les.jakebooks.model.enums.StatusCliente;

/**
 * Entidade que representa um cliente do sistema.
 * RN0021: Pelo menos um endereço de cobrança é obrigatório.
 * RN0022: Pelo menos um endereço de entrega é obrigatório.
 * RN0026: Dados obrigatórios do cliente.
 * RN0027: Cliente possui ranking numérico.
 * RNF0012: Senha criptografada e forte (mínimo 8 caracteres, maiúsculas, minúsculas e especiais).
 */
@Entity
@Table(name = "cliente")
public class Cliente {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String codigo;
    
    private String nome;
    
    private String genero;
    
    private LocalDate dataNascimento;
    
    private String cpf;
    
    private String telefone;
    
    private String email;
    
    private String senhaCriptografada;
    
    private Double ranking;

    /**
     * Campo técnico para controle de autorização.
     *
     * Este campo NÃO faz parte do modelo de domínio original, mas é uma
     * extensão técnica necessária para implementar requisitos funcionais
     * que especificam operações exclusivas de administradores:
     * - RF0038, RF0039, RF0041, RF0042, RF0051, RF0055
     *
     * Valor padrão: false (cliente comum)
     * Valor true: concede privilégios administrativos (ROLE_ADMIN)
     */
    private Boolean isAdmin = false;

    @Enumerated(EnumType.STRING)
    private StatusCliente status;
    
    // Relacionamentos
    @OneToMany(mappedBy = "cliente", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Endereco> enderecos = new HashSet<>();

    @OneToMany(mappedBy = "cliente", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Cartao> cartoes = new HashSet<>();

    // Construtores
    public Cliente() {
        this.isAdmin = false;
    }

    public Cliente(String codigo, String nome, String genero, LocalDate dataNascimento, String cpf,
                   String telefone, String email, String senhaCriptografada, Double ranking, StatusCliente status,
                   Boolean isAdmin) {
        this.codigo = codigo;
        this.nome = nome;
        this.genero = genero;
        this.dataNascimento = dataNascimento;
        this.cpf = cpf;
        this.telefone = telefone;
        this.email = email;
        this.senhaCriptografada = senhaCriptografada;
        this.ranking = ranking;
        this.status = status;
        this.isAdmin = isAdmin != null ? isAdmin : false;
    }

    // Getters e Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getGenero() {
        return genero;
    }

    public void setGenero(String genero) {
        this.genero = genero;
    }

    public LocalDate getDataNascimento() {
        return dataNascimento;
    }

    public void setDataNascimento(LocalDate dataNascimento) {
        this.dataNascimento = dataNascimento;
    }

    public String getCpf() {
        return cpf;
    }

    public void setCpf(String cpf) {
        this.cpf = cpf;
    }

    public String getTelefone() {
        return telefone;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getSenhaCriptografada() {
        return senhaCriptografada;
    }

    public void setSenhaCriptografada(String senhaCriptografada) {
        this.senhaCriptografada = senhaCriptografada;
    }

    public Double getRanking() {
        return ranking;
    }

    public void setRanking(Double ranking) {
        this.ranking = ranking;
    }

    public StatusCliente getStatus() {
        return status;
    }

    public void setStatus(StatusCliente status) {
        this.status = status;
    }

    public Boolean getIsAdmin() {
        return isAdmin;
    }

    public void setIsAdmin(Boolean isAdmin) {
        this.isAdmin = isAdmin;
    }

    public Set<Endereco> getEnderecos() {
        return enderecos;
    }

    public void setEnderecos(Set<Endereco> enderecos) {
        this.enderecos = enderecos;
    }

    public Set<Cartao> getCartoes() {
        return cartoes;
    }

    public void setCartoes(Set<Cartao> cartoes) {
        this.cartoes = cartoes;
    }
}
