package com.les.jakebooks.services;

import com.les.jakebooks.domain.*;
import com.les.jakebooks.dto.*;
import com.les.jakebooks.exception.RecursoNaoEncontradoException;
import com.les.jakebooks.exception.ValidacaoNegocioException;
import com.les.jakebooks.model.enums.StatusLivro;
import com.les.jakebooks.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service para gerenciar operações de Livro.
 * RN0011: Dados obrigatórios conforme modelo.
 * RN0012: Livro pode ter múltiplas categorias.
 * RN0013: Valor de venda baseado na margem do grupo.
 * RN0014: Redução abaixo da margem exige autorização.
 * RN0015: Inativação manual exige motivo.
 * RN0016: Inativação automática categoria FORA DE MERCADO.
 * RN0017: Ativação exige justificativa.
 */
@Service
@Transactional
public class LivroService {

    @Autowired
    private LivroRepository livroRepository;

    @Autowired
    private GrupoPrecificacaoRepository grupoPrecificacaoRepository;

    @Autowired
    private EditoraRepository editoraRepository;

    @Autowired
    private AutorRepository autorRepository;

    @Autowired
    private CategoriaRepository categoriaRepository;

    @Autowired
    private EstoqueRepository estoqueRepository;

    /**
     * Cadastra um novo livro.
     * RF0011: Cadastrar livro
     * RN0011: Dados obrigatórios conforme modelo
     * RN0013: Valor de venda baseado na margem do grupo
     *
     * @param dto DTO com dados do livro (campos editáveis + IDs dos relacionamentos)
     * @return DTO do livro criado
     * @throws ValidacaoNegocioException se dados obrigatórios estão ausentes
     * @throws RecursoNaoEncontradoException se editora, grupo ou relacionamentos não existem
     */
    public LivroDetalheDTO cadastrar(LivroFormDTO dto) {
        // Validar se livro com mesmo código já existe
        if (livroRepository.findByIsbn(dto.isbn()) != null) {
            throw new ValidacaoNegocioException("Já existe um livro cadastrado com este ISBN: " + dto.isbn());
        }

        // Buscar editora
        Editora editora = editoraRepository.findById(dto.idEditora())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Editora com ID " + dto.idEditora() + " não encontrada"));

        // Buscar grupo de precificação
        GrupoPrecificacao grupo = grupoPrecificacaoRepository.findById(dto.idGrupoPrecificacao())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Grupo de precificação com ID " + dto.idGrupoPrecificacao() + " não encontrado"));

        // Buscar autores
        List<Autor> autores = autorRepository.findAllById(dto.idsAutores());
        if (autores.isEmpty()) {
            throw new RecursoNaoEncontradoException("Nenhum autor encontrado com os IDs fornecidos");
        }

        // Buscar categorias
        List<Categoria> categorias = categoriaRepository.findAllById(dto.idsCategorias());
        if (categorias.isEmpty()) {
            throw new RecursoNaoEncontradoException("Nenhuma categoria encontrada com os IDs fornecidos");
        }

        // Criar livro
        Livro livro = new Livro();
        livro.setCodigo(dto.codigo());
        livro.setTitulo(dto.titulo());
        livro.setAno(dto.ano());
        livro.setEdicao(dto.edicao());
        livro.setIsbn(dto.isbn());
        livro.setNumeroPaginas(dto.numeroPaginas());
        livro.setSinopse(dto.sinopse());
        livro.setDimensoes(dto.dimensoes());
        livro.setCodigoBarras(dto.codigoBarras());
        livro.setStatus(dto.status());
        livro.setValorVenda(dto.valorVenda());
        livro.setGrupoPrecificacao(grupo);
        livro.setEditora(editora);
        livro.setAutores(autores);
        livro.setCategorias(categorias);

        Livro livroSalvo = livroRepository.save(livro);

        return converterParaDetalheDTO(livroSalvo);
    }

