package com.les.jakebooks.exception;

/**
 * Exceção lançada quando o carrinho está vazio ao tentar iniciar checkout.
 * Código RN: RN0031
 * Requisito: Validar carrinho não vazio antes de permitir checkout.
 */
public class CarrinhoVazioException extends NegocioException {

    private Long carrinhoId;

    public CarrinhoVazioException(String mensagem) {
        super(mensagem, "RN0031");
    }

    public CarrinhoVazioException(String mensagem, Long carrinhoId) {
        super(mensagem, "RN0031");
        this.carrinhoId = carrinhoId;
    }

    public Long getCarrinhoId() {
        return carrinhoId;
    }
}