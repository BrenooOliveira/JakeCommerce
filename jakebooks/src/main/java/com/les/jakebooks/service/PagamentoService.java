package com.les.jakebooks.service;

import com.les.jakebooks.domain.Cartao;
import com.les.jakebooks.domain.Cliente;
import com.les.jakebooks.domain.Cupom;
import com.les.jakebooks.domain.Pagamento;
import com.les.jakebooks.domain.PagamentoCartao;
import com.les.jakebooks.domain.PagamentoCupom;
import com.les.jakebooks.domain.Pedido;
import com.les.jakebooks.dto.CartaoResumoDTO;
import com.les.jakebooks.dto.CupomAplicadoDTO;
import com.les.jakebooks.dto.CupomDTO;
import com.les.jakebooks.dto.OpcoesPagamentoDTO;
import com.les.jakebooks.dto.PagamentoCartaoDadosDTO;
import com.les.jakebooks.dto.ProcessarPagamentoDTO;
import com.les.jakebooks.dto.ResultadoPagamentoDTO;
import com.les.jakebooks.dto.SelecaoPagamentoDTO;
import com.les.jakebooks.exception.CarrinhoBloqueadoPagamentoException;
import com.les.jakebooks.exception.CartaoNaoSelecionadoException;
import com.les.jakebooks.exception.CupomNaoEncontradoException;
import com.les.jakebooks.exception.RecursoNaoEncontradoException;
import com.les.jakebooks.exception.ValidacaoNegocioException;
import com.les.jakebooks.exception.ValorMinimoCartaoException;
import com.les.jakebooks.exception.ValorPagamentoInsuficienteException;
import com.les.jakebooks.exception.ValorPagamentoInvalidoException;
import com.les.jakebooks.domain.enums.StatusPagamento;
import com.les.jakebooks.domain.enums.StatusPagamentoCartao;
import com.les.jakebooks.domain.enums.TipoCupom;
import com.les.jakebooks.repository.CartaoRepository;
import com.les.jakebooks.repository.CupomRepository;
import com.les.jakebooks.repository.PagamentoCartaoRepository;
import com.les.jakebooks.repository.PagamentoCupomRepository;
import com.les.jakebooks.repository.PagamentoRepository;
import com.les.jakebooks.repository.PedidoRepository;
import com.les.jakebooks.service.LogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service para gerenciar operações de Pagamento.
 * RF0036: Selecionar pagamento (cartão, cupom promocional, cupom de troca).
 * RN0033: Apenas um cupom promocional por compra.
 * RN0034: Múltiplos cartões permitidos (mínimo R$10 por cartão).
 * RN0035: Consumir cupons antes do cartão.
 * RN0036: Gerar cupom para excedente.
 * RN0037: Validar pagamento.
 * RN0038: Status pagamento: APROVADA ou REPROVADA.
 * RN0065: 3 pagamentos REPROVADOS consecutivos bloqueiam carrinho.
 */
@Service
@Transactional
public class PagamentoService {

    @Autowired
    private CupomService cupomService;

    @Autowired
    private CupomRepository cupomRepository;

    @Autowired
    private CartaoRepository cartaoRepository;

    @Autowired
    private PagamentoRepository pagamentoRepository;

    @Autowired
    private PagamentoCartaoRepository pagamentoCartaoRepository;

    @Autowired
    private PagamentoCupomRepository pagamentoCupomRepository;

    @Autowired
    private PedidoRepository pedidoRepository;

    @Autowired
    private LogService logService;

    @Autowired
    private EstoqueService estoqueService;

    @Autowired
    private PaymentGatewayService paymentGatewayService;

    // Constantes
    private static final BigDecimal VALOR_MINIMO_CARTAO = new BigDecimal("10.00");
    private static final int LIMITE_TENTATIVAS_REPROVADAS = 3;

