package com.les.jakebooks.controller;

import com.les.jakebooks.dto.EntradaEstoqueDTO;
import com.les.jakebooks.dto.EstoqueListaDTO;
import com.les.jakebooks.dto.LivroDetalheDTO;
import com.les.jakebooks.exception.RecursoNaoEncontradoException;
import com.les.jakebooks.exception.ValidacaoNegocioException;
import com.les.jakebooks.service.EstoqueService;
import com.les.jakebooks.service.LivroService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Controller responsável pelo gerenciamento de estoque.
 * Segue padrão Frontend: sem lógica de negócio, apenas chamadas a Services.
 * RF0051: Entrada em estoque.
 */
@Controller
@RequestMapping("/admin/estoque")
public class EstoqueController {

    @Autowired
    private EstoqueService estoqueService;

    @Autowired
    private LivroService livroService;

    /**
     * Lista todos os livros com suas informações de estoque.
     * GET /admin/estoque
     * RF0051: Entrada em estoque (visualizar).
     *
     * @param model Model para adicionar atributos à view
     * @return view name "estoque/lista"
     */
    @GetMapping
    public String listar(Model model) {
        // Buscar todos os estoques
        List<EstoqueListaDTO> estoques = estoqueService.listarTodos();

        // Adicionar atributos ao modelo
        model.addAttribute("estoques", estoques);

        // Calcular totalizadores
        int totalItens = estoques.stream()
                .mapToInt(EstoqueListaDTO::quantidade)
                .sum();
        BigDecimal valorTotalEstoque = estoques.stream()
                .map(e -> e.custoAtual().multiply(BigDecimal.valueOf(e.quantidade())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        model.addAttribute("totalItens", totalItens);
        model.addAttribute("valorTotalEstoque", valorTotalEstoque);

        return "estoque/lista";
    }

    /**
     * Exibe formulário para registrar entrada de estoque.
     * GET /admin/estoque/entrada
     * RF0051: Entrada em estoque.
     *
     * @param model Model para adicionar atributos à view
     * @return view name "estoque/form-entrada"
     */
    @GetMapping("/entrada")
    public String formularioEntrada(Model model) {
        // Buscar todos os livros para popular o select
        List<LivroDetalheDTO> livros = livroService.listarTodos();

        model.addAttribute("livros", livros);
        model.addAttribute("dataEntrada", LocalDate.now());

        return "estoque/form-entrada";
    }

    /**
     * Registra uma entrada de estoque.
     * POST /admin/estoque/entrada
     * RF0051: Entrada em estoque.
     * RN0051: Valida dados obrigatórios.
     * RN0061: Valida quantidade > 0.
     * RN0062: Valida custo > 0.
     * RNF0064: Valida data.
     *
     * @param livroId      ID do livro
     * @param quantidade   quantidade da entrada
     * @param custo        custo unitário
     * @param fornecedor   fornecedor do produto
     * @param dataEntrada  data da entrada
     * @param attrs        RedirectAttributes para mensagens
     * @return redirect para /admin/estoque com mensagem de sucesso ou erro
     */
    @PostMapping("/entrada")
    public String registrarEntrada(
            @RequestParam Long livroId,
            @RequestParam Integer quantidade,
            @RequestParam BigDecimal custo,
            @RequestParam String fornecedor,
            @RequestParam LocalDate dataEntrada,
            RedirectAttributes attrs) {

        try {
            // Criar DTO
            EntradaEstoqueDTO dto = new EntradaEstoqueDTO(livroId, quantidade, custo, fornecedor, dataEntrada);

            // Registrar entrada
            estoqueService.registrarEntrada(dto);

            attrs.addFlashAttribute("mensagemSucesso", 
                    "Entrada de estoque registrada com sucesso! " + quantidade + " unidade(s) adicionada(s).");

            return "redirect:/admin/estoque";
        } catch (ValidacaoNegocioException e) {
            attrs.addFlashAttribute("mensagemErro", "Erro de validação: " + e.getMessage());
            return "redirect:/admin/estoque/entrada";
        } catch (RecursoNaoEncontradoException e) {
            attrs.addFlashAttribute("mensagemErro", "Livro não encontrado: " + e.getMessage());
            return "redirect:/admin/estoque/entrada";
        } catch (Exception e) {
            attrs.addFlashAttribute("mensagemErro", "Erro ao registrar entrada: " + e.getMessage());
            return "redirect:/admin/estoque/entrada";
        }
    }
}
