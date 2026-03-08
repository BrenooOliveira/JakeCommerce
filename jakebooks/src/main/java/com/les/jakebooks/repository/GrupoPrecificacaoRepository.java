package com.les.jakebooks.repository;

import com.les.jakebooks.domain.GrupoPrecificacao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository para a entidade GrupoPrecificacao.
 * RN0013: Valor de venda baseado na margem do grupo.
 */
@Repository
public interface GrupoPrecificacaoRepository extends JpaRepository<GrupoPrecificacao, Long> {

    /**
     * Busca grupo de precificação pelo nome único.
     */
    Optional<GrupoPrecificacao> findByNome(String nome);
}
