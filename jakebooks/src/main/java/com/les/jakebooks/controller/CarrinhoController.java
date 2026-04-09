package com.les.jakebooks.controller;

import com.les.jakebooks.dto.CarrinhoDTO;
import com.les.jakebooks.exception.RecursoNaoEncontradoException;
import com.les.jakebooks.exception.ValidacaoNegocioException;
import com.les.jakebooks.service.CarrinhoService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Controller responsável pelo gerenciamento de carrinho de compras.
 * Segue padrão Frontend: sem lógica de negócio, apenas chamadas a Services.
 * RF0031-RF0034: Operações com carrinho
 * RF0033, RF0035-RF0037: Fluxo de compra
 */
@Controller
@RequestMapping("/carrinho")
public class CarrinhoController {

    @Autowired
    private CarrinhoService carrinhoService;

    /**
     * Exibe o carrinho de compras do cliente.
     * GET /carrinho
     * RF0031: Gerenciar carrinho
     *
     * @param session sessão HTTP para obter código do cliente
     * @param model Model para adicionar atributos à view
     * @param attrs RedirectAttributes para mensagens de erro
     * @return view name "carrinho/view" ou redirect se não autenticado
     */
    @GetMapping
    public String view(
            HttpSession session,
            Model model,
            RedirectAttributes attrs) {

        try {
            String codigoCliente = (String) session.getAttribute("codigoClienteAutenticado");
            if (codigoCliente == null || codigoCliente.isEmpty()) {
                attrs.addFlashAttribute("mensagemErro", "Você precisa estar autenticado para acessar o carrinho");
                return "redirect:/login";
            }

            CarrinhoDTO carrinho = carrinhoService.obterOuCriar(codigoCliente);
            
            model.addAttribute("carrinho", carrinho);

            return "carrinho/view";
        } catch (RecursoNaoEncontradoException e) {
            attrs.addFlashAttribute("mensagemErro", "Cliente não encontrado");
            return "redirect:/login";
        }
    }

    /**
     * Adiciona um livro ao carrinho.
     * POST /carrinho/adicionar
     * RF0032: Definir quantidade no carrinho
     * RN0031: Validar estoque no carrinho
     * RN0063: Máximo 10 unidades do mesmo livro
     *
     * @param session sessão HTTP para obter código do cliente
     * @param codigoLivro código do livro a adicionar
     * @param quantidade quantidade a adicionar
     * @param attrs RedirectAttributes para mensagens
     * @return redirect para /carrinho
     */
    @PostMapping("/adicionar")
    public String adicionar(
            HttpSession session,
            @RequestParam String codigoLivro,
            @RequestParam(defaultValue = "1") int quantidade,
            RedirectAttributes attrs) {

        try {
            String codigoCliente = (String) session.getAttribute("codigoClienteAutenticado");
            if (codigoCliente == null || codigoCliente.isEmpty()) {
                attrs.addFlashAttribute("mensagemErro", "Você precisa estar autenticado");
                return "redirect:/login";
            }

            CarrinhoDTO carrinho = carrinhoService.adicionarItem(codigoCliente, codigoLivro, quantidade);
            attrs.addFlashAttribute("mensagemSucesso", "Livro adicionado ao carrinho com sucesso!");
            
            return "redirect:/carrinho";
        } catch (ValidacaoNegocioException e) {
            attrs.addFlashAttribute("mensagemErro", e.getMessage());
            return "redirect:/carrinho";
        } catch (RecursoNaoEncontradoException e) {
            attrs.addFlashAttribute("mensagemErro", "Livro não encontrado: " + e.getMessage());
            return "redirect:/livros";
        }
    }

    /**
     * Remove um livro do carrinho.
     * POST /carrinho/remover/{codigoLivro}
     * RF0031: Gerenciar carrinho
     *
     * @param session sessão HTTP para obter código do cliente
     * @param codigoLivro código do livro a remover
     * @param attrs RedirectAttributes para mensagens
     * @return redirect para /carrinho
     */
    @PostMapping("/remover/{codigoLivro}")
    public String remover(
            HttpSession session,
            @PathVariable String codigoLivro,
            RedirectAttributes attrs) {

        try {
            String codigoCliente = (String) session.getAttribute("codigoClienteAutenticado");
            if (codigoCliente == null || codigoCliente.isEmpty()) {
                attrs.addFlashAttribute("mensagemErro", "Você precisa estar autenticado");
                return "redirect:/login";
            }

            CarrinhoDTO carrinho = carrinhoService.removerItem(codigoCliente, codigoLivro);
            attrs.addFlashAttribute("mensagemSucesso", "Livro removido do carrinho");
            
            return "redirect:/carrinho";
        } catch (RecursoNaoEncontradoException e) {
            attrs.addFlashAttribute("mensagemErro", "Item não encontrado no carrinho");
            return "redirect:/carrinho";
        }
    }

    /**
     * Altera a quantidade de um item no carrinho.
     * POST /carrinho/quantidade
     * RF0032: Definir quantidade no carrinho
     * RN0063: Máximo 10 unidades do mesmo livro
     *
     * @param session sessão HTTP para obter código do cliente
     * @param codigoLivro código do livro
     * @param novaQuantidade nova quantidade
     * @param attrs RedirectAttributes para mensagens
     * @return redirect para /carrinho
     */
    @PostMapping("/quantidade")
    public String alterarQuantidade(
            HttpSession session,
            @RequestParam String codigoLivro,
            @RequestParam int novaQuantidade,
            RedirectAttributes attrs) {

        try {
            String codigoCliente = (String) session.getAttribute("codigoClienteAutenticado");
            if (codigoCliente == null || codigoCliente.isEmpty()) {
                attrs.addFlashAttribute("mensagemErro", "Você precisa estar autenticado");
                return "redirect:/login";
            }

            CarrinhoDTO carrinho = carrinhoService.alterarQuantidade(codigoCliente, codigoLivro, novaQuantidade);
            attrs.addFlashAttribute("mensagemSucesso", "Quantidade alterada com sucesso");
            
            return "redirect:/carrinho";
        } catch (ValidacaoNegocioException e) {
            attrs.addFlashAttribute("mensagemErro", e.getMessage());
            return "redirect:/carrinho";
        }
    }
}
