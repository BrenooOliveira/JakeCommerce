package com.les.jakebooks.dto;

import com.les.jakebooks.domain.Endereco;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * DTO para dados completos da transação de checkout.
 * TASK-CHK-05: Gerenciar Estado da Transacao
 *
 * Consolida todos os dados necessários para executar
 * o checkout completo de forma atomica.
 */
public class CheckoutTransacaoDTO {

    /**
     * ID do carrinho sendo finalizado
     */
    private Long carrinhoId;

    /**
     * Endereco de entrega selecionado
     */
    private Endereco enderecoEntrega;

    /**
     * Valor do frete calculado
     */
    private BigDecimal valorFrete;

    /**
     * Valor total dos produtos
     */
    private BigDecimal valorProdutos;

    /**
     * Valor total (produtos + frete)
     */
    private BigDecimal valorTotal;

    /**
     * IDs dos cupons de troca selecionados
     */
    private List<Long> cuponsIds;

    /**
     * Codigo do cupom promocional (opcional)
     */
    private String codigoCupomPromocional;

    /**
     * Map de cartaoId para valor a ser cobrado
     * RN0034: Minimo R$10 por cartao
     */
    private Map<Long, BigDecimal> cartoesValores;

    /**
     * ID do cliente que esta fazendo a compra
     */
    private Long clienteId;

    // Construtores
    public CheckoutTransacaoDTO() {
    }

    public CheckoutTransacaoDTO(Long carrinhoId, Endereco enderecoEntrega, BigDecimal valorFrete,
                                BigDecimal valorProdutos, BigDecimal valorTotal, List<Long> cuponsIds,
                                String codigoCupomPromocional, Map<Long, BigDecimal> cartoesValores, Long clienteId) {
        this.carrinhoId = carrinhoId;
        this.enderecoEntrega = enderecoEntrega;
        this.valorFrete = valorFrete;
        this.valorProdutos = valorProdutos;
        this.valorTotal = valorTotal;
        this.cuponsIds = cuponsIds;
        this.codigoCupomPromocional = codigoCupomPromocional;
        this.cartoesValores = cartoesValores;
        this.clienteId = clienteId;
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

    public BigDecimal getValorFrete() {
        return valorFrete;
    }

    public void setValorFrete(BigDecimal valorFrete) {
        this.valorFrete = valorFrete;
    }

    public BigDecimal getValorProdutos() {
        return valorProdutos;
    }

    public void setValorProdutos(BigDecimal valorProdutos) {
        this.valorProdutos = valorProdutos;
    }

    public BigDecimal getValorTotal() {
        return valorTotal;
    }

    public void setValorTotal(BigDecimal valorTotal) {
        this.valorTotal = valorTotal;
    }

    public List<Long> getCuponsIds() {
        return cuponsIds;
    }

    public void setCuponsIds(List<Long> cuponsIds) {
        this.cuponsIds = cuponsIds;
    }

    public String getCodigoCupomPromocional() {
        return codigoCupomPromocional;
    }

    public void setCodigoCupomPromocional(String codigoCupomPromocional) {
        this.codigoCupomPromocional = codigoCupomPromocional;
    }

    public Map<Long, BigDecimal> getCartoesValores() {
        return cartoesValores;
    }

    public void setCartoesValores(Map<Long, BigDecimal> cartoesValores) {
        this.cartoesValores = cartoesValores;
    }

    public Long getClienteId() {
        return clienteId;
    }

    public void setClienteId(Long clienteId) {
        this.clienteId = clienteId;
    }
}
