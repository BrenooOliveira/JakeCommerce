package com.les.jakebooks.model.enums;

/**
 * Enum que representa os possíveis status de um pedido no sistema.
 * RF0037: Status inicial de um pedido é EM_PROCESSAMENTO.
 * RF0038: Alterado para EM_TRANSPORTE quando despachado.
 * RF0039: Alterado para ENTREGUE quando confirmada a entrega.
 * RF0040: Alterado para EM_TROCA quando solicitada troca.
 * RF0042: Alterado para TROCADO quando troca é concluída.
 */
public enum StatusPedido {
    EM_PROCESSAMENTO("Em Processamento"),
    EM_TRANSPORTE("Em Transporte"),
    ENTREGUE("Entregue"),
    EM_TROCA("Em Troca"),
    TROCADO("Trocado");

    private final String descricao;

    StatusPedido(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}
