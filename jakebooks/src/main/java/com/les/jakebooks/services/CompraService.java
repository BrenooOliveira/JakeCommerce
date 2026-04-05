package com.les.jakebooks.services;

import com.les.jakebooks.domain.Endereco;
import com.les.jakebooks.domain.Pagamento;
import com.les.jakebooks.domain.Pedido;
import com.les.jakebooks.dto.ConversaoPedidoDTO;
import com.les.jakebooks.dto.ResultadoCompraDTO;
import com.les.jakebooks.exception.ValidacaoNegocioException;
import com.les.jakebooks.service.LogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

/**
 * Service orquestrador do fluxo de finalização de compra.
 * TASK-CHK-03: Converter Carrinho em Pedido
 * RF0037: Finalizar compra (status inicial: EM_PROCESSAMENTO)
 * RN0028: Baixa estoque apenas após pagamento aprovado
 *
 * Responsável por coordenar:
 * 1. Conversão de carrinho em pedido (PedidoService)
 * 2. Baixa de estoque (EstoqueService) - CHK-04
 * 3. Finalização do carrinho (CarrinhoService)
 * 4. Registro de log da transação (LogService)
 */
@Service
@Transactional
public class CompraService {

    @Autowired
    private PedidoService pedidoService;

    @Autowired
    private EstoqueService estoqueService;

    @Autowired
    private CarrinhoService carrinhoService;

    @Autowired
    private LogService logService;

    /**
     * Finaliza a compra executando toda a cadeia de operações.
     * TASK-CHK-03: Método principal de finalização
     *
     * Este método executa transacionalmente:
     * 1. Converte carrinho em pedido com status EM_PROCESSAMENTO
     * 2. Executa baixa de estoque (RN0028 - apenas após pagamento APROVADO)
     * 3. Finaliza carrinho alterando status para FINALIZADO
     * 4. Registra log da transação (RNF0012)
     *
     * Pré-condições:
     * - Carrinho validado (CHK-02)
     * - Endereço de entrega selecionado
     * - Frete calculado
     * - Pagamento processado com status APROVADA
     *
     * Pós-condições:
     * - Pedido criado com status EM_PROCESSAMENTO
     * - Estoque decrementado para cada item
     * - Carrinho com status FINALIZADO
     * - Log da transação registrado
     *
     * @param carrinhoId ID do carrinho a ser finalizado
     * @param enderecoEntrega endereço de entrega selecionado
     * @param pagamento pagamento processado e aprovado
     * @param valorFrete valor do frete calculado
     * @return DTO com resultado da compra (sucesso ou erro)
     */
    public ResultadoCompraDTO finalizarCompra(
            Long carrinhoId,
            Endereco enderecoEntrega,
            Pagamento pagamento,
            BigDecimal valorFrete) {

        try {
            // 1. Converter carrinho em pedido
            ConversaoPedidoDTO conversaoDTO = new ConversaoPedidoDTO(
                    carrinhoId,
                    enderecoEntrega,
                    pagamento,
                    valorFrete
            );

            Pedido pedido = pedidoService.converterCarrinhoEmPedido(conversaoDTO);

            // 2. Executar baixa de estoque (RN0028 - apenas após pagamento APROVADO)
            estoqueService.executarBaixaPorPedido(pedido);

            // 3. Finalizar carrinho (impede reutilização)
            carrinhoService.finalizarCarrinho(carrinhoId);

            // 4. Registrar log da transação (RNF0012)
            logService.registrar(
                "FINALIZAR_COMPRA",
                "Pedido",
                "Carrinho ID: " + carrinhoId,
                "Pedido ID: " + pedido.getId() + ", Status: " + pedido.getStatus() +
                ", Valor Total: " + pedido.getValorTotal(),
                "Compra finalizada com sucesso para cliente " + pedido.getCliente().getNome()
            );

            // Retornar sucesso
            return ResultadoCompraDTO.sucesso(
                    pedido.getId(),
                    "Compra finalizada com sucesso! Número do pedido: " + pedido.getId()
            );

        } catch (ValidacaoNegocioException e) {
            // Erro de validação de negócio
            logService.registrar(
                "FALHA_COMPRA",
                "Carrinho",
                "Carrinho ID: " + carrinhoId,
                "Erro: " + e.getMessage(),
                "Falha na finalização da compra"
            );

            return ResultadoCompraDTO.erro("Erro ao finalizar compra: " + e.getMessage());

        } catch (Exception e) {
            // Erro inesperado
            logService.registrar(
                "ERRO_COMPRA",
                "Carrinho",
                "Carrinho ID: " + carrinhoId,
                "Erro: " + e.getMessage(),
                "Erro inesperado na finalização da compra"
            );

            return ResultadoCompraDTO.erro(
                    "Erro inesperado ao finalizar compra. Por favor, entre em contato com o suporte."
            );
        }
    }
}
