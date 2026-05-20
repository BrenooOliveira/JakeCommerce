package com.les.jakebooks.controller;

import com.les.jakebooks.dto.TrocaDetalheDTO;
import com.les.jakebooks.domain.Pedido;
import com.les.jakebooks.exception.RecursoNaoEncontradoException;
import com.les.jakebooks.exception.ValidacaoNegocioException;
import com.les.jakebooks.service.TrocaService;
import com.les.jakebooks.repository.PedidoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/trocas")
public class ClienteTrocaController {

    @Autowired
    private PedidoRepository pedidoRepository;

    @Autowired
    private TrocaService trocaService;

    @GetMapping("/pedidos/{pedidoId}/solicitar")
    public String formularioSolicitarCliente(@PathVariable Long pedidoId, Model model, RedirectAttributes attrs) {
        Pedido pedido = pedidoRepository.findById(pedidoId).orElse(null);
        if (pedido == null) {
            attrs.addFlashAttribute("mensagemErro", "Pedido não encontrado");
            return "redirect:/pedidos";
        }
        model.addAttribute("pedido", pedido);
        model.addAttribute("pedidoId", pedidoId);
        return "trocas/solicitar";
    }

    @PostMapping("/pedidos/{pedidoId}/solicitar")
    public String solicitarTrocaCliente(@PathVariable Long pedidoId,
                                        @RequestParam String motivo,
                                        @RequestParam(required = false) Long[] itemIds,
                                        RedirectAttributes attrs) {
        try {
            List<Long> itens = null;
            if (itemIds != null && itemIds.length > 0) {
                itens = Arrays.stream(itemIds).collect(Collectors.toList());
            }
            TrocaDetalheDTO troca = trocaService.solicitar(pedidoId, motivo, itens);
            attrs.addFlashAttribute("mensagemSucesso", "Solicitação de troca criada com sucesso! ID da troca: " + troca.trocaId());
            return "redirect:/pedidos/" + pedidoId;
        } catch (ValidacaoNegocioException e) {
            attrs.addFlashAttribute("mensagemErro", "Erro ao solicitar troca: " + e.getMessage());
            return "redirect:/trocas/pedidos/" + pedidoId + "/solicitar";
        } catch (RecursoNaoEncontradoException e) {
            attrs.addFlashAttribute("mensagemErro", "Pedido não encontrado");
            return "redirect:/pedidos";
        }
    }
}
