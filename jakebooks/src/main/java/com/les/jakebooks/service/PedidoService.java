package com.les.jakebooks.service;

import com.les.jakebooks.domain.Carrinho;
import com.les.jakebooks.domain.Cartao;
import com.les.jakebooks.domain.Cliente;
import com.les.jakebooks.domain.Cupom;
import com.les.jakebooks.domain.Endereco;
import com.les.jakebooks.domain.Estoque;
import com.les.jakebooks.domain.ItemCarrinho;
import com.les.jakebooks.domain.ItemPedido;
import com.les.jakebooks.domain.Pagamento;
import com.les.jakebooks.domain.PagamentoCartao;
import com.les.jakebooks.domain.PagamentoCupom;
import com.les.jakebooks.domain.Pedido;
import com.les.jakebooks.dto.ConversaoPedidoDTO;
import com.les.jakebooks.dto.FinalizarPedidoDTO;
import com.les.jakebooks.dto.ItemCarrinhoDTO;
import com.les.jakebooks.dto.PagamentoCartaoDadosDTO;
import com.les.jakebooks.dto.PedidoAdminResumoDTO;
import com.les.jakebooks.dto.PedidoConfirmadoDTO;
import com.les.jakebooks.dto.PedidoDetalheDTO;
import com.les.jakebooks.dto.PedidoListagemDTO;
import com.les.jakebooks.dto.PedidoResumoDTO;
import com.les.jakebooks.dto.PedidoTransporteDTO;
import com.les.jakebooks.exception.RecursoNaoEncontradoException;
import com.les.jakebooks.exception.TransicaoStatusInvalidaException;
import com.les.jakebooks.exception.ValidacaoNegocioException;
import com.les.jakebooks.domain.enums.StatusCarrinho;
import com.les.jakebooks.domain.enums.StatusPagamento;
import com.les.jakebooks.domain.enums.StatusPedido;
import com.les.jakebooks.domain.enums.TipoCupom;
import com.les.jakebooks.repository.CarrinhoRepository;
import com.les.jakebooks.repository.CartaoRepository;
import com.les.jakebooks.repository.ClienteRepository;
import com.les.jakebooks.repository.CupomRepository;
import com.les.jakebooks.repository.EnderecoRepository;
import com.les.jakebooks.repository.EstoqueRepository;
import com.les.jakebooks.repository.PagamentoRepository;
import com.les.jakebooks.repository.PedidoRepository;
import com.les.jakebooks.service.LogService;
import com.les.jakebooks.util.SecurityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service para gerenciar operações de Pedido.
 * RF0033: Realizar compra.
 * RF0034: Calcular frete.
 * RF0035: Selecionar endereço.
 * RF0036: Selecionar pagamento (cartão, cupom promocional, cupom de troca).
 * RF0037: Finalizar compra (status inicial: EM PROCESSAMENTO).
 * RF0038: Despachar produtos (EM TRANSPORTE).
 * RF0039: Confirmar entrega (ENTREGUE).
 * RN0028: Baixa estoque apenas após pagamento aprovado.
 * RN0032: Validar estoque antes da finalização.
 * RN0033: Apenas um cupom promocional por compra.
 * RN0034: Múltiplos cartões permitidos (mínimo R$10 por cartão).
 * RN0035: Consumir cupons antes do cartão.
 * RN0036: Gerar cupom para excedente.
 * RN0037: Validar pagamento.
 * RN0038: Status pagamento: APROVADA ou REPROVADA.
 * RN0039: Status transporte: EM TRANSPORTE.
 * RN0040: Status entrega: ENTREGUE.
 * RN0064: Pedido mínimo 20 sem frete.
 * RN0065: 3 pagamentos REPROVADOS consecutivos bloqueiam carrinho.
 */
@Service
@Transactional
public class PedidoService {

    @Autowired
    private PedidoRepository pedidoRepository;

    @Autowired
    private CarrinhoRepository carrinhoRepository;

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private ClienteService clienteService;

    @Autowired
    private EnderecoRepository enderecoRepository;

    @Autowired
    private CartaoRepository cartaoRepository;

    @Autowired
    private CupomRepository cupomRepository;

    @Autowired
    private EstoqueRepository estoqueRepository;

    @Autowired
    private PagamentoRepository pagamentoRepository;

    @Autowired
    private LogService logService;

    // Constantes
    private static final BigDecimal VALOR_MINIMO_SEM_FRETE = new BigDecimal("20.00");
    private static final BigDecimal VALOR_MINIMO_CARTAO = new BigDecimal("10.00");
    private static final int TENTATIVAS_PAGAMENTO_REPROVADAS_BLOQUEIO = 3;

