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
import java.util.UUID;
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

        for (ItemPedido item : pedido.getItens()) {
            Estoque estoque = estoqueRepository.findByLivroId(item.getLivro().getId());
            if (estoque != null) {
                // Reentra quantidade no estoque
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
        cupomTroca.setCodigo("TROCA-" + UUID.randomUUID().toString().substring(0, 8));
        cupomTroca.setValor(valorTotalTroca);
        cupomTroca.setTipo(TipoCupom.TROCA);
        cupomTroca.setAtivo(true);
        cupomTroca = cupomRepository.save(cupomTroca);

        // RN0042: Alterar troca para RECEBIDA
        troca.setStatus(StatusTroca.RECEBIDA);
        trocaRepository.save(troca);

        // RN0042: Alterar pedido para TROCADO
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

        // Buscar cupom gerado se a troca foi concluída
        String codigoCupom = null;
        if (troca.getStatus().equals(StatusTroca.RECEBIDA)) {
            // Buscar cupom de troca gerado para este pedido
            // Aqui seria necessário ter um relacionamento ou busca mais específica
            // Por enquanto, deixamos como null ou podia-se ter um campo cupomId na Troca
            codigoCupom = "TROCA-" + troca.getId();
        }

        return new TrocaDetalheDTO(
                troca.getId(),
                pedido.getId(),
                pedido.getCliente().getNome(),
                pedido.getCliente().getCodigo(),
                troca.getDataSolicitacao(),
                troca.getStatus(),
                troca.getMotivo(),
                pedido.getItens().stream()
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
                codigoCupom
        );
    }
}
