package com.les.jakebooks.service;

import com.les.jakebooks.domain.Carrinho;
import com.les.jakebooks.domain.Endereco;
import com.les.jakebooks.domain.Pagamento;
import com.les.jakebooks.domain.Pedido;
import com.les.jakebooks.dto.CheckoutTransacaoDTO;
import com.les.jakebooks.dto.ConversaoPedidoDTO;
import com.les.jakebooks.dto.ProcessarPagamentoDTO;
import com.les.jakebooks.dto.ResultadoCheckoutDTO;
import com.les.jakebooks.exception.CarrinhoBloqueadoPagamentoException;
import com.les.jakebooks.exception.ValidacaoNegocioException;
import com.les.jakebooks.domain.enums.StatusPagamento;
import com.les.jakebooks.validator.CompraValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Service orquestrador de transacoes de checkout.
 * TASK-CHK-05: Gerenciar Estado da Transacao
 *
 * Responsavel por coordenar todo o fluxo de checkout de forma atomica:
 * 1. Validar pre-condicoes do carrinho
 * 2. Verificar bloqueio por tentativas reprovadas
 * 3. Processar pagamento
 * 4. Converter carrinho em pedido (se aprovado)
 * 5. Executar baixa de estoque (se aprovado)
 * 6. Controlar tentativas reprovadas (RN0065)
 * 7. Garantir atomicidade (rollback em caso de falha)
 *
 * RF0033: Realizar compra
 * RF0037: Finalizar compra
 * RN0028: Baixa estoque apenas apos pagamento aprovado
 * RN0065: 3 pagamentos REPROVADOS consecutivos bloqueiam carrinho
 */
@Service
public class TransacaoCheckoutService {

    @Autowired
    private CompraValidator compraValidator;

    @Autowired
    private CarrinhoService carrinhoService;

    @Autowired
    private PagamentoService pagamentoService;

    @Autowired
    private PedidoService pedidoService;

    @Autowired
    private EstoqueService estoqueService;

    @Autowired
    private LogTransacaoService logTransacaoService;

    /**
     * Executa checkout completo de forma atomica.
     * TASK-CHK-05: Metodo principal de orquestracao
     *
     * @param dados dados da transacao de checkout
     * @return resultado da transacao
     * @throws ValidacaoNegocioException se validacoes falharem
     * @throws CarrinhoBloqueadoPagamentoException se carrinho bloqueado
     */
    @Transactional
    public ResultadoCheckoutDTO executarCheckoutCompleto(CheckoutTransacaoDTO dados) {
        // Gerar ID unico de transacao para rastreabilidade
        String transacaoId = gerarTransacaoId();

        // Registrar inicio da transacao (RNF0012)
        logTransacaoService.iniciarTransacao(transacaoId, dados.getCarrinhoId());

        try {
            // ETAPA 1: Validar pre-condicoes do carrinho
            Carrinho carrinho = validarPreCondicoes(dados, transacaoId);

            // ETAPA 2: Verificar se carrinho esta bloqueado por tentativas reprovadas (RN0065)
            verificarBloqueioCarrinho(dados.getCarrinhoId(), transacaoId);

            // ETAPA 3: Processar pagamento
            Pagamento pagamento = processarPagamento(dados, transacaoId);

            // ETAPA 4: Verificar resultado do pagamento
            if (pagamento.getStatus() == StatusPagamento.REPROVADA) {
                return tratarPagamentoReprovado(dados.getCarrinhoId(), transacaoId, pagamento);
            }

            // ETAPA 5: Converter carrinho em pedido (TASK-CHK-03)
            Pedido pedido = converterCarrinhoEmPedido(carrinho, dados, pagamento);

            // ETAPA 6: Executar baixa de estoque (TASK-CHK-04, RN0028)
            estoqueService.executarBaixaPorPedido(pedido);

            // ETAPA 7: Finalizar carrinho
            carrinhoService.finalizarCarrinho(dados.getCarrinhoId());

            // ETAPA 8: Resetar contador de tentativas reprovadas (sucesso)
            carrinhoService.resetarTentativasReprovadas(dados.getCarrinhoId());

            // Registrar sucesso
            logTransacaoService.finalizarTransacao(transacaoId, pedido.getId());

            return ResultadoCheckoutDTO.sucesso(transacaoId, pedido.getId());

        } catch (CarrinhoBloqueadoPagamentoException e) {
            // Carrinho bloqueado por tentativas reprovadas
            logTransacaoService.finalizarTransacao(transacaoId, e.getMessage());
            return ResultadoCheckoutDTO.bloqueado(transacaoId);

        } catch (ValidacaoNegocioException e) {
            // Erro de validacao de negocio
            logTransacaoService.finalizarTransacao(transacaoId, e.getMessage());
            throw e;

        } catch (Exception e) {
            // Erro inesperado - rollback automatico por @Transactional
            logTransacaoService.finalizarTransacao(transacaoId, e.getMessage());
            throw new ValidacaoNegocioException(
                String.format("Erro ao processar checkout [Transacao: %s]: %s",
                        transacaoId, e.getMessage())
            );
        }
    }

