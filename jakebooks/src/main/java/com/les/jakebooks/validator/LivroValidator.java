package com.les.jakebooks.validator;

import org.springframework.stereotype.Component;

import com.les.jakebooks.exception.ValidacaoNegocioException;

/**
 * Validador de Livro.
 * 
 * Requisitos:
 * - RN0011: Dados obrigatórios conforme modelo
 * - RN0013: Valor de venda baseado na margem do grupo
 * - RN0014: Redução abaixo da margem exige autorização
 */
@Component
public class LivroValidator {

    /**
     * Valida dados obrigatórios do livro.
     * RN0011: Dados obrigatórios conforme modelo
     * 
     * @param codigo código único do livro
     * @param titulo título do livro
     * @param isbn ISBN do livro
     * @throws ValidacaoNegocioException se algum campo obrigatório estiver vazio
     */
    public void validarDadosObrigatorios(String codigo, String titulo, String isbn) {
        if (codigo == null || codigo.trim().isEmpty()) {
            throw new ValidacaoNegocioException("Código do livro é obrigatório");
        }

        if (titulo == null || titulo.trim().isEmpty()) {
            throw new ValidacaoNegocioException("Título do livro é obrigatório");
        }

        if (isbn == null || isbn.trim().isEmpty()) {
            throw new ValidacaoNegocioException("ISBN do livro é obrigatório");
        }

        validarISBN(isbn);
    }

    /**
     * Valida formato do ISBN.
     * 
     * @param isbn ISBN a validar
     * @throws ValidacaoNegocioException se o ISBN for inválido
     */
    public void validarISBN(String isbn) {
        String isbnLimpo = isbn.replaceAll("-", "");
        
        if (isbnLimpo.length() != 10 && isbnLimpo.length() != 13) {
            throw new ValidacaoNegocioException("ISBN deve ter 10 ou 13 dígitos");
        }

        if (!isbnLimpo.matches("\\d+")) {
            throw new ValidacaoNegocioException("ISBN deve conter apenas dígitos");
        }
    }

    /**
     * Valida valor de venda baseado na margem do grupo.
     * RN0013: Valor de venda baseado na margem do grupo
     * 
     * @param custoBase custo base do livro
     * @param percentualMargem percentual de margem do grupo
     * @param valorVenda valor de venda informado
     * @throws ValidacaoNegocioException se o valor de venda estiver abaixo da margem
     */
    public void validarValorVenda(Double custoBase, Double percentualMargem, Double valorVenda) {
        if (custoBase == null || custoBase < 0) {
            throw new ValidacaoNegocioException("Custo base deve ser informado e não pode ser negativo");
        }

        if (percentualMargem == null || percentualMargem < 0) {
            throw new ValidacaoNegocioException("Percentual de margem deve ser informado e não pode ser negativo");
        }

        if (valorVenda == null || valorVenda < 0) {
            throw new ValidacaoNegocioException("Valor de venda deve ser informado e não pode ser negativo");
        }

        Double valorMinimo = custoBase * (1 + (percentualMargem / 100));

        if (valorVenda < valorMinimo) {
            // RN0014: Redução abaixo da margem exige autorização - será tratado no Service
            throw new ValidacaoNegocioException(
                String.format("Valor de venda (%.2f) está abaixo da margem mínima (%.2f). Redução exige autorização.",
                    valorVenda, valorMinimo)
            );
        }
    }

    /**
     * Valida número de páginas.
     * 
     * @param numeroPaginas número de páginas
     * @throws ValidacaoNegocioException se o número de páginas for inválido
     */
    public void validarNumeroPaginas(Integer numeroPaginas) {
        if (numeroPaginas == null || numeroPaginas <= 0) {
            throw new ValidacaoNegocioException("Número de páginas deve ser maior que zero");
        }
    }

    /**
     * Valida ano de publicação.
     * 
     * @param ano ano de publicação
     * @throws ValidacaoNegocioException se o ano for inválido
     */
    public void validarAno(Integer ano) {
        if (ano == null || ano < 1000 || ano > java.time.Year.now().getValue()) {
            throw new ValidacaoNegocioException("Ano de publicação deve ser válido");
        }
    }
}
