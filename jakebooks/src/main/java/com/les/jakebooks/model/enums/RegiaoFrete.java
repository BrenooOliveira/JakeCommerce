package com.les.jakebooks.model.enums;

/**
 * Enum que representa as regiões de frete para cálculo de entrega.
 * RF0034: Calcular frete
 * RN0064: Pedido mínimo R$20 para frete grátis
 */
public enum RegiaoFrete {
    MESMA_CIDADE("Mesma Cidade"),
    MESMO_ESTADO("Mesmo Estado"),
    OUTRO_ESTADO("Outro Estado");

    private final String descricao;

    RegiaoFrete(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}
