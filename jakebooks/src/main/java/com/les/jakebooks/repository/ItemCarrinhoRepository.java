package com.les.jakebooks.repository;

import com.les.jakebooks.domain.ItemCarrinho;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository para a entidade ItemCarrinho.
 * RN0031: Validar estoque no carrinho.
 * RN0032: Validar estoque antes da finalização.
 */
@Repository
public interface ItemCarrinhoRepository extends JpaRepository<ItemCarrinho, Long> {

    /**
     * Busca itens de um carrinho.
     * RF0032: Definir quantidade no carrinho.
     */
    List<ItemCarrinho> findByCarrinhoId(Long carrinhoId);

    /**
     * Busca um item específico de um carrinho.
     */
    Optional<ItemCarrinho> findByCarrinhoIdAndLivroId(Long carrinhoId, Long livroId);

    /**
     * Conta itens em um carrinho.
     */
    long countByCarrinhoId(Long carrinhoId);
}
