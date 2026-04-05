package com.les.jakebooks.exception;

/**
 * Exceção lançada quando o carrinho está bloqueado por excesso de pagamentos reprovados.
 *
 * Código RN: RN0065
 * Requisito: 3 pagamentos REPROVADOS consecutivos bloqueiam carrinho.
 */
public class CarrinhoBloqueadoPagamentoException extends NegocioException {

    private static final int LIMITE_TENTATIVAS = 3;

    private Long clienteId;
    private int tentativasReprovadas;

    public CarrinhoBloqueadoPagamentoException(Long clienteId) {
        super("Carrinho bloqueado: " + LIMITE_TENTATIVAS + " tentativas de pagamento reprovadas consecutivas. " +
              "Entre em contato com o suporte.", "RN0065");
        this.clienteId = clienteId;
        this.tentativasReprovadas = LIMITE_TENTATIVAS;
    }

    public CarrinhoBloqueadoPagamentoException(Long clienteId, int tentativasReprovadas) {
        super("Carrinho bloqueado: " + tentativasReprovadas + " tentativas de pagamento reprovadas consecutivas. " +
              "Entre em contato com o suporte.", "RN0065");
        this.clienteId = clienteId;
        this.tentativasReprovadas = tentativasReprovadas;
    }

    public Long getClienteId() {
        return clienteId;
    }

    public int getTentativasReprovadas() {
        return tentativasReprovadas;
    }

    public static int getLimiteTentativas() {
        return LIMITE_TENTATIVAS;
    }
}
