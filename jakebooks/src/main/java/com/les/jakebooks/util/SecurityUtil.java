package com.les.jakebooks.util;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Utilitário para operações relacionadas a segurança e autenticação.
 * Fornece métodos estáticos para verificar autorização e obter dados do usuário logado.
 */
public class SecurityUtil {

    /**
     * Verifica se o usuário atualmente autenticado possui a role ADMIN.
     *
     * @return true se o usuário tem ROLE_ADMIN, false caso contrário
     */
    public static boolean isAdmin() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return false;
        }
        return auth.getAuthorities().stream()
            .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"));
    }

    /**
     * Retorna o email (username) do usuário atualmente autenticado.
     *
     * @return email do usuário logado, ou null se não autenticado
     */
    public static String getEmailUsuarioLogado() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return null;
        }
        return auth.getName();
    }

    /**
     * Verifica se existe um usuário autenticado no contexto atual.
     *
     * @return true se há usuário autenticado, false caso contrário
     */
    public static boolean isAuthenticated() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null && auth.isAuthenticated()
            && !"anonymousUser".equals(auth.getPrincipal());
    }
}
