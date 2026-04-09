package com.les.jakebooks.service;

import com.les.jakebooks.domain.Carrinho;
import com.les.jakebooks.domain.Cartao;
import com.les.jakebooks.domain.Cliente;
import com.les.jakebooks.domain.Cupom;
import com.les.jakebooks.domain.Endereco;
import com.les.jakebooks.domain.Pagamento;
import com.les.jakebooks.domain.Pedido;
import com.les.jakebooks.dto.CheckoutTransacaoDTO;
import com.les.jakebooks.dto.ConversaoPedidoDTO;
import com.les.jakebooks.dto.CupomAplicadoDTO;
import com.les.jakebooks.dto.FinalizarPedidoDTO;
import com.les.jakebooks.dto.PagamentoCartaoDadosDTO;
import com.les.jakebooks.dto.ProcessarPagamentoDTO;
import com.les.jakebooks.dto.ResultadoCheckoutDTO;
import com.les.jakebooks.dto.ResultadoCompraDTO;
import com.les.jakebooks.exception.CarrinhoBloqueadoPagamentoException;
import com.les.jakebooks.exception.NegocioException;
import com.les.jakebooks.exception.RecursoNaoEncontradoException;
import com.les.jakebooks.exception.ValidacaoNegocioException;
import com.les.jakebooks.domain.enums.StatusPagamento;
import com.les.jakebooks.domain.enums.TipoCupom;
import com.les.jakebooks.repository.CartaoRepository;
import com.les.jakebooks.repository.ClienteRepository;
import com.les.jakebooks.repository.CupomRepository;
import com.les.jakebooks.repository.EnderecoRepository;
import com.les.jakebooks.validator.CompraValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Orquestrador único do fluxo de compra / checkout.
 * RN0028: Baixa de estoque apenas após pagamento APROVADO.
 * RN0031/RN0032: Validação de estoque no carrinho e antes da finalização.
 * RN0033–RN0036, RN0064, RN0065: regras aplicadas na montagem do pagamento e no carrinho.
 */
@Service
public class CompraService {

    private static final BigDecimal VALOR_MINIMO_CARTAO = new BigDecimal("10.00");

    @Autowired
    private CompraValidator compraValidator;

    @Autowired
    private CarrinhoService carrinhoService;

    @Autowired
    private PedidoService pedidoService;

    @Autowired
    private PagamentoService pagamentoService;

    @Autowired
    private EstoqueService estoqueService;

    @Autowired
    private LogService logService;

    @Autowired
    private LogTransacaoService logTransacaoService;

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private EnderecoRepository enderecoRepository;

    @Autowired
    private CupomRepository cupomRepository;

    @Autowired
    private CartaoRepository cartaoRepository;

    @Autowired
    private CupomService cupomService;

    /**
     * Valida pré-condições do carrinho para checkout (RN0031, RN0032, RN0063).
     */
    public void validarCarrinho(Carrinho carrinho) {
        compraValidator.validarCarrinhoParaCheckout(carrinho);
    }

    /**
     * Calcula frete (RN0064). Delega para {@link PedidoService#calcularFrete(Long, Long)}.
     */
    public BigDecimal calcularFrete(Long carrinhoId, Long enderecoId) {
        return pedidoService.calcularFrete(carrinhoId, enderecoId);
    }

    /**
     * Processa pagamento conforme PAY-05 (cupons + cartões, gateway simulado).
     */
    public Pagamento processarPagamento(ProcessarPagamentoDTO dto, Long clienteId) {
        return pagamentoService.processarPagamento(dto, clienteId);
    }

    /**
     * Converte carrinho em pedido após pagamento aprovado.
     */
    public Pedido gerarPedido(Carrinho carrinho, Endereco enderecoEntrega, Pagamento pagamento, BigDecimal valorFrete) {
        ConversaoPedidoDTO conversaoDTO = new ConversaoPedidoDTO(
                carrinho.getId(),
                enderecoEntrega,
                pagamento,
                valorFrete
        );
        return pedidoService.converterCarrinhoEmPedido(conversaoDTO);
    }

    /**
     * Baixa estoque por pedido (RN0028 — só chamar com pagamento APROVADO).
     */
    public void baixarEstoque(Pedido pedido) {
        estoqueService.executarBaixaPorPedido(pedido);
    }

