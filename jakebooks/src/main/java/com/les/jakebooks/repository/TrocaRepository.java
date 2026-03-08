package com.les.jakebooks.repository;

import com.les.jakebooks.domain.Troca;
import com.les.jakebooks.model.enums.StatusTroca;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository para a entidade Troca.
 * RF0040: Solicitar troca.
 * RF0041: Autorizar troca.
 * RF0042: Visualizar trocas (admin).
 * RF0043: Confirmar recebimento de troca.
 */
@Repository
public interface TrocaRepository extends JpaRepository<Troca, Long> {

    /**
     * Busca trocas de um pedido.
     */
    List<Troca> findByPedidoId(Long pedidoId);

    /**
     * Busca trocas de um cliente.
     * RF0025: Consultar transações do cliente.
     */
    List<Troca> findByPedidoClienteIdOrderByDataSolicitacaoDesc(Long clienteId);

    /**
     * Busca trocas por status.
     * RF0042: Visualizar trocas (admin).
     */
    List<Troca> findByStatusOrderByDataSolicitacaoDesc(StatusTroca status);

    /**
     * Busca trocas de um cliente por status.
     */
    List<Troca> findByPedidoClienteIdAndStatusOrderByDataSolicitacaoDesc(Long clienteId, StatusTroca status);

    /**
     * Busca trocas por período.
     * RF0055: Analisar histórico por período.
     */
    @Query("SELECT t FROM Troca t WHERE t.dataSolicitacao BETWEEN :dataInicio AND :dataFim ORDER BY t.dataSolicitacao DESC")
    List<Troca> buscarPorPeriodo(
            @Param("dataInicio") LocalDateTime dataInicio,
            @Param("dataFim") LocalDateTime dataFim
    );
}
