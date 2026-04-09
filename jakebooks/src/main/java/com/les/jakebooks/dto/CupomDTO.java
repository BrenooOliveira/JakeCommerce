package com.les.jakebooks.dto;

import com.les.jakebooks.domain.enums.TipoCupom;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO para representação de cupom no fluxo de pagamento.
 * RF0036: Selecionar pagamento (cupom promocional, cupom de troca).
 * RN0033: Apenas um cupom promocional por compra.
 * RN0035: Consumir cupons antes do cartão.
 * RN0036: Gerar cupom para excedente.
 */
public record CupomDTO(
        /**
         * Identificador único do cupom.
         */
        Long id,

        /**
         * Código único do cupom (ex: PROMO10, TROCA-ABC123).
         */
        String codigo,

        /**
         * Valor do cupom em reais.
         */
        BigDecimal valor,

        /**
         * Tipo do cupom (PROMOCIONAL ou TROCA).
         * RN0033: Apenas um cupom promocional por compra.
         */
        TipoCupom tipo,

        /**
         * Data de validade do cupom (pode ser nula para cupons sem validade).
         */
        LocalDate dataValidade,

        /**
         * Indica se o cupom está ativo.
         */
        boolean ativo
) {
    /**
     * Verifica se o cupom é promocional.
     */
    public boolean isPromocional() {
        return TipoCupom.PROMOCIONAL.equals(tipo);
    }

    /**
     * Verifica se o cupom é de troca.
     */
    public boolean isTroca() {
        return TipoCupom.TROCA.equals(tipo);
    }

    /**
     * Verifica se o cupom está válido (ativo e não expirado).
     */
    public boolean isValido() {
        if (!ativo) {
            return false;
        }
        if (dataValidade != null && LocalDate.now().isAfter(dataValidade)) {
            return false;
        }
        return true;
    }
}