    /**
     * Valida pre-condicoes do carrinho.
     * TASK-CHK-02: Validar Pre-Condicoes do Carrinho
     *
     * @param dados dados da transacao
     * @param transacaoId ID da transacao
     * @return carrinho validado
     * @throws ValidacaoNegocioException se validacao falhar
     */
    private Carrinho validarPreCondicoes(CheckoutTransacaoDTO dados, String transacaoId) {
        try {
            Carrinho carrinho = carrinhoService.buscarPorId(dados.getCarrinhoId());

            // Executar validacoes (RN0031, RN0032, RN0063)
            compraValidator.validarCarrinhoParaCheckout(carrinho);

            logTransacaoService.registrarValidacao(transacaoId, dados.getCarrinhoId(), true, null);

            return carrinho;

        } catch (ValidacaoNegocioException e) {
            logTransacaoService.registrarValidacao(transacaoId, dados.getCarrinhoId(), false, e.getMessage());
            throw e;
        }
    }

    /**
     * Verifica se carrinho esta bloqueado por tentativas reprovadas.
     * RN0065: 3 pagamentos REPROVADOS consecutivos bloqueiam carrinho
     *
     * @param carrinhoId ID do carrinho
     * @param transacaoId ID da transacao
     * @throws CarrinhoBloqueadoPagamentoException se bloqueado
     */
    private void verificarBloqueioCarrinho(Long carrinhoId, String transacaoId) {
        try {
            carrinhoService.verificarCarrinhoBloqueado(carrinhoId);
        } catch (CarrinhoBloqueadoPagamentoException e) {
            logTransacaoService.registrarBloqueioCarrinho(carrinhoId, 3);
            throw e;
        }
    }

    /**
     * Processa pagamento usando PagamentoService.
     * RN0037: Validar pagamento
     * RN0038: Status APROVADA ou REPROVADA
     *
     * @param dados dados da transacao
     * @param transacaoId ID da transacao
     * @return pagamento processado
     */
    private Pagamento processarPagamento(CheckoutTransacaoDTO dados, String transacaoId) {
        ProcessarPagamentoDTO pagamentoDTO = ProcessarPagamentoDTO.builder()
                .valorTotal(dados.getValorTotal())
                .cuponsAplicados(null) // TODO: mapear cupons aplicados
                .cartoesValores(dados.getCartoesValores())
                .build();

        return pagamentoService.processarPagamento(pagamentoDTO, dados.getClienteId());
    }

    /**
     * Trata pagamento reprovado incrementando contador.
     * RN0065: Controlar tentativas reprovadas
     *
     * @param carrinhoId ID do carrinho
     * @param transacaoId ID da transacao
     * @param pagamento pagamento reprovado
     * @return resultado com status PAGAMENTO_REPROVADO
     */
    private ResultadoCheckoutDTO tratarPagamentoReprovado(
            Long carrinhoId,
            String transacaoId,
            Pagamento pagamento) {

        try {
            // Incrementar contador de tentativas reprovadas
            carrinhoService.registrarTentativaReprovada(carrinhoId);

            // Obter tentativas restantes
            int tentativasRestantes = carrinhoService.getTentativasRestantes(carrinhoId);

            // Registrar tentativa reprovada
            logTransacaoService.registrarTentativaReprovada(transacaoId, carrinhoId, tentativasRestantes);

            return ResultadoCheckoutDTO.pagamentoReprovado(
                    transacaoId,
                    "Pagamento reprovado. Tente novamente com outro metodo de pagamento.",
                    tentativasRestantes
            );

        } catch (CarrinhoBloqueadoPagamentoException e) {
            // Atingiu limite de tentativas - carrinho bloqueado
            logTransacaoService.registrarBloqueioCarrinho(carrinhoId, 3);
            return ResultadoCheckoutDTO.bloqueado(transacaoId);
        }
    }

    /**
     * Converte carrinho em pedido.
     * TASK-CHK-03: Converter Carrinho em Pedido
     *
     * @param carrinho carrinho a converter
     * @param dados dados da transacao
     * @param pagamento pagamento aprovado
     * @return pedido criado
     */
    private Pedido converterCarrinhoEmPedido(
            Carrinho carrinho,
            CheckoutTransacaoDTO dados,
            Pagamento pagamento) {

        ConversaoPedidoDTO conversaoDTO = new ConversaoPedidoDTO(
                carrinho.getId(),
                dados.getEnderecoEntrega(),
                pagamento,
                dados.getValorFrete()
        );

        return pedidoService.converterCarrinhoEmPedido(conversaoDTO);
    }

    /**
     * Gera ID unico de transacao para rastreabilidade.
     * Pattern: TXN_timestamp_random
     *
     * @return ID unico da transacao
     */
    private String gerarTransacaoId() {
        String timestamp = String.valueOf(System.currentTimeMillis());
        String random = UUID.randomUUID().toString().substring(0, 8);
        return String.format("TXN_%s_%s", timestamp, random);
    }
}
