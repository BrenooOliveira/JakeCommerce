package com.les.jakebooks.dto;

import java.math.BigDecimal;
import java.util.Map;

/**
 * DTO para formulário de pagamento (PAY-05).
 * Recebe os dados do formulário de processamento de pagamento.
 * RN0034: Múltiplos cartões permitidos (mínimo R$10 por cartão).
 */
public class PagamentoFormDTO {

    private Map<Long, BigDecimal> cartoesValores; // cartaoId -> valor

    // Construtores
    public PagamentoFormDTO() {
    }

    public PagamentoFormDTO(Map<Long, BigDecimal> cartoesValores) {
        this.cartoesValores = cartoesValores;
    }

    // Getters e Setters
    public Map<Long, BigDecimal> getCartoesValores() {
        return cartoesValores;
    }

    public void setCartoesValores(Map<Long, BigDecimal> cartoesValores) {
        this.cartoesValores = cartoesValores;
    }
}