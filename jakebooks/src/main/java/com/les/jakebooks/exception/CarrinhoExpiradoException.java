package com.les.jakebooks.exception;

/**
 * Exceção lançada quando o carrinho expira antes da finalização.
 * Código RN: RN0044, RN0045
 * Requisito: Bloqueio carrinho com aviso 5 minutos antes. Remover item desbloqueado.
 */
public class CarrinhoExpiradoException extends NegocioException {

    private String carrinhoId;

    public CarrinhoExpiradoException(String mensagem) {
        super(mensagem, "RN0044");
    }

    public CarrinhoExpiradoException(String mensagem, String carrinhoId) {
        super(mensagem, "RN0044");
        this.carrinhoId = carrinhoId;
    }

    public String getCarrinhoId() {
        return carrinhoId;
    }
}
