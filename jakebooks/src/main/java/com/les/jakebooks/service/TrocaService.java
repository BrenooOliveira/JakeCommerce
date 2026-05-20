package com.les.jakebooks.service;

import com.les.jakebooks.domain.Cupom;
import com.les.jakebooks.domain.Estoque;
import com.les.jakebooks.domain.ItemPedido;
import com.les.jakebooks.domain.Pedido;
import com.les.jakebooks.domain.Troca;
import com.les.jakebooks.dto.ItemCarrinhoDTO;
import com.les.jakebooks.dto.TrocaDetalheDTO;
import com.les.jakebooks.exception.RecursoNaoEncontradoException;
import com.les.jakebooks.exception.ValidacaoNegocioException;
import com.les.jakebooks.domain.enums.StatusPedido;
import com.les.jakebooks.domain.enums.StatusTroca;
import com.les.jakebooks.domain.enums.TipoCupom;
import com.les.jakebooks.repository.CupomRepository;
import com.les.jakebooks.repository.EstoqueRepository;
import com.les.jakebooks.repository.PedidoRepository;
import com.les.jakebooks.repository.TrocaRepository;
import com.les.jakebooks.util.SecurityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * Service para gerenciar operações de Troca.
 * RF0040: Solicitar troca.
 * RF0041: Autorizar troca.
 * RF0043: Confirmar recebimento de troca.
 * RF0044: Gerar cupom de troca.
 * RN0043: Apenas pedidos ENTREGUES podem solicitar troca.
 * RN0042: Após troca: TROCADO.
 * RF0054: Reentrada via troca.
 */
@Service
@Transactional
public class TrocaService {

    @Autowired
    private TrocaRepository trocaRepository;

    @Autowired
    private PedidoRepository pedidoRepository;

    @Autowired
    private EstoqueRepository estoqueRepository;

    @Autowired
    private CupomRepository cupomRepository;

    /**
     * Solicita uma troca para um pedido entregue.
     * RF0040: Solicitar troca.
     * RN0043: Apenas pedidos ENTREGUES podem solicitar troca.
     * RN0041: Pedido → EM_TROCA.
     *
     * @param pedidoId ID do pedido
     * @param motivo   motivo da troca
     * @return DTO da troca criada
     * @throws RecursoNaoEncontradoException se pedido não existe
     * @throws ValidacaoNegocioException     se pedido não está entregue ou não pertence ao cliente
     */
    public TrocaDetalheDTO solicitar(Long pedidoId, String motivo) {
        return solicitar(pedidoId, motivo, null);
    }

    /**
     * Solicita uma troca especificando os itens do pedido que serão trocados.
     * Se itemIds for null ou vazio, considera todos os itens do pedido.
     */
    public TrocaDetalheDTO solicitar(Long pedidoId, String motivo, List<Long> itemIds) {
        // Buscar pedido
        Pedido pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Pedido com ID " + pedidoId + " não encontrado"));

        // Validar autorização: cliente só pode solicitar troca do próprio pedido (exceto admin)
        String emailLogado = SecurityUtil.getEmailUsuarioLogado();
        if (!SecurityUtil.isAdmin() && !pedido.getCliente().getEmail().equals(emailLogado)) {
            throw new ValidacaoNegocioException(
                "Você não tem permissão para solicitar troca deste pedido"
            );
        }

        // RN0043: Validar se pedido está ENTREGUE
        if (!pedido.getStatus().equals(StatusPedido.ENTREGUE)) {
            throw new ValidacaoNegocioException("Apenas pedidos com status ENTREGUE podem solicitar troca. Status atual: " + pedido.getStatus().getDescricao());
        }

        // Validar se já existe troca em andamento
        List<Troca> trocasAtivas = trocaRepository.findByPedidoIdAndStatus(pedidoId);
        if (!trocasAtivas.isEmpty()) {
            throw new ValidacaoNegocioException("Já existe uma troca em andamento para este pedido");
        }

        // Criar troca com status SOLICITADA
        Troca troca = new Troca();
        troca.setPedido(pedido);
        troca.setDataSolicitacao(LocalDate.now());
        troca.setStatus(StatusTroca.SOLICITADA);
        troca.setMotivo(motivo);

        // Definir itens selecionados (CSV de item_pedido ids)
        if (itemIds == null || itemIds.isEmpty()) {
            // selecionar todos
            String csv = pedido.getItens().stream()
                    .map(i -> String.valueOf(i.getId()))
                    .collect(Collectors.joining(","));
            troca.setItens(csv);
        } else {
            String csv = itemIds.stream().map(String::valueOf).collect(Collectors.joining(","));
            troca.setItens(csv);
        }

        troca = trocaRepository.save(troca);

        // RN0041: Alterar pedido para EM_TROCA
        pedido.setStatus(StatusPedido.EM_TROCA);
        pedidoRepository.save(pedido);

        // Retornar DTO
        return converterParaDTO(troca);
    }

