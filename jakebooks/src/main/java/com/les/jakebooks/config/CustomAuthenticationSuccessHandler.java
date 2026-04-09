package com.les.jakebooks.config;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.savedrequest.SavedRequest;
import org.springframework.stereotype.Component;

import com.les.jakebooks.repository.ClienteRepository;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Após login: popula a sessão com dados do cliente e redireciona.
 * Admin → /admin (painel). Cliente → /. Respeita URL salva (SavedRequest) quando existir.
 */
@Component
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private static final String ROLE_ADMIN = "ROLE_ADMIN";

    @Autowired
    private ClienteRepository clienteRepository;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {

        String email = authentication.getName();

        clienteRepository.findByEmail(email).ifPresent(cliente -> {
            request.getSession().setAttribute("codigoClienteAutenticado", cliente.getCodigo());
            request.getSession().setAttribute("nomeClienteAutenticado", cliente.getNome());
        });

        SavedRequest savedRequest = new HttpSessionRequestCache().getRequest(request, response);
        if (savedRequest != null) {
            String redirectUrl = savedRequest.getRedirectUrl();
            if (redirectUrl != null && !redirectUrl.contains("/login")) {
                response.sendRedirect(redirectUrl);
                return;
            }
        }

        String ctx = request.getContextPath();
        boolean isAdmin = authentication.getAuthorities().stream()
            .anyMatch(a -> ROLE_ADMIN.equals(a.getAuthority()));

        if (isAdmin) {
            response.sendRedirect(ctx + "/admin");
        } else {
            response.sendRedirect(ctx + "/");
        }
    }
}
