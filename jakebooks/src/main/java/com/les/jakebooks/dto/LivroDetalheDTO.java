package com.les.jakebooks.dto;

import com.les.jakebooks.domain.enums.StatusLivro;
import java.math.BigDecimal;
import java.util.List;

/**
 * DTO para exibição completa de um livro.
 * Contém todos os campos da entidade incluindo dados dos relacionamentos.
 * Utilizado quando é necessário exibir informações detalhadas do livro.
 */
public record LivroDetalheDTO(
        /**
         * Identificador único (gerado)
         */
        Long id,

        /**
         * Código único do livro
         */
        String codigo,

        /**
         * Título do livro
         */
        String titulo,

        /**
         * Ano de publicação
         */
        Integer ano,

        /**
         * Edição
         */
        String edicao,

        /**
         * ISBN
         */
        String isbn,

        /**
         * Número de páginas
         */
        Integer numeroPaginas,

        /**
         * Sinopse
         */
        String sinopse,

        /**
         * Dimensões
         */
        String dimensoes,

        /**
         * Código de barras
         */
        String codigoBarras,

        /**
         * Status do livro
         */
        StatusLivro status,

        /**
         * Valor de venda
         */
        BigDecimal valorVenda,

        /**
         * Grupo de Precificação completo
         */
        GrupoPrecificacaoDTO grupoPrecificacao,

        /**
         * Editora completa
         */
        EditoraDTO editora,

        /**
         * Lista de Autores
         */
        List<AutorDTO> autores,

        /**
         * Lista de Categorias
         */
        List<CategoriaDTO> categorias,

        /**
         * Informações de Estoque
         */
        EstoqueDTO estoque
) {
}
