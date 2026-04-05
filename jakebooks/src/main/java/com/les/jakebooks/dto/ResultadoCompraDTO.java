package com.les.jakebooks.dto;

/**
 * DTO para resultado da finalização de compra.
 * TASK-CHK-03: Converter Carrinho em Pedido
 * RF0037: Finalizar compra
 *
 * Retorna informações sobre o sucesso ou falha da operação:
 * - ID do pedido criado
 * - Status da operação (SUCESSO/ERRO)
 * - Mensagem descritiva
 */
public class ResultadoCompraDTO {

    private Long pedidoId;
    private StatusResultado status;
    private String mensagem;

    // Enum para status do resultado
    public enum StatusResultado {
        SUCESSO,
        ERRO
    }

    // Construtores
    public ResultadoCompraDTO() {
    }

    public ResultadoCompraDTO(Long pedidoId, StatusResultado status, String mensagem) {
        this.pedidoId = pedidoId;
        this.status = status;
        this.mensagem = mensagem;
    }

    // Factory methods para facilitar criação
    public static ResultadoCompraDTO sucesso(Long pedidoId, String mensagem) {
        return new ResultadoCompraDTO(pedidoId, StatusResultado.SUCESSO, mensagem);
    }

    public static ResultadoCompraDTO erro(String mensagem) {
        return new ResultadoCompraDTO(null, StatusResultado.ERRO, mensagem);
    }

    // Getters e Setters
    public Long getPedidoId() {
        return pedidoId;
    }

    public void setPedidoId(Long pedidoId) {
        this.pedidoId = pedidoId;
    }

    public StatusResultado getStatus() {
        return status;
    }

    public void setStatus(StatusResultado status) {
        this.status = status;
    }

    public String getMensagem() {
        return mensagem;
    }

    public void setMensagem(String mensagem) {
        this.mensagem = mensagem;
    }

    // Método helper
    public boolean isSucesso() {
        return status == StatusResultado.SUCESSO;
    }
}
