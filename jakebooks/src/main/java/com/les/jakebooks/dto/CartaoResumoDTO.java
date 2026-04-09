package com.les.jakebooks.dto;

import com.les.jakebooks.domain.enums.BandeiraCartao;

/**
 * DTO resumido de cartão para exibição no checkout.
 * Não contém dados sensíveis completos.
 * RF0036: Selecionar pagamento (cartão).
 * RN0034: Múltiplos cartões permitidos (mínimo R$10 por cartão).
 */
public record CartaoResumoDTO(
        /**
         * Identificador único do cartão.
         */
        Long id,

        /**
         * Últimos 4 dígitos do cartão (mascarado).
         */
        String numeroMascarado,

        /**
         * Nome impresso no cartão.
         */
        String nomeImpresso,

        /**
         * Bandeira do cartão.
         */
        BandeiraCartao bandeira,

        /**
         * Indica se este é o cartão preferencial.
         */
        Boolean preferencial
) {
    /**
     * Retorna descrição formatada do cartão.
     * Ex: "VISA **** 1234 (Preferencial)"
     */
    public String getDescricaoFormatada() {
        String desc = bandeira.name() + " " + numeroMascarado;
        if (Boolean.TRUE.equals(preferencial)) {
            desc += " (Preferencial)";
        }
        return desc;
    }
}
