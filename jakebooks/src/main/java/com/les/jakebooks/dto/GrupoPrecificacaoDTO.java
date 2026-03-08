package com.les.jakebooks.dto;

import java.math.BigDecimal;

/**
 * DTO para Grupo de Precificação.
 * Utilizado como objeto aninhado em outros DTOs.
 */
public record GrupoPrecificacaoDTO(
        Long id,
        String nome,
        BigDecimal percentualMargem
) {
}