    /**
     * Monta as opções de pagamento disponíveis para o cliente.
     * RF0036: Selecionar pagamento.
     * RN0035: Listar cupons disponíveis para consumir antes do cartão.
     *
     * @param cliente cliente logado
     * @param valorProdutos valor total dos produtos
     * @param valorFrete valor do frete
     * @return DTO com opções de pagamento
     */
    public OpcoesPagamentoDTO montarOpcoesPagamento(Cliente cliente, BigDecimal valorProdutos, BigDecimal valorFrete) {
        BigDecimal valorTotal = valorProdutos.add(valorFrete);

        // Buscar cupons de troca disponíveis
        List<CupomDTO> cuponsTroca = cupomService.listarCuponsTrocaAtivos(cliente.getId());

        // Calcular saldo total em cupons de troca
        BigDecimal saldoCuponsTroca = cuponsTroca.stream()
                .map(CupomDTO::valor)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Buscar cartões do cliente
        List<Cartao> cartoes = cartaoRepository.findByClienteId(cliente.getId());
        List<CartaoResumoDTO> cartoesDTO = cartoes.stream()
                .map(this::converterCartaoParaResumoDTO)
                .collect(Collectors.toList());

        return new OpcoesPagamentoDTO(
                valorTotal,
                valorProdutos,
                valorFrete,
                cuponsTroca,
                saldoCuponsTroca,
                cartoesDTO,
                null,  // cupom promocional será inserido pelo usuário
                valorTotal  // valor restante inicial = valor total
        );
    }

    /**
     * Calcula o valor restante após aplicação de cupons.
     * RN0035: Consumir cupons antes do cartão.
     *
     * @param valorTotal valor total a pagar
     * @param cuponsIds IDs dos cupons de troca selecionados
     * @param codigoCupomPromocional código do cupom promocional (opcional)
     * @param clienteId ID do cliente
     * @return valor restante a pagar com cartão (pode ser negativo = excedente)
     */
    public BigDecimal calcularValorRestante(BigDecimal valorTotal, List<Long> cuponsIds,
                                            String codigoCupomPromocional, Long clienteId) {
        BigDecimal valorCupons = BigDecimal.ZERO;

        // Somar cupons de troca
        if (cuponsIds != null && !cuponsIds.isEmpty()) {
            List<Cupom> cuponsTroca = cupomService.validarCuponsTroca(clienteId, cuponsIds);
            valorCupons = cupomService.calcularValorTotalCupons(cuponsTroca);
        }

        // Adicionar cupom promocional se informado
        if (codigoCupomPromocional != null && !codigoCupomPromocional.isBlank()) {
            CupomDTO promocional = cupomService.validarCupomPromocional(codigoCupomPromocional);
            valorCupons = valorCupons.add(promocional.valor());
        }

        // Calcular restante (pode ser negativo = excedente)
        return valorTotal.subtract(valorCupons);
    }

    /**
     * Processa pagamento com cupons e gera excedente se necessario.
     * RN0036: Gerar cupom para excedente.
     *
     * @param cliente cliente que esta pagando
     * @param valorTotal valor total do pedido
     * @param cuponsAplicados lista de cupons aplicados
     * @return DTO com resultado do calculo incluindo cupom excedente se houver
     */
    public ResultadoPagamentoDTO processarPagamentoCupons(
            Cliente cliente,
            BigDecimal valorTotal,
            List<CupomAplicadoDTO> cuponsAplicados) {

        // Calcular valor total dos cupons aplicados
        BigDecimal valorCupons = cuponsAplicados.stream()
                .map(CupomAplicadoDTO::valor)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Calcular valor restante
        BigDecimal valorRestante = valorTotal.subtract(valorCupons);

        CupomDTO cupomExcedente = null;

        // RN0036: Se valor dos cupons excede o total, gerar cupom de troca
        if (valorRestante.compareTo(BigDecimal.ZERO) < 0) {
            BigDecimal excedente = valorRestante.abs();
            cupomExcedente = cupomService.gerarCupomTroca(cliente, excedente, "EXCEDENTE_PAGAMENTO");

            logService.registrar(
                    "GERAR_CUPOM_EXCEDENTE",
                    "Pagamento",
                    null,
                    "Cupom: " + cupomExcedente.codigo() + ", Valor: R$ " + excedente,
                    "Cupom de troca gerado para excedente de pagamento"
            );

            valorRestante = BigDecimal.ZERO;
        }

        return new ResultadoPagamentoDTO(
                valorTotal,
                valorCupons,
                valorRestante,
                cupomExcedente,
                valorRestante.compareTo(BigDecimal.ZERO) == 0
        );
    }

