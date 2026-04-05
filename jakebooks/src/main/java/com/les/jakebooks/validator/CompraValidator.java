package com.les.jakebooks.validator;

import com.les.jakebooks.domain.Carrinho;
import com.les.jakebooks.domain.Estoque;
import com.les.jakebooks.domain.ItemCarrinho;
import com.les.jakebooks.domain.Livro;
import com.les.jakebooks.exception.CarrinhoExpiradoException;
import com.les.jakebooks.exception.CarrinhoVazioException;
import com.les.jakebooks.exception.EstoqueInsuficienteException;
import com.les.jakebooks.exception.EstoqueNaoEncontradoException;
import com.les.jakebooks.exception.LimiteItensExcedidoException;
import com.les.jakebooks.exception.LivroInativoException;
import com.les.jakebooks.model.enums.StatusCarrinho;
import com.les.jakebooks.model.enums.StatusLivro;
import com.les.jakebooks.repository.EstoqueRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

/**
 * Validador responsável por validar todas as pré-condições do carrinho
 * para permitir o processo de checkout.
 *
 * TASK-CHK-02: Validar Pre-Condicoes do Carrinho
 * RN0031: Validar estoque no carrinho
 * RN0032: Validar estoque antes da finalização
 * RN0063: Máximo 10 unidades do mesmo livro por pedido
 */
@Component
public class CompraValidator {

    @Autowired
    private EstoqueRepository estoqueRepository;

    /**
     * Valida todas as pre-CondicSões do carrinho para checkout
     * RN0031, RN0032, RN0063
     */
    public void validarCarrinhoParaCheckout(Carrinho carrinho) {
        validarCarrinhoNaoVazio(carrinho);
        validarCarrinhoNaoExpirado(carrinho);
        validarItensCarrinho(carrinho.getItens());
    }

    /**
     * Valida se o carrinho não está vazio
     * @param carrinho carrinho a ser validado
     * @throws CarrinhoVazioException se o carrinho estiver vazio
     */
    private void validarCarrinhoNaoVazio(Carrinho carrinho) {
        if (carrinho.getItens() == null || carrinho.getItens().isEmpty()) {
            throw new CarrinhoVazioException(
                "Carrinho vazio. Adicione pelo menos um item antes de finalizar a compra."
            );
        }
    }

    /**
     * Valida se o carrinho não expirou e tem status ABERTO
     * @param carrinho carrinho a ser validado
     * @throws CarrinhoExpiradoException se o carrinho estiver expirado ou não estiver ABERTO
     */
    private void validarCarrinhoNaoExpirado(Carrinho carrinho) {
        if (!StatusCarrinho.ABERTO.equals(carrinho.getStatus())) {
            throw new CarrinhoExpiradoException(
                "Carrinho não está mais disponível para finalização"
            );
        }

        if (carrinho.getDataExpiracao() != null &&
            carrinho.getDataExpiracao().isBefore(LocalDate.now())) {
            throw new CarrinhoExpiradoException(
                "Carrinho expirou. Por favor, adicione os itens novamente"
            );
        }
    }

    /**
     * Valida todos os itens do carrinho
     * @param itens lista de itens do carrinho
     */
    private void validarItensCarrinho(List<ItemCarrinho> itens) {
        for (ItemCarrinho item : itens) {
            validarLivroAtivo(item.getLivro());
            validarEstoqueDisponivel(item);
            validarLimiteUnidades(item);
        }
    }

    /**
     * Valida se o livro está ativo
     * @param livro livro a ser validado
     * @throws LivroInativoException se o livro estiver inativo
     */
    private void validarLivroAtivo(Livro livro) {
        if (!StatusLivro.ATIVO.equals(livro.getStatus())) {
            throw new LivroInativoException(
                String.format("O livro '%s' não está mais disponível", livro.getTitulo()),
                livro.getTitulo(),
                livro.getCodigo()
            );
        }
    }

    /**
     * Valida se há estoque suficiente para o item
     * RN0031: Validar estoque no carrinho
     * @param item item do carrinho
     * @throws EstoqueNaoEncontradoException se não encontrar estoque
     * @throws EstoqueInsuficienteException se estoque for insuficiente
     */
    private void validarEstoqueDisponivel(ItemCarrinho item) {
        Estoque estoque = estoqueRepository.findByLivroId(item.getLivro().getId());

        if (estoque == null) {
            throw new EstoqueNaoEncontradoException(
                "Estoque não encontrado para o livro: " + item.getLivro().getTitulo(),
                item.getLivro().getTitulo(),
                item.getLivro().getCodigo()
            );
        }

        if (estoque.getQuantidade() < item.getQuantidade()) {
            throw new EstoqueInsuficienteException(
                String.format("Estoque insuficiente para '%s'. Disponível: %d, Solicitado: %d",
                    item.getLivro().getTitulo(),
                    estoque.getQuantidade(),
                    item.getQuantidade()),
                item.getLivro().getCodigo(),
                item.getQuantidade(),
                estoque.getQuantidade()
            );
        }
    }

    /**
     * Valida se o item não excede o limite de 10 unidades
     * RN0063: Máximo 10 unidades do mesmo livro por pedido
     * @param item item do carrinho
     * @throws LimiteItensExcedidoException se exceder o limite
     */
    private void validarLimiteUnidades(ItemCarrinho item) {
        if (item.getQuantidade() > 10) {
            throw new LimiteItensExcedidoException(
                String.format("Máximo 10 unidades por livro. Livro: '%s', Quantidade: %d",
                    item.getLivro().getTitulo(),
                    item.getQuantidade()),
                item.getLivro().getTitulo(),
                item.getQuantidade()
            );
        }
    }

    /**
     * Re-valida estoque antes da finalização (RN0032)
     * Executado imediatamente antes de processar o pagamento
     */
    public void revalidarEstoqueParaFinalizacao(Carrinho carrinho) {
        validarItensCarrinho(carrinho.getItens());
    }
}