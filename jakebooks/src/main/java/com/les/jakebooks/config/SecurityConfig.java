package com.les.jakebooks.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Configuração de segurança para criptografia de senhas.
 * Disponibiliza um PasswordEncoder baseado em BCrypt para uso em toda aplicação.
 */
@Configuration
public class SecurityConfig {

    /**
     * Define BCryptPasswordEncoder como bean gerenciado pelo Spring.
     * Utiliza força 12 para melhor equilíbrio entre segurança e performance.
     *
     * @return PasswordEncoder baseado em BCrypt
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }
}
