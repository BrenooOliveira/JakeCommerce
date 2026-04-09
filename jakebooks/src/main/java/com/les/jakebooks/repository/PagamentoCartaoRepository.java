package com.les.jakebooks.repository;

import com.les.jakebooks.domain.PagamentoCartao;
import com.les.jakebooks.domain.enums.StatusPagamentoCartao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository para a entidade PagamentoCartao.
 * PAY-05: Status individual de cada cartão registrado.
 * RN0034: Múltiplos cartões permitidos (mínimo R$10 por cartão).
 */
@Repository
public interface PagamentoCartaoRepository extends JpaRepository<PagamentoCartao, Long> {

    /**
     * Busca pagamentos por cartão de um pagamento específico.
     */
    List<PagamentoCartao> findByPagamentoId(Long pagamentoId);

    /**
     * Busca pagamentos por cartão por status.
     */
    List<PagamentoCartao> findByStatus(StatusPagamentoCartao status);

    /**
     * Busca pagamentos por cartão de um cartão específico.
     */
    List<PagamentoCartao> findByCartaoId(Long cartaoId);

    /**
     * Conta pagamentos aprovados de um pagamento específico.
     */
    @Query("SELECT COUNT(pc) FROM PagamentoCartao pc WHERE pc.pagamento.id = :pagamentoId AND pc.status = :status")
    long countByPagamentoIdAndStatus(@Param("pagamentoId") Long pagamentoId, @Param("status") StatusPagamentoCartao status);
}