package com.les.jakebooks.exception;

/**
 * Excecao lancada quando nenhum cartao e selecionado para pagamento.
 * RF0036: Selecionar pagamento
 * RN0034: Multiplos cartoes permitidos
 */
public class CartaoNaoSelecionadoException extends ValidacaoNegocioException {

    public CartaoNaoSelecionadoException() {
        super("Selecione pelo menos um cartao para pagamento");
    }

    public CartaoNaoSelecionadoException(String mensagem) {
        super(mensagem);
    }
}
