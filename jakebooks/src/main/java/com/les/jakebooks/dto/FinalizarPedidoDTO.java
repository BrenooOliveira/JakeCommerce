package com.les.jakebooks.dto;

import java.util.List;

/**
 * DTO para finalização de pedido.
 * RF0033: Realizar compra.
 * RF0035: Selecionar endereço.
 * RF0036: Selecionar pagamento (cartão, cupom promocional).
 * RN0033: Apenas um cupom promocional por compra.
 * RN0034: Múltiplos cartões permitidos (mínimo R$10 por cartão).
 */
public record FinalizarPedidoDTO(
        String codigoCliente,
        Long carrinhoId,
        Long enderecoId,
        String codigoCupomPromocional,
        List<PagamentoCartaoDadosDTO> pagamentosCartao
) {
}
