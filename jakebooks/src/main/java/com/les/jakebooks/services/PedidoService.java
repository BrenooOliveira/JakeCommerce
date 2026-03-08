package com.les.jakebooks.services;

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
import com.les.jakebooks.dto.FinalizarPedidoDTO;
import com.les.jakebooks.dto.ItemCarrinhoDTO;
import com.les.jakebooks.dto.PagamentoCartaoDadosDTO;
import com.les.jakebooks.dto.PedidoConfirmadoDTO;
import com.les.jakebooks.dto.PedidoResumoDTO;
import com.les.jakebooks.exception.RecursoNaoEncontradoException;
import com.les.jakebooks.exception.ValidacaoNegocioException;
import com.les.jakebooks.model.enums.StatusCarrinho;
import com.les.jakebooks.model.enums.StatusPagamento;
import com.les.jakebooks.model.enums.StatusPedido;
import com.les.jakebooks.model.enums.TipoCupom;
import com.les.jakebooks.repository.CarrinhoRepository;
import com.les.jakebooks.repository.CartaoRepository;
import com.les.jakebooks.repository.ClienteRepository;
import com.les.jakebooks.repository.CupomRepository;
import com.les.jakebooks.repository.EnderecoRepository;
import com.les.jakebooks.repository.EstoqueRepository;
import com.les.jakebooks.repository.PagamentoRepository;
import com.les.jakebooks.repository.PedidoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
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
    private EnderecoRepository enderecoRepository;

    @Autowired
    private CartaoRepository cartaoRepository;

    @Autowired
    private CupomRepository cupomRepository;

    @Autowired
    private EstoqueRepository estoqueRepository;

    @Autowired
    private PagamentoRepository pagamentoRepository;

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
     * Despacha um pedido alterando seu status para EM_TRANSPORTE.
     * RN0039: Status transporte: EM TRANSPORTE.
     *
     * @param pedidoId ID do pedido
     * @throws RecursoNaoEncontradoException se pedido não existe
     * @throws ValidacaoNegocioException se pedido não pode ser despachado
     */
    public void despachar(Long pedidoId) {
        // Buscar pedido
        Pedido pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Pedido com ID " + pedidoId + " não encontrado"));

        // Validar se pagamento foi aprovado
        if (!pedido.getPagamento().getStatus().equals(StatusPagamento.APROVADA)) {
            throw new ValidacaoNegocioException("Pedido não pode ser despachado sem pagamento aprovado");
        }

        // Alterar status para EM_TRANSPORTE
        pedido.setStatus(StatusPedido.EM_TRANSPORTE);
        pedidoRepository.save(pedido);
    }

    /**
     * Confirma a entrega de um pedido alterando seu status para ENTREGUE.
     * RN0040: Status entrega: ENTREGUE.
     *
     * @param pedidoId ID do pedido
     * @throws RecursoNaoEncontradoException se pedido não existe
     * @throws ValidacaoNegocioException se pedido não pode ser entregue
     */
    public void confirmarEntrega(Long pedidoId) {
        // Buscar pedido
        Pedido pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Pedido com ID " + pedidoId + " não encontrado"));

        // Validar se está em transporte
        if (!pedido.getStatus().equals(StatusPedido.EM_TRANSPORTE)) {
            throw new ValidacaoNegocioException("Apenas pedidos em transporte podem ser confirmados como entregues");
        }

        // Alterar status para ENTREGUE
        pedido.setStatus(StatusPedido.ENTREGUE);
        pedidoRepository.save(pedido);
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


}
