package com.les.jakebooks.dto;

import com.les.jakebooks.model.enums.StatusPedido;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO para resumo de pedidos na área administrativa.
 * Utilizado na TASK-SHP-04 para listagem de pedidos para despacho.
 * RF0038: Despachar produtos (EM TRANSPORTE).
 */
public record PedidoAdminResumoDTO(
        Long id,
        String codigoPedido,
        LocalDateTime dataCriacao,
        String nomeCliente,
        String enderecoEntrega,
        BigDecimal valorTotal,
        StatusPedido status,
        int quantidadeItens
) {
}