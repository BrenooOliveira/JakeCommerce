package com.les.jakebooks.controller;

import com.les.jakebooks.dto.CarrinhoDTO;
import com.les.jakebooks.dto.CartaoDTO;
import com.les.jakebooks.dto.ClienteDetalheDTO;
import com.les.jakebooks.dto.EnderecoDTO;
import com.les.jakebooks.dto.FinalizarPedidoDTO;
import com.les.jakebooks.dto.PagamentoCartaoDadosDTO;
import com.les.jakebooks.dto.ResultadoCheckoutDTO;
import com.les.jakebooks.dto.CupomDTO;
import com.les.jakebooks.exception.RecursoNaoEncontradoException;
import com.les.jakebooks.exception.ValidacaoNegocioException;
import com.les.jakebooks.domain.enums.BandeiraCartao;
import com.les.jakebooks.domain.enums.TipoEndereco;
import com.les.jakebooks.domain.enums.TipoResidencia;
import com.les.jakebooks.service.CarrinhoService;
import com.les.jakebooks.service.ClienteService;
import com.les.jakebooks.service.CompraService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * HTTP do fluxo de checkout: sem regra de negócio (delega a {@link CompraService}).
 * RF0034–RF0037: checkout e finalização.
 */
@Controller
@RequestMapping("/checkout")
public class CheckoutController {

    @Autowired
    private CarrinhoService carrinhoService;

    @Autowired
    private ClienteService clienteService;

    @Autowired
    private CompraService compraService;

    @Autowired
    private com.les.jakebooks.service.CupomService cupomService;

    @GetMapping
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

            CarrinhoDTO carrinho = carrinhoService.obterOuCriar(codigoCliente);

            if (carrinho.itens().isEmpty()) {
                attrs.addFlashAttribute("mensagemErro", "Seu carrinho está vazio");
                return "redirect:/carrinho";
            }

            ClienteDetalheDTO cliente = clienteService.buscarPorCodigo(codigoCliente);

            model.addAttribute("carrinho", carrinho);
            model.addAttribute("cliente", cliente);
            model.addAttribute("clienteCodigo", codigoCliente);
            model.addAttribute("enderecos", cliente.enderecos());
            model.addAttribute("cartoes", cliente.cartoes());
            model.addAttribute("bandeiras", BandeiraCartao.values());
            // Atributos para o formulário de novo endereço
            model.addAttribute("endereco", new EnderecoDTO(null, null, null, null, null, null, null, null, null, null, null, null));
            model.addAttribute("cartao", new CartaoDTO(null, null, null, null, null, false));
            model.addAttribute("tiposResidencia", TipoResidencia.values());
            model.addAttribute("tiposEndereco", TipoEndereco.values());

