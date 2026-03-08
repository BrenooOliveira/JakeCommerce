package com.les.jakebooks.dto;

import com.les.jakebooks.model.enums.StatusPedido;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * DTO para pedido confirmado após finalização.
 * RF0037: Finalizar compra (status inicial: EM PROCESSAMENTO).
 */
public record PedidoConfirmadoDTO(
        Long pedidoId,
        LocalDate dataCriacao,
        StatusPedido status,
        BigDecimal valorTotal,
        BigDecimal valorFrete,
        BigDecimal valorProdutos,
        String nomeCliente,
        String enderecoEntrega,
        List<ItemCarrinhoDTO> itens,
        String mensagemSucesso
) {
}
