package com.les.jakebooks.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import com.les.jakebooks.domain.enums.StatusLivro;

/**
 * Entidade que representa um livro no sistema.
 * RN0011: Dados obrigatórios conforme modelo.
 * RN0012: Livro pode ter múltiplas categorias.
 * RN0013: Valor de venda baseado na margem do grupo.
 * RN0014: Redução abaixo da margem exige autorização.
 */
@Entity
@Table(name = "livro")
public class Livro {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String codigo;
    
    private String titulo;
    
    private Integer ano;
    
    private String edicao;
    
    private String isbn;
    
    private Integer numeroPaginas;
    
    private String sinopse;
    
    private String dimensoes;
    
    private String codigoBarras;
    
    @Enumerated(EnumType.STRING)
    private StatusLivro status;
    
    private BigDecimal valorVenda;
    
    // Relacionamentos
    @ManyToOne
    @JoinColumn(name = "grupo_precificacao_id")
    private GrupoPrecificacao grupoPrecificacao;
    
    @ManyToOne
    @JoinColumn(name = "editora_id")
    private Editora editora;
    
    @ManyToMany
    @JoinTable(
        name = "livro_autor",
        joinColumns = @JoinColumn(name = "livro_id"),
        inverseJoinColumns = @JoinColumn(name = "autor_id")
    )
    private List<Autor> autores = new ArrayList<>();
    
    @ManyToMany
    @JoinTable(
        name = "livro_categoria",
        joinColumns = @JoinColumn(name = "livro_id"),
        inverseJoinColumns = @JoinColumn(name = "categoria_id")
    )
    private List<Categoria> categorias = new ArrayList<>();
    
    @OneToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "estoque_id")
    private Estoque estoque;

    // Construtores
    public Livro() {
    }

    public Livro(String codigo, String titulo, Integer ano, String edicao, String isbn, 
                 Integer numeroPaginas, String sinopse, String dimensoes, String codigoBarras, 
                 StatusLivro status, BigDecimal valorVenda, GrupoPrecificacao grupoPrecificacao, Editora editora) {
        this.codigo = codigo;
        this.titulo = titulo;
        this.ano = ano;
        this.edicao = edicao;
        this.isbn = isbn;
        this.numeroPaginas = numeroPaginas;
        this.sinopse = sinopse;
        this.dimensoes = dimensoes;
        this.codigoBarras = codigoBarras;
        this.status = status;
        this.valorVenda = valorVenda;
        this.grupoPrecificacao = grupoPrecificacao;
        this.editora = editora;
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

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public Integer getAno() {
        return ano;
    }

    public void setAno(Integer ano) {
        this.ano = ano;
    }

    public String getEdicao() {
        return edicao;
    }

    public void setEdicao(String edicao) {
        this.edicao = edicao;
    }

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public Integer getNumeroPaginas() {
        return numeroPaginas;
    }

    public void setNumeroPaginas(Integer numeroPaginas) {
        this.numeroPaginas = numeroPaginas;
    }

    public String getSinopse() {
        return sinopse;
    }

    public void setSinopse(String sinopse) {
        this.sinopse = sinopse;
    }

    public String getDimensoes() {
        return dimensoes;
    }

    public void setDimensoes(String dimensoes) {
        this.dimensoes = dimensoes;
    }

    public String getCodigoBarras() {
        return codigoBarras;
    }

    public void setCodigoBarras(String codigoBarras) {
        this.codigoBarras = codigoBarras;
    }

    public StatusLivro getStatus() {
        return status;
    }

    public void setStatus(StatusLivro status) {
        this.status = status;
    }

    public BigDecimal getValorVenda() {
        return valorVenda;
    }

    public void setValorVenda(BigDecimal valorVenda) {
        this.valorVenda = valorVenda;
    }

    public GrupoPrecificacao getGrupoPrecificacao() {
        return grupoPrecificacao;
    }

    public void setGrupoPrecificacao(GrupoPrecificacao grupoPrecificacao) {
        this.grupoPrecificacao = grupoPrecificacao;
    }

    public Editora getEditora() {
        return editora;
    }

    public void setEditora(Editora editora) {
        this.editora = editora;
    }

    public List<Autor> getAutores() {
        return autores;
    }

    public void setAutores(List<Autor> autores) {
        this.autores = autores;
    }

    public List<Categoria> getCategorias() {
        return categorias;
    }

    public void setCategorias(List<Categoria> categorias) {
        this.categorias = categorias;
    }

    public Estoque getEstoque() {
        return estoque;
    }

    public void setEstoque(Estoque estoque) {
        this.estoque = estoque;
    }
}
