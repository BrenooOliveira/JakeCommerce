package com.les.jakebooks.domain.enums;

/**
 * Enum que representa o status de um pagamento com cartão.
 * Usado para rastrear o resultado de cada transação individual com cartão.
 * PAY-05: Status individual de cada cartão na simulação de gateway.
 */
public enum StatusPagamentoCartao {

    /**
     * Pagamento aprovado pelo gateway simulado
     */
    APROVADO,

    /**
     * Pagamento reprovado pelo gateway simulado
     */
    REPROVADO
}