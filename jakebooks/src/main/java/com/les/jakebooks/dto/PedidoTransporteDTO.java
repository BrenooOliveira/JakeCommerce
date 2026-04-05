package com.les.jakebooks.dto;

import com.les.jakebooks.model.enums.StatusPedido;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO para pedidos em transporte na área administrativa.
 * Utilizado na TASK-SHP-05 para listagem de pedidos aguardando confirmação de entrega.
 * RF0039: Confirmar entrega (ENTREGUE).
 */
public record PedidoTransporteDTO(
        Long id,
        String codigoPedido,
        LocalDateTime dataDespacho,
        String nomeCliente,
        String enderecoEntrega,
        BigDecimal valorTotal,
        StatusPedido status,
        int quantidadeItens,
        long diasEmTransporte,
        boolean atrasado
) {
}