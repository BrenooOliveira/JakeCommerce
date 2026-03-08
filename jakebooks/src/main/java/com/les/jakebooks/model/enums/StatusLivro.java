package com.les.jakebooks.model.enums;

/**
 * Enum que representa os possíveis status de um livro no sistema.
 * RN0016: Um livro pode ser inativado manualmente ou automaticamente.
 */
public enum StatusLivro {
    ATIVO("Ativo"),
    INATIVO("Inativo");

    private final String descricao;

    StatusLivro(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}
