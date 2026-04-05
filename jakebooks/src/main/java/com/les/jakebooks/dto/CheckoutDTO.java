package com.les.jakebooks.dto;

import java.io.Serializable;

/**
 * DTO para armazenar dados da sessão do checkout.
 * RF0033: Realizar compra
 * RF0035: Selecionar endereço de entrega
 */
public class CheckoutDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long enderecoEntregaId;
    private FreteDTO frete;
    // Outros campos serão adicionados nas próximas tasks (pagamento, etc)

    public CheckoutDTO() {
    }

    public Long getEnderecoEntregaId() {
        return enderecoEntregaId;
    }

    public void setEnderecoEntregaId(Long enderecoEntregaId) {
        this.enderecoEntregaId = enderecoEntregaId;
    }

    public FreteDTO getFrete() {
        return frete;
    }

    public void setFrete(FreteDTO frete) {
        this.frete = frete;
    }
}
