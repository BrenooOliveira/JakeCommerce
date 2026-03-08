package com.les.jakebooks.model.enums;

/**
 * Enum que representa os tipos de cupom disponíveis no sistema.
 * RN0033: Apenas um cupom promocional por compra.
 * RN0036: Gerar cupom de troca para valor excedente.
 * RF0044: Cupom de troca é gerado automaticamente ao concluir uma troca.
 */
public enum TipoCupom {
    PROMOCIONAL("Promocional"),
    TROCA("Troca");

    private final String descricao;

    TipoCupom(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}
