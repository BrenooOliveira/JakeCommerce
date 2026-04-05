package com.les.jakebooks.dto;

import com.les.jakebooks.model.enums.StatusPagamento;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * DTO para armazenar dados da sessão do checkout.
 * RF0033: Realizar compra
 * RF0035: Selecionar endereço de entrega
 * RF0036: Selecionar pagamento (cartão, cupom promocional, cupom de troca)
 */
public class CheckoutDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    // Endereço
    private Long enderecoEntregaId;

    // Carrinho - PAY-06
    private Long carrinhoId;

    // Frete
    private FreteDTO frete;

    // Pagamento - RF0036
    private SelecaoPagamentoDTO selecaoPagamento;

    // Valores calculados
    private BigDecimal valorProdutos;
    private BigDecimal valorTotal;

    // Cupom promocional validado
    private CupomDTO cupomPromocionalValidado;

    // PAY-05: Dados do pagamento processado
    private List<CupomAplicadoDTO> cuponsAplicados;
    private Long pagamentoId;
    private StatusPagamento statusPagamento;

    public CheckoutDTO() {
    }

    // Getters e Setters - Endereço
    public Long getEnderecoEntregaId() {
        return enderecoEntregaId;
    }

    public void setEnderecoEntregaId(Long enderecoEntregaId) {
        this.enderecoEntregaId = enderecoEntregaId;
    }

    // Getters e Setters - Carrinho
    public Long getCarrinhoId() {
        return carrinhoId;
    }

    public void setCarrinhoId(Long carrinhoId) {
        this.carrinhoId = carrinhoId;
    }

    // Getters e Setters - Frete
    public FreteDTO getFrete() {
        return frete;
    }

    public void setFrete(FreteDTO frete) {
        this.frete = frete;
    }

    // Getters e Setters - Pagamento
    public SelecaoPagamentoDTO getSelecaoPagamento() {
        return selecaoPagamento;
    }

    public void setSelecaoPagamento(SelecaoPagamentoDTO selecaoPagamento) {
        this.selecaoPagamento = selecaoPagamento;
    }

    // Getters e Setters - Valores
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

    // Getters e Setters - Cupom promocional
    public CupomDTO getCupomPromocionalValidado() {
        return cupomPromocionalValidado;
    }

    public void setCupomPromocionalValidado(CupomDTO cupomPromocionalValidado) {
        this.cupomPromocionalValidado = cupomPromocionalValidado;
    }

    // Getters e Setters - PAY-05
    public List<CupomAplicadoDTO> getCuponsAplicados() {
        return cuponsAplicados;
    }

    public void setCuponsAplicados(List<CupomAplicadoDTO> cuponsAplicados) {
        this.cuponsAplicados = cuponsAplicados;
    }

    public Long getPagamentoId() {
        return pagamentoId;
    }

    public void setPagamentoId(Long pagamentoId) {
        this.pagamentoId = pagamentoId;
    }

    public StatusPagamento getStatusPagamento() {
        return statusPagamento;
    }

    public void setStatusPagamento(StatusPagamento statusPagamento) {
        this.statusPagamento = statusPagamento;
    }

    /**
     * Verifica se o checkout está pronto para finalização.
     * Requer endereço selecionado e frete calculado.
     */
    public boolean isProntoParaPagamento() {
        return enderecoEntregaId != null && frete != null;
    }

    /**
     * Verifica se o pagamento foi selecionado.
     */
    public boolean temPagamentoSelecionado() {
        return selecaoPagamento != null &&
               (selecaoPagamento.temCuponsTroca() ||
                selecaoPagamento.temCupomPromocional() ||
                selecaoPagamento.temPagamentosCartao());
    }
}
