package com.les.jakebooks.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * DTO para processar pagamento completo (PAY-05).
 * Contém todos os dados necessários para processar cupons e cartões.
 * RN0035: Consumir cupons antes do cartão.
 * RN0037: Validar pagamento antes de processar.
 */
public class ProcessarPagamentoDTO {

    private BigDecimal valorTotal;
    private List<CupomAplicadoDTO> cuponsAplicados;
    private Map<Long, BigDecimal> cartoesValores; // cartaoId -> valor

    // Construtores
    public ProcessarPagamentoDTO() {
    }

    public ProcessarPagamentoDTO(BigDecimal valorTotal, List<CupomAplicadoDTO> cuponsAplicados, Map<Long, BigDecimal> cartoesValores) {
        this.valorTotal = valorTotal;
        this.cuponsAplicados = cuponsAplicados;
        this.cartoesValores = cartoesValores;
    }

    // Builder pattern para facilitar construção
    public static ProcessarPagamentoDTOBuilder builder() {
        return new ProcessarPagamentoDTOBuilder();
    }

    public static class ProcessarPagamentoDTOBuilder {
        private BigDecimal valorTotal;
        private List<CupomAplicadoDTO> cuponsAplicados;
        private Map<Long, BigDecimal> cartoesValores;

        public ProcessarPagamentoDTOBuilder valorTotal(BigDecimal valorTotal) {
            this.valorTotal = valorTotal;
            return this;
        }

        public ProcessarPagamentoDTOBuilder cuponsAplicados(List<CupomAplicadoDTO> cuponsAplicados) {
            this.cuponsAplicados = cuponsAplicados;
            return this;
        }

        public ProcessarPagamentoDTOBuilder cartoesValores(Map<Long, BigDecimal> cartoesValores) {
            this.cartoesValores = cartoesValores;
            return this;
        }

        public ProcessarPagamentoDTO build() {
            return new ProcessarPagamentoDTO(valorTotal, cuponsAplicados, cartoesValores);
        }
    }

    // Getters e Setters
    public BigDecimal getValorTotal() {
        return valorTotal;
    }

    public void setValorTotal(BigDecimal valorTotal) {
        this.valorTotal = valorTotal;
    }

    public List<CupomAplicadoDTO> getCuponsAplicados() {
        return cuponsAplicados;
    }

    public void setCuponsAplicados(List<CupomAplicadoDTO> cuponsAplicados) {
        this.cuponsAplicados = cuponsAplicados;
    }

    public Map<Long, BigDecimal> getCartoesValores() {
        return cartoesValores;
    }

    public void setCartoesValores(Map<Long, BigDecimal> cartoesValores) {
        this.cartoesValores = cartoesValores;
    }
}