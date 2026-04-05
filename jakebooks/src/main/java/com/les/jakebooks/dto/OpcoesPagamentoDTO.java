package com.les.jakebooks.dto;

import java.math.BigDecimal;
import java.util.List;

/**
 * DTO que representa as opções de pagamento disponíveis para o cliente.
 * RF0036: Selecionar pagamento (cartão, cupom promocional, cupom de troca).
 * RN0033: Apenas um cupom promocional por compra.
 * RN0034: Múltiplos cartões permitidos (mínimo R$10 por cartão).
 * RN0035: Consumir cupons antes do cartão.
 */
public record OpcoesPagamentoDTO(
        /**
         * Valor total a pagar (produtos + frete).
         */
        BigDecimal valorTotal,

        /**
         * Valor dos produtos no carrinho.
         */
        BigDecimal valorProdutos,

        /**
         * Valor do frete.
         */
        BigDecimal valorFrete,

        /**
         * Lista de cupons de troca disponíveis do cliente.
         * RN0035: Cupons devem ser consumidos antes dos cartões.
         */
        List<CupomDTO> cuponsTroca,

        /**
         * Saldo total em cupons de troca disponíveis.
         */
        BigDecimal saldoCuponsTroca,

        /**
         * Lista de cartões cadastrados do cliente.
         * RN0034: Múltiplos cartões permitidos.
         */
        List<CartaoResumoDTO> cartoes,

        /**
         * Cupom promocional aplicado (se houver).
         * RN0033: Apenas um cupom promocional por compra.
         */
        CupomDTO cupomPromocional,

        /**
         * Valor restante após aplicação de cupons.
         * Se <= 0, não é necessário usar cartão.
         */
        BigDecimal valorRestante
) {
    /**
     * Verifica se o cliente tem saldo suficiente em cupons.
     * Se true, não precisa usar cartão.
     */
    public boolean temSaldoSuficienteEmCupons() {
        return saldoCuponsTroca.compareTo(valorTotal) >= 0;
    }

    /**
     * Verifica se há valor excedente (cupons > valor total).
     * RN0036: Gerar cupom para excedente.
     */
    public boolean temValorExcedente() {
        return valorRestante != null && valorRestante.compareTo(BigDecimal.ZERO) < 0;
    }

    /**
     * Retorna o valor excedente (se houver).
     */
    public BigDecimal getValorExcedente() {
        if (temValorExcedente()) {
            return valorRestante.negate();
        }
        return BigDecimal.ZERO;
    }

    /**
     * Verifica se precisa de cartão para completar o pagamento.
     */
    public boolean precisaCartao() {
        return valorRestante != null && valorRestante.compareTo(BigDecimal.ZERO) > 0;
    }
}
