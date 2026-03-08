package com.les.jakebooks.dto;

/**
 * DTO para Autor.
 * Utilizado como objeto aninhado em outros DTOs.
 */
public record AutorDTO(
        Long id,
        String nome
) {
}
