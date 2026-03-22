package com.les.jakebooks.controller;

import com.les.jakebooks.dto.CarrinhoDTO;
import com.les.jakebooks.dto.ClienteDetalheDTO;
import com.les.jakebooks.dto.FinalizarPedidoDTO;
import com.les.jakebooks.dto.PagamentoCartaoDadosDTO;
import com.les.jakebooks.dto.PedidoConfirmadoDTO;
import com.les.jakebooks.exception.RecursoNaoEncontradoException;
import com.les.jakebooks.exception.ValidacaoNegocioException;
import com.les.jakebooks.model.enums.BandeiraCartao;
import com.les.jakebooks.repository.CartaoRepository;
import com.les.jakebooks.repository.EnderecoRepository;
import com.les.jakebooks.services.CarrinhoService;
import com.les.jakebooks.services.ClienteService;
import com.les.jakebooks.services.PedidoService;
import com.les.jakebooks.util.SecurityUtil;
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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

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

    @Autowired
    private ClienteService clienteService;

    @Autowired
    private PedidoService pedidoService;

    @Autowired
    private EnderecoRepository enderecoRepository;

    @Autowired
    private CartaoRepository cartaoRepository;

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
            model.addAttribute("isAdmin", SecurityUtil.isAdmin());
            
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

    /**
     * Exibe a página de checkout para finalizar a compra.
     * GET /carrinho/checkout
     * RF0034: Calcular frete
     * RF0035: Selecionar endereço
     * RF0036: Selecionar pagamento
     *
     * @param session sessão HTTP para obter código do cliente
     * @param model Model para adicionar atributos à view
     * @param attrs RedirectAttributes para mensagens de erro
     * @return view name "carrinho/checkout" ou redirect se carrinho vazio
     */
    @GetMapping("/checkout")
    public String checkout(
            HttpSession session,
            Model model,
            RedirectAttributes attrs) {

        try {
            String codigoCliente = (String) session.getAttribute("codigoClienteAutenticado");
            if (codigoCliente == null || codigoCliente.isEmpty()) {
                attrs.addFlashAttribute("mensagemErro", "Você precisa estar autenticado");
                return "redirect:/login";
            }

            // Obter carrinho
            CarrinhoDTO carrinho = carrinhoService.obterOuCriar(codigoCliente);
            
            // Validar se carrinho tem itens
            if (carrinho.itens().isEmpty()) {
                attrs.addFlashAttribute("mensagemErro", "Seu carrinho está vazio");
                return "redirect:/carrinho";
            }

            // Obter dados do cliente
            ClienteDetalheDTO cliente = clienteService.buscarPorCodigo(codigoCliente);

            // Preparar dados para formulário de checkout
            model.addAttribute("carrinho", carrinho);
            model.addAttribute("cliente", cliente);
            model.addAttribute("enderecos", cliente.enderecos());
            model.addAttribute("cartoes", cliente.cartoes());
            model.addAttribute("bandeiras", BandeiraCartao.values());
            model.addAttribute("isAdmin", SecurityUtil.isAdmin());

            return "carrinho/checkout";
        } catch (RecursoNaoEncontradoException e) {
            attrs.addFlashAttribute("mensagemErro", "Dados não encontrados: " + e.getMessage());
            return "redirect:/carrinho";
        }
    }

    /**
     * Finaliza a compra e cria um pedido.
     * POST /carrinho/finalizar
     * RF0037: Finalizar compra (cria pedido com status EM PROCESSAMENTO)
     * RF0033: Realizar compra com validações
     * RN0033: Apenas um cupom promocional por compra
     * RN0034: Múltiplos cartões permitidos (mínimo R$10 por cartão)
     * RN0035: Consumir cupons antes do cartão
     *
     * @param session sessão HTTP para obter código do cliente
     * @param carrinhoId ID do carrinho
     * @param enderecoId ID do endereço de entrega
     * @param codigoCupom código do cupom promocional (opcional)
     * @param pagamentoJson dados de pagamento com cartões em JSON
     * @param attrs RedirectAttributes para mensagens
     * @return redirect para detalhes do pedido ou volta para checkout
     */
    @PostMapping("/finalizar")
    public String finalizar(
            HttpSession session,
            @RequestParam Long carrinhoId,
            @RequestParam Long enderecoId,
            @RequestParam(required = false) String codigoCupom,
            @RequestParam(required = false, defaultValue = "[]") String pagamentoJson,
            RedirectAttributes attrs) {

        try {
            String codigoCliente = (String) session.getAttribute("codigoClienteAutenticado");
            if (codigoCliente == null || codigoCliente.isEmpty()) {
                attrs.addFlashAttribute("mensagemErro", "Você precisa estar autenticado");
                return "redirect:/login";
            }

            // Parse dos dados de pagamento (formato simples: lista de cartãoId:valor)
            List<PagamentoCartaoDadosDTO> pagamentos = new ArrayList<>();
            if (!pagamentoJson.isEmpty() && !pagamentoJson.equals("[]")) {
                // Simplificado: em produção, usar Jackson ou similar
                // Exemplo: "1:100.00,2:50.00"
                String[] items = pagamentoJson.split(",");
                for (String item : items) {
                    String[] parts = item.trim().split(":");
                    if (parts.length == 2) {
                        try {
                            Long cartaoId = Long.parseLong(parts[0]);
                            BigDecimal valor = new BigDecimal(parts[1]);
                            pagamentos.add(new PagamentoCartaoDadosDTO(cartaoId, valor));
                        } catch (NumberFormatException e) {
                            // Ignorar formato inválido
                        }
                    }
                }
            }

            // Criar DTO para finalização
            FinalizarPedidoDTO finalizarDto = new FinalizarPedidoDTO(
                    codigoCliente,
                    carrinhoId,
                    enderecoId,
                    codigoCupom != null && !codigoCupom.isEmpty() ? codigoCupom : null,
                    pagamentos
            );

            // Finalizar pedido
            PedidoConfirmadoDTO pedido = pedidoService.finalizarPedido(finalizarDto);

            attrs.addFlashAttribute("mensagemSucesso", 
                    "Pedido realizado com sucesso! ID do pedido: " + pedido.pedidoId());
            
            return "redirect:/pedidos/" + pedido.pedidoId();
        } catch (ValidacaoNegocioException e) {
            attrs.addFlashAttribute("mensagemErro", "Erro na validação: " + e.getMessage());
            return "redirect:/carrinho/checkout";
        } catch (RecursoNaoEncontradoException e) {
            attrs.addFlashAttribute("mensagemErro", "Dados não encontrados: " + e.getMessage());
            return "redirect:/carrinho/checkout";
        } catch (Exception e) {
            attrs.addFlashAttribute("mensagemErro", "Erro ao processar pedido: " + e.getMessage());
            return "redirect:/carrinho/checkout";
        }
    }
}
