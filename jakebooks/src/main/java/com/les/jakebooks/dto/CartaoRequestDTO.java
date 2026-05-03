package com.les.jakebooks.dto;

import com.les.jakebooks.domain.enums.BandeiraCartao;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Request para cadastro de novo cartão no checkout.
 */
public record CartaoRequestDTO(
        @NotBlank(message = "Número do cartão é obrigatório")
        @Pattern(regexp = "\\d{13,19}", message = "Número do cartão deve ter entre 13 e 19 dígitos")
        String numero,

        @NotBlank(message = "Nome impresso é obrigatório")
        @Size(min = 5, max = 50, message = "Nome impresso deve ter entre 5 e 50 caracteres")
        String nomeImpresso,

        @NotNull(message = "Bandeira é obrigatória")
        BandeiraCartao bandeira,

        @NotBlank(message = "Código de segurança é obrigatório")
        @Pattern(regexp = "\\d{3,4}", message = "Código de segurança deve ter 3 ou 4 dígitos")
        String codigoSeguranca,

        @NotNull(message = "Campo preferencial é obrigatório")
        Boolean preferencial
) {
}
