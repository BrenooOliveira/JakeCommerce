package com.les.jakebooks.dto;

import com.les.jakebooks.model.enums.StatusLivro;
import java.math.BigDecimal;

/**
 * DTO para listagem de livros.
 * Contém apenas os campos essenciais para exibição em tabelas/listas.
 * Otimizado para melhor performance em consultas com muitos registros.
 * RF0015: Consultar livros com filtros combinados
 */
public record LivroListagemDTO(
        /**
         * Identificador único
         */
        Long id,

        /**
         * Código do livro
         */
        String codigo,

        /**
         * Título do livro
         */
        String titulo,

        /**
         * Valor de venda
         * RN0013: Valor de venda baseado na margem do grupo
         */
        BigDecimal valorVenda,

        /**
         * Status do livro
         */
        StatusLivro status,

        /**
         * Nome da editora
         */
        String nomeEditora
) {
}
