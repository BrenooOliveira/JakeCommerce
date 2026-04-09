package com.les.jakebooks.domain.enums;

/**
 * Enum que representa os possíveis status de um pedido no sistema.
 * RF0037: Status inicial de um pedido é EM_PROCESSAMENTO.
 * RF0038: Alterado para EM_TRANSPORTE quando despachado.
 * RF0039: Alterado para ENTREGUE quando confirmada a entrega.
 * RF0040: Alterado para EM_TROCA quando solicitada troca.
 * RF0042: Alterado para TROCADO quando troca é concluída.
 */
public enum StatusPedido {
    EM_PROCESSAMENTO("Em Processamento", "bg-warning text-dark"),
    EM_TRANSPORTE("Em Transporte", "bg-info"),
    ENTREGUE("Entregue", "bg-success"),
    EM_TROCA("Em Troca", "bg-secondary"),
    TROCADO("Trocado", "bg-primary");

    private final String descricao;
    private final String corBadge;

    StatusPedido(String descricao, String corBadge) {
        this.descricao = descricao;
        this.corBadge = corBadge;
    }

    public String getDescricao() {
        return descricao;
    }

    public String getCorBadge() {
        return corBadge;
    }
}
