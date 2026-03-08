package com.les.jakebooks.exception;

/**
 * Exceção lançada quando o quantidade do pedido ou carrinho viola os limites.
 * 
 * Código RN: RN0063, RN0064, RN0065
 * Requisito: Máximo 10 unidades do mesmo livro por pedido
 *            Pedido mínimo 20 sem frete
 *            3 pagamentos REPROVADOS consecutivos bloqueiam carrinho
 */
public class LimitePedidoException extends NegocioException {

    private Integer quantidadeAtual;
    private Integer limiteMinimo;
    private Integer limiteMaximo;
    private Double valorAtual;
    private Double valorMinimo;

    public LimitePedidoException(String mensagem) {
        super(mensagem, "RN0063");
    }

    public LimitePedidoException(String mensagem, Integer quantidadeAtual, Integer limiteMaximo) {
        super(mensagem, "RN0063");
        this.quantidadeAtual = quantidadeAtual;
        this.limiteMaximo = limiteMaximo;
    }

    public LimitePedidoException(String mensagem, Double valorAtual, Double valorMinimo, String codigoRN) {
        super(mensagem, codigoRN);
        this.valorAtual = valorAtual;
        this.valorMinimo = valorMinimo;
    }

    public LimitePedidoException(String mensagem, Integer limiteMinimo, Integer limiteMaximo, Throwable causa) {
        super(mensagem, "RN0063", causa);
        this.limiteMinimo = limiteMinimo;
        this.limiteMaximo = limiteMaximo;
    }

    public Integer getQuantidadeAtual() {
        return quantidadeAtual;
    }

    public Integer getLimiteMinimo() {
        return limiteMinimo;
    }

    public Integer getLimiteMaximo() {
        return limiteMaximo;
    }

    public Double getValorAtual() {
        return valorAtual;
    }

    public Double getValorMinimo() {
        return valorMinimo;
    }
}