    /**
     * Valida a seleção de pagamento do cliente.
     * RN0033: Apenas um cupom promocional.
     * RN0034: Mínimo R$10 por cartão.
     * RN0037: Validar pagamento.
     *
     * @param selecao dados da seleção de pagamento
     * @param valorTotal valor total a pagar
     * @param clienteId ID do cliente
     * @throws ValidacaoNegocioException se validação falhar
     */
    public void validarSelecaoPagamento(SelecaoPagamentoDTO selecao, BigDecimal valorTotal, Long clienteId) {
        // Calcular valor restante após cupons
        BigDecimal valorRestante = calcularValorRestante(
                valorTotal,
                selecao.getCuponsIds(),
                selecao.getCodigoCupomPromocional(),
                clienteId
        );

        // Se valor restante > 0, precisa de cartão
        if (valorRestante.compareTo(BigDecimal.ZERO) > 0) {
            // Validar se há pagamentos com cartão
            if (!selecao.temPagamentosCartao()) {
                throw new ValorPagamentoInsuficienteException(valorTotal,
                        valorTotal.subtract(valorRestante));
            }

            // RN0034: Validar mínimo R$10 por cartão
            for (PagamentoCartaoDadosDTO dados : selecao.getPagamentosCartao()) {
                if (dados.valor().compareTo(VALOR_MINIMO_CARTAO) < 0) {
                    throw new ValorMinimoCartaoException(dados.valor(), dados.cartaoId());
                }
            }

            // Validar se soma dos cartões cobre o valor restante
            BigDecimal totalCartoes = selecao.getValorTotalCartoes();
            if (totalCartoes.compareTo(valorRestante) < 0) {
                throw new ValorPagamentoInsuficienteException(valorRestante, totalCartoes);
            }
        }
    }

    /**
     * Cria o objeto Pagamento com base na seleção do cliente.
     * RN0035: Consumir cupons antes do cartão.
     * RN0036: Gerar cupom para excedente.
     *
     * @param selecao dados da seleção de pagamento
     * @param valorTotal valor total a pagar
     * @param cliente cliente que está pagando
     * @return objeto Pagamento criado (não persistido)
     */
    public Pagamento criarPagamento(SelecaoPagamentoDTO selecao, BigDecimal valorTotal, Cliente cliente) {
        Pagamento pagamento = new Pagamento();
        pagamento.setStatus(StatusPagamento.PENDENTE);
        pagamento.setValorTotal(valorTotal);
        pagamento.setDataCriacao(LocalDateTime.now());

        BigDecimal valorRestante = valorTotal;
        List<Cupom> cuponsConsumidos = new ArrayList<>();

        // RN0035: Aplicar cupons de troca primeiro
        if (selecao.temCuponsTroca()) {
            List<Cupom> cuponsTroca = cupomService.validarCuponsTroca(
                    cliente.getId(), selecao.getCuponsIds());

            for (Cupom cupom : cuponsTroca) {
                BigDecimal valorUsar = cupom.getValor().min(valorRestante.max(BigDecimal.ZERO));

                if (valorUsar.compareTo(BigDecimal.ZERO) > 0) {
                    PagamentoCupom pagamentoCupom = new PagamentoCupom(valorUsar, cupom);
                    pagamentoCupom.setPagamento(pagamento);
                    pagamento.getPagamentosCupom().add(pagamentoCupom);

                    valorRestante = valorRestante.subtract(cupom.getValor());
                    cuponsConsumidos.add(cupom);
                }
            }
        }

        // Aplicar cupom promocional
        if (selecao.temCupomPromocional()) {
            Cupom cupomPromocional = cupomService.buscarPorCodigo(
                    selecao.getCodigoCupomPromocional().trim().toUpperCase());

            BigDecimal valorUsar = cupomPromocional.getValor().min(valorRestante.max(BigDecimal.ZERO));

            if (valorUsar.compareTo(BigDecimal.ZERO) > 0) {
                PagamentoCupom pagamentoCupom = new PagamentoCupom(valorUsar, cupomPromocional);
                pagamentoCupom.setPagamento(pagamento);
                pagamento.getPagamentosCupom().add(pagamentoCupom);

                valorRestante = valorRestante.subtract(cupomPromocional.getValor());
                cuponsConsumidos.add(cupomPromocional);
            }
        }

        // RN0036: Gerar cupom para excedente
        if (valorRestante.compareTo(BigDecimal.ZERO) < 0) {
            BigDecimal valorExcedente = valorRestante.negate();
            CupomDTO cupomExcedente = cupomService.gerarCupomTroca(
                    cliente, valorExcedente, "EXCEDENTE_PAGAMENTO");

            logService.registrar(
                    "GERAR_CUPOM_EXCEDENTE",
                    "Pagamento",
                    null,
                    "Cupom: " + cupomExcedente.codigo() + ", Valor: R$ " + valorExcedente,
                    "Cupom de troca gerado para excedente de pagamento"
            );

            valorRestante = BigDecimal.ZERO;
        }

        // Aplicar cartões para o valor restante
        if (valorRestante.compareTo(BigDecimal.ZERO) > 0 && selecao.temPagamentosCartao()) {
            for (PagamentoCartaoDadosDTO dados : selecao.getPagamentosCartao()) {
                Cartao cartao = cartaoRepository.findById(dados.cartaoId())
                        .orElseThrow(() -> new RecursoNaoEncontradoException(
                                "Cartão não encontrado: ID " + dados.cartaoId()));

                // Validar se cartão pertence ao cliente
                if (!cartao.getCliente().getId().equals(cliente.getId())) {
                    throw new ValidacaoNegocioException("Cartão não pertence ao cliente");
                }

                BigDecimal valorCartao = dados.valor().min(valorRestante);

                PagamentoCartao pagamentoCartao = new PagamentoCartao(valorCartao, cartao);
                pagamentoCartao.setPagamento(pagamento);
                pagamento.getPagamentosCartao().add(pagamentoCartao);

                valorRestante = valorRestante.subtract(valorCartao);
            }
        }

        // Guardar cupons consumidos para marcar após aprovação
        pagamento.setCuponsConsumidos(cuponsConsumidos);

        return pagamento;
    }

