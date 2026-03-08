package com.les.jakebooks.dto;

import java.math.BigDecimal;

/**
 * DTO para dados de pagamento com cartão.
 * RN0034: Múltiplos cartões permitidos (mínimo R$10 por cartão).
 */
public record PagamentoCartaoDadosDTO(
        Long cartaoId,
        BigDecimal valor
) {
}
