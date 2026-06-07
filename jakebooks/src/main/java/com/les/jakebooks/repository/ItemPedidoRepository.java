package com.les.jakebooks.repository;

import com.les.jakebooks.domain.ItemPedido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
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

    /**
     * Busca agregação de vendas por produto em um período.
     * RF0055: Analisar histórico por período comparando produtos.
     * Considera apenas pedidos ENTREGUE ou TROCADO.
     */
    @Query("SELECT NEW map(" +
            "l.titulo as titulo, " +
            "CAST(FUNCTION('DATE', p.dataCriacao) as date) as data, " +
            "SUM(ip.valorUnitario * ip.quantidade) as valor) " +
            "FROM ItemPedido ip " +
            "JOIN ip.livro l " +
            "JOIN ip.pedido p " +
            "WHERE p.dataCriacao BETWEEN :dataInicio AND :dataFim " +
            "AND (p.status = 'ENTREGUE' OR p.status = 'TROCADO') " +
            "GROUP BY l.titulo, CAST(FUNCTION('DATE', p.dataCriacao) as date) " +
            "ORDER BY l.titulo, CAST(FUNCTION('DATE', p.dataCriacao) as date)")
    List<Object> buscarVendasPorProduto(
            @Param("dataInicio") LocalDate dataInicio,
            @Param("dataFim") LocalDate dataFim
    );

    /**
     * Busca agregação de vendas por categoria em um período.
     * RF0055: Analisar histórico por período comparando categorias.
     * Considera apenas pedidos ENTREGUE ou TROCADO.
     */
    @Query("SELECT NEW map(" +
            "c.nome as categoria, " +
            "CAST(FUNCTION('DATE', p.dataCriacao) as date) as data, " +
            "SUM(ip.quantidade) as quantidade) " +
            "FROM ItemPedido ip " +
            "JOIN ip.livro l " +
            "JOIN l.categorias c " +
            "JOIN ip.pedido p " +
            "WHERE p.dataCriacao BETWEEN :dataInicio AND :dataFim " +
            "AND (p.status = 'ENTREGUE' OR p.status = 'TROCADO') " +
            "AND c.id IN :categoriaIds " +
            "GROUP BY c.nome, CAST(FUNCTION('DATE', p.dataCriacao) as date) " +
            "ORDER BY c.nome, CAST(FUNCTION('DATE', p.dataCriacao) as date)")
    List<Object> buscarVendasPorCategoria(
            @Param("dataInicio") LocalDate dataInicio,
            @Param("dataFim") LocalDate dataFim,
            @Param("categoriaIds") List<Long> categoriaIds
    );
}
