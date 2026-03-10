package com.les.jakebooks.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Controller para gerenciar rotas de autenticação.
 * Retorna templates de login, logout e acesso negado.
 * 
 * RNF0012: Sistema de autenticação e autorização com Spring Security
 */
@Controller
@RequestMapping
public class AuthController {

    /**
     * Retorna a página de login.
     * Rota: GET /login
     * 
     * @return template login/form
     */
    @GetMapping("/login")
    public String login() {
        return "login/form";
    }

    /**
     * Página de acesso negado (403).
     * Retorna quando usuário tenta acessar recurso sem permissão.
     * 
     * @return template error/403
     */
    @GetMapping("/acesso-negado")
    public String acessoNegado() {
        return "error/403";
    }
}
