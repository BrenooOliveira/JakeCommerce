package com.les.jakebooks.model.enums;

/**
 * Enum que representa os possíveis status de um carrinho de compras.
 * RN0044: Carrinho é bloqueado com aviso 5 minutos antes da expiração.
 */
public enum StatusCarrinho {
    ABERTO("Aberto"),
    EXPIRADO("Expirado"),
    FINALIZADO("Finalizado");

    private final String descricao;

    StatusCarrinho(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}
