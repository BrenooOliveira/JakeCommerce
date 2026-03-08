package com.les.jakebooks.repository;

import com.les.jakebooks.domain.ItemPedido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository para a entidade ItemPedido.
 * Itens que compõem um pedido específico.
 */
@Repository
public interface ItemPedidoRepository extends JpaRepository<ItemPedido, Long> {

    /**
     * Busca itens de um pedido.
     */
    List<ItemPedido> findByPedidoId(Long pedidoId);

    /**
     * Busca itens de um pedido por livro.
     */
    ItemPedido findByPedidoIdAndLivroId(Long pedidoId, Long livroId);

    /**
     * Conta itens em um pedido.
     */
    long countByPedidoId(Long pedidoId);
}
