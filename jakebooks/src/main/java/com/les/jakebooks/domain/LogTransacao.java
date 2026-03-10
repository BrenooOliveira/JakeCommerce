package com.les.jakebooks.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

/**
 * Entidade que registra todas as transações/operações do sistema.
 * 
 * RNF0012: Log de transações com data, hora, usuário, operação e dados alterados.
 * 
 * Esta entidade garante rastreabilidade completa de todas as operações de escrita:
 * - Criação de clientes, livros, pedidos
 * - Alteração de dados
 * - Exclusão lógica
 * - Transações de pagamento
 */
@Entity
@Table(name = "log_transacao")
public class LogTransacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Data e hora da operação em UTC.
     */
    @Column(nullable = false)
    private LocalDateTime dataHora;

    /**
     * Email do usuário que realizou a operação.
     * "SISTEMA" para operações automáticas (cronjobs, imports)
     */
    @Column(nullable = false, length = 100)
    private String usuario;

    /**
     * Tipo de operação realizada.
     * Exemplos: CRIAR, ALTERAR, DELETAR, PAGAR, ENVIAR_PEDIDO, SOLICITAR_TROCA
     */
    @Column(nullable = false, length = 50)
    private String operacao;

    /**
     * Nome da entidade afetada.
     * Exemplos: Cliente, Livro, Pedido, Pagamento, Troca
     */
    @Column(nullable = false, length = 50)
    private String entidade;

    /**
     * Estado anterior da entidade como JSON.
     * Para criações: null ou {}
     * Para atualizações: JSON com dados anteriores
     */
    @Lob
    @Column(columnDefinition = "TEXT")
    private String dadosAnteriores;

    /**
     * Estado novo da entidade como JSON.
     * Para criações: JSON com dados criados
     * Para atualizações: JSON com dados novos
     * Para deleções: null ou JSON com dados removidos
     */
    @Lob
    @Column(columnDefinition = "TEXT")
    private String dadosNovos;

    /**
     * Descrição adicional (opcional).
     * Contexto extra da operação.
     */
    @Column(length = 500)
    private String descricao;

    // Construtores
    public LogTransacao() {
    }

    public LogTransacao(LocalDateTime dataHora, String usuario, String operacao, 
                       String entidade, String dadosAnteriores, String dadosNovos) {
        this.dataHora = dataHora;
        this.usuario = usuario;
        this.operacao = operacao;
        this.entidade = entidade;
        this.dadosAnteriores = dadosAnteriores;
        this.dadosNovos = dadosNovos;
    }

    // Getters e Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDateTime getDataHora() {
        return dataHora;
    }

    public void setDataHora(LocalDateTime dataHora) {
        this.dataHora = dataHora;
    }

    public String getUsuario() {
        return usuario;
    }

    public void setUsuario(String usuario) {
        this.usuario = usuario;
    }

    public String getOperacao() {
        return operacao;
    }

    public void setOperacao(String operacao) {
        this.operacao = operacao;
    }

    public String getEntidade() {
        return entidade;
    }

    public void setEntidade(String entidade) {
        this.entidade = entidade;
    }

    public String getDadosAnteriores() {
        return dadosAnteriores;
    }

    public void setDadosAnteriores(String dadosAnteriores) {
        this.dadosAnteriores = dadosAnteriores;
    }

    public String getDadosNovos() {
        return dadosNovos;
    }

    public void setDadosNovos(String dadosNovos) {
        this.dadosNovos = dadosNovos;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    @Override
    public String toString() {
        return "LogTransacao{" +
                "id=" + id +
                ", dataHora=" + dataHora +
                ", usuario='" + usuario + '\'' +
                ", operacao='" + operacao + '\'' +
                ", entidade='" + entidade + '\'' +
                '}';
    }
}