    /**
     * Processa o pagamento simulando gateway.
     * RN0037: Validar pagamento.
     * RN0038: Status APROVADA ou REPROVADA.
     * RN0065: 3 reprovações consecutivas bloqueiam carrinho.
     *
     * @param pagamento pagamento a processar
     * @param cliente cliente que está pagando
     * @return pagamento com status atualizado
     */
    public Pagamento processarPagamento(Pagamento pagamento, Cliente cliente) {
        // Verificar se cliente está bloqueado por tentativas reprovadas
        verificarBloqueio(cliente.getId());

        // Simular processamento de gateway
        boolean aprovado = simularGatewayPagamento(pagamento);

        if (aprovado) {
            pagamento.setStatus(StatusPagamento.APROVADA);

            // RN0028: Reduzir estoque APÓS pagamento APROVADO
            if (pagamento.getPedido() != null) {
                try {
                    estoqueService.executarBaixaPorPedido(pagamento.getPedido());
                } catch (Exception e) {
                    // Se falhar a baixa de estoque, marcar pagamento como reprovado
                    pagamento.setStatus(StatusPagamento.REPROVADA);
                    logService.registrar(
                            "ERRO_BAIXA_ESTOQUE",
                            "Pagamento",
                            "Status: APROVADA (gateway)",
                            "Status: REPROVADA (falha estoque)",
                            "Erro ao baixar estoque: " + e.getMessage()
                    );
                    return pagamentoRepository.save(pagamento);
                }
            }

            // Consumir cupons utilizados (RN0035)
            if (pagamento.getCuponsConsumidos() != null) {
                cupomService.consumirCupons(pagamento.getCuponsConsumidos());
            }

            // Zerar contador de tentativas reprovadas
            zerarTentativasReprovadas(cliente.getId());

            logService.registrar(
                    "PAGAMENTO_APROVADO",
                    "Pagamento",
                    "Status: PENDENTE",
                    "Status: APROVADA + Estoque baixado",
                    "Pagamento aprovado e estoque reduzido - Valor: R$ " + pagamento.getValorTotal()
            );
        } else {
            pagamento.setStatus(StatusPagamento.REPROVADA);

            // Incrementar contador de tentativas reprovadas
            incrementarTentativasReprovadas(cliente.getId());

            logService.registrar(
                    "PAGAMENTO_REPROVADO",
                    "Pagamento",
                    "Status: PENDENTE",
                    "Status: REPROVADA",
                    "Pagamento reprovado - Valor: R$ " + pagamento.getValorTotal()
            );
        }

        return pagamentoRepository.save(pagamento);
    }

