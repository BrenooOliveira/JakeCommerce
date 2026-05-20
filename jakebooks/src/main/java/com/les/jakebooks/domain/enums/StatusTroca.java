package com.les.jakebooks.domain.enums;

/**
 * Enum que representa os possíveis status de uma troca no sistema.
 * RF0040: Solicitação de troca.
 * RF0041: Autorização de troca.
 * RF0043: Confirmação de recebimento de troca.
 * RF0044: Conclusão de troca e geração de cupom.
 */
public enum StatusTroca {
    SOLICITADA("Solicitada"),
    AUTORIZADA("Autorizada"),
    DESCARTADA("Descartada"),
    RECEBIDA("Recebida"),
    CONCLUIDA("Concluída");

    private final String descricao;

    StatusTroca(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}
