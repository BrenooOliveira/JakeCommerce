package com.les.jakebooks.exception;

/**
 * Exceção lançada quando um cliente não possui nenhum endereço de entrega cadastrado.
 * RN0022: Pelo menos um endereço de entrega é obrigatório.
 * RF0035: Selecionar endereço de entrega.
 */
public class EnderecoEntregaNaoEncontradoException extends ValidacaoNegocioException {

    public EnderecoEntregaNaoEncontradoException(String mensagem) {
        super(mensagem);
    }
}