    /**
     * Simula processamento de gateway de pagamento.
     * Delega para PaymentGatewayService.
     *
     * @param pagamento pagamento a processar
     * @return true se aprovado, false se reprovado
     */
    private boolean simularGatewayPagamento(Pagamento pagamento) {
        // Processar cada cartão com o gateway
        if (pagamento.getPagamentosCartao() == null || pagamento.getPagamentosCartao().isEmpty()) {
            // Se não há cartões, consideramos aprovado se houver cupons que cobrem o total
            return pagamento.getValorPagoCupons() != null &&
                   pagamento.getValorPagoCupons().compareTo(pagamento.getValorTotal()) >= 0;
        }

        // Simular processamento de cada cartão
        for (PagamentoCartao pagamentoCartao : pagamento.getPagamentosCartao()) {
            StatusPagamentoCartao status = paymentGatewayService.simularProcessamento(pagamentoCartao);
            pagamentoCartao.setStatus(status);
        }

        // Pagar é aprovado se TODOS os cartões foram aprovados
        return paymentGatewayService.todosCartõesAprovados(pagamento.getPagamentosCartao());
    }

    /**
     * Verifica se o cliente está bloqueado por tentativas reprovadas.
     * RN0065: 3 pagamentos REPROVADOS consecutivos bloqueiam carrinho.
     *
     * @param clienteId ID do cliente
     * @throws CarrinhoBloqueadoPagamentoException se bloqueado
     */
    public void verificarBloqueio(Long clienteId) {
        long tentativasReprovadas = pagamentoRepository.countTentativasReprovadasConsecutivas(clienteId);

        if (tentativasReprovadas >= LIMITE_TENTATIVAS_REPROVADAS) {
            throw new CarrinhoBloqueadoPagamentoException(clienteId, (int) tentativasReprovadas);
        }
    }

    /**
     * Incrementa contador de tentativas reprovadas.
     * Implementação simplificada - em produção seria em tabela separada.
     */
    private void incrementarTentativasReprovadas(Long clienteId) {
        // Implementação feita através da contagem de pagamentos reprovados consecutivos
        // no repository (countTentativasReprovadasConsecutivas)
    }

    /**
     * Zera contador de tentativas reprovadas após pagamento aprovado.
     * Implementação simplificada - em produção seria em tabela separada.
     */
    private void zerarTentativasReprovadas(Long clienteId) {
        // Implementação feita através da contagem de pagamentos reprovados consecutivos
        // no repository - quando há aprovação, a sequência é quebrada
    }

    /**
     * Valida distribuicao de pagamento entre cartoes.
     * RN0034: Minimo R$10 por cartao.
     *
     * @param cartoesValores Map de cartaoId para valor a cobrar
     * @param valorRestante valor total a ser pago com cartoes
     * @param clienteId ID do cliente
     * @throws CartaoNaoSelecionadoException se nenhum cartao selecionado
     * @throws ValorMinimoCartaoException se algum valor < R$10
     * @throws ValorPagamentoInvalidoException se soma != valorRestante
     */
    public void validarDistribuicaoCartoes(
            Map<Long, BigDecimal> cartoesValores,
            BigDecimal valorRestante,
            Long clienteId) {

        if (cartoesValores == null || cartoesValores.isEmpty()) {
            throw new CartaoNaoSelecionadoException();
        }

        BigDecimal somaValores = BigDecimal.ZERO;

        for (Map.Entry<Long, BigDecimal> entry : cartoesValores.entrySet()) {
            Long cartaoId = entry.getKey();
            BigDecimal valor = entry.getValue();

            // Validar que cartao pertence ao cliente
            Cartao cartao = cartaoRepository.findById(cartaoId)
                    .orElseThrow(() -> new RecursoNaoEncontradoException(
                            "Cartao nao encontrado: ID " + cartaoId));

            if (!cartao.getCliente().getId().equals(clienteId)) {
                throw new ValidacaoNegocioException("Cartao nao pertence ao cliente");
            }

            // Validar valor minimo por cartao (RN0034)
            if (valor.compareTo(VALOR_MINIMO_CARTAO) < 0) {
                throw new ValorMinimoCartaoException(valor, cartaoId);
            }

            somaValores = somaValores.add(valor);
        }

        // Validar que soma dos valores = valor restante (tolerancia de 1 centavo)
        BigDecimal diferenca = somaValores.subtract(valorRestante).abs();
        if (diferenca.compareTo(new BigDecimal("0.01")) > 0) {
            throw new ValorPagamentoInvalidoException(somaValores, valorRestante);
        }
    }