    /**
     * Fluxo completo de checkout a partir da tela de finalização (carrinho autenticado).
     */
    @Transactional
    public ResultadoCheckoutDTO executarCheckout(FinalizarPedidoDTO dto) {
        String transacaoId = gerarTransacaoId();
        logTransacaoService.iniciarTransacao(transacaoId, dto.carrinhoId());

        try {
            Cliente cliente = clienteRepository.findByCodigo(dto.codigoCliente())
                    .orElseThrow(() -> new RecursoNaoEncontradoException(
                            "Cliente com código " + dto.codigoCliente() + " não encontrado"));

            Carrinho carrinho = carrinhoService.buscarPorId(dto.carrinhoId());

            if (!carrinho.getCliente().getId().equals(cliente.getId())) {
                throw new ValidacaoNegocioException("Carrinho não pertence ao cliente");
            }

            validarCarrinho(carrinho);
            verificarBloqueioCarrinho(carrinho.getId(), transacaoId);

            Endereco endereco = enderecoRepository.findById(dto.enderecoId())
                    .orElseThrow(() -> new RecursoNaoEncontradoException(
                            "Endereço com ID " + dto.enderecoId() + " não encontrado"));

            if (!endereco.getCliente().getId().equals(cliente.getId())) {
                throw new ValidacaoNegocioException("Endereço não pertence ao cliente");
            }

            compraValidator.revalidarEstoqueParaFinalizacao(carrinho);

            BigDecimal valorFrete = calcularFrete(dto.carrinhoId(), dto.enderecoId());
            BigDecimal valorProdutos = calcularValorProdutos(carrinho);
            BigDecimal valorTotal = valorProdutos.add(valorFrete);

            List<CupomAplicadoDTO> cuponsAplicados = new ArrayList<>();
            BigDecimal valorRestante = valorTotal;
            BigDecimal excedenteCupomPromocional = null;

            if (dto.codigoCupomPromocional() != null && !dto.codigoCupomPromocional().isBlank()) {
                Cupom cupom = cupomRepository.findByCodigoAndAtivoTrue(dto.codigoCupomPromocional().trim().toUpperCase())
                        .orElseThrow(() -> new ValidacaoNegocioException("Cupom inválido ou expirado"));

                if (!TipoCupom.PROMOCIONAL.equals(cupom.getTipo())) {
                    throw new ValidacaoNegocioException("Apenas cupons promocionais podem ser usados nesta compra");
                }

                BigDecimal valorAplicar = cupom.getValor().min(valorRestante.max(BigDecimal.ZERO));
                if (valorAplicar.compareTo(BigDecimal.ZERO) > 0) {
                    cuponsAplicados.add(new CupomAplicadoDTO(
                            cupom.getId(),
                            cupom.getCodigo(),
                            valorAplicar,
                            TipoCupom.PROMOCIONAL));
                    BigDecimal excedente = cupom.getValor().subtract(valorAplicar);
                    if (excedente.compareTo(BigDecimal.ZERO) > 0) {
                        excedenteCupomPromocional = excedente;
                    }
                    valorRestante = valorRestante.subtract(valorAplicar);
                }
            }

            Map<Long, BigDecimal> cartoesValores = new LinkedHashMap<>();
            if (valorRestante.compareTo(BigDecimal.ZERO) > 0) {
                if (dto.pagamentosCartao() == null || dto.pagamentosCartao().isEmpty()) {
                    throw new ValidacaoNegocioException(
                            "Formas de pagamento insuficientes. Faltam: R$ " + valorRestante);
                }
                cartoesValores = montarPagamentosCartao(dto.pagamentosCartao(), cliente, valorRestante);
            }

            ProcessarPagamentoDTO pagamentoDTO = ProcessarPagamentoDTO.builder()
                    .valorTotal(valorTotal)
                    .cuponsAplicados(cuponsAplicados.isEmpty() ? null : cuponsAplicados)
                    .cartoesValores(cartoesValores.isEmpty() ? null : cartoesValores)
                    .build();

            Pagamento pagamento = processarPagamento(pagamentoDTO, cliente.getId());

            if (pagamento.getStatus() == StatusPagamento.REPROVADA) {
                return tratarPagamentoReprovado(carrinho.getId(), transacaoId, pagamento);
            }

            if (excedenteCupomPromocional != null && excedenteCupomPromocional.compareTo(BigDecimal.ZERO) > 0) {
                cupomService.gerarCupomTroca(cliente, excedenteCupomPromocional, "EXCEDENTE_CUPOM_PROMOCIONAL");
            }

            Pedido pedido = gerarPedido(carrinho, endereco, pagamento, valorFrete);
            baixarEstoque(pedido);
            carrinhoService.finalizarCarrinho(carrinho.getId());
            carrinhoService.resetarTentativasReprovadas(carrinho.getId());

            logTransacaoService.finalizarTransacao(transacaoId, pedido.getId());
            logService.registrar(
                    "FINALIZAR_COMPRA",
                    "Pedido",
                    "Carrinho ID: " + dto.carrinhoId(),
                    "Pedido ID: " + pedido.getId() + ", Status: " + pedido.getStatus(),
                    "Compra finalizada com sucesso para cliente " + cliente.getNome());

            return ResultadoCheckoutDTO.sucesso(transacaoId, pedido.getId());

        } catch (CarrinhoBloqueadoPagamentoException e) {
            logTransacaoService.finalizarTransacao(transacaoId, e.getMessage());
            return ResultadoCheckoutDTO.bloqueado(transacaoId);
        } catch (NegocioException e) {
            logTransacaoService.finalizarTransacao(transacaoId, e.getMessage());
            throw e;
        } catch (ValidacaoNegocioException | RecursoNaoEncontradoException e) {
            logTransacaoService.finalizarTransacao(transacaoId, e.getMessage());
            throw e;
        } catch (Exception e) {
            logTransacaoService.finalizarTransacao(transacaoId, e.getMessage());
            throw new ValidacaoNegocioException(
                    String.format("Erro ao processar checkout [Transação: %s]: %s", transacaoId, e.getMessage()),
                    e);
        }
    }

