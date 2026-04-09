package com.les.jakebooks.config;

import com.les.jakebooks.util.SecurityUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/**
 * Controller Advice para atributos globais de modelo.
 */
@ControllerAdvice
public class GlobalModelAttributeAdvice {

    /**
     * URI atual (Thymeleaf 3.1+ não expõe #httpServletRequest por padrão).
     */
    @ModelAttribute("requestURI")
    public String requestURI(HttpServletRequest request) {
        return request.getRequestURI();
    }

    /**
     * Código do cliente logado (sessão), para links como "meu cadastro" na home.
     */
    @ModelAttribute("codigoClienteAutenticado")
    public String codigoClienteAutenticado(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return null;
        }
        Object codigo = session.getAttribute("codigoClienteAutenticado");
        return codigo != null ? String.valueOf(codigo) : null;
    }

    /**
     * Colunas do layout (sidebar admin): mesmo critério de {@code hasRole('ADMIN')}.
     */
    @ModelAttribute("painelAdmin")
    public boolean painelAdmin() {
        return SecurityUtil.isAdmin();
    }
}
