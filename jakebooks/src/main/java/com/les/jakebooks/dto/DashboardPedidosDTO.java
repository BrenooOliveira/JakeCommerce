package com.les.jakebooks.dto;

import java.math.BigDecimal;

/**
 * DTO para dashboard de pedidos na área administrativa.
 * TASK-SHP-06: Listar Pedidos por Status (Admin).
 * Centraliza estatísticas e contadores por status.
 */
public record DashboardPedidosDTO(
    long totalPedidos,
    long emProcessamento,
    long emTransporte,
    long entregues,
    long emTroca,
    BigDecimal valorTotalVendas
) {
}