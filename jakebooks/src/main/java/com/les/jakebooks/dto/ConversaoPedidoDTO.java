package com.les.jakebooks.dto;

import com.les.jakebooks.domain.Endereco;
import com.les.jakebooks.domain.Pagamento;

import java.math.BigDecimal;

/**
 * DTO para conversão de carrinho em pedido.
 * TASK-CHK-03: Converter Carrinho em Pedido
 * RF0037: Finalizar compra (status inicial: EM_PROCESSAMENTO)
 *
 * Agrupa dados necessários para criar pedido após pagamento aprovado:
 * - ID do carrinho finalizado
 * - Endereço de entrega selecionado
 * - Pagamento processado e aprovado
 * - Valor do frete calculado
 */
public class ConversaoPedidoDTO {

    private Long carrinhoId;
    private Endereco enderecoEntrega;
    private Pagamento pagamento;
    private BigDecimal valorFrete;

    // Construtores
    public ConversaoPedidoDTO() {
    }

    public ConversaoPedidoDTO(Long carrinhoId, Endereco enderecoEntrega,
                             Pagamento pagamento, BigDecimal valorFrete) {
        this.carrinhoId = carrinhoId;
        this.enderecoEntrega = enderecoEntrega;
        this.pagamento = pagamento;
        this.valorFrete = valorFrete;
    }

    // Getters e Setters
    public Long getCarrinhoId() {
        return carrinhoId;
    }

    public void setCarrinhoId(Long carrinhoId) {
        this.carrinhoId = carrinhoId;
    }

    public Endereco getEnderecoEntrega() {
        return enderecoEntrega;
    }

    public void setEnderecoEntrega(Endereco enderecoEntrega) {
        this.enderecoEntrega = enderecoEntrega;
    }

    public Pagamento getPagamento() {
        return pagamento;
    }

    public void setPagamento(Pagamento pagamento) {
        this.pagamento = pagamento;
    }

    public BigDecimal getValorFrete() {
        return valorFrete;
    }

    public void setValorFrete(BigDecimal valorFrete) {
        this.valorFrete = valorFrete;
    }
}
