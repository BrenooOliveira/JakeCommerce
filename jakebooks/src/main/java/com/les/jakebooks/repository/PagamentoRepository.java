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
     * Busca pagamentos reprovados de um cliente para validar bloqueio.
     * RN0065: 3 pagamentos REPROVADOS consecutivos bloqueiam cliente.
     */
    @Query("SELECT p FROM Pagamento p WHERE p.pedido.cliente.id = :clienteId AND p.status = :status ORDER BY p.dataCriacao DESC")
    List<Pagamento> findByPedidoClienteIdAndStatusOrderByDataCriacaoDesc(@Param("clienteId") Long clienteId, @Param("status") StatusPagamento status);
}
