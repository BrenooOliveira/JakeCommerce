package com.les.jakebooks.exception;

/**
 * Exceção lançada quando uma troca não pode ser realizada.
 * Código RN: RN0043
 * Requisito: Apenas pedidos ENTREGUES podem solicitar troca
 */
public class TrocaNaoPermitidaException extends NegocioException {

    private String codigoPedido;
    private String statusPedidoAtual;
    private String motivoRejeicao;

    public TrocaNaoPermitidaException(String mensagem) {
        super(mensagem, "RN0043");
    }

    public TrocaNaoPermitidaException(String mensagem, String codigoPedido) {
        super(mensagem, "RN0043");
        this.codigoPedido = codigoPedido;
    }

    public TrocaNaoPermitidaException(String mensagem, String codigoPedido, String motivoRejeicao) {
        super(mensagem, "RN0043");
        this.codigoPedido = codigoPedido;
        this.motivoRejeicao = motivoRejeicao;
    }

    public String getCodigoPedido() {
        return codigoPedido;
    }

    public String getStatusPedidoAtual() {
        return statusPedidoAtual;
    }

    public String getMotivoRejeicao() {
        return motivoRejeicao;
    }
}
