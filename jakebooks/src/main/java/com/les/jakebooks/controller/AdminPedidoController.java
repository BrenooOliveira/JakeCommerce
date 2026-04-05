package com.les.jakebooks.controller;

import com.les.jakebooks.dto.PedidoAdminResumoDTO;
import com.les.jakebooks.dto.PedidoDetalheDTO;
import com.les.jakebooks.dto.PedidoListagemDTO;
import com.les.jakebooks.dto.PedidoTransporteDTO;
import com.les.jakebooks.exception.TransicaoStatusInvalidaException;
import com.les.jakebooks.exception.ValidacaoNegocioException;
import com.les.jakebooks.model.enums.StatusPedido;
import com.les.jakebooks.services.PedidoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Map;

/**
 * Controller para área administrativa de pedidos.
 * TASK-SHP-04: Implementação de despacho de pedidos.
 * TASK-SHP-05: Implementação de confirmação de entrega.
 * TASK-SHP-06: Dashboard e listagem geral de pedidos por status.
 * RF0038: Despachar produtos (EM_TRANSPORTE).
 * RF0039: Confirmar entrega (ENTREGUE).
 * Acesso restrito a administradores.
 */
@Controller
@RequestMapping("/admin/pedidos")
public class AdminPedidoController {

    @Autowired
    private PedidoService pedidoService;

    /**
     * Dashboard principal e listagem de todos os pedidos.
     * TASK-SHP-06: Endpoint GET /admin/pedidos
     *
     * @param status filtro por status (opcional)
     * @param page página atual (padrão 0)
     * @param busca busca por código (opcional)
     * @param model modelo para view
     * @return nome da view admin/pedidos/index
     */
    @GetMapping
    public String listarTodos(
            @RequestParam(required = false) StatusPedido status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(required = false) String busca,
            Model model) {

        try {
            // Dashboard - contagem por status
            Map<StatusPedido, Long> contagem = pedidoService.contarPedidosPorStatus();
            model.addAttribute("contagem", contagem);

            // Lista paginada
            Pageable pageable = PageRequest.of(page, 20, Sort.by("dataCriacao").descending());
            Page<PedidoListagemDTO> pedidos = pedidoService.listarPedidos(status, pageable);
            model.addAttribute("pedidos", pedidos);
            model.addAttribute("statusFiltro", status);

            // Busca por código
            if (busca != null && !busca.trim().isEmpty()) {
                pedidoService.buscarPorCodigo(busca.trim())
                        .ifPresent(p -> model.addAttribute("pedidoBusca", p));
            }

            // Disponibilizar todos os status para o filtro
            model.addAttribute("statusPedidos", StatusPedido.values());

            return "admin/pedidos/index";

        } catch (ValidacaoNegocioException e) {
            model.addAttribute("erro", e.getMessage());
            return "admin/pedidos/index";
        }
    }

    /**
     * Exibe detalhes completos de um pedido específico.
     * TASK-SHP-06: Endpoint GET /admin/pedidos/{id}
     *
     * @param id ID do pedido
     * @param model modelo para view
     * @return nome da view admin/pedidos/detalhes
     */
    @GetMapping("/{id}")
    public String detalhesPedido(@PathVariable Long id, Model model) {
        try {
            PedidoDetalheDTO pedido = pedidoService.buscarPorId(id)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pedido não encontrado"));

            model.addAttribute("pedido", pedido);
            return "admin/pedidos/detalhes";

        } catch (ValidacaoNegocioException e) {
            model.addAttribute("erro", e.getMessage());
            return "redirect:/admin/pedidos";
        }
    }

    /**
     * Lista pedidos aguardando despacho (status EM_PROCESSAMENTO).
     * TASK-SHP-04: Endpoint GET /admin/pedidos/despacho
     *
     * @param model modelo para view
     * @return nome da view admin/pedidos/despacho
     */
    @GetMapping("/despacho")
    public String listarParaDespacho(Model model) {
        try {
            List<PedidoAdminResumoDTO> pedidos = pedidoService.listarPedidosParaDespacho();
            model.addAttribute("pedidos", pedidos);
            return "admin/pedidos/despacho";
        } catch (ValidacaoNegocioException e) {
            model.addAttribute("erro", e.getMessage());
            return "admin/pedidos/despacho";
        }
    }

    /**
     * Despacha um pedido específico.
     * TASK-SHP-04: Endpoint POST /admin/pedidos/{id}/despachar
     *
     * @param id ID do pedido a ser despachado
     * @param redirectAttributes atributos para redirect
     * @return redirect para lista de despacho
     */
    @PostMapping("/{id}/despachar")
    public String despachar(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            PedidoAdminResumoDTO pedidoDespachado = pedidoService.despacharPedido(id);
            redirectAttributes.addFlashAttribute("sucesso",
                "Pedido " + pedidoDespachado.codigoPedido() + " despachado com sucesso!");

        } catch (TransicaoStatusInvalidaException e) {
            redirectAttributes.addFlashAttribute("erro", e.getMessage());

        } catch (ValidacaoNegocioException e) {
            redirectAttributes.addFlashAttribute("erro", e.getMessage());

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("erro",
                "Erro interno do servidor. Tente novamente.");
        }

        return "redirect:/admin/pedidos/despacho";
    }

    /**
     * Lista pedidos em transporte aguardando confirmação de entrega.
     * TASK-SHP-05: Endpoint GET /admin/pedidos/transporte
     *
     * @param model modelo para view
     * @return nome da view admin/pedidos/transporte
     */
    @GetMapping("/transporte")
    public String listarEmTransporte(Model model) {
        try {
            List<PedidoTransporteDTO> pedidos = pedidoService.listarPedidosEmTransporte();
            model.addAttribute("pedidos", pedidos);
            return "admin/pedidos/transporte";
        } catch (ValidacaoNegocioException e) {
            model.addAttribute("erro", e.getMessage());
            return "admin/pedidos/transporte";
        }
    }

    /**
     * Confirma a entrega de um pedido específico.
     * TASK-SHP-05: Endpoint POST /admin/pedidos/{id}/confirmar-entrega
     *
     * @param id ID do pedido com entrega a ser confirmada
     * @param redirectAttributes atributos para redirect
     * @return redirect para lista de transporte
     */
    @PostMapping("/{id}/confirmar-entrega")
    public String confirmarEntrega(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            PedidoTransporteDTO pedidoEntregue = pedidoService.confirmarEntrega(id);
            redirectAttributes.addFlashAttribute("sucesso",
                "Entrega do pedido " + pedidoEntregue.codigoPedido() + " confirmada com sucesso!");

        } catch (TransicaoStatusInvalidaException e) {
            redirectAttributes.addFlashAttribute("erro", e.getMessage());

        } catch (ValidacaoNegocioException e) {
            redirectAttributes.addFlashAttribute("erro", e.getMessage());

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("erro",
                "Erro interno do servidor. Tente novamente.");
        }

        return "redirect:/admin/pedidos/transporte";
    }
}