    /**
     * Checkout atômico a partir do DTO de transação (mesmas regras que {@link #executarCheckout(FinalizarPedidoDTO)}).
     */
    @Transactional
    public ResultadoCheckoutDTO executarCheckoutCompleto(CheckoutTransacaoDTO dados) {
        Cliente cliente = clienteRepository.findById(dados.getClienteId())
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Cliente com ID " + dados.getClienteId() + " não encontrado"));

        Endereco endereco = dados.getEnderecoEntrega();
        if (endereco == null || endereco.getId() == null) {
            throw new ValidacaoNegocioException("Endereço de entrega é obrigatório");
        }

        List<PagamentoCartaoDadosDTO> pagamentosCartao = new ArrayList<>();
        if (dados.getCartoesValores() != null) {
            for (Map.Entry<Long, BigDecimal> e : dados.getCartoesValores().entrySet()) {
                pagamentosCartao.add(new PagamentoCartaoDadosDTO(e.getKey(), e.getValue()));
            }
        }

        FinalizarPedidoDTO dto = new FinalizarPedidoDTO(
                cliente.getCodigo(),
                dados.getCarrinhoId(),
                endereco.getId(),
                dados.getCodigoCupomPromocional(),
                pagamentosCartao);

        return executarCheckout(dto);
    }

