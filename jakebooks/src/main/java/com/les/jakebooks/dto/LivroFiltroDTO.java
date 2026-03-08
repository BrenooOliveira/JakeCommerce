package com.les.jakebooks.dto;

import com.les.jakebooks.model.enums.StatusLivro;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

/**
 * DTO para busca e filtro de livros.
 * Contém apenas os campos de filtro e busca, todos opcionais.
 * RF0015: Consultar livros com filtros combinados
 */
public record LivroFiltroDTO(
        /**
         * Filtro por título (opcional)
         * Busca parcial (LIKE)
         */
        @Size(max = 255, message = "Título deve ter no máximo 255 caracteres")
        String titulo,

        /**
         * Filtro por nome do autor (opcional)
         * Busca parcial (LIKE)
         */
        @Size(max = 255, message = "Nome do autor deve ter no máximo 255 caracteres")
        String nomeAutor,

        /**
         * Filtro por ID da categoria (opcional)
         * Busca exata
         */
        Long idCategoria,

        /**
         * Filtro por status (opcional)
         * RN0016: Validação automática categoria FORA DE MERCADO
         */
        StatusLivro status,

        /**
         * Filtro por preço mínimo (opcional)
         * RN0013: Valor de venda baseado na margem do grupo
         */
        @DecimalMin(value = "0.01", message = "Preço mínimo deve ser maior que 0")
        BigDecimal precoMin,

        /**
         * Filtro por preço máximo (opcional)
         * RN0013: Valor de venda baseado na margem do grupo
         */
        @DecimalMin(value = "0.01", message = "Preço máximo deve ser maior que 0")
        BigDecimal precoMax
) {
}
