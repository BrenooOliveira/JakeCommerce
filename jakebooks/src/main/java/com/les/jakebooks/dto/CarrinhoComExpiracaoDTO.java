package com.les.jakebooks.dto;

import com.les.jakebooks.model.enums.StatusCarrinho;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * DTO para carrinho com informações de expiração.
 * RN0044: Carrinho é bloqueado com aviso 5 minutos antes da expiração.
 * RNF venda: Exibir itens removidos do carrinho por expiração.
 */
public record CarrinhoComExpiracaoDTO(
        Long id,
        String codigoCliente,
        StatusCarrinho status,
        LocalDate dataCriacao,
        LocalDate dataExpiracao,
        Boolean proxinoDeExpirar,  // true se faltam menos de 5 minutos
        Integer minutosFaltando,    // minutos até expiração
        List<ItemCarrinhoDTO> itens,
        List<ItemCarrinhoDTO> itensRemovidos,  // itens removidos por expiração
        BigDecimal valorTotal,
        String mensagemAviso        // mensagem de aviso se próximo de expirar
) {
}
