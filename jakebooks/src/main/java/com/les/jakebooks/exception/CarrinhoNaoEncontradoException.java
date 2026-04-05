package com.les.jakebooks.exception;

/**
 * Exceção lançada quando o carrinho especificado não é encontrado.
 * TASK-PAY-06: Controlar tentativas reprovadas.
 */
public class CarrinhoNaoEncontradoException extends ValidacaoNegocioException {

    public CarrinhoNaoEncontradoException(Long carrinhoId) {
        super("Carrinho com ID " + carrinhoId + " não encontrado.");
    }
}