    /**
     * Altera um livro existente.
     * RF0014: Alterar livro
     * RN0014: Redução abaixo da margem exige autorização
     *
     * @param codigo código único do livro
     * @param dto novos dados do livro
     * @param autorizacaoReducao true se tem autorização para reduzir margem
     * @return DTO do livro alterado
     * @throws RecursoNaoEncontradoException se livro não existe
     * @throws ValidacaoNegocioException se regra de negócio é violada
     */
    public LivroDetalheDTO alterar(String codigo, LivroFormDTO dto, boolean autorizacaoReducao) {
        Livro livro = livroRepository.findByIsbn(codigo);
        if (livro == null) {
            throw new RecursoNaoEncontradoException("Livro com código " + codigo + " não encontrado");
        }

        // Validar se há redução de margem (RN0014)
        GrupoPrecificacao novoGrupo = grupoPrecificacaoRepository.findById(dto.idGrupoPrecificacao())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Grupo de precificação não encontrado"));

        if (!novoGrupo.getId().equals(livro.getGrupoPrecificacao().getId())) {
            // Grupo de precificação foi alterado, verificar se há redução
            BigDecimal margemAtual = livro.getGrupoPrecificacao().getPercentualMargem();
            BigDecimal novaMargen = novoGrupo.getPercentualMargem();

            if (novaMargen.compareTo(margemAtual) < 0) {
                // Há redução de margem, exigir autorização
                if (!autorizacaoReducao) {
                    throw new ValidacaoNegocioException("Redução de margem exige autorização: " +
                            "Margem atual: " + margemAtual + "% | Nova margem: " + novaMargen + "%");
                }
            }
        }

        // Buscar relacionamentos
        Editora editora = editoraRepository.findById(dto.idEditora())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Editora não encontrada"));

        List<Autor> autores = autorRepository.findAllById(dto.idsAutores());
        List<Categoria> categorias = categoriaRepository.findAllById(dto.idsCategorias());

        // Atualizar livro
        livro.setTitulo(dto.titulo());
        livro.setAno(dto.ano());
        livro.setEdicao(dto.edicao());
        livro.setIsbn(dto.isbn());
        livro.setNumeroPaginas(dto.numeroPaginas());
        livro.setSinopse(dto.sinopse());
        livro.setDimensoes(dto.dimensoes());
        livro.setCodigoBarras(dto.codigoBarras());
        livro.setValorVenda(dto.valorVenda());
        livro.setGrupoPrecificacao(novoGrupo);
        livro.setEditora(editora);
        livro.setAutores(autores);
        livro.setCategorias(categorias);

        Livro livroAtualizado = livroRepository.save(livro);

        return converterParaDetalheDTO(livroAtualizado);
    }

    /**
     * Inativa manualmente um livro.
     * RF0012: Inativar livro manualmente
     * RN0015: Inativação manual exige motivo
     *
     * @param codigo código do livro
     * @param motivo motivo da inativação (obrigatório)
     * @return DTO do livro inativado
     * @throws RecursoNaoEncontradoException se livro não existe
     * @throws ValidacaoNegocioException se motivo não for fornecido
     */
    public LivroDetalheDTO inativarManual(String codigo, String motivo) {
        if (motivo == null || motivo.trim().isEmpty()) {
            throw new ValidacaoNegocioException("Motivo da inativação é obrigatório");
        }

        Livro livro = livroRepository.findByIsbn(codigo);
        if (livro == null) {
            throw new RecursoNaoEncontradoException("Livro com código " + codigo + " não encontrado");
        }

        livro.setStatus(StatusLivro.INATIVO);
        Livro livroAtualizado = livroRepository.save(livro);

        return converterParaDetalheDTO(livroAtualizado);
    }

