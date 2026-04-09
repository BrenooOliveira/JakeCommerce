package com.les.jakebooks.service;

import com.les.jakebooks.domain.Estoque;
import com.les.jakebooks.domain.ItemPedido;
import com.les.jakebooks.domain.Livro;
import com.les.jakebooks.domain.Pedido;
import com.les.jakebooks.dto.EntradaEstoqueDTO;
import com.les.jakebooks.dto.EstoqueListaDTO;
import com.les.jakebooks.dto.MovimentoEstoque;
import com.les.jakebooks.exception.RecursoNaoEncontradoException;
import com.les.jakebooks.exception.ValidacaoNegocioException;
import com.les.jakebooks.repository.EstoqueRepository;
import com.les.jakebooks.repository.LivroRepository;
import com.les.jakebooks.service.LogService;
import com.les.jakebooks.util.SecurityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service para gerenciar operações de Estoque.
 * RF0051: Entrada em estoque.
 * RF0052: Calcular valor de venda.
 * RF0053: Baixa automática após venda.
 * RF0054: Reentrada via troca.
 * RN0051: Entrada exige produto, quantidade, custo, fornecedor e data.
 * RN005x: Considerar maior custo para cálculo de venda.
 * RN0061: Não permitir quantidade zero.
 * RN0062: Todo item deve possuir custo.
 * RNF0064: Não permitir registro sem data.
 */
@Service
@Transactional
public class EstoqueService {

    @Autowired
    private EstoqueRepository estoqueRepository;

    @Autowired
    private LivroRepository livroRepository;

    @Autowired
    private LogService logService;

