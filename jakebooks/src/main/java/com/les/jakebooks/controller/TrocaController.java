package com.les.jakebooks.controller;

import com.les.jakebooks.dto.TrocaDetalheDTO;
import com.les.jakebooks.exception.RecursoNaoEncontradoException;
import com.les.jakebooks.exception.ValidacaoNegocioException;
import com.les.jakebooks.model.enums.StatusTroca;
import com.les.jakebooks.services.TrocaService;
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
 * Controller responsável pelo gerenciamento de trocas (administrativo e cliente).
 * Segue padrão Frontend: sem lógica de negócio, apenas chamadas a Services.
 * RF0040-RF0043: Operações com trocas
 */
@Controller
@RequestMapping("/trocas")
public class TrocaController {

    @Autowired
    private TrocaService trocaService;

    /**
     * Lista todas as trocas com filtro por status.
     * GET /trocas
     * RF0042: Visualizar trocas (admin)
     *
     * @param status status da troca para filtrar (opcional)
     * @param model Model para adicionar atributos à view
     * @return view name "trocas/lista"
     */
    @GetMapping
    public String listar(
            @RequestParam(required = false) String status,
            Model model) {

        // Buscar todas as trocas
        List<TrocaDetalheDTO> trocas = trocaService.listarTodas();

        // Filtrar por status se indicado
        if (status != null && !status.isEmpty()) {
            try {
                StatusTroca statusEnum = StatusTroca.valueOf(status);
                trocas = trocas.stream()
                        .filter(troca -> troca.status() == statusEnum)
                        .toList();
            } catch (IllegalArgumentException e) {
                // Status inválido, manter todas
            }
        }

        // Adicionar atributos ao modelo
        model.addAttribute("trocas", trocas);
        model.addAttribute("statusTrocas", StatusTroca.values());
        model.addAttribute("statusSelecionado", status);
        model.addAttribute("isAdmin", true);

        return "trocas/lista";
    }

    /**
     * Exibe detalhes de uma troca específica.
     * GET /trocas/{id}
     *
     * @param id ID da troca
     * @param model Model para adicionar atributos à view
     * @param attrs RedirectAttributes para mensagens de erro
     * @return view name "trocas/detalhe" ou redirect se não encontrado
     */
    @GetMapping("/{id}")
    public String detalhe(
            @PathVariable Long id,
            Model model,
            RedirectAttributes attrs) {

        try {
            TrocaDetalheDTO troca = trocaService.buscarDetalhes(id);
            
            model.addAttribute("troca", troca);
            model.addAttribute("statusTrocas", StatusTroca.values());
            model.addAttribute("isAdmin", true);

            return "trocas/detalhe";
        } catch (Exception e) {
            attrs.addFlashAttribute("mensagemErro", "Troca não encontrada");
            return "redirect:/trocas";
        }
    }

    /**
     * Autoriza uma troca (muda status para AUTORIZADA).
     * POST /trocas/{id}/autorizar
     * RF0041: Autorizar troca
     * RN0041: Altera status para AUTORIZADA
     *
     * @param id ID da troca
     * @param attrs RedirectAttributes para mensagens
     * @return redirect para /trocas/{id}
     */
    @PostMapping("/{id}/autorizar")
    public String autorizar(
            @PathVariable Long id,
            RedirectAttributes attrs) {

        try {
            trocaService.autorizar(id);
            attrs.addFlashAttribute("mensagemSucesso", "Troca autorizada com sucesso!");
            return "redirect:/trocas/" + id;
        } catch (ValidacaoNegocioException e) {
            attrs.addFlashAttribute("mensagemErro", "Erro ao autorizar: " + e.getMessage());
            return "redirect:/trocas/" + id;
        } catch (RecursoNaoEncontradoException e) {
            attrs.addFlashAttribute("mensagemErro", "Troca não encontrada");
            return "redirect:/trocas";
        }
    }

