package com.les.jakebooks.dto;

import com.les.jakebooks.domain.enums.StatusPedido;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO para detalhes completos de um pedido na área administrativa.
 * TASK-SHP-06: Listar Pedidos por Status (Admin).
 * Usado para exibir todos os detalhes de um pedido específico.
 */
public record PedidoDetalheDTO(
        Long id,
        String codigoPedido,
        LocalDateTime dataCriacao,
        LocalDateTime dataDespacho,
        LocalDateTime dataEntrega,
        String nomeCliente,
        String emailCliente,
        String enderecoCompleto,
        BigDecimal valorTotal,
        BigDecimal valorFrete,
        StatusPedido status,
        List<ItemPedidoDTO> itens,
        boolean trocaHabilitada
) {

    /**
     * DTO para itens do pedido.
     */
    public record ItemPedidoDTO(
            String nomeProduto,
            Integer quantidade,
            BigDecimal precoUnitario,
            BigDecimal subtotal
    ) {
    }
}