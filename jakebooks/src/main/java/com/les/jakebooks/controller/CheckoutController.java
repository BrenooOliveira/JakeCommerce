package com.les.jakebooks.controller;

import com.les.jakebooks.domain.Cliente;
import com.les.jakebooks.dto.CarrinhoDTO;
import com.les.jakebooks.dto.CheckoutDTO;
import com.les.jakebooks.dto.EnderecoDTO;
import com.les.jakebooks.dto.FreteDTO;
import com.les.jakebooks.exception.AcessoNegadoException;
import com.les.jakebooks.exception.EnderecoEntregaNaoEncontradoException;
import com.les.jakebooks.exception.RecursoNaoEncontradoException;
import com.les.jakebooks.repository.ClienteRepository;
import com.les.jakebooks.services.CarrinhoService;
import com.les.jakebooks.services.EnderecoService;
import com.les.jakebooks.services.FreteService;
import com.les.jakebooks.util.SecurityUtil;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.List;

/**
 * Controller responsável pelo fluxo de checkout.
 * Segue padrão Frontend: sem lógica de negócio, apenas chamadas a Services.
 * RF0033: Realizar compra
 * RF0035: Selecionar endereço de entrega
 */
@Controller
@RequestMapping("/checkout")
@PreAuthorize("isAuthenticated()")
public class CheckoutController {

    @Autowired
    private EnderecoService enderecoService;

    @Autowired
    private FreteService freteService;

    @Autowired
    private CarrinhoService carrinhoService;

    @Autowired
    private ClienteRepository clienteRepository;

    /**
     * Exibe tela de seleção de endereço de entrega.
     * GET /checkout/endereco
     * RF0035: Selecionar endereço de entrega
     * RN0022: Cliente deve ter pelo menos um endereço de entrega
     *
     * @param model Model para adicionar atributos à view
     * @return view name "checkout/endereco"
     */
    @GetMapping("/endereco")
    public String exibirSelecaoEndereco(Model model) {
        // Obter email do cliente logado
        String emailLogado = SecurityUtil.getEmailUsuarioLogado();

        // Buscar cliente pelo email
        Cliente cliente = clienteRepository.findByEmail(emailLogado)
                .orElseThrow(() -> new RuntimeException("Cliente não encontrado"));

        try {
            // Buscar endereços de entrega do cliente
            List<EnderecoDTO> enderecos = enderecoService.listarEnderecosEntrega(cliente.getId());
            model.addAttribute("enderecos", enderecos);
            model.addAttribute("cliente", cliente);
            return "checkout/endereco";

        } catch (EnderecoEntregaNaoEncontradoException e) {
            // Cliente não tem endereços de entrega cadastrados
            model.addAttribute("erro", e.getMessage());
            model.addAttribute("redirecionarCadastro", true);
            return "checkout/endereco";
        }
    }

    /**
     * Processa seleção de endereço de entrega.
     * POST /checkout/endereco
     * RF0035: Selecionar endereço de entrega
     *
     * Valida endereço e armazena na sessão do checkout.
     * Redireciona para cálculo de frete (TASK-SHP-03).
     *
     * @param enderecoId ID do endereço selecionado
     * @param session HttpSession para armazenar dados do checkout
     * @param redirectAttributes RedirectAttributes para mensagens flash
     * @return redirect para /checkout/frete ou /checkout/endereco em caso de erro
     */
    @PostMapping("/endereco")
    public String selecionarEndereco(
            @RequestParam Long enderecoId,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        // Obter email do cliente logado
        String emailLogado = SecurityUtil.getEmailUsuarioLogado();

        // Buscar cliente pelo email
        Cliente cliente = clienteRepository.findByEmail(emailLogado)
                .orElseThrow(() -> new RuntimeException("Cliente não encontrado"));

        try {
            // Validar e selecionar endereço
            EnderecoDTO endereco = enderecoService.selecionarEnderecoEntrega(
                    cliente.getId(), enderecoId
            );

            // Obter ou criar CheckoutDTO na sessão
            CheckoutDTO checkout = (CheckoutDTO) session.getAttribute("checkout");
            if (checkout == null) {
                checkout = new CheckoutDTO();
            }

            // Armazenar endereço selecionado
            checkout.setEnderecoEntregaId(enderecoId);
            session.setAttribute("checkout", checkout);

            // Adicionar mensagem de sucesso
            redirectAttributes.addFlashAttribute("mensagemSucesso",
                    "Endereço de entrega selecionado com sucesso!");

            // Redirecionar para cálculo de frete (TASK-SHP-03)
            return "redirect:/checkout/frete";

        } catch (RecursoNaoEncontradoException | AcessoNegadoException | EnderecoEntregaNaoEncontradoException e) {
            // Adicionar mensagem de erro e retornar para seleção
            redirectAttributes.addFlashAttribute("mensagemErro", e.getMessage());
            return "redirect:/checkout/endereco";
        }
    }

