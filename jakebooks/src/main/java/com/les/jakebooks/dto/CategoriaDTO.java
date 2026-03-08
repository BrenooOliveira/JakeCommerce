package com.les.jakebooks.dto;

/**
 * DTO para Categoria.
 * Utilizado como objeto aninhado em outros DTOs.
 */
public record CategoriaDTO(
        Long id,
        String nome
) {
}