    /**
     * Confirma recebimento de uma troca (muda status para CONCLUIDA e gera cupom).
     * POST /trocas/{id}/receber
     * RF0043: Confirmar recebimento de troca
     * RN0043: Gera cupom para troca e altera status para CONCLUIDA
     *
     * @param id ID da troca
     * @param attrs RedirectAttributes para mensagens
     * @return redirect para /trocas/{id}
     */
    @PostMapping("/{id}/receber")
    public String receberTroca(
            @PathVariable Long id,
            RedirectAttributes attrs) {

        try {
            trocaService.confirmarRecebimento(id);
            attrs.addFlashAttribute("mensagemSucesso", "Recebimento da troca confirmado! Cupom foi gerado para o cliente.");
            return "redirect:/trocas/" + id;
        } catch (ValidacaoNegocioException e) {
            attrs.addFlashAttribute("mensagemErro", "Erro ao confirmar recebimento: " + e.getMessage());
            return "redirect:/trocas/" + id;
        } catch (RecursoNaoEncontradoException e) {
            attrs.addFlashAttribute("mensagemErro", "Troca não encontrada");
            return "redirect:/trocas";
        }
    }

    /**
     * Exibe formulário para solicitar troca de um pedido entregue.
     * GET /pedidos/{pedidoId}/solicitar-troca
     * RF0040: Solicitar troca
     *
     * @param pedidoId ID do pedido
     * @param model Model para adicionar atributos à view
     * @param attrs RedirectAttributes para mensagens de erro
     * @return view name "trocas/solicitar" ou redirect se pedido não é entregue
     */
    @GetMapping("/pedidos/{pedidoId}/solicitar")
    public String formularioSolicitar(
            @PathVariable Long pedidoId,
            Model model,
            RedirectAttributes attrs) {

        try {
            // Validação será feita no service ao criar a negociação
            model.addAttribute("pedidoId", pedidoId);
            model.addAttribute("isAdmin", false);

            return "trocas/solicitar";
        } catch (Exception e) {
            attrs.addFlashAttribute("mensagemErro", "Erro ao acessar formulário de troca");
            return "redirect:/pedidos/" + pedidoId;
        }
    }

    /**
     * Cria uma solicitação de troca para um pedido entregue.
     * POST /pedidos/{pedidoId}/solicitar-troca
     * RF0040: Solicitar troca
     * RN0043: Apenas pedidos ENTREGUES podem solicitar troca
     *
     * @param pedidoId ID do pedido
     * @param motivo motivo da troca
     * @param attrs RedirectAttributes para mensagens
     * @return redirect para /trocas/{trocaId} ou volta ao pedido se erro
     */
    @PostMapping("/pedidos/{pedidoId}/solicitar")
    public String solicitarTroca(
            @PathVariable Long pedidoId,
            @RequestParam String motivo,
            RedirectAttributes attrs) {

        try {
            // Validar se motivo foi preenchido
            if (motivo == null || motivo.trim().isEmpty()) {
                attrs.addFlashAttribute("mensagemErro", "Motivo da troca é obrigatório");
                return "redirect:/trocas/pedidos/" + pedidoId + "/solicitar";
            }

            // Criar solicitação de troca
            TrocaDetalheDTO troca = trocaService.solicitar(pedidoId, motivo);

            attrs.addFlashAttribute("mensagemSucesso", 
                    "Solicitação de troca criada com sucesso! ID da troca: " + troca.trocaId());
            
            return "redirect:/trocas/" + troca.trocaId();
        } catch (ValidacaoNegocioException e) {
            attrs.addFlashAttribute("mensagemErro", "Erro ao solicitar troca: " + e.getMessage());
            return "redirect:/pedidos/" + pedidoId;
        } catch (RecursoNaoEncontradoException e) {
            attrs.addFlashAttribute("mensagemErro", "Pedido não encontrado");
            return "redirect:/pedidos";
        }
    }
}
