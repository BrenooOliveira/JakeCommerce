package com.les.jakebooks.dto;

import com.les.jakebooks.domain.enums.TipoCupom;

import java.math.BigDecimal;

/**
 * DTO para representar um cupom aplicado ao pagamento.
 * RF0036: Selecionar pagamento (cupom promocional, cupom de troca).
 * RN0033: Apenas um cupom promocional por compra.
 * RN0035: Consumir cupons antes do cartao.
 *
 * @param id ID do cupom
 * @param codigo codigo do cupom
 * @param valor valor do cupom
 * @param tipo tipo do cupom (TROCA ou PROMOCIONAL)
 */
public record CupomAplicadoDTO(
    Long id,
    String codigo,
    BigDecimal valor,
    TipoCupom tipo
) {}