    /**
     * Registra uma entrada de estoque.
     * RF0051: Entrada em estoque.
     * RN0051: Valida todos os campos obrigatórios.
     * RN0061: Valida quantidade > 0.
     * RN0062: Valida custo.
     * RNF0064: Valida data.
     * RN005x: Atualiza custoAtual com maior custo.
     * RF0052: Recalcula valorVenda do livro.
     *
     * @param dto dados da entrada de estoque
     * @throws RecursoNaoEncontradoException se livro não existe
     * @throws ValidacaoNegocioException     se validações falham
     */
    public void registrarEntrada(EntradaEstoqueDTO dto) {
        // Validar autorização: apenas admin pode registrar entrada em estoque (RF0051)
        if (!SecurityUtil.isAdmin()) {
            throw new ValidacaoNegocioException(
                "Acesso negado. Apenas administradores podem registrar entrada em estoque."
            );
        }

        // Validar se livro existe
        Livro livro = livroRepository.findById(dto.livroId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Livro com ID " + dto.livroId() + " não encontrado"));

        // RN0061: Validar quantidade > 0
        if (dto.quantidade() == null || dto.quantidade() <= 0) {
            throw new ValidacaoNegocioException("Quantidade deve ser maior que zero");
        }

        // RN0062: Validar custo
        if (dto.custo() == null || dto.custo().compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidacaoNegocioException("Custo deve ser maior que zero");
        }

        // RNF0064: Validar data
        if (dto.dataEntrada() == null) {
            throw new ValidacaoNegocioException("Data de entrada é obrigatória");
        }

        // Validar fornecedor (RN0051)
        if (dto.fornecedor() == null || dto.fornecedor().trim().isEmpty()) {
            throw new ValidacaoNegocioException("Fornecedor é obrigatório");
        }

        // Buscar ou criar estoque
        Estoque estoque = estoqueRepository.findByLivroId(livro.getId());
        
        if (estoque == null) {
            // Criar novo estoque
            estoque = new Estoque();
            estoque.setLivro(livro);
            estoque.setQuantidade(dto.quantidade());
            estoque.setCustoAtual(dto.custo());
            estoque.setDataEntrada(dto.dataEntrada());
        } else {
            // Atualizar estoque existente
            estoque.setQuantidade(estoque.getQuantidade() + dto.quantidade());

            // RN005x: Atualizar custoAtual com maior custo
            if (dto.custo().compareTo(estoque.getCustoAtual()) > 0) {
                estoque.setCustoAtual(dto.custo());
                estoque.setDataEntrada(dto.dataEntrada());
            }
        }

        estoque = estoqueRepository.save(estoque);
        livro.setEstoque(estoque);

        // RF0052: Recalcular valor de venda
        BigDecimal novoValorVenda = calcularValorVenda(livro.getId());
        livro.setValorVenda(novoValorVenda);
        livroRepository.save(livro);
    }

    /**
     * Calcula o valor de venda de um livro.
     * RF0052: Calcular valor de venda.
     * RN0013: Valor de venda baseado na margem do grupo.
     * Fórmula: custo * (1 + percentualMargem/100)
     *
     * @param livroId ID do livro
     * @return valor de venda calculado
     * @throws RecursoNaoEncontradoException se livro não existe
     * @throws ValidacaoNegocioException     se estoque ou grupo não existe
     */
    public BigDecimal calcularValorVenda(Long livroId) {
        // Buscar livro
        Livro livro = livroRepository.findById(livroId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Livro com ID " + livroId + " não encontrado"));

        // Validar estoque
        if (livro.getEstoque() == null) {
            throw new ValidacaoNegocioException("Livro não possui estoque registrado");
        }

        // Validar grupo de precificação
        if (livro.getGrupoPrecificacao() == null) {
            throw new ValidacaoNegocioException("Livro não possui grupo de precificação definido");
        }

        // Calcular: custo * (1 + percentualMargem/100)
        BigDecimal custoAtual = livro.getEstoque().getCustoAtual();
        BigDecimal percentualMargem = livro.getGrupoPrecificacao().getPercentualMargem();

        // Margem como percentual: percentualMargem / 100
        BigDecimal margem = percentualMargem.divide(new BigDecimal("100"), 4, java.math.RoundingMode.HALF_UP);
        
        // Valor de venda = custo * (1 + margem)
        BigDecimal valorVenda = custoAtual.multiply(BigDecimal.ONE.add(margem));

        // Arredondar para 2 casas decimais
        return valorVenda.setScale(2, java.math.RoundingMode.HALF_UP);
    }

    /**
     * Baixa quantidade do estoque.
     * RF0053: Baixa automática após venda.
     * RN0028: Chamado apenas após pagamento APROVADO.
     *
     * @param livroId    ID do livro
     * @param quantidade quantidade a baixar
     * @throws RecursoNaoEncontradoException se estoque não existe
     * @throws ValidacaoNegocioException     se quantidade insuficiente
     */
    public void baixar(Long livroId, int quantidade) {
        // Buscar estoque
        Estoque estoque = estoqueRepository.findByLivroId(livroId);
        
        if (estoque == null) {
            throw new RecursoNaoEncontradoException("Estoque para livro com ID " + livroId + " não encontrado");
        }

        // Validar quantidade
        if (quantidade <= 0) {
            throw new ValidacaoNegocioException("Quantidade a baixar deve ser maior que zero");
        }

        if (estoque.getQuantidade() < quantidade) {
            throw new ValidacaoNegocioException("Quantidade insuficiente no estoque. " +
                    "Disponível: " + estoque.getQuantidade() + ", Solicitado: " + quantidade);
        }

        // Baixar quantidade
        estoque.setQuantidade(estoque.getQuantidade() - quantidade);
        estoqueRepository.save(estoque);
    }

    /**
     * Reentra quantidade no estoque.
     * RF0054: Reentrada via troca.
     * Chamado pelo TrocaService ao confirmar recebimento de troca.
     *
     * @param livroId    ID do livro
     * @param quantidade quantidade a reentradar
     * @throws RecursoNaoEncontradoException se estoque não existe
     * @throws ValidacaoNegocioException     se quantidade inválida
     */
    public void reentrada(Long livroId, int quantidade) {
        // Buscar estoque
        Estoque estoque = estoqueRepository.findByLivroId(livroId);

        if (estoque == null) {
            throw new RecursoNaoEncontradoException("Estoque para livro com ID " + livroId + " não encontrado");
        }

        // Validar quantidade
        if (quantidade <= 0) {
            throw new ValidacaoNegocioException("Quantidade a reentradar deve ser maior que zero");
        }

        // Reentradar quantidade
        estoque.setQuantidade(estoque.getQuantidade() + quantidade);
        estoqueRepository.save(estoque);
    }

    /**
     * Executa baixa de estoque para todos os itens de um pedido.
     * TASK-CHK-04: Coordenar Baixa de Estoque
     * RF0053: Baixa automática após venda
     * RN0028: Baixa estoque apenas após pagamento aprovado
     *
     * Este método executa a baixa transacional de estoque com:
     * - Validação de pré-condições (pagamento APROVADO, status EM_PROCESSAMENTO)
     * - Re-validação de estoque com lock pessimista
     * - Registro detalhado de log de movimentos
     * - Atomicidade garantida (tudo ou nada)
     *
     * Pré-condição: Pagamento do pedido deve ter status APROVADA.
     * Pós-condição: Estoque decrementado para cada item do pedido.
     *
     * @param pedido pedido cujos itens terão estoque baixado
     * @throws PagamentoNaoAprovadoException se pagamento não está APROVADO
     * @throws StatusPedidoInvalidoException se pedido não está EM_PROCESSAMENTO
     * @throws EstoqueInsuficienteParaBaixaException se estoque insuficiente no momento da baixa
     * @throws EstoqueNaoEncontradoException se estoque não existe
     * @throws ValidacaoNegocioException para outras validações
     */
    @Transactional
    public void executarBaixaPorPedido(Pedido pedido) {
        // Validar pré-condições básicas
        if (pedido == null) {
            throw new ValidacaoNegocioException("Pedido não pode ser null");
        }

        if (pedido.getItens() == null || pedido.getItens().isEmpty()) {
            throw new ValidacaoNegocioException("Pedido não possui itens");
        }

        // Validar pré-condições de negócio (TASK-CHK-04)
        validarPreCondicoesBaixa(pedido);

        // Re-validar estoque disponível antes da baixa (segurança adicional)
        revalidarEstoqueDisponivel(pedido.getItens());

        // Executar baixa para cada item com lock pessimista
        List<MovimentoEstoque> movimentos = new ArrayList<>();

        for (ItemPedido item : pedido.getItens()) {
            MovimentoEstoque movimento = executarBaixaItem(item);
            movimentos.add(movimento);
        }

        // Registrar log consolidado da operação (RNF0012)
        registrarLogBaixaEstoque(pedido, movimentos);
    }

    /**
     * Valida pré-condições para baixa de estoque.
     * TASK-CHK-04: Validações de negócio críticas
     *
     * @param pedido pedido a validar
     * @throws PagamentoNaoAprovadoException se pagamento não APROVADO (RN0028)
     * @throws StatusPedidoInvalidoException se status não EM_PROCESSAMENTO
     */
    private void validarPreCondicoesBaixa(Pedido pedido) {
        // RN0028: Baixa apenas após pagamento APROVADO
        if (pedido.getPagamento() == null ||
            pedido.getPagamento().getStatus() != com.les.jakebooks.domain.enums.StatusPagamento.APROVADA) {
            throw new com.les.jakebooks.exception.PagamentoNaoAprovadoException(
                "Baixa de estoque permitida apenas para pagamentos aprovados"
            );
        }

        // Validar status do pedido
        if (pedido.getStatus() != com.les.jakebooks.domain.enums.StatusPedido.EM_PROCESSAMENTO) {
            throw new com.les.jakebooks.exception.StatusPedidoInvalidoException(
                "Pedido deve estar EM_PROCESSAMENTO para baixa de estoque. Status atual: " +
                pedido.getStatus()
            );
        }
    }

    /**
     * Re-valida disponibilidade de estoque antes da baixa.
     * TASK-CHK-04: Segurança adicional para evitar baixa com estoque insuficiente
     *
     * @param itens itens do pedido
     * @throws EstoqueNaoEncontradoException se estoque não existe
     * @throws EstoqueInsuficienteParaBaixaException se estoque insuficiente
     */
    private void revalidarEstoqueDisponivel(List<ItemPedido> itens) {
        for (ItemPedido item : itens) {
            // Buscar estoque com lock pessimista
            Estoque estoque = estoqueRepository.findByLivroIdWithLock(item.getLivro().getId())
                .orElseThrow(() -> new com.les.jakebooks.exception.EstoqueNaoEncontradoException(
                    "Estoque não encontrado para livro: " + item.getLivro().getTitulo()
                ));

            // Validar quantidade disponível
            if (estoque.getQuantidade() < item.getQuantidade()) {
                throw new com.les.jakebooks.exception.EstoqueInsuficienteParaBaixaException(
                    item.getLivro().getTitulo(),
                    estoque.getQuantidade(),
                    item.getQuantidade()
                );
            }
        }
    }

    /**
     * Executa baixa de um item específico com lock pessimista.
     * TASK-CHK-04: Baixa atomica por item
     *
     * @param item item do pedido
     * @return MovimentoEstoque com dados do movimento para log
     * @throws EstoqueNaoEncontradoException se estoque não existe
     * @throws EstoqueInsuficienteParaBaixaException se quantidade insuficiente
     */
    private MovimentoEstoque executarBaixaItem(ItemPedido item) {
        // Buscar estoque com lock pessimista (previne race conditions)
        Estoque estoque = estoqueRepository.findByLivroIdWithLock(item.getLivro().getId())
            .orElseThrow(() -> new com.les.jakebooks.exception.EstoqueNaoEncontradoException(
                "Estoque não encontrado para baixa"
            ));

        // Capturar estado anterior
        Integer quantidadeAnterior = estoque.getQuantidade();
        Integer quantidadeBaixa = item.getQuantidade();
        Integer novaQuantidade = quantidadeAnterior - quantidadeBaixa;

        // Validar se nova quantidade é válida (double-check)
        if (novaQuantidade < 0) {
            throw new com.les.jakebooks.exception.EstoqueInsuficienteParaBaixaException(
                item.getLivro().getTitulo(),
                quantidadeAnterior,
                quantidadeBaixa
            );
        }

        // Executar baixa
        estoque.setQuantidade(novaQuantidade);
        estoque = estoqueRepository.save(estoque);

        // Retornar dados do movimento para log
        return com.les.jakebooks.dto.MovimentoEstoque.builder()
            .livroId(item.getLivro().getId())
            .tituloLivro(item.getLivro().getTitulo())
            .quantidadeAnterior(quantidadeAnterior)
            .quantidadeBaixa(quantidadeBaixa)
            .quantidadeNova(novaQuantidade)
            .dataMovimento(java.time.LocalDateTime.now())
            .build();
    }

    /**
     * Registra log consolidado da baixa de estoque.
     * TASK-CHK-04: Log detalhado conforme RNF0012
     *
     * @param pedido pedido que teve estoque baixado
     * @param movimentos lista de movimentos executados
     */
    private void registrarLogBaixaEstoque(Pedido pedido, List<MovimentoEstoque> movimentos) {
        // Log consolidado da operação
        String resumo = String.format(
            "Pedido ID: %d | Cliente: %s | Itens: %d",
            pedido.getId(),
            pedido.getCliente().getNome(),
            movimentos.size()
        );

        // Log detalhado de cada movimento
        StringBuilder detalhes = new StringBuilder();
        for (MovimentoEstoque mov : movimentos) {
            detalhes.append(String.format(
                "Livro: %s (ID: %d) | Antes: %d | Baixa: %d | Depois: %d; ",
                mov.getTituloLivro(),
                mov.getLivroId(),
                mov.getQuantidadeAnterior(),
                mov.getQuantidadeBaixa(),
                mov.getQuantidadeNova()
            ));
        }

        // Registrar no log (RNF0012)
        logService.registrar(
            "BAIXA_ESTOQUE",
            "Estoque",
            resumo,
            detalhes.toString(),
            "Baixa de estoque executada com sucesso para pedido " + pedido.getId()
        );
    }

    /**
     * Lista todos os estoques com informações dos livros.
     * RF0051: Visualizar entrada em estoque.
     *
     * @return lista de DTOs com estoque e informações do livro
     */
    public List<EstoqueListaDTO> listarTodos() {
        List<Estoque> estoques = estoqueRepository.findAll();

        return estoques.stream()
                .map(estoque -> new EstoqueListaDTO(
                        estoque.getId(),
                        estoque.getLivro().getId(),
                        estoque.getLivro().getCodigo(),
                        estoque.getLivro().getTitulo(),
                        estoque.getQuantidade(),
                        estoque.getCustoAtual(),
                        estoque.getDataEntrada(),
                        estoque.getLivro().getValorVenda()
                ))
                .collect(Collectors.toList());
    }

    /**
     * Reverte baixa de estoque em caso de necessidade.
     * TASK-CHK-04: Reversão para casos excepcionais de inconsistência
     *
     * Usado apenas em situações excepcionais onde é necessário
     * desfazer uma baixa de estoque já executada.
     *
     * @param pedido pedido cuja baixa será revertida
     * @param motivo motivo da reversão (para log)
     * @throws EstoqueNaoEncontradoException se estoque não existe
     * @throws ValidacaoNegocioException para validações
     */
    @Transactional
    public void reverterBaixaPorPedido(Pedido pedido, String motivo) {
        // Validar pedido
        if (pedido == null) {
            throw new ValidacaoNegocioException("Pedido não pode ser null");
        }

        if (pedido.getItens() == null || pedido.getItens().isEmpty()) {
            throw new ValidacaoNegocioException("Pedido não possui itens");
        }

        // Reverter baixa para cada item
        for (ItemPedido item : pedido.getItens()) {
            Estoque estoque = estoqueRepository.findByLivroId(item.getLivro().getId());

            if (estoque == null) {
                throw new com.les.jakebooks.exception.EstoqueNaoEncontradoException(
                    "Estoque não encontrado para reversão: " + item.getLivro().getTitulo()
                );
            }

            // Adicionar quantidade de volta ao estoque
            Integer quantidadeAnterior = estoque.getQuantidade();
            Integer quantidadeRevertida = item.getQuantidade();
            Integer novaQuantidade = quantidadeAnterior + quantidadeRevertida;

            estoque.setQuantidade(novaQuantidade);
            estoqueRepository.save(estoque);

            // Registrar log da reversão individual
            logService.registrar(
                "REVERSAO_ESTOQUE",
                "Estoque",
                String.format("Livro: %s | Antes: %d", item.getLivro().getTitulo(), quantidadeAnterior),
                String.format("Depois: %d | Revertido: %d", novaQuantidade, quantidadeRevertida),
                String.format("Item do pedido %d revertido. Motivo: %s", pedido.getId(), motivo)
            );
        }

        // Registrar log consolidado da reversão (RNF0012)
        logService.registrar(
            "REVERSAO_ESTOQUE_PEDIDO",
            "Pedido",
            "Pedido ID: " + pedido.getId(),
            "Motivo: " + motivo + " | Itens revertidos: " + pedido.getItens().size(),
            "Reversão de estoque executada para pedido " + pedido.getId()
        );
    }
}
