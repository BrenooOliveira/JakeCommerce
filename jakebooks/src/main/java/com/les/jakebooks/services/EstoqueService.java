package com.les.jakebooks.services;

import com.les.jakebooks.domain.Estoque;
import com.les.jakebooks.domain.Livro;
import com.les.jakebooks.dto.EntradaEstoqueDTO;
import com.les.jakebooks.dto.EstoqueListaDTO;
import com.les.jakebooks.exception.RecursoNaoEncontradoException;
import com.les.jakebooks.exception.ValidacaoNegocioException;
import com.les.jakebooks.repository.EstoqueRepository;
import com.les.jakebooks.repository.LivroRepository;
import com.les.jakebooks.util.SecurityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
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
}
