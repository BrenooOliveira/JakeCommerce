package com.les.jakebooks.repository;

import com.les.jakebooks.domain.Pedido;
import com.les.jakebooks.domain.enums.StatusPedido;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository para a entidade Pedido.
 * RF0033: Realizar compra.
 * RF0037: Finalizar compra (status inicial: EM PROCESSAMENTO).
 */
@Repository
public interface PedidoRepository extends JpaRepository<Pedido, Long> {

    /**
     * Busca pedidos de um cliente ordenados por data de criação decrescente.
     * RF0025: Consultar transações do cliente.
     * RF0039: Confirmar entrega (ENTREGUE).
     */
    List<Pedido> findByClienteCodigoOrderByDataCriacaoDesc(String clienteCodigo);

    /**
     * Busca pedidos de um cliente pelo email ordenados por data de criação decrescente.
     * RF0025: Consultar transações do cliente.
     */
    List<Pedido> findByClienteEmailOrderByDataCriacaoDesc(String email);

    /**
     * Busca pedidos de um cliente pelo email e status.
     */
    List<Pedido> findByClienteEmailAndStatusOrderByDataCriacaoDesc(String email, StatusPedido status);

    /**
     * Busca pedidos de um cliente por status.
     */
    List<Pedido> findByClienteIdAndStatusOrderByDataCriacaoDesc(Long clienteId, StatusPedido status);

    /**
     * Busca pedidos por status.
     * RF0038: Despachar produtos (EM TRANSPORTE).
     * RF0040: Solicitar troca (status EM TROCA).
     */
    List<Pedido> findByStatusOrderByDataCriacaoDesc(StatusPedido status);

    /**
     * Busca pedidos por status ordenados por data de despacho ascendente.
     * TASK-SHP-05: Para listar pedidos em transporte por ordem de despacho.
     * RF0039: Confirmar entrega (ENTREGUE).
     */
    List<Pedido> findByStatusOrderByDataDespachoAsc(StatusPedido status);

    /**
     * Busca pedidos que podem ser trocados (status ENTREGUE).
     * RN0043: Apenas pedidos ENTREGUES podem solicitar troca.
     */
    List<Pedido> findByClienteIdAndStatus(Long clienteId, StatusPedido status);

    /**
     * Busca pedido por cliente e período.
     * RF0055: Analisar histórico por período.
     */
    @Query("SELECT p FROM Pedido p WHERE p.cliente.id = :clienteId AND p.dataCriacao BETWEEN :dataInicio AND :dataFim")
    List<Pedido> buscarPorClienteEPeriodo(
            @Param("clienteId") Long clienteId,
            @Param("dataInicio") LocalDateTime dataInicio,
            @Param("dataFim") LocalDateTime dataFim
    );

    /**
     * Busca pedidos por período.
     * RF0055: Analisar histórico por período comparando produtos ou categorias.
     */
    @Query("SELECT p FROM Pedido p WHERE p.dataCriacao BETWEEN :dataInicio AND :dataFim ORDER BY p.dataCriacao DESC")
    List<Pedido> buscarPorPeriodo(
            @Param("dataInicio") LocalDateTime dataInicio,
            @Param("dataFim") LocalDateTime dataFim
    );

    /**
     * Conta pedidos por status.
     * TASK-SHP-06: Para dashboard de pedidos.
     */
    Long countByStatus(StatusPedido status);

    /**
     * Busca pedidos por status com paginação.
     * TASK-SHP-06: Listagem paginada de pedidos.
     */
    Page<Pedido> findByStatus(StatusPedido status, Pageable pageable);

    /**
     * Busca pedido por código gerado.
     * TASK-SHP-06: Busca rápida por código de pedido (formato PED-000001).
     */
    @Query("SELECT p FROM Pedido p WHERE CONCAT('PED-', LPAD(CAST(p.id AS string), 6, '0')) = :codigo")
    Optional<Pedido> findByCodigo(@Param("codigo") String codigo);
}
