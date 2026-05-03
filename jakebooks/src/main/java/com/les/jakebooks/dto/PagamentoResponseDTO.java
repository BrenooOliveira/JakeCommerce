package com.les.jakebooks.dto;

import com.les.jakebooks.domain.Pagamento;
import com.les.jakebooks.domain.enums.StatusPagamento;

import java.math.BigDecimal;

/**
 * Response do processamento de pagamento.
 */
public record PagamentoResponseDTO(
        Long pedidoId,
        StatusPagamento status,
        BigDecimal valorTotal
) {
    public static PagamentoResponseDTO from(Pagamento pagamento) {
        Long pedidoId = pagamento.getPedido() != null ? pagamento.getPedido().getId() : null;
        return new PagamentoResponseDTO(pedidoId, pagamento.getStatus(), pagamento.getValorTotal());
    }
}
