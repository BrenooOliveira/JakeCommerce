package com.les.jakebooks.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.les.jakebooks.interceptor.TransacaoInterceptor;

/**
 * Configuração do Spring MVC para registrar interceptors.
 * 
 * Responsável por:
 * - Registrar o interceptor de transações
 * - Configurar rotas que devem ser interceptadas
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private final TransacaoInterceptor transacaoInterceptor;

    public WebMvcConfig(TransacaoInterceptor transacaoInterceptor) {
        this.transacaoInterceptor = transacaoInterceptor;
    }

    /**
     * Registra os interceptors da aplicação.
     * 
     * RNF0012: Log de transações com data, hora, usuário, operação e dados alterados.
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(transacaoInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(
                    "/static/**",
                    "/css/**",
                    "/js/**",
                    "/images/**",
                    "/error/**"
                );
    }
}
