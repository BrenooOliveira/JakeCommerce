package com.les.jakebooks.dto;

import com.les.jakebooks.domain.enums.StatusPedido;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO para resumo de pedido do cliente.
 * Utilizado em listagem de transações do cliente.
 * RF0025: Consultar transações do cliente
 */
public record PedidoResumoDTO(
        Long id,
        LocalDate dataCriacao,
        StatusPedido status,
        BigDecimal valorTotal,
        BigDecimal valorFrete,
        Integer quantidadeItens
) {
}
