package com.les.jakebooks.repository;

import com.les.jakebooks.domain.Cartao;
import com.les.jakebooks.domain.enums.BandeiraCartao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository para a entidade Cartao.
 * RN0024: Campos obrigatórios do cartão.
 * RN0025: Bandeira deve estar cadastrada.
 * RN0034: Múltiplos cartões permitidos (mínimo 10 por cartão).
 */
@Repository
public interface CartaoRepository extends JpaRepository<Cartao, Long> {

    /**
     * Busca cartões de um cliente.
     * RF0027: Cadastrar múltiplos cartões (um preferencial).
     */
    List<Cartao> findByClienteId(Long clienteId);

    /**
     * Busca cartão preferencial de um cliente.
     */
    Optional<Cartao> findByClienteIdAndPreferencial(Long clienteId, Boolean preferencial);

    /**
     * Busca cartão pela combinação cliente e número.
     */
    Optional<Cartao> findByClienteIdAndNumero(Long clienteId, String numero);

    /**
     * Busca cartões por bandeira.
     */
    List<Cartao> findByBandeira(BandeiraCartao bandeira);
}

