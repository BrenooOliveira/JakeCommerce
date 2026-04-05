package com.les.jakebooks.exception;

/**
 * Exceção lançada quando há tentativa de transição inválida de status de pedido.
 * Utilizada na TASK-SHP-04 para validar despacho de pedidos.
 * RN0039: Status transporte: EM_TRANSPORTE (transição válida apenas de EM_PROCESSAMENTO).
 */
public class TransicaoStatusInvalidaException extends ValidacaoNegocioException {

    public TransicaoStatusInvalidaException(String mensagem) {
        super(mensagem);
    }
}