    /**
     * Autoriza uma troca.
     * RF0041: Autorizar troca.
     *
     * @param trocaId ID da troca
     * @throws RecursoNaoEncontradoException se troca não existe
     * @throws ValidacaoNegocioException     se troca não está solicitada
     */
    public void autorizar(Long trocaId) {
        // Validar autorização: apenas admin pode autorizar trocas (RF0041)
        if (!SecurityUtil.isAdmin()) {
            throw new ValidacaoNegocioException(
                "Acesso negado. Apenas administradores podem autorizar trocas."
            );
        }

        // Buscar troca
        Troca troca = trocaRepository.findById(trocaId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Troca com ID " + trocaId + " não encontrada"));

        // Validar se está SOLICITADA
        if (!troca.getStatus().equals(StatusTroca.SOLICITADA)) {
            throw new ValidacaoNegocioException("Apenas trocas com status SOLICITADA podem ser autorizadas. Status atual: " + troca.getStatus().getDescricao());
        }

        // Alterar status para AUTORIZADA
        troca.setStatus(StatusTroca.AUTORIZADA);
        trocaRepository.save(troca);
    }

    /**
     * Descarta (nega) uma troca. Apenas admin.
     * Altera status para DESCARTADA e restaura o pedido para ENTREGUE.
     *
     * @param trocaId ID da troca
     * @param justificativa motivo/observação da decisão
     */
    public void descartar(Long trocaId, String justificativa) {
        if (!SecurityUtil.isAdmin()) {
            throw new ValidacaoNegocioException("Acesso negado. Apenas administradores podem descartar trocas.");
        }

        Troca troca = trocaRepository.findById(trocaId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Troca com ID " + trocaId + " não encontrada"));

        if (!(troca.getStatus().equals(StatusTroca.SOLICITADA) || troca.getStatus().equals(StatusTroca.AUTORIZADA))) {
            throw new ValidacaoNegocioException("Apenas trocas com status SOLICITADA ou AUTORIZADA podem ser descartadas. Status atual: " + troca.getStatus().getDescricao());
        }

        // Registrar justificativa (acrescentar ao motivo existente)
        if (justificativa != null && !justificativa.trim().isEmpty()) {
            String novoMotivo = troca.getMotivo() + " | DECISAO ADMIN: " + justificativa;
            troca.setMotivo(novoMotivo);
        }

        troca.setStatus(StatusTroca.DESCARTADA);
        trocaRepository.save(troca);

        // Restaurar pedido para ENTREGUE
        Pedido pedido = troca.getPedido();
        pedido.setStatus(StatusPedido.ENTREGUE);
        pedidoRepository.save(pedido);
    }

    /**
     * Confirma o recebimento de uma troca.
     * RF0043: Confirmar recebimento de troca.
     * RF0054: Reentrada via troca.
     * RF0044: Gerar cupom de troca.
     * RN0042: Pedido → TROCADO.
     *
     * @param trocaId ID da troca
     * @throws RecursoNaoEncontradoException se troca não existe
     * @throws ValidacaoNegocioException     se troca não está autorizada
     */
    public void confirmarRecebimento(Long trocaId) {
        // Validar autorização: apenas admin pode confirmar recebimento de trocas (RF0043)
        if (!SecurityUtil.isAdmin()) {
            throw new ValidacaoNegocioException(
                "Acesso negado. Apenas administradores podem confirmar recebimento de trocas."
            );
        }

        // Buscar troca
        Troca troca = trocaRepository.findById(trocaId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Troca com ID " + trocaId + " não encontrada"));

        // Validar se está AUTORIZADA
        if (!troca.getStatus().equals(StatusTroca.AUTORIZADA)) {
            throw new ValidacaoNegocioException("Apenas trocas com status AUTORIZADA podem ter recebimento confirmado. Status atual: " + troca.getStatus().getDescricao());
        }

        // RF0054: Reentra itens no estoque
        Pedido pedido = troca.getPedido();
        BigDecimal valorTotalTroca = BigDecimal.ZERO;

        // Determinar itens selecionados na troca (CSV de item_pedido ids)
        List<Long> itensSelecionados = null;
        if (troca.getItens() != null && !troca.getItens().trim().isEmpty()) {
            itensSelecionados = Arrays.stream(troca.getItens().split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .map(Long::valueOf)
                    .collect(Collectors.toList());
        }

        for (ItemPedido item : pedido.getItens()) {
            if (itensSelecionados != null && !itensSelecionados.contains(item.getId())) {
                continue; // pular itens não selecionados
            }

            Optional<Estoque> optEstoque = estoqueRepository.findByLivroIdWithLock(item.getLivro().getId());
            if (optEstoque.isPresent()) {
                Estoque estoque = optEstoque.get();
                int novaQuantidade = estoque.getQuantidade() + item.getQuantidade();
                estoque.setQuantidade(novaQuantidade);
                estoqueRepository.save(estoque);
            }
            // Acumular valor para cupom
            valorTotalTroca = valorTotalTroca.add(
                    item.getValorUnitario().multiply(BigDecimal.valueOf(item.getQuantidade()))
            );
        }

        // RF0044: Gerar cupom de troca
        Cupom cupomTroca = new Cupom();
        cupomTroca.setCodigo("TROCA-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        cupomTroca.setValor(valorTotalTroca);
        // Definir validade do cupom de troca (6 meses por padrão)
        cupomTroca.setDataValidade(LocalDate.now().plusMonths(6));
        cupomTroca.setTipo(TipoCupom.TROCA);
        cupomTroca.setAtivo(true);
        // Vincular cupom ao cliente proprietário do pedido
        cupomTroca.setCliente(pedido.getCliente());
        cupomTroca = cupomRepository.save(cupomTroca);

        // Vincular cupom na troca e marcar como concluída
        troca.setCupom(cupomTroca);
        troca.setStatus(StatusTroca.CONCLUIDA);
        trocaRepository.save(troca);

        // Alterar pedido para TROCADO
        pedido.setStatus(StatusPedido.TROCADO);
        pedidoRepository.save(pedido);
    }

    /**
     * Busca os detalhes de uma troca específica.
     *
     * @param trocaId ID da troca
     * @return DTO da troca
     * @throws RecursoNaoEncontradoException se troca não existe
     */
    public TrocaDetalheDTO buscarDetalhes(Long trocaId) {
        // Buscar troca
        Troca troca = trocaRepository.findById(trocaId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Troca com ID " + trocaId + " não encontrada"));

        // Converter para DTO
        return converterParaDTO(troca);
    }

    /**
     * Lista todas as trocas do sistema.
     * RF0042: Visualizar trocas (admin).
     *
     * @return lista de DTOs de todas as trocas
     */
    public List<TrocaDetalheDTO> listarTodas() {
        // Validar autorização: apenas admin pode visualizar todas as trocas (RF0042)
        if (!SecurityUtil.isAdmin()) {
            throw new ValidacaoNegocioException(
                "Acesso negado. Apenas administradores podem visualizar todas as trocas."
            );
        }

        // Buscar todas as trocas ordenadas por data
        List<Troca> trocas = trocaRepository.findAllOrderByDataSolicitacaoDesc();

        // Converter para DTO
        return trocas.stream()
                .map(this::converterParaDTO)
                .collect(Collectors.toList());
    }

    /**
     * Método auxiliar para converter entidade Troca para DTO.
     *
     * @param troca entidade a converter
     * @return DTO da troca
     */
    private TrocaDetalheDTO converterParaDTO(Troca troca) {
        Pedido pedido = troca.getPedido();

        // Buscar cupom gerado se existe vínculo na troca
        String codigoCupom = null;
        java.math.BigDecimal valorCupom = null;
        if (troca.getCupom() != null) {
            codigoCupom = troca.getCupom().getCodigo();
            valorCupom = troca.getCupom().getValor();
        }

        // Filtrar itens retornados para apenas os selecionados na troca
        List<Long> itensSelecionadosDTO = (troca.getItens() != null && !troca.getItens().trim().isEmpty())
                ? Arrays.stream(troca.getItens().split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .map(Long::valueOf)
                    .collect(Collectors.toList())
                : null;

        return new TrocaDetalheDTO(
                troca.getId(),
                pedido.getId(),
                pedido.getCliente().getNome(),
                pedido.getCliente().getCodigo(),
                troca.getDataSolicitacao(),
                troca.getStatus(),
                troca.getMotivo(),
            pedido.getItens().stream()
                .filter(item -> itensSelecionadosDTO == null || itensSelecionadosDTO.contains(item.getId()))
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
                codigoCupom,
                valorCupom
        );
    }
}
