package com.les.jakebooks.exception;

/**
 * Exceção lançada quando o pagamento é reprovado.
 * Código RN: RN0037, RN0038, RN0065
 * Requisito: Validar pagamento. Status pagamento: APROVADA ou REPROVADA.
 *            3 pagamentos REPROVADOS consecutivos bloqueiam carrinho
 */
public class PagamentoReprovadoException extends NegocioException {

    private String codigoPedido;
    private Integer tentativasConsecutivas;

    public PagamentoReprovadoException(String mensagem) {
        super(mensagem, "RN0037");
    }

    public PagamentoReprovadoException(String mensagem, String codigoPedido) {
        super(mensagem, "RN0037");
        this.codigoPedido = codigoPedido;
    }

    public PagamentoReprovadoException(String mensagem, String codigoPedido, Integer tentativasConsecutivas) {
        super(mensagem, "RN0065");
        this.codigoPedido = codigoPedido;
        this.tentativasConsecutivas = tentativasConsecutivas;
    }

    public String getCodigoPedido() {
        return codigoPedido;
    }

    public Integer getTentativasConsecutivas() {
        return tentativasConsecutivas;
    }
}
