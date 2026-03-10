package com.les.jakebooks.repository;

import com.les.jakebooks.domain.LogTransacao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository para a entidade LogTransacao.
 * Fornece acesso a dados de auditoria e rastreabilidade.
 * 
 * RNF0012: Log de transações com data, hora, usuário, operação e dados alterados.
 */
@Repository
public interface LogRepository extends JpaRepository<LogTransacao, Long> {

    /**
     * Busca logs de um usuário específico.
     * 
     * @param usuario email do usuário
     * @return lista de transações do usuário
     */
    List<LogTransacao> findByUsuarioOrderByDataHoraDesc(String usuario);

    /**
     * Busca logs de uma entidade específica.
     * Útil para auditar histórico de um Cliente, Livro, Pedido, etc.
     * 
     * @param entidade nome da entidade (Cliente, Livro, Pedido)
     * @return lista de transações da entidade
     */
    List<LogTransacao> findByEntidadeOrderByDataHoraDesc(String entidade);

    /**
     * Busca logs de uma operação específica.
     * Exemplos: CRIAR, ALTERAR, DELETAR, PAGAR
     * 
     * @param operacao tipo de operação
     * @return lista de transações da operação
     */
    List<LogTransacao> findByOperacaoOrderByDataHoraDesc(String operacao);

    /**
     * Busca logs dentro de um período de tempo.
     * Útil para relatórios de auditoria.
     * 
     * @param dataInicio data/hora inicial
     * @param dataFim data/hora final
     * @return lista de transações no período
     */
    @Query("SELECT l FROM LogTransacao l WHERE l.dataHora BETWEEN :dataInicio AND :dataFim ORDER BY l.dataHora DESC")
    List<LogTransacao> findByPeriodo(@Param("dataInicio") LocalDateTime dataInicio, 
                                      @Param("dataFim") LocalDateTime dataFim);

    /**
     * Busca logs de um usuário em um período específico.
     * Útil para analisar atividades de um usuário em um intervalo.
     * 
     * @param usuario email do usuário
     * @param dataInicio data/hora inicial
     * @param dataFim data/hora final
     * @return lista de transações
     */
    @Query("SELECT l FROM LogTransacao l WHERE l.usuario = :usuario AND l.dataHora BETWEEN :dataInicio AND :dataFim ORDER BY l.dataHora DESC")
    List<LogTransacao> findByUsuarioAndPeriodo(@Param("usuario") String usuario,
                                                @Param("dataInicio") LocalDateTime dataInicio,
                                                @Param("dataFim") LocalDateTime dataFim);

    /**
     * Busca logs de uma entidade em um período específico.
     * Útil para histórico completo de um recurso.
     * 
     * @param entidade nome da entidade
     * @param dataInicio data/hora inicial
     * @param dataFim data/hora final
     * @return lista de transações
     */
    @Query("SELECT l FROM LogTransacao l WHERE l.entidade = :entidade AND l.dataHora BETWEEN :dataInicio AND :dataFim ORDER BY l.dataHora DESC")
    List<LogTransacao> findByEntidadeAndPeriodo(@Param("entidade") String entidade,
                                                 @Param("dataInicio") LocalDateTime dataInicio,
                                                 @Param("dataFim") LocalDateTime dataFim);

    /**
     * Busca logs por usuário e operação.
     * Útil para auditar ações específicas de um usuário.
     * 
     * @param usuario email do usuário
     * @param operacao tipo de operação
     * @return lista de transações
     */
    List<LogTransacao> findByUsuarioAndOperacaoOrderByDataHoraDesc(String usuario, String operacao);

    /**
     * Conta quantas transações ocorreram em um período.
     * Útil para estatísticas.
     * 
     * @param dataInicio data/hora inicial
     * @param dataFim data/hora final
     * @return número de transações
     */
    @Query("SELECT COUNT(l) FROM LogTransacao l WHERE l.dataHora BETWEEN :dataInicio AND :dataFim")
    Long countByPeriodo(@Param("dataInicio") LocalDateTime dataInicio,
                        @Param("dataFim") LocalDateTime dataFim);
}
