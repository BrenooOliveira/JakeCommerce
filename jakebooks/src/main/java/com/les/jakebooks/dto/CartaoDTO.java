package com.les.jakebooks.dto;

import com.les.jakebooks.domain.enums.BandeiraCartao;
import jakarta.validation.constraints.*;

/**
 * DTO para cadastro/edição de cartão de cliente.
 * RF0027: Cadastrar múltiplos cartões (um preferencial)
 * RN0024: Campos obrigatórios do cartão
 * RN0025: Bandeira deve estar cadastrada
 * RN0034: Múltiplos cartões permitidos (mínimo R$ 10 por transação)
 */
public record CartaoDTO(
        /**
         * Identificador único
         */
        Long id,

        /**
         * Número do cartão (16 dígitos, será mascarado na resposta)
         * RN0024: Campos obrigatórios
         */
        @NotBlank(message = "Número do cartão é obrigatório")
        @Pattern(regexp = "\\d{16}", message = "Número de cartão deve ter 16 dígitos")
        String numero,

        /**
         * Nome impresso no cartão
         * RN0024: Campos obrigatórios
         */
        @NotBlank(message = "Nome impresso no cartão é obrigatório")
        @Size(min = 3, max = 100, message = "Nome impresso deve ter entre 3 e 100 caracteres")
        String nomeImpresso,

        /**
         * Bandeira do cartão
         * RN0025: Bandeira deve estar cadastrada
         */
        @NotNull(message = "Bandeira é obrigatória")
        BandeiraCartao bandeira,

        /**
         * Código de segurança (CVV/CVC)
         * RN0024: Campos obrigatórios
         */
        @NotBlank(message = "Código de segurança é obrigatório")
        @Pattern(regexp = "\\d{3,4}", message = "Código de segurança deve ter 3 ou 4 dígitos")
        String codigoSeguranca,

        /**
         * Indica se este é o cartão preferencial
         * RF0027: Cadastrar múltiplos cartões (um preferencial)
         */
        @NotNull(message = "Preferencial é obrigatório")
        Boolean preferencial
) {
}