    /**
     * Exibe tela de cálculo e confirmação do frete.
     * GET /checkout/frete
     * RF0034: Calcular frete
     * RN0064: Pedido mínimo R$20 para frete grátis
     *
     * @param session HttpSession para recuperar dados do checkout
     * @param model Model para adicionar atributos à view
     * @param redirectAttributes RedirectAttributes para mensagens flash
     * @return view name "checkout/frete" ou redirect se endereço não selecionado
     */
    @GetMapping("/frete")
    public String exibirFrete(
            HttpSession session,
            Model model,
            RedirectAttributes redirectAttributes) {

        // Obter email do cliente logado
        String emailLogado = SecurityUtil.getEmailUsuarioLogado();

        // Buscar cliente pelo email
        Cliente cliente = clienteRepository.findByEmail(emailLogado)
                .orElseThrow(() -> new RuntimeException("Cliente não encontrado"));

        // Obter checkout da sessão
        CheckoutDTO checkout = (CheckoutDTO) session.getAttribute("checkout");

        // Validar se endereço foi selecionado
        if (checkout == null || checkout.getEnderecoEntregaId() == null) {
            redirectAttributes.addFlashAttribute("mensagemErro",
                    "Selecione um endereço de entrega primeiro");
            return "redirect:/checkout/endereco";
        }

        try {
            // Buscar endereço selecionado
            EnderecoDTO endereco = enderecoService.selecionarEnderecoEntrega(
                    cliente.getId(), checkout.getEnderecoEntregaId()
            );

            // Buscar carrinho do cliente
            CarrinhoDTO carrinho = carrinhoService.obterOuCriar(cliente.getCodigo());
            BigDecimal valorCarrinho = carrinho.valorTotal();

            // Calcular frete
            FreteDTO frete = freteService.calcularFrete(
                    checkout.getEnderecoEntregaId(),
                    valorCarrinho
            );

            // Armazenar frete na sessão
            checkout.setFrete(frete);
            session.setAttribute("checkout", checkout);

            // Calcular valor total (carrinho + frete)
            BigDecimal valorTotal = valorCarrinho.add(frete.getValor());

            // Adicionar dados ao modelo
            model.addAttribute("endereco", endereco);
            model.addAttribute("frete", frete);
            model.addAttribute("valorCarrinho", valorCarrinho);
            model.addAttribute("valorTotal", valorTotal);

            return "checkout/frete";

        } catch (RecursoNaoEncontradoException e) {
            redirectAttributes.addFlashAttribute("mensagemErro", e.getMessage());
            return "redirect:/checkout/endereco";
        }
    }

    /**
     * Confirma o frete e prossegue para pagamento.
     * POST /checkout/frete
     * RF0034: Calcular frete
     *
     * @param session HttpSession para validar dados do checkout
     * @param redirectAttributes RedirectAttributes para mensagens flash
     * @return redirect para /checkout/pagamento ou /checkout/frete se frete não calculado
     */
    @PostMapping("/frete")
    public String confirmarFrete(
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        // Obter checkout da sessão
        CheckoutDTO checkout = (CheckoutDTO) session.getAttribute("checkout");

        // Validar se frete foi calculado
        if (checkout == null || checkout.getFrete() == null) {
            redirectAttributes.addFlashAttribute("mensagemErro",
                    "Frete não foi calculado. Tente novamente.");
            return "redirect:/checkout/frete";
        }

        // Frete já calculado, prosseguir para pagamento
        redirectAttributes.addFlashAttribute("mensagemSucesso",
                "Frete confirmado! Selecione a forma de pagamento.");

        // Por enquanto, redirecionar para o carrinho (será /checkout/pagamento na próxima task)
        return "redirect:/carrinho/view";
    }
}
