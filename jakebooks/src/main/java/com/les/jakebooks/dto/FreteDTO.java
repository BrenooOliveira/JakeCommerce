package com.les.jakebooks.dto;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * DTO para informações de frete.
 * RF0034: Calcular frete
 * RN0064: Pedido mínimo R$20 sem frete
 */
public class FreteDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private BigDecimal valor;
    private String descricao;
    private Integer prazoEstimadoDias;
    private Boolean gratis;

    public FreteDTO() {
    }

    public FreteDTO(BigDecimal valor, String descricao, Integer prazoEstimadoDias, Boolean gratis) {
        this.valor = valor;
        this.descricao = descricao;
        this.prazoEstimadoDias = prazoEstimadoDias;
        this.gratis = gratis;
    }

    public BigDecimal getValor() {
        return valor;
    }

    public void setValor(BigDecimal valor) {
        this.valor = valor;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public Integer getPrazoEstimadoDias() {
        return prazoEstimadoDias;
    }

    public void setPrazoEstimadoDias(Integer prazoEstimadoDias) {
        this.prazoEstimadoDias = prazoEstimadoDias;
    }

    public Boolean getGratis() {
        return gratis;
    }

    public void setGratis(Boolean gratis) {
        this.gratis = gratis;
    }

    /**
     * Retorna descrição completa do frete incluindo valor e prazo.
     * Formato: "Entrega Local - R$ 5,00 - Entrega em até 3 dias úteis"
     * Ou: "Frete Grátis - Entrega em até 7 dias úteis" (quando grátis)
     *
     * @return descrição formatada do frete
     */
    public String getDescricaoCompleta() {
        if (gratis) {
            return String.format("Frete Grátis - Entrega em até %d dias úteis", prazoEstimadoDias);
        }
        return String.format("%s - R$ %.2f - Entrega em até %d dias úteis",
                descricao, valor, prazoEstimadoDias);
    }
}
