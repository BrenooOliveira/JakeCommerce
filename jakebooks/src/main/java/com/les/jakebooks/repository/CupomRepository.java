package com.les.jakebooks.repository;

import com.les.jakebooks.domain.Cupom;
import com.les.jakebooks.domain.enums.TipoCupom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository para a entidade Cupom.
 * RN0033: Apenas um cupom promocional por compra.
 * RN0035: Consumir cupons antes do cartão.
 * RN0036: Gerar cupom para excedente.
 * RF0044: Cupom de troca é gerado automaticamente ao concluir uma troca.
 */
@Repository
public interface CupomRepository extends JpaRepository<Cupom, Long> {

    /**
     * Busca cupom por código e ativo.
     * RF0036: Selecionar pagamento (cupom promocional).
     */
    Optional<Cupom> findByCodigoAndAtivoTrue(String codigo);

    /**
     * Busca cupom apenas pelo código.
     */
    Optional<Cupom> findByCodigo(String codigo);

    /**
     * Busca cupons por tipo.
     */
    List<Cupom> findByTipo(TipoCupom tipo);

    /**
     * Busca cupons ativos.
     */
    List<Cupom> findByAtivoTrue();

    /**
     * Busca cupons por tipo e ativos.
     */
    List<Cupom> findByTipoAndAtivoTrue(TipoCupom tipo);

    /**
     * Busca cupons de troca ativos de um cliente.
     * RN0035: Listar cupons de troca disponíveis do cliente.
     */
    List<Cupom> findByClienteIdAndTipoAndAtivoTrue(Long clienteId, TipoCupom tipo);

    /**
     * Busca cupons de troca ativos de um cliente (método conveniente).
     * RN0035: Listar cupons de troca disponíveis do cliente.
     */
    @Query("SELECT c FROM Cupom c WHERE c.cliente.id = :clienteId AND c.tipo = 'TROCA' AND c.ativo = true " +
           "AND (c.dataValidade IS NULL OR c.dataValidade >= CURRENT_DATE)")
    List<Cupom> findCuponsTrocaAtivosDoCliente(@Param("clienteId") Long clienteId);

    /**
     * Busca todos os cupons de um cliente (ativos e inativos).
     */
    List<Cupom> findByClienteId(Long clienteId);

    /**
     * Busca cupons promocionais ativos e válidos (públicos ou do cliente).
     * RN0033: Cupons promocionais podem ser públicos (cliente nulo) ou do cliente.
     */
    @Query("SELECT c FROM Cupom c WHERE c.tipo = 'PROMOCIONAL' AND c.ativo = true " +
           "AND (c.dataValidade IS NULL OR c.dataValidade >= CURRENT_DATE) " +
           "AND (c.cliente IS NULL OR c.cliente.id = :clienteId)")
    List<Cupom> findCuponsPromocionaisDisponiveis(@Param("clienteId") Long clienteId);
}