    /**
     * Inativa automaticamente livros de categorias FORA_DE_MERCADO.
     * RF0013: Inativar livro automaticamente
     * RN0016: Inativação automática categoria FORA DE MERCADO
     *
     * @return quantidade de livros inativados
     */
    public long inativarAutomatico() {
        // Buscar categoria FORA_DE_MERCADO
        List<Categoria> categoriasForaMercado = categoriaRepository.findAll()
                .stream()
                .filter(c -> c.getNome().contains("FORA") || c.getNome().contains("Fora"))
                .collect(Collectors.toList());

        if (categoriasForaMercado.isEmpty()) {
            return 0;
        }

        long totalInativados = 0;
        for (Categoria categoria : categoriasForaMercado) {
            List<Livro> livrosAtivos = livroRepository.findByCategoriasId(categoria.getId())
                    .stream()
                    .filter(l -> l.getStatus() == StatusLivro.ATIVO)
                    .collect(Collectors.toList());

            for (Livro livro : livrosAtivos) {
                livro.setStatus(StatusLivro.INATIVO);
                livroRepository.save(livro);
                totalInativados++;
            }
        }

        return totalInativados;
    }

    /**
     * Ativa um livro inativo.
     * RF0016: Ativar livro
     * RN0017: Ativação exige justificativa
     *
     * @param codigo código do livro
     * @param justificativa justificativa da ativação (obrigatória)
     * @return DTO do livro ativado
     * @throws RecursoNaoEncontradoException se livro não existe
     * @throws ValidacaoNegocioException se justificativa não for fornecida ou livro já está ativo
     */
    public LivroDetalheDTO ativar(String codigo, String justificativa) {
        if (justificativa == null || justificativa.trim().isEmpty()) {
            throw new ValidacaoNegocioException("Justificativa da ativação é obrigatória");
        }

        Livro livro = livroRepository.findByIsbn(codigo);
        if (livro == null) {
            throw new RecursoNaoEncontradoException("Livro com código " + codigo + " não encontrado");
        }

        if (livro.getStatus() == StatusLivro.ATIVO) {
            throw new ValidacaoNegocioException("Livro já está ativo");
        }

        livro.setStatus(StatusLivro.ATIVO);
        Livro livroAtualizado = livroRepository.save(livro);

        return converterParaDetalheDTO(livroAtualizado);
    }

    /**
     * Busca livros com filtros combinados.
     * RF0015: Consultar livros com filtros combinados
     *
     * @param filtro critérios de busca (podem ser nulos para resultado completo)
     * @return lista de livros que correspondem aos filtros
     */
    public List<LivroListagemDTO> buscarComFiltros(LivroFiltroDTO filtro) {
        List<Livro> livros;

        // Se não houver filtros específicos, retornar listagem simples
        if (temFiltroAtivo(filtro)) {
            livros = livroRepository.buscarComFiltros(
                    filtro.titulo(),
                    null, // autorId será tratado diferente
                    filtro.status(),
                    filtro.idCategoria()
            );

            // Se houver busca por autor, fazer pós-processamento
            if (filtro.nomeAutor() != null && !filtro.nomeAutor().isEmpty()) {
                livros = livros.stream()
                        .filter(l -> l.getAutores().stream()
                                .anyMatch(a -> a.getNome().toLowerCase()
                                        .contains(filtro.nomeAutor().toLowerCase())))
                        .collect(Collectors.toList());
            }

            // Filtrar por faixa de preço
            if (filtro.precoMin() != null || filtro.precoMax() != null) {
                livros = livros.stream()
                        .filter(l -> {
                            if (filtro.precoMin() != null && l.getValorVenda().compareTo(filtro.precoMin()) < 0) {
                                return false;
                            }
                            if (filtro.precoMax() != null && l.getValorVenda().compareTo(filtro.precoMax()) > 0) {
                                return false;
                            }
                            return true;
                        })
                        .collect(Collectors.toList());
            }
        } else {
            // Sem filtros, retornar todos
            livros = livroRepository.findAll();
        }

        return livros.stream()
                .map(this::converterParaListagemDTO)
                .collect(Collectors.toList());
    }

