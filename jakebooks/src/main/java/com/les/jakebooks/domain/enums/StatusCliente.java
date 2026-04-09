package com.les.jakebooks.domain.enums;

/**
 * Enum que representa os possíveis status de um cliente no sistema.
 * RN0028: Cliente pode ser bloqueado após 3 pagamentos reprovados consecutivos.
 */
public enum StatusCliente {
    ATIVO("Ativo"),
    INATIVO("Inativo"),
    BLOQUEADO("Bloqueado");

    private final String descricao;

    StatusCliente(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}
