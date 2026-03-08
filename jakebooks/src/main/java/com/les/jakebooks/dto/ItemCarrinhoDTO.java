package com.les.jakebooks.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO para item dentro de um carrinho.
 * RN0031: Validar estoque no carrinho
 * RN0063: Máximo 10 unidades do mesmo livro
 */
public record ItemCarrinhoDTO(
        Long id,
        Long livroId,
        String codigoLivro,
        String tituloLivro,
        Integer quantidade,
        BigDecimal valorUnitario,
        BigDecimal subtotal
) {
}
