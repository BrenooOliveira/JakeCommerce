package com.les.jakebooks.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Configuração de segurança para a aplicação JakeCommerce.
 * 
 * Áreas de Acesso:
 * ─────────────────────────────────────────────────────────
 * PÚBLICO (sem autenticação):
 *   - /                           (página inicial)
 *   - /livros                     (listagem de livros)
 *   - /login                      (formulário de login)
 *   - /clientes/novo              (cadastro de cliente)
 *   - /css/**, /js/**             (recursos estáticos)
 * 
 * AUTENTICADO (Cliente):
 *   - /carrinho/**                (adicionar, remover, checkout)
 *   - /pedidos/**                 (visualizar pedidos próprios)
 *   - /trocas/solicitar           (solicitar troca)
 *   - /clientes/**                (perfil, alterar dados)
 * 
 * ADMIN:
 *   - /admin/**                   (painel administrativo)
 *   - /estoque/**                 (gerenciar estoque)
 *   - /analise/**                 (análise de dados)
 *   - /trocas/**                  (gerenciar trocas)
 * 
 * Autenticação:
 * - UserDetailsService: CustomUserDetailsService carrega Cliente pelo email
 * - PasswordEncoder: BCryptPasswordEncoder força 12
 * - Login: POST /login com username (email) e password
 * 
 * RNF0012: Senha criptografada com BCrypt (força 12)
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
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

    /**
     * Configura o AuthenticationManager com DaoAuthenticationProvider.
     * DaoAuthenticationProvider usa UserDetailsService e PasswordEncoder.
     * 
     * @param config AuthenticationConfiguration fornecida pelo Spring
     * @return AuthenticationManager configurado
     * @throws Exception se houver erro na configuração
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    /**
     * Configura o SecurityFilterChain (Spring Security 6.x).
     * Define autorização, autenticação, CSRF, headers de segurança.
     * 
     * @param http configuração de segurança HTTP
     * @return SecurityFilterChain configurado
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // Configuração de autorização HTTP
            .authorizeHttpRequests(authz -> authz
                // Rotas públicas (sem autenticação)
                .requestMatchers(
                    "/",
                    "/livros",
                    "/login",
                    "/clientes/novo",
                    "/clientes",
                    "/css/**",
                    "/js/**",
                    "/images/**",
                    "/h2-console/**"
                ).permitAll()

                // Rotas autenticadas - Qualquer cliente autenticado
                .requestMatchers(
                    "/carrinho/**",
                    "/pedidos/**",
                    "/trocas/solicitar",
                    "/clientes/perfil",
                    "/clientes/alterar",
                    "/clientes/alterar-senha"
                ).authenticated()

                // Rotas administrativas - requer ROLE_ADMIN
                .requestMatchers(
                    "/admin/**",
                    "/estoque/**",
                    "/analise/**",
                    "/trocas/**"
                ).hasRole("ADMIN")

                // Qualquer outra rota requer autenticação
                .anyRequest().authenticated()
            )

            // Configuração de login customizado
            .formLogin(form -> form
                .loginPage("/login")                // URL do formulário de login
                .loginProcessingUrl("/login")       // URL para processar o login (POST)
                .usernameParameter("email")         // Nome do parâmetro (email em vez de username)
                .passwordParameter("senha")         // Nome do parâmetro da senha
                .defaultSuccessUrl("/")             // Redireciona após login bem-sucedido
                .failureUrl("/login?erro=true")     // Redireciona em caso de erro
                .permitAll()                        // Permite acesso à página de login
            )

            // Configuração de logout
            .logout(logout -> logout
                .logoutUrl("/logout")               // URL para logout
                .logoutSuccessUrl("/")              // Redireciona após logout
                .permitAll()
            )

            // Configuração de CSRF (Cross-Site Request Forgery)
            .csrf(csrf -> csrf
                .ignoringRequestMatchers("/h2-console/**")  // Desabilitar CSRF para H2 console
            )

            // Configuração de headers de segurança
            .headers(headers -> headers
                .frameOptions(frame -> frame.disable())  // Permitir H2 console (frame)
            );

        return http.build();
    }
}
