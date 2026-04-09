package com.les.jakebooks.controller;

import com.les.jakebooks.domain.Pedido;
import com.les.jakebooks.dto.PedidoResumoDTO;
import com.les.jakebooks.exception.RecursoNaoEncontradoException;
import com.les.jakebooks.exception.ValidacaoNegocioException;
import com.les.jakebooks.domain.enums.StatusPedido;
import com.les.jakebooks.repository.PedidoRepository;
import com.les.jakebooks.service.PedidoService;
import org.springframework.beans.factory.annotation.Autowired;
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
 * Controller responsável pelo gerenciamento de pedidos (administrativo).
 * Segue padrão Frontend: sem lógica de negócio, apenas chamadas a Services.
 * RF0038-RF0039: Operações com pedidos
 * RF0042: Visualizar trocas
 */
@Controller
@RequestMapping("/pedidos")
public class PedidoController {

    @Autowired
    private PedidoService pedidoService;

    @Autowired
    private PedidoRepository pedidoRepository;

    /**
     * Lista todos os pedidos com filtros opcionais.
     * GET /pedidos
     * RF0038: Despachar produtos
     * RF0039: Confirmar entrega
     *
     * @param status status do pedido para filtrar (opcional)
     * @param codigoCliente código do cliente para filtrar (opcional)
     * @param model Model para adicionar atributos à view
     * @return view name "pedidos/lista"
     */
    @GetMapping
    public String listar(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String codigoCliente,
            Model model) {

        List<Pedido> pedidos;

        // Aplicar filtros
        if (status != null && !status.isEmpty()) {
            try {
                StatusPedido statusEnum = StatusPedido.valueOf(status);
                pedidos = pedidoRepository.findByStatusOrderByDataCriacaoDesc(statusEnum);
            } catch (IllegalArgumentException e) {
                // Status inválido, listar todos
                pedidos = pedidoRepository.findAll();
            }
        } else {
            // Listar todos os pedidos
            pedidos = pedidoRepository.findAll();
        }

        // Adicionar atributos ao modelo
        model.addAttribute("pedidos", pedidos);
        model.addAttribute("statusPedidos", StatusPedido.values());
        model.addAttribute("statusSelecionado", status);

        return "pedidos/lista";
    }

    /**
     * Exibe detalhes de um pedido específico.
     * GET /pedidos/{id}
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

            // Adicionar atributos
            model.addAttribute("pedido", pedido);
            model.addAttribute("statusPedidos", StatusPedido.values());

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