    /**
     * Finalização legada: recebe pagamento já aprovado e sessão de pós-processamento.
     */
    @Transactional
    public ResultadoCompraDTO finalizarCompra(
            Long carrinhoId,
            Endereco enderecoEntrega,
            Pagamento pagamento,
            BigDecimal valorFrete) {

        try {
            ConversaoPedidoDTO conversaoDTO = new ConversaoPedidoDTO(
                    carrinhoId,
                    enderecoEntrega,
                    pagamento,
                    valorFrete
            );

            Pedido pedido = pedidoService.converterCarrinhoEmPedido(conversaoDTO);
            baixarEstoque(pedido);
            carrinhoService.finalizarCarrinho(carrinhoId);

            logService.registrar(
                    "FINALIZAR_COMPRA",
                    "Pedido",
                    "Carrinho ID: " + carrinhoId,
                    "Pedido ID: " + pedido.getId() + ", Status: " + pedido.getStatus()
                            + ", Valor Total: " + pedido.getValorTotal(),
                    "Compra finalizada com sucesso para cliente " + pedido.getCliente().getNome());

            return ResultadoCompraDTO.sucesso(
                    pedido.getId(),
                    "Compra finalizada com sucesso! Número do pedido: " + pedido.getId());

        } catch (ValidacaoNegocioException e) {
            logService.registrar(
                    "FALHA_COMPRA",
                    "Carrinho",
                    "Carrinho ID: " + carrinhoId,
                    "Erro: " + e.getMessage(),
                    "Falha na finalização da compra");

            return ResultadoCompraDTO.erro("Erro ao finalizar compra: " + e.getMessage());

        } catch (Exception e) {
            logService.registrar(
                    "ERRO_COMPRA",
                    "Carrinho",
                    "Carrinho ID: " + carrinhoId,
                    "Erro: " + e.getMessage(),
                    "Erro inesperado na finalização da compra");

            return ResultadoCompraDTO.erro(
                    "Erro inesperado ao finalizar compra. Por favor, entre em contato com o suporte.");
        }
    }

    private void verificarBloqueioCarrinho(Long carrinhoId, String transacaoId) {
        try {
            carrinhoService.verificarCarrinhoBloqueado(carrinhoId);
        } catch (CarrinhoBloqueadoPagamentoException e) {
            logTransacaoService.registrarBloqueioCarrinho(carrinhoId, 3);
            throw e;
        }
    }

    private ResultadoCheckoutDTO tratarPagamentoReprovado(
            Long carrinhoId,
            String transacaoId,
            Pagamento pagamento) {

        try {
            carrinhoService.registrarTentativaReprovada(carrinhoId);
            int tentativasRestantes = carrinhoService.getTentativasRestantes(carrinhoId);
            logTransacaoService.registrarTentativaReprovada(transacaoId, carrinhoId, tentativasRestantes);
            return ResultadoCheckoutDTO.pagamentoReprovado(
                    transacaoId,
                    "Pagamento reprovado. Tente novamente com outro método de pagamento.",
                    tentativasRestantes);
        } catch (CarrinhoBloqueadoPagamentoException e) {
            logTransacaoService.registrarBloqueioCarrinho(carrinhoId, 3);
            return ResultadoCheckoutDTO.bloqueado(transacaoId);
        }
    }

    private Map<Long, BigDecimal> montarPagamentosCartao(
            List<PagamentoCartaoDadosDTO> pagamentosCartao,
            Cliente cliente,
            BigDecimal valorRestanteInicial) {

        Map<Long, BigDecimal> cartoesValores = new LinkedHashMap<>();
        BigDecimal valorRestante = valorRestanteInicial;

        for (PagamentoCartaoDadosDTO dados : pagamentosCartao) {
            if (dados.valor().compareTo(VALOR_MINIMO_CARTAO) < 0) {
                throw new ValidacaoNegocioException("Valor mínimo por cartão é R$ 10.00");
            }

            Cartao cartao = cartaoRepository.findById(dados.cartaoId())
                    .orElseThrow(() -> new RecursoNaoEncontradoException(
                            "Cartão com ID " + dados.cartaoId() + " não encontrado"));

            if (!cartao.getCliente().getId().equals(cliente.getId())) {
                throw new ValidacaoNegocioException("Cartão não pertence ao cliente");
            }

            BigDecimal valorAplicar = dados.valor().min(valorRestante);
            cartoesValores.merge(dados.cartaoId(), valorAplicar, BigDecimal::add);
            valorRestante = valorRestante.subtract(valorAplicar);
        }

        if (valorRestante.compareTo(BigDecimal.ZERO) > 0) {
            throw new ValidacaoNegocioException(
                    "Formas de pagamento insuficientes. Faltam: R$ " + valorRestante);
        }

        return cartoesValores;
    }

    private BigDecimal calcularValorProdutos(Carrinho carrinho) {
        return carrinho.getItens().stream()
                .map(item -> item.getValorUnitario().multiply(BigDecimal.valueOf(item.getQuantidade())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private String gerarTransacaoId() {
        String timestamp = String.valueOf(System.currentTimeMillis());
        String random = UUID.randomUUID().toString().substring(0, 8);
        return String.format("TXN_%s_%s", timestamp, random);
    }
}
