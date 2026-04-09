package com.les.jakebooks.domain.enums;

/**
 * Enum que representa os possíveis status de um carrinho de compras.
 * RN0044: Carrinho é bloqueado com aviso 5 minutos antes da expiração.
 */
public enum StatusCarrinho {
    ABERTO("Aberto"),
    EXPIRADO("Expirado"),
    FINALIZADO("Finalizado"),
    BLOQUEADO("Bloqueado");

    private final String descricao;

    StatusCarrinho(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}