    /**
     * Busca um livro pelo código e retorna dados completos.
     * RF0024: Consultar cliente (análogo para livro)
     *
     * @param codigo código do livro
     * @return DTO com detalhes completos do livro
     * @throws RecursoNaoEncontradoException se livro não existe
     */
    public LivroDetalheDTO buscarPorCodigo(String codigo) {
        Livro livro = livroRepository.findByIsbn(codigo);
        if (livro == null) {
            throw new RecursoNaoEncontradoException("Livro com código " + codigo + " não encontrado");
        }
        return converterParaDetalheDTO(livro);
    }

    /**
     * Converter entidade Livro para DTO de listagem.
     *
     * @param livro entidade a converter
     * @return DTO de listagem
     */
    private LivroListagemDTO converterParaListagemDTO(Livro livro) {
        return new LivroListagemDTO(
                livro.getId(),
                livro.getCodigo(),
                livro.getTitulo(),
                livro.getValorVenda(),
                livro.getStatus(),
                livro.getEditora() != null ? livro.getEditora().getNome() : ""
        );
    }

    /**
     * Converter entidade Livro para DTO de detalhe.
     *
     * @param livro entidade a converter
     * @return DTO de detalhe completo
     */
    private LivroDetalheDTO converterParaDetalheDTO(Livro livro) {
        GrupoPrecificacaoDTO grupoDTO = livro.getGrupoPrecificacao() != null ?
                new GrupoPrecificacaoDTO(
                        livro.getGrupoPrecificacao().getId(),
                        livro.getGrupoPrecificacao().getNome(),
                        livro.getGrupoPrecificacao().getPercentualMargem()
                ) : null;

        EditoraDTO editoraDTO = livro.getEditora() != null ?
                new EditoraDTO(livro.getEditora().getId(), livro.getEditora().getNome()) : null;

        List<AutorDTO> autoresDTO = livro.getAutores().stream()
                .map(a -> new AutorDTO(a.getId(), a.getNome()))
                .collect(Collectors.toList());

        List<CategoriaDTO> categoriasDTO = livro.getCategorias().stream()
                .map(c -> new CategoriaDTO(c.getId(), c.getNome()))
                .collect(Collectors.toList());

        EstoqueDTO estoqueDTO = livro.getEstoque() != null ?
                new EstoqueDTO(
                        livro.getEstoque().getId(),
                        livro.getEstoque().getQuantidade(),
                        livro.getEstoque().getCustoAtual(),
                        livro.getEstoque().getDataEntrada().atStartOfDay()
                ) : null;

        return new LivroDetalheDTO(
                livro.getId(),
                livro.getCodigo(),
                livro.getTitulo(),
                livro.getAno(),
                livro.getEdicao(),
                livro.getIsbn(),
                livro.getNumeroPaginas(),
                livro.getSinopse(),
                livro.getDimensoes(),
                livro.getCodigoBarras(),
                livro.getStatus(),
                livro.getValorVenda(),
                grupoDTO,
                editoraDTO,
                autoresDTO,
                categoriasDTO,
                estoqueDTO
        );
    }

    /**
     * Lista todos os livros ativos do sistema.
     * Utilizado para populares selects em formulários.
     *
     * @return lista de DTOs de detalhes de todos os livros ativos
     */
    public List<LivroDetalheDTO> listarTodos() {
        List<Livro> livros = livroRepository.findAll();
        
        return livros.stream()
                .map(this::converterParaDetalheDTO)
                .collect(Collectors.toList());
    }

    /**
     * Verifica se há algum filtro ativo.
     *
     * @param filtro DTO de filtro
     * @return true se algum filtro foi definido
     */
    private boolean temFiltroAtivo(LivroFiltroDTO filtro) {
        return (filtro.titulo() != null && !filtro.titulo().isEmpty()) ||
                (filtro.nomeAutor() != null && !filtro.nomeAutor().isEmpty()) ||
                filtro.idCategoria() != null ||
                filtro.status() != null ||
                filtro.precoMin() != null ||
                filtro.precoMax() != null;
    }
}
