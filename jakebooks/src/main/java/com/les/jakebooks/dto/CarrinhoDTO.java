package com.les.jakebooks.dto;

import com.les.jakebooks.model.enums.StatusCarrinho;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * DTO para exibição do carrinho de compras.
 * RF0031: Gerenciar carrinho
 * RF0032: Definir quantidade no carrinho
 */
public record CarrinhoDTO(
        Long id,
        String codigoCliente,
        StatusCarrinho status,
        LocalDate dataCriacao,
        LocalDate dataExpiracao,
        List<ItemCarrinhoDTO> itens,
        BigDecimal valorTotal
) {
}