    /**
     * Sugere distribuicao automatica entre cartoes.
     * RN0034: Minimo R$10 por cartao.
     *
     * @param cartoesIds IDs dos cartoes selecionados
     * @param valorRestante valor a ser distribuido
     * @return Map de cartaoId para valor sugerido
     */
    public Map<Long, BigDecimal> sugerirDistribuicao(List<Long> cartoesIds, BigDecimal valorRestante) {
        if (cartoesIds == null || cartoesIds.isEmpty()) {
            return Map.of();
        }

        Map<Long, BigDecimal> distribuicao = new HashMap<>();

        if (cartoesIds.size() == 1) {
            // Um cartao: valor total
            distribuicao.put(cartoesIds.get(0), valorRestante);
        } else {
            // Multiplos cartoes: dividir igualmente
            BigDecimal valorPorCartao = valorRestante.divide(
                    new BigDecimal(cartoesIds.size()),
                    2,
                    RoundingMode.DOWN
            );

            // Se valor por cartao < 10, usar apenas cartoes suficientes
            if (valorPorCartao.compareTo(VALOR_MINIMO_CARTAO) < 0) {
                int maxCartoes = valorRestante.divide(VALOR_MINIMO_CARTAO, 0, RoundingMode.DOWN).intValue();
                if (maxCartoes == 0) {
                    maxCartoes = 1;
                }
                valorPorCartao = valorRestante.divide(new BigDecimal(maxCartoes), 2, RoundingMode.DOWN);

                for (int i = 0; i < maxCartoes && i < cartoesIds.size(); i++) {
                    distribuicao.put(cartoesIds.get(i), valorPorCartao);
                }
            } else {
                for (Long cartaoId : cartoesIds) {
                    distribuicao.put(cartaoId, valorPorCartao);
                }
            }

            // Ajustar centavos no primeiro cartao
            BigDecimal soma = distribuicao.values().stream()
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal diferenca = valorRestante.subtract(soma);
            if (diferenca.compareTo(BigDecimal.ZERO) != 0) {
                Long primeiroCartao = new ArrayList<>(distribuicao.keySet()).get(0);
                distribuicao.put(primeiroCartao, distribuicao.get(primeiroCartao).add(diferenca));
            }
        }

        return distribuicao;
    }

