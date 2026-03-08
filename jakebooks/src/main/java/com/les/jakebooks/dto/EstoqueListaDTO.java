package com.les.jakebooks.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO para listar estoques com informações do livro.
 * Utilizado na view lista de estoque.
 * RF0051: Entrada em estoque.
 */
public record EstoqueListaDTO(
        Long estoqueId,
        Long livroId,
        String codigoLivro,
        String tituloLivro,
        Integer quantidade,
        BigDecimal custoAtual,
        LocalDate dataEntrada,
        BigDecimal valorVenda
) {
}
