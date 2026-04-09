package com.les.jakebooks.domain.enums;

/**
 * Enum que representa os possíveis status de um pagamento no sistema.
 * RN0038: Status do pagamento é APROVADA ou REPROVADA.
 * RN0037: Validar pagamento antes de processar.
 */
public enum StatusPagamento {
    PENDENTE("Pendente"),
    APROVADA("Aprovada"),
    REPROVADA("Reprovada");

    private final String descricao;

    StatusPagamento(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}
