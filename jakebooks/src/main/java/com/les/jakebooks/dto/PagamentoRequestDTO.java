package com.les.jakebooks.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.List;

/**
 * Request para processamento de pagamento do checkout.
 */
public record PagamentoRequestDTO(
        @NotNull(message = "Pedido é obrigatório")
        @Positive(message = "Pedido deve ser um ID válido")
        Long pedidoId,

        @Positive(message = "Cupom deve ser um ID válido")
        Long cupomId,

        @NotEmpty(message = "Informe ao menos um cartão")
        @Valid
        List<PagamentoCartaoDadosDTO> cartoes
) {
}
