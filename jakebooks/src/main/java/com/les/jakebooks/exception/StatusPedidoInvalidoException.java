package com.les.jakebooks.exception;

/**
 * Exceção lançada quando o status do pedido não permite a operação solicitada.
 * TASK-CHK-04: Coordenar Baixa de Estoque
 * Requisito: Pedido deve estar EM_PROCESSAMENTO para baixa de estoque.
 */
public class StatusPedidoInvalidoException extends NegocioException {

    public StatusPedidoInvalidoException(String mensagem) {
        super(mensagem, "PEDIDO_STATUS");
    }

    public StatusPedidoInvalidoException() {
        super("Status do pedido não permite esta operação", "PEDIDO_STATUS");
    }
}
