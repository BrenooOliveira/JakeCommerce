package com.les.jakebooks.dto;

import com.les.jakebooks.model.enums.StatusPedido;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO para listagem de pedidos na área administrativa.
 * TASK-SHP-06: Listar Pedidos por Status (Admin).
 * Usado para exibir pedidos na tabela com informações resumidas.
 */
public record PedidoListagemDTO(
        Long id,
        String codigoPedido,
        LocalDateTime dataCriacao,
        String nomeCliente,
        BigDecimal valorTotal,
        StatusPedido status
) {
}