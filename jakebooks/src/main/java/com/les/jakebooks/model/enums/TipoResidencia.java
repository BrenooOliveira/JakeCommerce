package com.les.jakebooks.model.enums;

/**
 * Enum que representa os tipos de residência para qualificação do endereço.
 * Informação complementar no cadastro do endereço do cliente.
 */
public enum TipoResidencia {
    CASA("Casa"),
    APARTAMENTO("Apartamento"),
    COMERCIO("Comércio"),
    GALPAO("Galpão"),
    OUTROS("Outros");

    private final String descricao;

    TipoResidencia(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}
