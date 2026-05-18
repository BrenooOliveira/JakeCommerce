package com.les.jakebooks.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO para criação simples de cupom promocional no admin.
 */
public record CriarCupomRequestDTO(
        @NotBlank(message = "O código do cupom é obrigatório")
        String codigo,

        @NotNull(message = "O valor do cupom é obrigatório")
        @Positive(message = "O valor do cupom deve ser maior que zero")
        BigDecimal valor,

        LocalDate dataValidade
) {
}
