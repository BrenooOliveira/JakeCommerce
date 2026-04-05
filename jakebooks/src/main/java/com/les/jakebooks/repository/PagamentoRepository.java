package com.les.jakebooks.repository;

import com.les.jakebooks.domain.Pagamento;
import com.les.jakebooks.model.enums.StatusPagamento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository para a entidade Pagamento.
 * RN0037: Validar pagamento antes de processar.
 * RN0038: Status pagamento: APROVADA ou REPROVADA.
 * RN0065: 3 pagamentos REPROVADOS consecutivos bloqueiam carrinho.
 */
@Repository
public interface PagamentoRepository extends JpaRepository<Pagamento, Long> {

    /**
     * Busca pagamentos de um pedido.
     * RF0036: Selecionar pagamento (cartão, cupom promocional, cupom de troca).
     */
    List<Pagamento> findByPedidoId(Long pedidoId);

    /**
     * Busca pagamentos por status.
     */
    List<Pagamento> findByStatus(StatusPagamento status);

    /**
     * Busca pagamentos reprovados de um cliente ordenados por data.
     * RN0065: 3 pagamentos REPROVADOS consecutivos bloqueiam cliente.
     */
    @Query("SELECT p FROM Pagamento p WHERE p.pedido.cliente.id = :clienteId AND p.status = :status ORDER BY p.dataCriacao DESC")
    List<Pagamento> findByPedidoClienteIdAndStatusOrderByDataCriacaoDesc(@Param("clienteId") Long clienteId, @Param("status") StatusPagamento status);

    /**
     * Conta tentativas de pagamento reprovadas consecutivas de um cliente.
     * RN0065: 3 pagamentos REPROVADOS consecutivos bloqueiam carrinho.
     *
     * Conta os pagamentos REPROVADOS mais recentes até encontrar um APROVADO.
     */
    @Query(value = """
            SELECT COUNT(*) FROM (
                SELECT p.status, p.data_criacao,
                       ROW_NUMBER() OVER (ORDER BY p.data_criacao DESC) as rn
                FROM pagamento p
                INNER JOIN pedido ped ON p.pedido_id = ped.id
                WHERE ped.cliente_id = :clienteId
                ORDER BY p.data_criacao DESC
            ) sub
            WHERE sub.status = 'REPROVADA'
              AND sub.rn <= (
                  SELECT COALESCE(MIN(sub2.rn) - 1, COUNT(*))
                  FROM (
                      SELECT p2.status,
                             ROW_NUMBER() OVER (ORDER BY p2.data_criacao DESC) as rn
                      FROM pagamento p2
                      INNER JOIN pedido ped2 ON p2.pedido_id = ped2.id
                      WHERE ped2.cliente_id = :clienteId
                  ) sub2
                  WHERE sub2.status = 'APROVADA'
              )
            """, nativeQuery = true)
    long countTentativasReprovadasConsecutivas(@Param("clienteId") Long clienteId);

    /**
     * Busca os últimos N pagamentos de um cliente.
     */
    @Query("SELECT p FROM Pagamento p WHERE p.pedido.cliente.id = :clienteId ORDER BY p.dataCriacao DESC")
    List<Pagamento> findUltimosPagamentosDoCliente(@Param("clienteId") Long clienteId);
}