    /**
     * Calcula o frete para um endereço específico.
     * RF0034: Calcular frete.
     *
     * @param carrinhoId  ID do carrinho
     * @param enderecoId  ID do endereço
     * @return o valor do frete
     * @throws RecursoNaoEncontradoException se carrinho ou endereço não existe
     */
    public BigDecimal calcularFrete(Long carrinhoId, Long enderecoId) {
        // Validar se carrinho existe
        Carrinho carrinho = carrinhoRepository.findById(carrinhoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Carrinho com ID " + carrinhoId + " não encontrado"));

        // Validar se endereço existe
        Endereco endereco = enderecoRepository.findById(enderecoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Endereço com ID " + enderecoId + " não encontrado"));

        // Calcular valor total dos itens do carrinho
        BigDecimal valorTotal = carrinho.getItens().stream()
                .map(item -> item.getValorUnitario().multiply(BigDecimal.valueOf(item.getQuantidade())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // RN0064: Pedido mínimo 20 sem frete
        if (valorTotal.compareTo(VALOR_MINIMO_SEM_FRETE) >= 0) {
            return BigDecimal.ZERO;
        }

        // Cálculo simples de frete: R$ 15.00 padrão + R$ 1.00 por km aproximado
        // Simulação: usar código postal como base para distância
        BigDecimal frete = new BigDecimal("15.00");
        
        // Adicionar variação por estado (simulado)
        if (endereco.getEstado() != null && endereco.getEstado().length() > 0) {
            // Adicionar R$ 2.00 por estado distante (próximos da região sul/norte)
            if (endereco.getEstado().matches("(AM|AP|RR|AC|MT|PA)")) {
                frete = frete.add(new BigDecimal("10.00"));
            } else if (endereco.getEstado().matches("(RS|SC|PR)")) {
                frete = frete.add(new BigDecimal("5.00"));
            }
        }

        return frete;
    }

    /**
     * Finaliza um pedido com todas as validações e criação de Pagamento.
     * RF0033: Realizar compra.
     * RF0037: Finalizar compra.
     * RN0032: Validar estoque.
     * RN0033: Apenas um cupom promocional.
     * RN0034: Múltiplos cartões.
     * RN0035: Consumir cupons antes do cartão.
     * RN0064: Pedido mínimo R$20.
     *
     * @param dto dados para finalizar pedido
     * @return DTO do pedido confirmado
     * @throws ValidacaoNegocioException se validações falham
     * @throws RecursoNaoEncontradoException se recursos não existem
     */
    public PedidoConfirmadoDTO finalizarPedido(FinalizarPedidoDTO dto) {
        // Buscar cliente
        Cliente cliente = clienteRepository.findByCodigo(dto.codigoCliente())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Cliente com código " + dto.codigoCliente() + " não encontrado"));

        // Buscar carrinho
        Carrinho carrinho = carrinhoRepository.findById(dto.carrinhoId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Carrinho com ID " + dto.carrinhoId() + " não encontrado"));

        // Validar se carrinho pertence ao cliente
        if (!carrinho.getCliente().getId().equals(cliente.getId())) {
            throw new ValidacaoNegocioException("Carrinho não pertence ao cliente");
        }

        // Buscar endereço
        Endereco endereco = enderecoRepository.findById(dto.enderecoId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Endereço com ID " + dto.enderecoId() + " não encontrado"));

        // Validar se carrinho tem itens
        if (carrinho.getItens().isEmpty()) {
            throw new ValidacaoNegocioException("Carrinho está vazio");
        }

        // RN0032: Revalidar estoque de todos os itens
        for (ItemCarrinho item : carrinho.getItens()) {
            Estoque estoque = estoqueRepository.findByLivroId(item.getLivro().getId());
            if (estoque == null || estoque.getQuantidade() < item.getQuantidade()) {
                throw new ValidacaoNegocioException("Estoque insuficiente para o livro: " + item.getLivro().getTitulo());
            }
        }

        // Calcular valor total dos produtos
        BigDecimal valorProdutos = carrinho.getItens().stream()
                .map(item -> item.getValorUnitario().multiply(BigDecimal.valueOf(item.getQuantidade())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Calcular frete
        BigDecimal valorFrete = calcularFrete(dto.carrinhoId(), dto.enderecoId());

        // RN0064: Validar pedido mínimo sem frete
        BigDecimal valorTotal = valorProdutos.add(valorFrete);

        // Montar Pagamento
        Pagamento pagamento = new Pagamento();
        pagamento.setStatus(StatusPagamento.PENDENTE);

        BigDecimal valorRestante = valorTotal;

        // RN0035: Consumir cupons antes do cartão
        // RN0033: Apenas um cupom promocional por compra
        if (dto.codigoCupomPromocional() != null && !dto.codigoCupomPromocional().isEmpty()) {
            Cupom cupom = cupomRepository.findByCodigoAndAtivoTrue(dto.codigoCupomPromocional())
                    .orElseThrow(() -> new ValidacaoNegocioException("Cupom inválido ou expirado"));

            // Validar se é cupom promocional
            if (!cupom.getTipo().equals(TipoCupom.PROMOCIONAL)) {
                throw new ValidacaoNegocioException("Apenas cupons promocionais podem ser usados nesta compra");
            }

            // Usar cupom (parcial ou total)
            BigDecimal valorCupom = cupom.getValor().min(valorRestante);
            PagamentoCupom pagamentoCupom = new PagamentoCupom(valorCupom, cupom);
            pagamentoCupom.setPagamento(pagamento);
            pagamento.getPagamentosCupom().add(pagamentoCupom);

            valorRestante = valorRestante.subtract(valorCupom);
        }

        // RN0034: Múltiplos cartões com mínimo R$10
        if (dto.pagamentosCartao() != null && !dto.pagamentosCartao().isEmpty()) {
            for (PagamentoCartaoDadosDTO dados : dto.pagamentosCartao()) {
                // Validar valor mínimo por cartão
                if (dados.valor().compareTo(VALOR_MINIMO_CARTAO) < 0) {
                    throw new ValidacaoNegocioException("Valor mínimo por cartão é R$ 10.00");
                }

                // Buscar cartão
                Cartao cartao = cartaoRepository.findById(dados.cartaoId())
                        .orElseThrow(() -> new RecursoNaoEncontradoException("Cartão com ID " + dados.cartaoId() + " não encontrado"));

                // Validar se cartão pertence ao cliente
                if (!cartao.getCliente().getId().equals(cliente.getId())) {
                    throw new ValidacaoNegocioException("Cartão não pertence ao cliente");
                }

                // Usar valor do cartão (máximo o valor restante)
                BigDecimal valorCartao = dados.valor().min(valorRestante);
                PagamentoCartao pagamentoCartao = new PagamentoCartao(valorCartao, cartao);
                pagamentoCartao.setPagamento(pagamento);
                pagamento.getPagamentosCartao().add(pagamentoCartao);

                valorRestante = valorRestante.subtract(valorCartao);
            }
        }

        // RN0036: Gerar cupom para excedente se houver valores em excesso
        if (valorRestante.compareTo(BigDecimal.ZERO) < 0) {
            // Há excedente
            BigDecimal valorExcedente = valorRestante.negate();
            Cupom cupomExcedente = new Cupom();
            cupomExcedente.setCodigo("TROCA-" + UUID.randomUUID().toString().substring(0, 8));
            cupomExcedente.setValor(valorExcedente);
            cupomExcedente.setTipo(TipoCupom.TROCA);
            cupomExcedente.setAtivo(true);
            cupomRepository.save(cupomExcedente);

            valorRestante = BigDecimal.ZERO;
        }

        // Validar se o pagamento foi totalmente coberto
        if (valorRestante.compareTo(BigDecimal.ZERO) > 0) {
            throw new ValidacaoNegocioException("Formas de pagamento insuficientes. Faltam: R$ " + valorRestante);
        }

        pagamento.setValorTotal(valorTotal);
        pagamento = pagamentoRepository.save(pagamento);

        // Criar Pedido com status EM_PROCESSAMENTO
        Pedido pedido = new Pedido();
        pedido.setCliente(cliente);
        pedido.setEndereco(endereco);
        pedido.setDataCriacao(LocalDate.now());
        pedido.setStatus(StatusPedido.EM_PROCESSAMENTO);
        pedido.setValorTotal(valorTotal);
        pedido.setValorFrete(valorFrete);
        pedido.setPagamento(pagamento);

        // Adicionar itens do carrinho ao pedido
        for (ItemCarrinho itemCarrinho : carrinho.getItens()) {
            ItemPedido itemPedido = new ItemPedido();
            itemPedido.setLivro(itemCarrinho.getLivro());
            itemPedido.setQuantidade(itemCarrinho.getQuantidade());
            itemPedido.setValorUnitario(itemCarrinho.getValorUnitario());
            itemPedido.setPedido(pedido);
            pedido.getItens().add(itemPedido);
        }

        pedido = pedidoRepository.save(pedido);

        // Marcar carrinho como finalizado
        carrinho.setStatus(StatusCarrinho.FINALIZADO);
        carrinhoRepository.save(carrinho);

        // Retornar DTO do pedido confirmado
        return new PedidoConfirmadoDTO(
                pedido.getId(),
                pedido.getDataCriacao(),
                pedido.getStatus(),
                pedido.getValorTotal(),
                pedido.getValorFrete(),
                valorProdutos,
                cliente.getNome(),
                endereco.getLogradouro() + ", " + endereco.getNumero() + " - " + endereco.getCidade() + "/" + endereco.getEstado(),
                carrinho.getItens().stream()
                        .map(item -> new ItemCarrinhoDTO(
                                item.getId(),
                                item.getLivro().getId(),
                                item.getLivro().getCodigo(),
                                item.getLivro().getTitulo(),
                                item.getQuantidade(),
                                item.getValorUnitario(),
                                item.getValorUnitario().multiply(BigDecimal.valueOf(item.getQuantidade()))
                        ))
                        .collect(Collectors.toList()),
                "Pedido finalizado com sucesso! Número do pedido: " + pedido.getId()
        );
    }

    /**
     * Converte carrinho em pedido após pagamento aprovado.
     * TASK-CHK-03: Converter Carrinho em Pedido
     * RF0037: Finalizar compra (status inicial: EM_PROCESSAMENTO)
     * RN0064: Pedido mínimo 20 sem frete
     *
     * Pré-condições:
     * - Carrinho com status ABERTO
     * - Pagamento processado com status APROVADA
     * - Endereço de entrega selecionado
     * - Frete calculado
     *
     * Pós-condições:
     * - Pedido criado com status EM_PROCESSAMENTO
     * - ItemPedido criado para cada ItemCarrinho
     * - Carrinho NÃO alterado (será finalizado por CompraService)
     *
     * @param dados DTO com dados para conversão
     * @return Pedido criado
     * @throws ValidacaoNegocioException se validações falham
     * @throws RecursoNaoEncontradoException se carrinho não existe
     */
    @Transactional
    public Pedido converterCarrinhoEmPedido(ConversaoPedidoDTO dados) {
        // Buscar carrinho pelo ID
        Carrinho carrinho = carrinhoRepository.findById(dados.getCarrinhoId())
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                    "Carrinho com ID " + dados.getCarrinhoId() + " não encontrado"));

        // Validar pré-condições
        validarConversao(carrinho, dados);

        // Criar pedido
        Pedido pedido = criarPedido(carrinho, dados);
        pedido = pedidoRepository.save(pedido);

        // Criar itens do pedido a partir dos itens do carrinho
        for (ItemCarrinho itemCarrinho : carrinho.getItens()) {
            ItemPedido itemPedido = new ItemPedido();
            itemPedido.setPedido(pedido);
            itemPedido.setLivro(itemCarrinho.getLivro());
            itemPedido.setQuantidade(itemCarrinho.getQuantidade());
            itemPedido.setValorUnitario(itemCarrinho.getValorUnitario());
            pedido.getItens().add(itemPedido);
        }

        // Salvar pedido novamente com itens
        pedido = pedidoRepository.save(pedido);

        return pedido;
    }

    /**
     * Valida pré-condições para conversão de carrinho em pedido.
     * TASK-CHK-03: Validações específicas para conversão
     *
     * @param carrinho carrinho a ser convertido
     * @param dados dados da conversão
     * @throws ValidacaoNegocioException se validações falham
     */
    private void validarConversao(Carrinho carrinho, ConversaoPedidoDTO dados) {
        // Validar status do carrinho
        if (carrinho.getStatus() != StatusCarrinho.ABERTO) {
            throw new ValidacaoNegocioException(
                "Carrinho não está disponível para conversão. Status atual: " + carrinho.getStatus()
            );
        }

        // Validar pagamento aprovado
        if (dados.getPagamento().getStatus() != StatusPagamento.APROVADA) {
            throw new ValidacaoNegocioException(
                "Conversão permitida apenas para pagamentos aprovados. Status: " +
                dados.getPagamento().getStatus()
            );
        }

        // Validar se carrinho tem itens
        if (carrinho.getItens() == null || carrinho.getItens().isEmpty()) {
            throw new ValidacaoNegocioException("Carrinho está vazio");
        }

        // Validar se endereço foi fornecido
        if (dados.getEnderecoEntrega() == null) {
            throw new ValidacaoNegocioException("Endereço de entrega não foi selecionado");
        }

        // Validar se frete foi calculado
        if (dados.getValorFrete() == null) {
            throw new ValidacaoNegocioException("Valor do frete não foi calculado");
        }
    }

    /**
     * Cria entidade Pedido a partir do carrinho e dados fornecidos.
     * TASK-CHK-03: Criação do pedido
     *
     * @param carrinho carrinho fonte
     * @param dados dados da conversão
     * @return Pedido criado (ainda não salvo)
     */
    private Pedido criarPedido(Carrinho carrinho, ConversaoPedidoDTO dados) {
        Pedido pedido = new Pedido();

        // Dados básicos
        pedido.setDataCriacao(LocalDate.now());
        pedido.setStatus(StatusPedido.EM_PROCESSAMENTO);
        pedido.setCliente(carrinho.getCliente());
        pedido.setEndereco(dados.getEnderecoEntrega());
        pedido.setPagamento(dados.getPagamento());
        pedido.setValorFrete(dados.getValorFrete());

        // Calcular valor total: subtotal itens + frete - cupons
        BigDecimal subtotalItens = calcularSubtotalItens(carrinho.getItens());
        BigDecimal valorCupons = dados.getPagamento().getValorPagoCupons() != null
                ? dados.getPagamento().getValorPagoCupons()
                : BigDecimal.ZERO;
        BigDecimal valorTotal = subtotalItens.add(dados.getValorFrete()).subtract(valorCupons);

        // Garantir que valor total não seja negativo
        pedido.setValorTotal(valorTotal.max(BigDecimal.ZERO));

        return pedido;
    }

    /**
     * Calcula subtotal dos itens do carrinho.
     * TASK-CHK-03: Cálculo auxiliar
     *
     * @param itens itens do carrinho
     * @return subtotal
     */
    private BigDecimal calcularSubtotalItens(List<ItemCarrinho> itens) {
        return itens.stream()
                .map(item -> item.getValorUnitario().multiply(
                    BigDecimal.valueOf(item.getQuantidade())
                ))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Processa o pagamento de um pedido.
     * RN0028: Baixa estoque apenas após pagamento aprovado.
     * RN0038: Status do pagamento APROVADA ou REPROVADA.
     * RN0065: 3 pagamentos REPROVADOS consecutivos bloqueiam cliente.
     *
     * @param pedidoId ID do pedido
     * @throws RecursoNaoEncontradoException se pedido não existe
     * @throws ValidacaoNegocioException se validações falham
     */
    public void processarPagamento(Long pedidoId) {
        // Buscar pedido
        Pedido pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Pedido com ID " + pedidoId + " não encontrado"));

        // Simular validação de pagamento (aqui integraria com gateway)
        // Por exemplo, validar dados dos cartões
        boolean pagamentoAprovado = true;

        if (pagamentoAprovado) {
            // RN0028: Baixar estoque após pagamento aprovado
            for (ItemPedido item : pedido.getItens()) {
                Estoque estoque = estoqueRepository.findByLivroId(item.getLivro().getId());
                if (estoque != null) {
                    int novaQuantidade = estoque.getQuantidade() - item.getQuantidade();
                    estoque.setQuantidade(novaQuantidade);
                    estoqueRepository.save(estoque);
                }
            }

            // Marcar pagamento como aprovado
            pedido.getPagamento().setStatus(StatusPagamento.APROVADA);
            pagamentoRepository.save(pedido.getPagamento());
        } else {
            // Marcar pagamento como reprovado
            pedido.getPagamento().setStatus(StatusPagamento.REPROVADA);
            pagamentoRepository.save(pedido.getPagamento());

            // RN0065: Verificar 3 pagamentos REPROVADOS consecutivos
            List<Pagamento> pagamentosReprovados = pagamentoRepository.findByPedidoClienteIdAndStatusOrderByDataCriacaoDesc(
                    pedido.getCliente().getId(), StatusPagamento.REPROVADA);

            if (pagamentosReprovados.size() >= TENTATIVAS_PAGAMENTO_REPROVADAS_BLOQUEIO) {
                // Bloquear carrinho do cliente
                Optional<Carrinho> carrinhoAberto = carrinhoRepository.findByClienteIdAndStatusEquals(
                        pedido.getCliente().getId(), StatusCarrinho.ABERTO);

                if (carrinhoAberto.isPresent()) {
                    Carrinho carrinho = carrinhoAberto.get();
                    carrinho.setStatus(StatusCarrinho.EXPIRADO);  // Usar EXPIRADO como bloqueado
                    carrinhoRepository.save(carrinho);
                }
            }

            throw new ValidacaoNegocioException("Pagamento foi reprovado. Por favor, tente novamente com outro cartão.");
        }
    }

    /**
     * Despacha pedido alterando status para EM_TRANSPORTE.
     * TASK-SHP-04: Implementação completa de despacho de pedidos.
     * RF0038: Despachar produtos (EM_TRANSPORTE).
     * RN0039: Status transporte: EM_TRANSPORTE (transição válida apenas de EM_PROCESSAMENTO).
     *
     * @param pedidoId ID do pedido
     * @return DTO do pedido despachado
     * @throws RecursoNaoEncontradoException se pedido não existe
     * @throws TransicaoStatusInvalidaException se status atual não é EM_PROCESSAMENTO
     * @throws ValidacaoNegocioException para outras validações de negócio
     */
    @Transactional
    public PedidoAdminResumoDTO despacharPedido(Long pedidoId) {
        // Validar autorização: apenas admin pode despachar pedidos (RF0038)
        if (!SecurityUtil.isAdmin()) {
            throw new ValidacaoNegocioException(
                "Acesso negado. Apenas administradores podem despachar pedidos."
            );
        }

        // Buscar pedido
        Pedido pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                    "Pedido não encontrado: " + pedidoId
                ));

        // Validar transição de status específica (TASK-SHP-04)
        if (pedido.getStatus() != StatusPedido.EM_PROCESSAMENTO) {
            throw new TransicaoStatusInvalidaException(
                "Pedido deve estar EM_PROCESSAMENTO para ser despachado. " +
                "Status atual: " + pedido.getStatus()
            );
        }

        // Validar se pagamento foi aprovado
        if (!pedido.getPagamento().getStatus().equals(StatusPagamento.APROVADA)) {
            throw new ValidacaoNegocioException("Pedido não pode ser despachado sem pagamento aprovado");
        }

        // Capturar estado anterior para log
        StatusPedido statusAnterior = pedido.getStatus();

        // Alterar status para EM_TRANSPORTE e registrar data de despacho
        pedido.setStatus(StatusPedido.EM_TRANSPORTE);
        pedido.setDataDespacho(LocalDateTime.now());

        pedido = pedidoRepository.save(pedido);

        // Registrar log da operação (RNF0012)
        logService.registrar(
            "DESPACHAR_PEDIDO",
            "Pedido",
            "Status: " + statusAnterior,
            "Status: " + pedido.getStatus() + ", Data Despacho: " + pedido.getDataDespacho(),
            "Pedido " + pedidoId + " despachado por administrador"
        );

        // Retornar DTO do pedido
        return toAdminResumoDTO(pedido);
    }

    /**
     * Busca todos os pedidos de um cliente.
     * RF0025: Consultar transações do cliente.
     *
     * @param codigoCliente código do cliente
     * @return lista de DTOs dos pedidos
     * @throws RecursoNaoEncontradoException se cliente não existe
     */
    public List<PedidoResumoDTO> buscarPorCliente(String codigoCliente) {
        // Validar se cliente existe
        clienteRepository.findByCodigo(codigoCliente)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Cliente com código " + codigoCliente + " não encontrado"));

        // Buscar pedidos do cliente
        List<Pedido> pedidos = pedidoRepository.findByClienteCodigoOrderByDataCriacaoDesc(codigoCliente);

        // Converter para DTO
        return pedidos.stream()
                .map(pedido -> new PedidoResumoDTO(
                        pedido.getId(),
                        pedido.getDataCriacao(),
                        pedido.getStatus(),
                        pedido.getValorTotal(),
                        pedido.getValorFrete(),
                        pedido.getItens().size()
                ))
                .collect(Collectors.toList());
    }

    /**
     * Lista pedidos aguardando despacho (status EM_PROCESSAMENTO).
     * TASK-SHP-04: Método específico para área administrativa.
     * RF0038: Despachar produtos (EM_TRANSPORTE).
     *
     * @return lista de DTOs dos pedidos para despacho ordenados por data de criação
     */
    public List<PedidoAdminResumoDTO> listarPedidosParaDespacho() {
        // Validar autorização: apenas admin pode listar pedidos para despacho
        if (!SecurityUtil.isAdmin()) {
            throw new ValidacaoNegocioException(
                "Acesso negado. Apenas administradores podem listar pedidos para despacho."
            );
        }

        // Buscar pedidos com status EM_PROCESSAMENTO
        List<Pedido> pedidos = pedidoRepository.findByStatusOrderByDataCriacaoDesc(StatusPedido.EM_PROCESSAMENTO);

        // Converter para DTO administrativo
        return pedidos.stream()
                .map(this::toAdminResumoDTO)
                .collect(Collectors.toList());
    }

    /**
     * Converte entidade Pedido para PedidoAdminResumoDTO.
     * TASK-SHP-04: DTO específico para área administrativa.
     *
     * @param pedido entidade do pedido
     * @return DTO para administração
     */
    private PedidoAdminResumoDTO toAdminResumoDTO(Pedido pedido) {
        // Monta string do codigo do pedido
        String codigoPedido = "PED-" + String.format("%06d", pedido.getId());

        // Monta endereço de entrega completo
        Endereco endereco = pedido.getEndereco();
        String enderecoCompleto = endereco.getLogradouro() + ", " +
                                 endereco.getNumero() + " - " +
                                 endereco.getBairro() + " - " +
                                 endereco.getCidade() + "/" + endereco.getEstado();

        // Converte data de criação para LocalDateTime
        LocalDateTime dataCriacaoDateTime = pedido.getDataCriacao().atStartOfDay();

        return new PedidoAdminResumoDTO(
                pedido.getId(),
                codigoPedido,
                dataCriacaoDateTime,
                pedido.getCliente().getNome(),
                enderecoCompleto,
                pedido.getValorTotal(),
                pedido.getStatus(),
                pedido.getItens().size()
        );
    }

    /**
     * Confirma entrega de pedido alterando status para ENTREGUE.
     * TASK-SHP-05: Implementação completa de confirmação de entrega.
     * RF0039: Confirmar entrega (ENTREGUE).
     * RN0040: Status entrega: ENTREGUE (transição válida apenas de EM_TRANSPORTE).
     *
     * @param pedidoId ID do pedido
     * @return DTO do pedido com entrega confirmada
     * @throws RecursoNaoEncontradoException se pedido não existe
     * @throws TransicaoStatusInvalidaException se status atual não é EM_TRANSPORTE
     * @throws ValidacaoNegocioException para outras validações de negócio
     */
    @Transactional
    public PedidoTransporteDTO confirmarEntrega(Long pedidoId) {
        // Validar autorização: apenas admin pode confirmar entrega (RF0039)
        if (!SecurityUtil.isAdmin()) {
            throw new ValidacaoNegocioException(
                "Acesso negado. Apenas administradores podem confirmar entrega de pedidos."
            );
        }

        // Buscar pedido
        Pedido pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                    "Pedido não encontrado: " + pedidoId
                ));

        // Validar transição de status específica (TASK-SHP-05)
        if (pedido.getStatus() != StatusPedido.EM_TRANSPORTE) {
            throw new TransicaoStatusInvalidaException(
                "Pedido deve estar EM_TRANSPORTE para confirmar entrega. " +
                "Status atual: " + pedido.getStatus()
            );
        }

        // Capturar estado anterior para log
        StatusPedido statusAnterior = pedido.getStatus();

        // Alterar status para ENTREGUE e registrar data de entrega
        pedido.setStatus(StatusPedido.ENTREGUE);
        pedido.setDataEntrega(LocalDateTime.now());

        // Habilitar opção de troca para o cliente
        pedido.setTrocaHabilitada(true);

        pedido = pedidoRepository.save(pedido);

        // Registrar log da operação (RNF0012)
        logService.registrar(
            "CONFIRMAR_ENTREGA",
            "Pedido",
            "Status: " + statusAnterior,
            "Status: " + pedido.getStatus() + ", Data Entrega: " + pedido.getDataEntrega() + ", Troca Habilitada: " + pedido.getTrocaHabilitada(),
            "Pedido " + pedidoId + " entrega confirmada por administrador"
        );

        // Retornar DTO do pedido
        return toTransporteDTO(pedido);
    }

    /**
     * Lista pedidos em transporte aguardando confirmação de entrega (status EM_TRANSPORTE).
     * TASK-SHP-05: Método específico para área administrativa.
     * RF0039: Confirmar entrega (ENTREGUE).
     *
     * @return lista de DTOs dos pedidos em transporte ordenados por data de despacho
     */
    public List<PedidoTransporteDTO> listarPedidosEmTransporte() {
        // Validar autorização: apenas admin pode listar pedidos em transporte
        if (!SecurityUtil.isAdmin()) {
            throw new ValidacaoNegocioException(
                "Acesso negado. Apenas administradores podem listar pedidos em transporte."
            );
        }

        // Buscar pedidos com status EM_TRANSPORTE
        List<Pedido> pedidos = pedidoRepository.findByStatusOrderByDataDespachoAsc(StatusPedido.EM_TRANSPORTE);

        // Converter para DTO com cálculo de dias em transporte
        return pedidos.stream()
                .map(this::toTransporteDTO)
                .collect(Collectors.toList());
    }

    /**
     * Converte entidade Pedido para PedidoTransporteDTO.
     * TASK-SHP-05: DTO específico para pedidos em transporte.
     *
     * @param pedido entidade do pedido
     * @return DTO para transporte com cálculo de dias
     */
    private PedidoTransporteDTO toTransporteDTO(Pedido pedido) {
        // Monta string do codigo do pedido
        String codigoPedido = "PED-" + String.format("%06d", pedido.getId());

        // Monta endereço de entrega completo
        Endereco endereco = pedido.getEndereco();
        String enderecoCompleto = endereco.getLogradouro() + ", " +
                                 endereco.getNumero() + " - " +
                                 endereco.getBairro() + " - " +
                                 endereco.getCidade() + "/" + endereco.getEstado();

        // Calcular dias em transporte
        long diasEmTransporte = 0;
        boolean atrasado = false;

        if (pedido.getDataDespacho() != null) {
            diasEmTransporte = ChronoUnit.DAYS.between(
                pedido.getDataDespacho().toLocalDate(),
                LocalDateTime.now().toLocalDate()
            );

            // Considera atrasado se mais de 15 dias (conforme especificação)
            atrasado = diasEmTransporte > 15;
        }

        return new PedidoTransporteDTO(
                pedido.getId(),
                codigoPedido,
                pedido.getDataDespacho(),
                pedido.getCliente().getNome(),
                enderecoCompleto,
                pedido.getValorTotal(),
                pedido.getStatus(),
                pedido.getItens().size(),
                diasEmTransporte,
                atrasado
        );
    }

    /**
     * Retorna contagem de pedidos por status.
     * TASK-SHP-06: Dashboard de pedidos administrativo.
     *
     * @return map com contagem por status
     */
    public Map<StatusPedido, Long> contarPedidosPorStatus() {
        // Validar autorização: apenas admin pode acessar contadores
        if (!SecurityUtil.isAdmin()) {
            throw new ValidacaoNegocioException(
                "Acesso negado. Apenas administradores podem acessar estatísticas de pedidos."
            );
        }

        return Arrays.stream(StatusPedido.values())
                .collect(Collectors.toMap(
                    status -> status,
                    status -> pedidoRepository.countByStatus(status)
                ));
    }

    /**
     * Lista pedidos filtrados por status com paginação.
     * TASK-SHP-06: Listagem paginada para área administrativa.
     *
     * @param status status do pedido (null para todos)
     * @param pageable configuração de paginação
     * @return página de pedidos
     */
    public Page<PedidoListagemDTO> listarPedidos(StatusPedido status, Pageable pageable) {
        // Validar autorização: apenas admin pode listar todos os pedidos
        if (!SecurityUtil.isAdmin()) {
            throw new ValidacaoNegocioException(
                "Acesso negado. Apenas administradores podem listar todos os pedidos."
            );
        }

        Page<Pedido> pedidos;

        if (status != null) {
            pedidos = pedidoRepository.findByStatus(status, pageable);
        } else {
            pedidos = pedidoRepository.findAll(pageable);
        }

        return pedidos.map(this::toListagemDTO);
    }

    /**
     * Busca pedido por código.
     * TASK-SHP-06: Busca rápida por código de pedido.
     *
     * @param codigo código do pedido (formato PED-000001)
     * @return opcional com detalhes do pedido
     */
    public Optional<PedidoDetalheDTO> buscarPorCodigo(String codigo) {
        // Validar autorização: apenas admin pode buscar pedidos por código
        if (!SecurityUtil.isAdmin()) {
            throw new ValidacaoNegocioException(
                "Acesso negado. Apenas administradores podem buscar pedidos por código."
            );
        }

        return pedidoRepository.findByCodigo(codigo)
                .map(this::toDetalheDTO);
    }

    /**
     * Busca pedido por ID com detalhes completos.
     * TASK-SHP-06: Detalhamento de pedido específico.
     *
     * @param id ID do pedido
     * @return opcional com detalhes do pedido
     */
    public Optional<PedidoDetalheDTO> buscarPorId(Long id) {
        // Validar autorização: apenas admin pode buscar detalhes de qualquer pedido
        if (!SecurityUtil.isAdmin()) {
            throw new ValidacaoNegocioException(
                "Acesso negado. Apenas administradores podem acessar detalhes de pedidos."
            );
        }

        return pedidoRepository.findById(id)
                .map(this::toDetalheDTO);
    }

    /**
     * Converte entidade Pedido para PedidoListagemDTO.
     * TASK-SHP-06: DTO para listagem administrativa.
     *
     * @param pedido entidade do pedido
     * @return DTO para listagem
     */
    private PedidoListagemDTO toListagemDTO(Pedido pedido) {
        // Monta string do codigo do pedido
        String codigoPedido = "PED-" + String.format("%06d", pedido.getId());

        // Converte data de criação para LocalDateTime
        LocalDateTime dataCriacaoDateTime = pedido.getDataCriacao().atStartOfDay();

        return new PedidoListagemDTO(
                pedido.getId(),
                codigoPedido,
                dataCriacaoDateTime,
                pedido.getCliente().getNome(),
                pedido.getValorTotal(),
                pedido.getStatus()
        );
    }

    /**
     * Converte entidade Pedido para PedidoDetalheDTO.
     * TASK-SHP-06: DTO para detalhes completos de pedido.
     *
     * @param pedido entidade do pedido
     * @return DTO com detalhes completos
     */
    private PedidoDetalheDTO toDetalheDTO(Pedido pedido) {
        // Monta string do codigo do pedido
        String codigoPedido = "PED-" + String.format("%06d", pedido.getId());

        // Monta endereço de entrega completo
        Endereco endereco = pedido.getEndereco();
        String enderecoCompleto = endereco.getLogradouro() + ", " +
                                 endereco.getNumero() + " - " +
                                 endereco.getBairro() + " - " +
                                 endereco.getCidade() + "/" + endereco.getEstado();

        // Converte data de criação para LocalDateTime
        LocalDateTime dataCriacaoDateTime = pedido.getDataCriacao().atStartOfDay();

        // Converte itens do pedido
        List<PedidoDetalheDTO.ItemPedidoDTO> itens = pedido.getItens().stream()
                .map(item -> new PedidoDetalheDTO.ItemPedidoDTO(
                        item.getLivro().getTitulo(),
                        item.getQuantidade(),
                        item.getValorUnitario(),
                        item.getValorUnitario().multiply(BigDecimal.valueOf(item.getQuantidade()))
                ))
                .collect(Collectors.toList());

        return new PedidoDetalheDTO(
                pedido.getId(),
                codigoPedido,
                dataCriacaoDateTime,
                pedido.getDataDespacho(),
                pedido.getDataEntrega(),
                pedido.getCliente().getNome(),
                pedido.getCliente().getEmail(),
                enderecoCompleto,
                pedido.getValorTotal(),
                pedido.getValorFrete(),
                pedido.getStatus(),
                itens,
                pedido.getTrocaHabilitada() != null && pedido.getTrocaHabilitada()
        );
    }


}
