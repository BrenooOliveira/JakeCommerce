package com.les.jakebooks.repository;

import com.les.jakebooks.domain.Cupom;
import com.les.jakebooks.model.enums.TipoCupom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository para a entidade Cupom.
 * RN0033: Apenas um cupom promocional por compra.
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
}
