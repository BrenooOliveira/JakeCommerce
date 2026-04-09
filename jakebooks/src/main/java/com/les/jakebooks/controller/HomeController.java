package com.les.jakebooks.controller;

import org.springframework.stereotype.Controller;
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
     * @return view name "index" que usa o layout base
     */
    @GetMapping("/")
    public String home() {
        return "index";
    }

    /**
     * Carrega página de erro 404 customizada.
     * 
     * @return view name "error/404"
     */
    @GetMapping("/error/404")
    public String notFound() {
        return "error/404";
    }

    /**
     * Carrega página de erro 500 customizada.
     * 
     * @return view name "error/500"
     */
    @GetMapping("/error/500")
    public String internalError() {
        return "error/500";
    }
}
