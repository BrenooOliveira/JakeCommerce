package com.les.jakebooks.dto;

import java.math.BigDecimal;

/**
 * DTO para retornar resultado do calculo de pagamento com cupons.
 * RN0036: Gerar cupom para excedente.
 *
 * @param valorTotal valor total do pedido
 * @param valorPagoComCupons valor pago com cupons de troca e promocional
 * @param valorRestante valor restante a pagar com cartao (0 se cupons cobriram tudo)
 * @param cupomExcedenteGerado cupom gerado se houve excedente (pode ser null)
 * @param pagamentoCompleto true se cupons cobriram o valor total
 */
public record ResultadoPagamentoDTO(
    BigDecimal valorTotal,
    BigDecimal valorPagoComCupons,
    BigDecimal valorRestante,
    CupomDTO cupomExcedenteGerado,
    boolean pagamentoCompleto
) {
    /**
     * Verifica se foi gerado cupom de excedente.
     */
    public boolean temExcedente() {
        return cupomExcedenteGerado != null;
    }

    /**
     * Calcula o valor do excedente (se houver).
     */
    public BigDecimal getValorExcedente() {
        return cupomExcedenteGerado != null ? cupomExcedenteGerado.valor() : BigDecimal.ZERO;
    }
}
