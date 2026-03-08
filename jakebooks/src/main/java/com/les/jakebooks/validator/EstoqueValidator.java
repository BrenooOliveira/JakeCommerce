package com.les.jakebooks.validator;

import org.springframework.stereotype.Component;

import com.les.jakebooks.exception.EstoqueInsuficienteException;

/**
 * Validador de estoque.
 * 
 * Requisitos:
 * - RN0031: Validar estoque no carrinho
 * - RN0032: Validar estoque antes da finalização
 * - RN0061: Não permitir quantidade zero
 * - RN0062: Todo item deve possuir custo
 * - RNF0064: Não permitir registro sem data
 */
@Component
public class EstoqueValidator {

    /**
     * Valida se há quantidade suficiente no estoque.
     * 
     * @param codigoLivro código do livro
     * @param quantidadeSolicitada quantidade solicitada
     * @param quantidadeDisponivel quantidade disponível em estoque
     * @throws EstoqueInsuficienteException se não houver estoque suficiente
     */
    public void validarQuantidadeDisponivel(String codigoLivro, Integer quantidadeSolicitada, Integer quantidadeDisponivel) {
        if (quantidadeDisponivel == null || quantidadeDisponivel < quantidadeSolicitada) {
            String mensagem = String.format(
                "Estoque insuficiente para o livro %s. Solicitado: %d | Disponível: %d",
                codigoLivro, quantidadeSolicitada, quantidadeDisponivel != null ? quantidadeDisponivel : 0
            );
            throw new EstoqueInsuficienteException(
                mensagem, 
                codigoLivro, 
                quantidadeSolicitada, 
                quantidadeDisponivel != null ? quantidadeDisponivel : 0
            );
        }
    }

    /**
     * Valida se a quantidade é válida (maior que zero).
     * RN0061: Não permitir quantidade zero
     * 
     * @param quantidade quantidade a validar
     * @throws IllegalArgumentException se a quantidade for zero ou negativa
     */
    public void validarQuantidadePositiva(Integer quantidade) {
        if (quantidade == null || quantidade <= 0) {
            throw new IllegalArgumentException("Quantidade deve ser maior que zero");
        }
    }

    /**
     * Valida se o custo foi informado.
     * RN0062: Todo item deve possuir custo
     * 
     * @param custoAtual custo a validar
     * @throws IllegalArgumentException se o custo não for informado ou for negativo
     */
    public void validarCusto(Double custoAtual) {
        if (custoAtual == null || custoAtual < 0) {
            throw new IllegalArgumentException("Custo deve ser informado e não pode ser negativo");
        }
    }

    /**
     * Valida se a data de entrada foi informada.
     * RNF0064: Não permitir registro sem data
     * 
     * @param dataEntrada data de entrada
     * @throws IllegalArgumentException se a data não for informada
     */
    public void validarDataEntrada(java.time.LocalDate dataEntrada) {
        if (dataEntrada == null) {
            throw new IllegalArgumentException("Data de entrada é obrigatória");
        }
    }

    /**
     * Valida limite máximo de unidades por pedido.
     * RN0063: Máximo 10 unidades do mesmo livro por pedido
     * 
     * @param quantidade quantidade solicitada
     * @throws IllegalArgumentException se exceder o limite
     */
    public void validarLimiteUnidadesPerPedido(Integer quantidade) {
        if (quantidade > 10) {
            throw new IllegalArgumentException("Máximo 10 unidades do mesmo livro por pedido. Solicitado: " + quantidade);
        }
    }
}
