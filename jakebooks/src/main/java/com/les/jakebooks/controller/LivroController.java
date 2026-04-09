package com.les.jakebooks.controller;

import com.les.jakebooks.dto.*;
import com.les.jakebooks.exception.RecursoNaoEncontradoException;
import com.les.jakebooks.exception.ValidacaoNegocioException;
import com.les.jakebooks.domain.enums.StatusLivro;
import com.les.jakebooks.repository.AutorRepository;
import com.les.jakebooks.repository.CategoriaRepository;
import com.les.jakebooks.repository.EditoraRepository;
import com.les.jakebooks.repository.GrupoPrecificacaoRepository;
import com.les.jakebooks.service.LivroService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

/**
 * Controller responsável pela gerência de livros.
 * Segue padrão Frontend: sem lógica de negócio, apenas chamadas a Services.
 * RF0011-RF0016: Operações com livros
 */
@Controller
@RequestMapping("/livros")
public class LivroController {

    @Autowired
    private LivroService livroService;

    @Autowired
    private CategoriaRepository categoriaRepository;

    @Autowired
    private AutorRepository autorRepository;

    @Autowired
    private EditoraRepository editoraRepository;

    @Autowired
    private GrupoPrecificacaoRepository grupoPrecificacaoRepository;

    /**
     * Lista livros com filtros opcionais.
     * GET /livros
     * RF0015: Consultar livros com filtros combinados
     *
     * @param filtro DTO com critérios de busca (opcionais)
     * @param page número da página (padrão 0)
     * @param model Model para adicionar atributos à view
     * @return view name "livros/lista"
     */
    @GetMapping
    public String listar(
            @ModelAttribute LivroFiltroDTO filtro,
            @RequestParam(defaultValue = "0") int page,
            Model model) {

        // Buscar livros com filtros
        List<LivroListagemDTO> livros = livroService.buscarComFiltros(filtro);

        // Converter para Page (paginação manual)
        Pageable pageable = PageRequest.of(page, 10);
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), livros.size());
        List<LivroListagemDTO> livrosPaginados = livros.subList(start, end);
        Page<LivroListagemDTO> pageResult = new PageImpl<>(livrosPaginados, pageable, livros.size());

        // Adicionar ao model
        model.addAttribute("page", pageResult);
        model.addAttribute("pageLink", "/livros");
        model.addAttribute("filtro", filtro);
        model.addAttribute("statusLivros", StatusLivro.values());
        model.addAttribute("categorias", categoriaRepository.findAll());

        return "livros/lista";
    }

    /**
     * Exibe formulário para novo livro.
     * GET /livros/novo
     * RF0011: Cadastrar livro
     *
     * @param model Model para adicionar atributos à view
     * @return view name "livros/form"
     */
    @GetMapping("/novo")
    public String formularioNovo(Model model) {
        model.addAttribute("livroForm", new LivroFormDTO(
                null, null, null, null, null, null, null, null, null, null, null, null, null, null, null
        ));
        adicionarDadosFormulario(model);
        model.addAttribute("modoEdicao", false);
        return "livros/form";
    }

    /**
     * Cria novo livro.
     * POST /livros
     * RF0011: Cadastrar livro
     *
     * @param dto DTO com dados do livro
     * @param result resultado da validação
     * @param attrs RedirectAttributes para mensagens flash
     * @return redirect para /livros em caso de sucesso, ou volta ao formulário
     */
    @PostMapping
    public String criar(
            @Valid @ModelAttribute("livroForm") LivroFormDTO dto,
            BindingResult result,
            RedirectAttributes attrs) {

        if (result.hasErrors()) {
            attrs.addFlashAttribute("mensagemErro", "Verifique os erros abaixo");
            return "redirect:/livros/novo";
        }

        try {
            livroService.cadastrar(dto);
            attrs.addFlashAttribute("mensagemSucesso", "Livro cadastrado com sucesso!");
            return "redirect:/livros";
        } catch (ValidacaoNegocioException e) {
            attrs.addFlashAttribute("mensagemErro", e.getMessage());
            return "redirect:/livros/novo";
        } catch (RecursoNaoEncontradoException e) {
            attrs.addFlashAttribute("mensagemErro", "Recurso não encontrado: " + e.getMessage());
            return "redirect:/livros/novo";
        }
    }

    /**
     * Exibe detalhes de um livro.
     * GET /livros/{codigo}
     * RF0015: Consultar livros
     *
     * @param codigo código único do livro
     * @param model Model para adicionar atributos à view
     * @param attrs RedirectAttributes para mensagens de erro
     * @return view name "livros/detalhe" ou redirect se não encontrado
     */
    @GetMapping("/{codigo}")
    public String detalhe(
            @PathVariable String codigo,
            Model model,
            RedirectAttributes attrs) {

        try {
            LivroDetalheDTO livro = livroService.buscarPorCodigo(codigo);
            model.addAttribute("livro", livro);
            return "livros/detalhe";
        } catch (RecursoNaoEncontradoException e) {
            attrs.addFlashAttribute("mensagemErro", "Livro não encontrado");
            return "redirect:/livros";
        }
    }

    /**
     * Exibe formulário para editar um livro existente.
     * GET /livros/{codigo}/editar
     * RF0014: Alterar livro
     *
     * @param codigo código único do livro
     * @param model Model para adicionar atributos à view
     * @param attrs RedirectAttributes para mensagens de erro
     * @return view name "livros/form" preenchido ou redirect se não encontrado
     */
    @GetMapping("/{codigo}/editar")
    public String formularioEditar(
            @PathVariable String codigo,
            Model model,
            RedirectAttributes attrs) {

        try {
            LivroDetalheDTO livroExistente = livroService.buscarPorCodigo(codigo);

            // Converter para FormDTO (sem o campo status)
            LivroFormDTO livroForm = new LivroFormDTO(
                    livroExistente.codigo(),
                    livroExistente.titulo(),
                    livroExistente.ano(),
                    livroExistente.edicao(),
                    livroExistente.isbn(),
                    livroExistente.numeroPaginas(),
                    livroExistente.sinopse(),
                    livroExistente.dimensoes(),
                    livroExistente.codigoBarras(),
                    livroExistente.status(),
                    livroExistente.valorVenda(),
                    livroExistente.grupoPrecificacao().id(),
                    livroExistente.editora().id(),
                    livroExistente.autores().stream().map(AutorDTO::id).toList(),
                    livroExistente.categorias().stream().map(CategoriaDTO::id).toList()
            );

            model.addAttribute("livroForm", livroForm);
            model.addAttribute("livroExistente", livroExistente);
            adicionarDadosFormulario(model);
            model.addAttribute("modoEdicao", true);
            model.addAttribute("codigoLivro", codigo);

            return "livros/form";
        } catch (RecursoNaoEncontradoException e) {
            attrs.addFlashAttribute("mensagemErro", "Livro não encontrado");
            return "redirect:/livros";
        }
    }

    /**
     * Atualiza um livro existente.
     * POST /livros/{codigo}
     * RF0014: Alterar livro
     *
     * @param codigo código único do livro
     * @param dto DTO com novos dados
     * @param result resultado da validação
     * @param autorizacaoReducao se há autorização para redução de margem
     * @param attrs RedirectAttributes para mensagens flash
     * @return redirect para /livros/{codigo} em caso de sucesso
     */
    @PostMapping("/{codigo}")
    public String atualizar(
            @PathVariable String codigo,
            @Valid @ModelAttribute("livroForm") LivroFormDTO dto,
            BindingResult result,
            @RequestParam(defaultValue = "false") boolean autorizacaoReducao,
            RedirectAttributes attrs) {

        if (result.hasErrors()) {
            attrs.addFlashAttribute("mensagemErro", "Verifique os erros abaixo");
            return "redirect:/livros/" + codigo + "/editar";
        }

        try {
            livroService.alterar(codigo, dto, autorizacaoReducao);
            attrs.addFlashAttribute("mensagemSucesso", "Livro atualizado com sucesso!");
            return "redirect:/livros/" + codigo;
        } catch (ValidacaoNegocioException e) {
            attrs.addFlashAttribute("mensagemErro", e.getMessage());
            return "redirect:/livros/" + codigo + "/editar";
        } catch (RecursoNaoEncontradoException e) {
            attrs.addFlashAttribute("mensagemErro", "Livro ou recurso não encontrado");
            return "redirect:/livros";
        }
    }

    /**
     * Inativa um livro manualmente.
     * POST /livros/{codigo}/inativar
     * RF0012: Inativar livro manualmente
     *
     * @param codigo código único do livro
     * @param motivo motivo da inativação
     * @param attrs RedirectAttributes para mensagens flash
     * @return redirect para /livros/{codigo}
     */
    @PostMapping("/{codigo}/inativar")
    public String inativar(
            @PathVariable String codigo,
            @RequestParam String motivo,
            RedirectAttributes attrs) {

        try {
            livroService.inativarManual(codigo, motivo);
            attrs.addFlashAttribute("mensagemSucesso", "Livro inativado com sucesso!");
            return "redirect:/livros/" + codigo;
        } catch (ValidacaoNegocioException | RecursoNaoEncontradoException e) {
            attrs.addFlashAttribute("mensagemErro", e.getMessage());
            return "redirect:/livros/" + codigo;
        }
    }

    /**
     * Ativa um livro inativo.
     * POST /livros/{codigo}/ativar
     * RF0016: Ativar livro
     *
     * @param codigo código único do livro
     * @param justificativa justificativa da ativação
     * @param attrs RedirectAttributes para mensagens flash
     * @return redirect para /livros/{codigo}
     */
    @PostMapping("/{codigo}/ativar")
    public String ativar(
            @PathVariable String codigo,
            @RequestParam String justificativa,
            RedirectAttributes attrs) {

        try {
            livroService.ativar(codigo, justificativa);
            attrs.addFlashAttribute("mensagemSucesso", "Livro ativado com sucesso!");
            return "redirect:/livros/" + codigo;
        } catch (ValidacaoNegocioException | RecursoNaoEncontradoException e) {
            attrs.addFlashAttribute("mensagemErro", e.getMessage());
            return "redirect:/livros/" + codigo;
        }
    }

    /**
     * Adiciona dados necessários ao formulário (selects, dropdowns, etc).
     * Método privado para reutilização.
     *
     * @param model Model para adicionar atributos
     */
    private void adicionarDadosFormulario(Model model) {
        model.addAttribute("grupos", grupoPrecificacaoRepository.findAll());
        model.addAttribute("editoras", editoraRepository.findAll());
        model.addAttribute("autores", autorRepository.findAll());
        model.addAttribute("categorias", categoriaRepository.findAll());
        model.addAttribute("statusLivros", StatusLivro.values());
    }
}
