package com.les.jakebooks.exception;

/**
 * Exceção lançada quando há tentativa de baixa de estoque sem pagamento aprovado.
 * TASK-CHK-04: Coordenar Baixa de Estoque
 * Código RN: RN0028
 * Requisito: Baixa estoque apenas após pagamento APROVADO.
 */
public class PagamentoNaoAprovadoException extends NegocioException {

    public PagamentoNaoAprovadoException(String mensagem) {
        super(mensagem, "RN0028");
    }

    public PagamentoNaoAprovadoException() {
        super("Baixa de estoque permitida apenas para pagamentos aprovados", "RN0028");
    }
}
