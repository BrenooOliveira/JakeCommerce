package com.les.jakebooks.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * DTO para receber a seleção de pagamento do cliente.
 * RF0036: Selecionar pagamento (cartão, cupom promocional, cupom de troca).
 * RN0033: Apenas um cupom promocional por compra.
 * RN0034: Múltiplos cartões permitidos (mínimo R$10 por cartão).
 * RN0035: Consumir cupons antes do cartão.
 */
public class SelecaoPagamentoDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * IDs dos cupons de troca selecionados.
     * RN0035: Cupons de troca podem ser múltiplos.
     */
    private List<Long> cuponsIds;

    /**
     * Código do cupom promocional (se houver).
     * RN0033: Apenas um cupom promocional por compra.
     */
    @Size(max = 50, message = "Código do cupom muito longo")
    private String codigoCupomPromocional;

    /**
     * Lista de pagamentos com cartão.
     * RN0034: Múltiplos cartões permitidos.
     */
    private List<PagamentoCartaoDadosDTO> pagamentosCartao;

    // Construtores
    public SelecaoPagamentoDTO() {
    }

    public SelecaoPagamentoDTO(List<Long> cuponsIds, String codigoCupomPromocional,
                               List<PagamentoCartaoDadosDTO> pagamentosCartao) {
        this.cuponsIds = cuponsIds;
        this.codigoCupomPromocional = codigoCupomPromocional;
        this.pagamentosCartao = pagamentosCartao;
    }

    // Getters e Setters
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

    public List<PagamentoCartaoDadosDTO> getPagamentosCartao() {
        return pagamentosCartao;
    }

    public void setPagamentosCartao(List<PagamentoCartaoDadosDTO> pagamentosCartao) {
        this.pagamentosCartao = pagamentosCartao;
    }

    /**
     * Verifica se há cupons de troca selecionados.
     */
    public boolean temCuponsTroca() {
        return cuponsIds != null && !cuponsIds.isEmpty();
    }

    /**
     * Verifica se há cupom promocional informado.
     */
    public boolean temCupomPromocional() {
        return codigoCupomPromocional != null && !codigoCupomPromocional.isBlank();
    }

    /**
     * Verifica se há pagamentos com cartão.
     */
    public boolean temPagamentosCartao() {
        return pagamentosCartao != null && !pagamentosCartao.isEmpty();
    }

    /**
     * Calcula o valor total informado nos cartões.
     */
    public BigDecimal getValorTotalCartoes() {
        if (pagamentosCartao == null || pagamentosCartao.isEmpty()) {
            return BigDecimal.ZERO;
        }
        return pagamentosCartao.stream()
                .map(PagamentoCartaoDadosDTO::valor)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
