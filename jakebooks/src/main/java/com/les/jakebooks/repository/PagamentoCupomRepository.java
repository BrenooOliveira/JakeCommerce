package com.les.jakebooks.repository;

import com.les.jakebooks.domain.PagamentoCupom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

/**
 * Repository para a entidade PagamentoCupom.
 * PAY-05: Registrar pagamentos com cupons.
 * RN0033: Apenas um cupom promocional por compra.
 * RN0035: Consumir cupons antes do cartão.
 */
@Repository
public interface PagamentoCupomRepository extends JpaRepository<PagamentoCupom, Long> {

    /**
     * Busca pagamentos por cupom de um pagamento específico.
     */
    List<PagamentoCupom> findByPagamentoId(Long pagamentoId);

    /**
     * Busca pagamentos por cupom de um cupom específico.
     */
    List<PagamentoCupom> findByCupomId(Long cupomId);

    /**
     * Calcula o valor total pago com cupons em um pagamento específico.
     */
    @Query("SELECT COALESCE(SUM(pc.valor), 0) FROM PagamentoCupom pc WHERE pc.pagamento.id = :pagamentoId")
    BigDecimal calcularValorTotalCuponsPorPagamento(@Param("pagamentoId") Long pagamentoId);
}