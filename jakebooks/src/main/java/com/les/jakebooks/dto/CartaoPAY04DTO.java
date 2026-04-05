package com.les.jakebooks.dto;

/**
 * DTO de cartão para exibição no checkout PAY-04.
 * Conforme especificação TASK-PAY-04.
 */
public record CartaoPAY04DTO(
        Long id,
        String nomeTitular,
        String numeroMascarado,  // **** **** **** 1234
        String bandeira,
        String validade,
        boolean preferencial
) {
}