    /**
     * Processa pagamento completo conforme PAY-05.
     * @return Pagamento com status APROVADA ou REPROVADA
     */
    @Transactional
    public Pagamento processar(Long pedidoId, Long cupomId, Map<Long, BigDecimal> cartoesValores, Long clienteId) {
        Pedido pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Pedido não encontrado: ID " + pedidoId));

        if (pedido.getCliente() == null || !pedido.getCliente().getId().equals(clienteId)) {
            throw new ValidacaoNegocioException("Pedido não pertence ao cliente informado");
        }

        verificarBloqueio(clienteId);

        Pagamento pagamento = new Pagamento();
        pagamento.setPedido(pedido);
        pagamento.setDataCriacao(LocalDateTime.now());
        pagamento.setValorTotal(pedido.getValorTotal());
        pagamento.setStatus(StatusPagamento.PENDENTE);

        BigDecimal valorPagoCupons = BigDecimal.ZERO;
        BigDecimal valorPagoCartoes = BigDecimal.ZERO;
        BigDecimal valorRestante = pedido.getValorTotal();
        List<Cupom> cuponsConsumidos = new ArrayList<>();

        if (cupomId != null) {
            Cupom cupom = cupomRepository.findById(cupomId)
                    .orElseThrow(() -> new CupomNaoEncontradoException(cupomId));

            if (!Boolean.TRUE.equals(cupom.getAtivo())) {
                throw new ValidacaoNegocioException("Cupom já utilizado ou inativo");
            }

            if (cupom.getCliente() != null && !cupom.getCliente().getId().equals(clienteId)) {
                throw new ValidacaoNegocioException("Cupom não pertence ao cliente informado");
            }

            BigDecimal valorAplicado = cupom.getValor().min(valorRestante.max(BigDecimal.ZERO));
            if (valorAplicado.compareTo(BigDecimal.ZERO) > 0) {
                PagamentoCupom pagamentoCupom = new PagamentoCupom(valorAplicado, cupom);
                pagamentoCupom.setPagamento(pagamento);
                pagamento.getPagamentosCupom().add(pagamentoCupom);

                valorPagoCupons = valorPagoCupons.add(valorAplicado);
                valorRestante = valorRestante.subtract(valorAplicado);
                cuponsConsumidos.add(cupom);
            }
        }

        boolean todosCartoesAprovados = true;
        if (valorRestante.compareTo(BigDecimal.ZERO) > 0) {
            if (cartoesValores == null || cartoesValores.isEmpty()) {
                throw new CartaoNaoSelecionadoException();
            }

            BigDecimal totalCartoes = BigDecimal.ZERO;
            for (Map.Entry<Long, BigDecimal> entry : cartoesValores.entrySet()) {
                Long cartaoId = entry.getKey();
                BigDecimal valor = entry.getValue();

                if (valor == null || valor.compareTo(VALOR_MINIMO_CARTAO) < 0) {
                    throw new ValorMinimoCartaoException(valor, cartaoId);
                }

                Cartao cartao = cartaoRepository.findById(cartaoId)
                        .orElseThrow(() -> new RecursoNaoEncontradoException(
                                "Cartão não encontrado: ID " + cartaoId));

                if (!cartao.getCliente().getId().equals(clienteId)) {
                    throw new ValidacaoNegocioException("Cartão não pertence ao cliente");
                }

                PagamentoCartao pagamentoCartao = new PagamentoCartao(valor, cartao);
                pagamentoCartao.setPagamento(pagamento);

                StatusPagamentoCartao statusCartao = paymentGatewayService.simularProcessamento(pagamentoCartao);
                pagamentoCartao.setStatus(statusCartao);
                pagamento.getPagamentosCartao().add(pagamentoCartao);

                if (statusCartao == StatusPagamentoCartao.APROVADO) {
                    valorPagoCartoes = valorPagoCartoes.add(valor);
                } else {
                    todosCartoesAprovados = false;
                }

                totalCartoes = totalCartoes.add(valor);
            }

            if (totalCartoes.compareTo(valorRestante) < 0) {
                throw new ValorPagamentoInsuficienteException(valorRestante, totalCartoes);
            }
        }

        pagamento.setValorPagoCupons(valorPagoCupons);
        pagamento.setValorPagoCartoes(valorPagoCartoes);

        boolean pagamentoCompleto = valorPagoCupons.add(valorPagoCartoes)
                .compareTo(pagamento.getValorTotal()) >= 0;

        if (pagamentoCompleto && todosCartoesAprovados) {
            pagamento.setStatus(StatusPagamento.APROVADA);

            // RN0028: reduzir estoque somente após aprovação.
            estoqueService.executarBaixaPorPedido(pedido);

            // RN0035: consumir cupom somente após aprovação.
            cupomService.consumirCupons(cuponsConsumidos);

            zerarTentativasReprovadas(clienteId);
        } else {
            pagamento.setStatus(StatusPagamento.REPROVADA);
            incrementarTentativasReprovadas(clienteId);
        }

        return pagamentoRepository.save(pagamento);
    }

