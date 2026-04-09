package com.les.jakebooks.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Ponto de entrada do painel administrativo após login.
 */
@Controller
public class AdminRootController {

    @GetMapping("/admin")
    public String painelAdmin() {
        return "redirect:/admin/pedidos";
    }
}
