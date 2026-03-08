package com.les.jakebooks.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO para registrar entrada de estoque.
 * RF0051: Entrada em estoque.
 * RN0051: Entrada exige produto, quantidade, custo, fornecedor e data.
 * RN0061: Não permitir quantidade zero.
 * RN0062: Todo item deve possuir custo.
 * RNF0064: Não permitir registro sem data.
 */
public record EntradaEstoqueDTO(
        Long livroId,
        Integer quantidade,
        BigDecimal custo,
        String fornecedor,
        LocalDate dataEntrada
) {
}
