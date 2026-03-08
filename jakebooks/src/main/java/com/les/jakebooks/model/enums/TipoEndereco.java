package com.les.jakebooks.model.enums;

/**
 * Enum que representa os tipos de endereço que um cliente pode registrar.
 * RN0021: Pelo menos um endereço de cobrança é obrigatório.
 * RN0022: Pelo menos um endereço de entrega é obrigatório.
 */
public enum TipoEndereco {
    COBRANCA("Cobrança"),
    ENTREGA("Entrega"),
    AMBOS("Cobrança e Entrega");

    private final String descricao;

    TipoEndereco(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}
