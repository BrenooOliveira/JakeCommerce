package com.les.jakebooks.repository;

import com.les.jakebooks.domain.Carrinho;
import com.les.jakebooks.model.enums.StatusCarrinho;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository para a entidade Carrinho.
 * RN0044: Carrinho é bloqueado com aviso 5 minutos antes da expiração.
 */
@Repository
public interface CarrinhoRepository extends JpaRepository<Carrinho, Long> {

    /**
     * Busca carrinhos de um cliente por status.
     * RF0031: Gerenciar carrinho.
     */
    List<Carrinho> findByClienteIdAndStatus(Long clienteId, StatusCarrinho status);

    /**
     * Busca o carrinho aberto de um cliente.
     */
    Optional<Carrinho> findByClienteIdAndStatusEquals(Long clienteId, StatusCarrinho status);

    /**
     * Busca carrinhos expirados.
     */
    List<Carrinho> findByStatus(StatusCarrinho status);
}
