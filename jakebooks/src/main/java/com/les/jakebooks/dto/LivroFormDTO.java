package com.les.jakebooks.dto;

import com.les.jakebooks.domain.enums.StatusLivro;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * DTO para cadastro e edição de livros.
 * Contém apenas os campos editáveis e referências de IDs para relacionamentos.
 * RF0011: Cadastrar livro
 * RF0014: Alterar livro
 */
public record LivroFormDTO(
        /**
         * Código único do livro
         * RN0011: Dados obrigatórios conforme modelo
         */
        @NotBlank(message = "Código é obrigatório")
        @Size(min = 1, max = 50, message = "Código deve ter entre 1 e 50 caracteres")
        String codigo,

        /**
         * Título do livro
         * RN0011: Dados obrigatórios conforme modelo
         */
        @NotBlank(message = "Título é obrigatório")
        @Size(min = 1, max = 255, message = "Título deve ter entre 1 e 255 caracteres")
        String titulo,

        /**
         * Ano de publicação
         */
        @NotNull(message = "Ano é obrigatório")
        @Positive(message = "Ano deve ser um número positivo")
        Integer ano,

        /**
         * Edição do livro
         */
        @Size(max = 50, message = "Edição deve ter no máximo 50 caracteres")
        String edicao,

        /**
         * ISBN do livro
         * RN0011: Dados obrigatórios conforme modelo
         */
        @NotBlank(message = "ISBN é obrigatório")
        @Size(min = 10, max = 20, message = "ISBN deve ter entre 10 e 20 caracteres")
        String isbn,

        /**
         * Número de páginas
         */
        @NotNull(message = "Número de páginas é obrigatório")
        @Positive(message = "Número de páginas deve ser positivo")
        Integer numeroPaginas,

        /**
         * Sinopse/descrição do livro
         */
        @Size(max = 2000, message = "Sinopse deve ter no máximo 2000 caracteres")
        String sinopse,

        /**
         * Dimensões do livro (Ex: 20x15x2 cm)
         */
        @Size(max = 50, message = "Dimensões deve ter no máximo 50 caracteres")
        String dimensoes,

        /**
         * Código de barras
         */
        @Size(max = 50, message = "Código de barras deve ter no máximo 50 caracteres")
        String codigoBarras,

        /**
         * Status do livro
         * RN0011: Dados obrigatórios conforme modelo
         */
        @NotNull(message = "Status é obrigatório")
        StatusLivro status,

        /**
         * Valor de venda do livro
         * RN0013: Valor de venda baseado na margem do grupo
         */
        @NotNull(message = "Valor de venda é obrigatório")
        @DecimalMin(value = "0.01", message = "Valor de venda deve ser maior que 0")
        BigDecimal valorVenda,

        /**
         * ID do Grupo de Precificação
         * RN0013: Valor de venda baseado na margem do grupo
         */
        @NotNull(message = "Grupo de precificação é obrigatório")
        Long idGrupoPrecificacao,

        /**
         * ID da Editora
         * RN0011: Dados obrigatórios conforme modelo
         */
        @NotNull(message = "Editora é obrigatória")
        Long idEditora,

        /**
         * IDs dos Autores
         * RN0011: Dados obrigatórios conforme modelo
         */
        @NotEmpty(message = "Deve haver pelo menos um autor")
        List<Long> idsAutores,

        /**
         * IDs das Categorias
         * RN0012: Livro pode ter múltiplas categorias
         */
        @NotEmpty(message = "Deve haver pelo menos uma categoria")
        List<Long> idsCategorias
) {
}
