package com.les.jakebooks.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO para Estoque.
 * Utilizado como objeto aninhado em outros DTOs.
 * RN0051: Entrada exige produto, quantidade, custo, fornecedor e data.
 */
public record EstoqueDTO(
        Long id,
        Integer quantidade,
        BigDecimal custoAtual,
        LocalDateTime dataEntrada
) {
}