    /**
     * Processa pagamento completo conforme PAY-05.
     * Mantém compatibilidade com fluxo atual de checkout sem pedido pré-criado.
     * @return Pagamento com status APROVADA ou REPROVADA
     */
    @Transactional
    public Pagamento processarPagamento(ProcessarPagamentoDTO dto, Long clienteId) {
        // 1. Criar entidade Pagamento
        Pagamento pagamento = new Pagamento();
        pagamento.setDataCriacao(LocalDateTime.now());
        pagamento.setValorTotal(dto.getValorTotal());
        pagamento.setStatus(StatusPagamento.PENDENTE);

        // 2. Registrar pagamentos com cupons
        BigDecimal valorPagoCupons = BigDecimal.ZERO;
        List<Cupom> cuponsConsumidos = new ArrayList<>();

        if (dto.getCuponsAplicados() != null && !dto.getCuponsAplicados().isEmpty()) {
            for (CupomAplicadoDTO cupomDto : dto.getCuponsAplicados()) {
                Cupom cupom = cupomRepository.findById(cupomDto.id())
                    .orElseThrow(() -> new CupomNaoEncontradoException(cupomDto.id()));

                if (!Boolean.TRUE.equals(cupom.getAtivo())) {
                    throw new ValidacaoNegocioException("Cupom já utilizado ou inativo");
                }

                PagamentoCupom pc = new PagamentoCupom();
                pc.setPagamento(pagamento);
                pc.setCupom(cupom);
                pc.setValor(cupomDto.valor());
                pagamento.getPagamentosCupom().add(pc);
                cuponsConsumidos.add(cupom);

                valorPagoCupons = valorPagoCupons.add(cupomDto.valor());
            }
        }

        pagamento.setValorPagoCupons(valorPagoCupons);

        // 3. Processar pagamentos com cartoes (se houver)
        BigDecimal valorPagoCartoes = BigDecimal.ZERO;
        boolean todosCartoesAprovados = true;

        if (dto.getCartoesValores() != null && !dto.getCartoesValores().isEmpty()) {
            for (Map.Entry<Long, BigDecimal> entry : dto.getCartoesValores().entrySet()) {
                Long cartaoId = entry.getKey();
                BigDecimal valor = entry.getValue();

                Cartao cartao = cartaoRepository.findById(cartaoId)
                    .orElseThrow(() -> new RecursoNaoEncontradoException(
                            "Cartão não encontrado: ID " + cartaoId));

                PagamentoCartao pc = new PagamentoCartao();
                pc.setPagamento(pagamento);
                pc.setCartao(cartao);
                pc.setValor(valor);

                // Simular processamento com gateway
                StatusPagamentoCartao statusCartao = simularGateway(cartao, valor);
                pc.setStatus(statusCartao);
                pagamento.getPagamentosCartao().add(pc);

                if (statusCartao == StatusPagamentoCartao.APROVADO) {
                    valorPagoCartoes = valorPagoCartoes.add(valor);
                } else {
                    todosCartoesAprovados = false;
                }
            }
        }

        pagamento.setValorPagoCartoes(valorPagoCartoes);

        // 4. Definir status final do pagamento
        boolean pagamentoCompleto =
            valorPagoCupons.add(valorPagoCartoes).compareTo(dto.getValorTotal()) >= 0;

        if (pagamentoCompleto && todosCartoesAprovados) {
            pagamento.setStatus(StatusPagamento.APROVADA);

            // RN0035: consumir cupons apenas após aprovação final.
            cupomService.consumirCupons(cuponsConsumidos);
        } else {
            pagamento.setStatus(StatusPagamento.REPROVADA);
        }

        pagamento = pagamentoRepository.save(pagamento);

        // 5. Log da operacao
        logService.registrar(
            pagamento.getStatus() == StatusPagamento.APROVADA ?
                "PAGAMENTO_APROVADO" : "PAGAMENTO_REPROVADO",
            "Pagamento",
            "Status: PENDENTE",
            "Status: " + pagamento.getStatus(),
            String.format("Pagamento %d - Status: %s - Valor: R$ %.2f",
                pagamento.getId(), pagamento.getStatus(), dto.getValorTotal())
        );

        return pagamento;
    }

    /**
     * Simula gateway de pagamento (ambiente academico)
     * DEPRECATED: Usar PaymentGatewayService.simularProcessamento()
     */
    @Deprecated
    private StatusPagamentoCartao simularGateway(Cartao cartao, BigDecimal valor) {
        // Delegado para PaymentGatewayService - manter por compatibilidade
        PagamentoCartao dummy = new PagamentoCartao(valor, cartao);
        return paymentGatewayService.simularProcessamento(dummy);
    }

    /**
     * Converte entidade Cartao para DTO resumido.
     *
     * @param cartao entidade a converter
     * @return DTO resumido do cartão
     */
    private CartaoResumoDTO converterCartaoParaResumoDTO(Cartao cartao) {
        // Mascarar número do cartão
        String numero = cartao.getNumero();
        String numeroMascarado = "**** **** **** " + numero.substring(numero.length() - 4);

        return new CartaoResumoDTO(
                cartao.getId(),
                numeroMascarado,
                cartao.getNomeImpresso(),
                cartao.getBandeira(),
                cartao.getPreferencial()
        );
    }
}
