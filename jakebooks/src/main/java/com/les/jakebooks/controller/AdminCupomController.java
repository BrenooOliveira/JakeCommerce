package com.les.jakebooks.controller;

import com.les.jakebooks.domain.Cupom;
import com.les.jakebooks.domain.enums.TipoCupom;
import com.les.jakebooks.dto.CriarCupomRequestDTO;
import com.les.jakebooks.service.CupomService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;

/**
 * Administração simples de cupons promocionais.
 */
@Controller
@RequestMapping("/admin/cupons")
public class AdminCupomController {

    @Autowired
    private CupomService cupomService;

    @GetMapping
    public String listar(Model model) {
        List<Cupom> cupons = cupomService.buscarPorTipo(TipoCupom.PROMOCIONAL);
        model.addAttribute("cupons", cupons);
        model.addAttribute("cupomForm", new CriarCupomRequestDTO("", null, null));
        return "admin/cupons/listar";
    }

    @PostMapping
    public String criar(
            @Valid @ModelAttribute("cupomForm") CriarCupomRequestDTO cupomForm,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("cupons", cupomService.buscarPorTipo(TipoCupom.PROMOCIONAL));
            return "admin/cupons/listar";
        }

        Cupom cupom = new Cupom();
        cupom.setCodigo(cupomForm.codigo().trim().toUpperCase());
        cupom.setValor(cupomForm.valor());
        cupom.setTipo(TipoCupom.PROMOCIONAL);
        cupom.setAtivo(true);
        cupom.setDataCriacao(LocalDate.now());
        cupom.setDataValidade(cupomForm.dataValidade());

        cupomService.salvar(cupom);

        redirectAttributes.addFlashAttribute("mensagemSucesso", "Cupom cadastrado com sucesso.");
        return "redirect:/admin/cupons";
    }
}