            return "carrinho/checkout";
        } catch (RecursoNaoEncontradoException e) {
            attrs.addFlashAttribute("mensagemErro", "Dados não encontrados: " + e.getMessage());
            return "redirect:/carrinho";
        }
    }

    @PostMapping("/novo-endereco")
    public String novoEnderecoCheckout(
            HttpSession session,
            @Valid @ModelAttribute EnderecoDTO dto,
            BindingResult result,
            RedirectAttributes attrs) {

        try {
            String codigoCliente = (String) session.getAttribute("codigoClienteAutenticado");
            if (codigoCliente == null || codigoCliente.isEmpty()) {
                attrs.addFlashAttribute("mensagemErro", "Você precisa estar autenticado");
                return "redirect:/login";
            }

            if (result.hasErrors()) {
                attrs.addFlashAttribute("mensagemErro", "Verifique os erros do formulário");
                return "redirect:/checkout";
            }

            clienteService.adicionarEndereco(codigoCliente, dto);
            attrs.addFlashAttribute("mensagemSucesso", "Endereço adicionado com sucesso!");
            return "redirect:/checkout";
        } catch (ValidacaoNegocioException | RecursoNaoEncontradoException e) {
            attrs.addFlashAttribute("mensagemErro", e.getMessage());
            return "redirect:/checkout";
        }
    }

    @PostMapping("/novo-cartao")
    public String novoCartaoCheckout(
            HttpSession session,
            @Valid @ModelAttribute CartaoDTO dto,
            BindingResult result,
            RedirectAttributes attrs) {

        try {
            String codigoCliente = (String) session.getAttribute("codigoClienteAutenticado");
            if (codigoCliente == null || codigoCliente.isEmpty()) {
                attrs.addFlashAttribute("mensagemErro", "Você precisa estar autenticado");
                return "redirect:/login";
            }

            if (result.hasErrors()) {
                attrs.addFlashAttribute("mensagemErro", "Verifique os erros do formulário de cartão");
                return "redirect:/checkout";
            }

            clienteService.adicionarCartao(codigoCliente, dto);
            attrs.addFlashAttribute("mensagemSucesso", "Cartão adicionado com sucesso!");
            return "redirect:/checkout";
        } catch (ValidacaoNegocioException | RecursoNaoEncontradoException e) {
            attrs.addFlashAttribute("mensagemErro", e.getMessage());
            return "redirect:/checkout";
        }
    }

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

            List<PagamentoCartaoDadosDTO> pagamentos = new ArrayList<>();
            if (!pagamentoJson.isEmpty() && !pagamentoJson.equals("[]")) {
                String[] items = pagamentoJson.split(",");
                for (String item : items) {
                    String[] parts = item.trim().split(":");
                    if (parts.length == 2) {
                        try {
                            Long cartaoId = Long.parseLong(parts[0]);
                            BigDecimal valor = new BigDecimal(parts[1]);
                            pagamentos.add(new PagamentoCartaoDadosDTO(cartaoId, valor));
                        } catch (NumberFormatException ignored) {
                            // Formato inválido ignorado
                        }
                    }
                }
            }

            FinalizarPedidoDTO finalizarDto = new FinalizarPedidoDTO(
                    codigoCliente,
                    carrinhoId,
                    enderecoId,
                    codigoCupom != null && !codigoCupom.isEmpty() ? codigoCupom : null,
                    pagamentos);

            ResultadoCheckoutDTO resultado = compraService.executarCheckout(finalizarDto);

            if (resultado.getStatus() == ResultadoCheckoutDTO.StatusResultadoCheckout.SUCESSO
                    && resultado.getPedidoId() != null) {
                attrs.addFlashAttribute("mensagemSucesso",
                        "Pedido realizado com sucesso! ID do pedido: " + resultado.getPedidoId());
                return "redirect:/pedidos/" + resultado.getPedidoId();
            }

            if (resultado.getStatus() == ResultadoCheckoutDTO.StatusResultadoCheckout.PAGAMENTO_REPROVADO) {
                String msg = resultado.getMensagem();
                if (resultado.getTentativasRestantes() != null) {
                    msg += " Tentativas restantes antes do bloqueio: " + resultado.getTentativasRestantes() + ".";
                }
                attrs.addFlashAttribute("mensagemErro", msg);
                return "redirect:/checkout";
            }

            if (resultado.getStatus() == ResultadoCheckoutDTO.StatusResultadoCheckout.BLOQUEADO) {
                attrs.addFlashAttribute("mensagemErro", resultado.getMensagem());
                return "redirect:/checkout";
            }

            attrs.addFlashAttribute("mensagemErro",
                    resultado.getMensagem() != null ? resultado.getMensagem() : "Não foi possível concluir o checkout.");
            return "redirect:/checkout";

        } catch (RecursoNaoEncontradoException e) {
            attrs.addFlashAttribute("mensagemErro", "Dados não encontrados: " + e.getMessage());
            return "redirect:/checkout";
        }
    }

        @org.springframework.web.bind.annotation.RequestMapping(
            value = "/validar-cupom",
            method = {RequestMethod.GET, RequestMethod.POST},
            produces = "application/json")
        @org.springframework.web.bind.annotation.ResponseBody
        public org.springframework.http.ResponseEntity<?> validarCupom(
            jakarta.servlet.http.HttpSession session,
            @RequestParam String codigo) {

        // Apenas valida e retorna o valor do cupom promocional como JSON.
        com.les.jakebooks.dto.CupomDTO dto = cupomService.validarCupomPromocional(codigo);

        java.util.Map<String, Object> body = java.util.Map.of(
                "valor", dto.valor(),
                "codigo", dto.codigo()
        );

        return org.springframework.http.ResponseEntity.ok(body);
    }
}
