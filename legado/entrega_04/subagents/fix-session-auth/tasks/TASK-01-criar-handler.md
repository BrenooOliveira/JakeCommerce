# TASK-01: Criar CustomAuthenticationSuccessHandler

## Objetivo

Criar a classe `CustomAuthenticationSuccessHandler` que popula a sessao HTTP com os dados do cliente apos autenticacao bem-sucedida.

## Status: CONCLUIDA

## Arquivo a Criar

```
jakebooks/src/main/java/com/les/jakebooks/config/CustomAuthenticationSuccessHandler.java
```

## Codigo

```java
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
 * Handler customizado para autenticacao bem-sucedida.
 *
 * Responsabilidades:
 * 1. Popular sessao HTTP com codigo e nome do cliente
 * 2. Redirecionar para pagina solicitada ou home
 *
 * Atributos populados na sessao:
 * - codigoClienteAutenticado: codigo unico do cliente
 * - nomeClienteAutenticado: nome do cliente para exibicao
 */
@Component
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    @Autowired
    private ClienteRepository clienteRepository;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {

        // Obtem o email do usuario autenticado (usado como username)
        String email = authentication.getName();

        // Busca o cliente pelo email e popula a sessao
        clienteRepository.findByEmail(email).ifPresent(cliente -> {
            request.getSession().setAttribute("codigoClienteAutenticado", cliente.getCodigo());
            request.getSession().setAttribute("nomeClienteAutenticado", cliente.getNome());
        });

        // Verifica se existe uma requisicao salva (pagina que o usuario tentou acessar)
        SavedRequest savedRequest = new HttpSessionRequestCache().getRequest(request, response);

        if (savedRequest != null) {
            // Redireciona para a pagina que o usuario tentou acessar
            response.sendRedirect(savedRequest.getRedirectUrl());
        } else {
            // Redireciona para a pagina inicial
            response.sendRedirect("/");
        }
    }
}
```

## Dependencias

- `ClienteRepository` com metodo `findByEmail(String email)` - ja existe
- Entidade `Cliente` com `getCodigo()` e `getNome()` - ja existe

## Checklist

- [x] Criar arquivo `CustomAuthenticationSuccessHandler.java`
- [x] Verificar que compila sem erros
- [x] Verificar imports corretos

## Proxima Task

Apos concluir, executar TASK-02-integrar-security.md
