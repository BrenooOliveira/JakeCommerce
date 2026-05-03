package com.les.jakebooks.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

/**
 * DTO para dados de pagamento com cartão.
 * RN0034: Múltiplos cartões permitidos (mínimo R$10 por cartão).
 */
public record PagamentoCartaoDadosDTO(
        @NotNull(message = "Cartão é obrigatório")
        @Positive(message = "Cartão deve ser um ID válido")
        Long cartaoId,

        @NotNull(message = "Valor do cartão é obrigatório")
        @DecimalMin(value = "10.00", message = "Valor mínimo por cartão é R$10,00")
        BigDecimal valor
) {
}
