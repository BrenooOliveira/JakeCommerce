package com.les.jakebooks.model.enums;

/**
 * Enum que representa as bandeiras de cartão de crédito aceitas pelo sistema.
 * RN0025: A bandeira deve estar cadastrada no sistema.
 * RN0034: Múltiplos cartões permitidos (mínimo R$ 10 por transação).
 */
public enum BandeiraCartao {
    VISA("Visa"),
    MASTERCARD("MasterCard"),
    ELO("Elo"),
    AMEX("American Express");

    private final String descricao;

    BandeiraCartao(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}
