package com.les.jakebooks.dto;

/**
 * DTO para Editora.
 * Utilizado como objeto aninhado em outros DTOs.
 */
public record EditoraDTO(
        Long id,
        String nome
) {
}
