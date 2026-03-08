package com.les.jakebooks.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Controller responsável pela página inicial da aplicação.
 * Segue padrão de Controller: sem lógica de negócio, apenas carregamento de dados via Service.
 */
@Controller
public class HomeController {

    /**
     * Carrega a página inicial da aplicação.
     * 
     * @param model Model para adicionar atributos à view
     * @return view name "index" que usa o layout base
     */
    @GetMapping("/")
    public String home(Model model) {
        // Adiciona flag para exibir/ocultar sidebar admin
        model.addAttribute("isAdmin", false);
        return "index";
    }

    /**
     * Carrega página de erro 404 customizada.
     * 
     * @param model Model para adicionar atributos à view
     * @return view name "error/404"
     */
    @GetMapping("/error/404")
    public String notFound(Model model) {
        model.addAttribute("isAdmin", false);
        return "error/404";
    }

    /**
     * Carrega página de erro 500 customizada.
     * 
     * @param model Model para adicionar atributos à view
     * @return view name "error/500"
     */
    @GetMapping("/error/500")
    public String internalError(Model model) {
        model.addAttribute("isAdmin", false);
        return "error/500";
    }
}
