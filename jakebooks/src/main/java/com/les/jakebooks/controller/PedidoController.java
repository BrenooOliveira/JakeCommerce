package com.les.jakebooks.controller;

import com.les.jakebooks.domain.Pedido;
import com.les.jakebooks.dto.PedidoAdminResumoDTO;
import com.les.jakebooks.dto.PedidoConfirmadoDTO;
import com.les.jakebooks.dto.PedidoResumoDTO;
import com.les.jakebooks.dto.PedidoTransporteDTO;
import com.les.jakebooks.exception.RecursoNaoEncontradoException;
import com.les.jakebooks.exception.ValidacaoNegocioException;
import com.les.jakebooks.model.enums.StatusPedido;
import com.les.jakebooks.repository.PedidoRepository;
import com.les.jakebooks.services.PedidoService;
import com.les.jakebooks.util.SecurityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

/**
 * Controller responsável pelo gerenciamento de pedidos.
 * Segue padrão Frontend: sem lógica de negócio, apenas chamadas a Services.
 * RF0025: Consultar transações do cliente
 * RF0038-RF0039: Operações administrativas com pedidos
 *
 * Autorização:
 * - Listagem: Admin vê todos, Cliente vê próprios pedidos
 * - Detalhe: Admin vê todos, Cliente vê próprios pedidos
 * - Despachar/Entregar: Apenas Admin
 */
@Controller
@RequestMapping("/pedidos")
public class PedidoController {

    @Autowired
    private PedidoService pedidoService;

    @Autowired
    private PedidoRepository pedidoRepository;

    /**
     * Lista pedidos com filtros opcionais.
     * GET /pedidos
     * Admin: vê todos os pedidos
     * Cliente: vê apenas próprios pedidos
     * RF0025: Consultar transações do cliente
     * RF0038: Despachar produtos (admin)
     * RF0039: Confirmar entrega (admin)
     *
     * @param status status do pedido para filtrar (opcional)
     * @param codigoCliente código do cliente para filtrar (opcional, apenas admin)
     * @param model Model para adicionar atributos à view
     * @return view name "pedidos/lista"
     */
    @GetMapping
    public String listar(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String codigoCliente,
            Model model) {

        List<Pedido> pedidos;
        boolean isAdmin = SecurityUtil.isAdmin();
        String emailLogado = SecurityUtil.getEmailUsuarioLogado();

        // Aplicar filtros baseado no perfil
        if (isAdmin) {
            // Admin vê todos os pedidos
            if (status != null && !status.isEmpty()) {
                try {
                    StatusPedido statusEnum = StatusPedido.valueOf(status);
                    pedidos = pedidoRepository.findByStatusOrderByDataCriacaoDesc(statusEnum);
                } catch (IllegalArgumentException e) {
                    pedidos = pedidoRepository.findAll();
                }
            } else {
                pedidos = pedidoRepository.findAll();
            }
        } else {
            // Cliente vê apenas próprios pedidos
            if (status != null && !status.isEmpty()) {
                try {
                    StatusPedido statusEnum = StatusPedido.valueOf(status);
                    pedidos = pedidoRepository.findByClienteEmailAndStatusOrderByDataCriacaoDesc(emailLogado, statusEnum);
                } catch (IllegalArgumentException e) {
                    pedidos = pedidoRepository.findByClienteEmailOrderByDataCriacaoDesc(emailLogado);
                }
            } else {
                pedidos = pedidoRepository.findByClienteEmailOrderByDataCriacaoDesc(emailLogado);
            }
        }

        // Adicionar atributos ao modelo
        model.addAttribute("pedidos", pedidos);
        model.addAttribute("statusPedidos", StatusPedido.values());
        model.addAttribute("statusSelecionado", status);
        model.addAttribute("isAdmin", isAdmin);

        return "pedidos/lista";
    }

    /**
     * Exibe detalhes de um pedido específico.
     * GET /pedidos/{id}
     * Cliente só pode ver próprios pedidos, admin pode ver todos.
     *
     * @param id ID do pedido
     * @param model Model para adicionar atributos à view
     * @param attrs RedirectAttributes para mensagens de erro
     * @return view name "pedidos/detalhe" ou redirect se não encontrado
     */
    @GetMapping("/{id}")
    public String detalhe(
            @PathVariable Long id,
            Model model,
            RedirectAttributes attrs) {

        try {
            // Buscar pedido pelo ID
            Pedido pedido = pedidoRepository.findById(id)
                    .orElseThrow(() -> new RecursoNaoEncontradoException("Pedido com ID " + id + " não encontrado"));

            // Validar acesso: admin pode ver todos, cliente só próprios pedidos
            String emailLogado = SecurityUtil.getEmailUsuarioLogado();
            if (!SecurityUtil.isAdmin() && !pedido.getCliente().getEmail().equals(emailLogado)) {
                attrs.addFlashAttribute("mensagemErro", "Você não tem permissão para visualizar este pedido");
                return "redirect:/";
            }

            // Adicionar atributos
            model.addAttribute("pedido", pedido);
            model.addAttribute("statusPedidos", StatusPedido.values());
            model.addAttribute("isAdmin", SecurityUtil.isAdmin());

            return "pedidos/detalhe";
        } catch (RecursoNaoEncontradoException e) {
            attrs.addFlashAttribute("mensagemErro", "Pedido não encontrado");
            return "redirect:/pedidos";
        }
    }

    /**
     * Despacha um pedido (muda status para EM_TRANSPORTE).
     * POST /pedidos/{id}/despachar
     * RF0038: Despachar produtos (EM TRANSPORTE)
     * RN0039: Status transporte: EM TRANSPORTE
     *
     * @param id ID do pedido
     * @param attrs RedirectAttributes para mensagens
     * @return redirect para /pedidos/{id}
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{id}/despachar")
    public String despachar(
            @PathVariable Long id,
            RedirectAttributes attrs) {

        try {
            pedidoService.despacharPedido(id);
            attrs.addFlashAttribute("mensagemSucesso", "Pedido despachado com sucesso! Status: EM_TRANSPORTE");
            return "redirect:/pedidos/" + id;
        } catch (ValidacaoNegocioException e) {
            attrs.addFlashAttribute("mensagemErro", "Erro ao despachar: " + e.getMessage());
            return "redirect:/pedidos/" + id;
        } catch (RecursoNaoEncontradoException e) {
            attrs.addFlashAttribute("mensagemErro", "Pedido não encontrado");
            return "redirect:/pedidos";
        }
    }

    /**
     * Confirma a entrega de um pedido (muda status para ENTREGUE).
     * POST /pedidos/{id}/entregar
     * RF0039: Confirmar entrega (ENTREGUE)
     * RN0040: Status entrega: ENTREGUE
     *
     * @param id ID do pedido
     * @param attrs RedirectAttributes para mensagens
     * @return redirect para /pedidos/{id}
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{id}/entregar")
    public String entregar(
            @PathVariable Long id,
            RedirectAttributes attrs) {

        try {
            pedidoService.confirmarEntrega(id);
            attrs.addFlashAttribute("mensagemSucesso", "Entrega confirmada com sucesso! Status: ENTREGUE");
            return "redirect:/pedidos/" + id;
        } catch (ValidacaoNegocioException e) {
            attrs.addFlashAttribute("mensagemErro", "Erro ao confirmar entrega: " + e.getMessage());
            return "redirect:/pedidos/" + id;
        } catch (RecursoNaoEncontradoException e) {
            attrs.addFlashAttribute("mensagemErro", "Pedido não encontrado");
            return "redirect:/pedidos";
        }
    }
}
