package com.les.jakebooks.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Configuração de segurança centralizada: rotas públicas, ROLE_CLIENTE e ROLE_ADMIN.
 *
 * RNF0012: Senha criptografada com BCrypt (força 12)
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private CustomAuthenticationSuccessHandler successHandler;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(authz -> authz
                .requestMatchers(
                    "/",
                    "/login",
                    "/acesso-negado",
                    "/error",
                    "/error/**",
                    "/css/**",
                    "/js/**",
                    "/images/**",
                    "/h2-console/**"
                ).permitAll()

                .requestMatchers("/livros/novo").hasRole("ADMIN")
                .requestMatchers("/livros/*/editar").hasRole("ADMIN")
                .requestMatchers(HttpMethod.POST, "/livros").hasRole("ADMIN")
                .requestMatchers(HttpMethod.POST, "/livros/*/inativar", "/livros/*/ativar").hasRole("ADMIN")
                .requestMatchers(HttpMethod.POST, "/livros/*").hasRole("ADMIN")
                .requestMatchers(HttpMethod.GET, "/livros", "/livros/*").permitAll()

                .requestMatchers(HttpMethod.GET, "/clientes").hasRole("ADMIN")
                .requestMatchers(HttpMethod.GET, "/clientes/novo").permitAll()
                .requestMatchers(HttpMethod.POST, "/clientes").permitAll()
                .requestMatchers("/clientes/**").hasRole("CLIENTE")

                .requestMatchers("/admin/trocas/pedidos/*/solicitar").hasRole("CLIENTE")
                .requestMatchers("/admin", "/admin/**").hasRole("ADMIN")

                .requestMatchers(
                    "/carrinho/**",
                    "/pedidos/**"
                ).hasRole("CLIENTE")

                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .usernameParameter("email")
                .passwordParameter("senha")
                .successHandler(successHandler)
                .failureUrl("/login?erro=true")
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/")
                .permitAll()
            )
            .exceptionHandling(ex -> ex.accessDeniedPage("/acesso-negado"))
            .csrf(csrf -> csrf
                .ignoringRequestMatchers("/h2-console/**")
            )
            .headers(headers -> headers
                .frameOptions(frame -> frame.disable())
            );

        return http.build();
    }
}
