package com.les.jakebooks.validator;

import org.springframework.stereotype.Component;

import com.les.jakebooks.exception.ValidacaoNegocioException;

/**
 * Validador de Carrinho.
 * 
 * Requisitos:
 * - RN0031: Validar estoque no carrinho
 * - RN0044: Bloqueio carrinho com aviso 5 minutos antes
 * - RN0045: Remover item desbloqueado
 * - RN0063: Máximo 10 unidades do mesmo livro por pedido
 */
@Component
public class CarrinhoValidator {

    /**
     * Valida se o carrinho está expirado.
     * RN0044: Bloqueio carrinho com aviso 5 minutos antes
     * 
     * @param minutosPrevistaExpiracao minutos até a expiração
     * @throws ValidacaoNegocioException se o carrinho está prestes a expirar
     */
    public void validarExpiracaoProxima(Integer minutosPrevistaExpiracao) {
        if (minutosPrevistaExpiracao != null && minutosPrevistaExpiracao <= 5 && minutosPrevistaExpiracao > 0) {
            throw new ValidacaoNegocioException(
                String.format("AVISO: Seu carrinho expirará em %d minutos. Complete a compra ou adicione itens para renovar a sessão.",
                    minutosPrevistaExpiracao)
            );
        }
    }

    /**
     * Valida se a quantidade no carrinho é válida.
     * RN0063: Máximo 10 unidades do mesmo livro por pedido
     * 
     * @param quantidade quantidade no carrinho
     * @throws ValidacaoNegocioException se a quantidade for inválida
     */
    public void validarQuantidadeCarrinho(Integer quantidade) {
        if (quantidade == null || quantidade <= 0) {
            throw new ValidacaoNegocioException("Quantidade deve ser maior que zero");
        }

        if (quantidade > 10) {
            throw new ValidacaoNegocioException(
                String.format("Máximo 10 unidades do mesmo livro por pedido. Quantidade informada: %d", quantidade)
            );
        }
    }

    /**
     * Valida dados do carrinho antes de finalizar compra.
     * 
     * @param statusCarrinho status atual do carrinho
     * @throws ValidacaoNegocioException se o carrinho não é válido
     */
    public void validarCarrinhoParaCheckout(String statusCarrinho) {
        if (statusCarrinho == null || !statusCarrinho.equals("ABERTO")) {
            throw new ValidacaoNegocioException(
                String.format("Carrinho inválido para checkout. Status: %s. Apenas carrinhos ABERTOS podem ser processados.",
                    statusCarrinho)
            );
        }
    }

    /**
     * Valida se há itens no carrinho.
     * 
     * @param numeroItens número de itens no carrinho
     * @throws ValidacaoNegocioException se o carrinho está vazio
     */
    public void validarCarrinhoNaoVazio(Integer numeroItens) {
        if (numeroItens == null || numeroItens <= 0) {
            throw new ValidacaoNegocioException("Seu carrinho está vazio. Adicione livros antes de finalizar a compra.");
        }
    }
}
