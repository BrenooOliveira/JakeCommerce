package com.les.jakebooks.dto;

import com.les.jakebooks.model.enums.StatusTroca;
import java.time.LocalDate;
import java.util.List;

/**
 * DTO para detalhes de uma troca (visualização admin).
 * RF0042: Visualizar trocas (admin).
 * RF0040: Solicitar troca.
 * RF0041: Autorizar troca.
 * RF0043: Confirmar recebimento de troca.
 */
public record TrocaDetalheDTO(
        Long trocaId,
        Long pedidoId,
        String nomeCliente,
        String codigoCliente,
        LocalDate dataSolicitacao,
        StatusTroca status,
        String motivo,
        List<ItemCarrinhoDTO> itensRetornados,
        String cupomGerado
) {
}
