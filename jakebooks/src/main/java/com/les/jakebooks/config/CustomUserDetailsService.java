package com.les.jakebooks.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.les.jakebooks.domain.Cliente;
import com.les.jakebooks.model.enums.StatusCliente;
import com.les.jakebooks.repository.ClienteRepository;

import java.util.ArrayList;
import java.util.List;

/**
 * Implementação customizada de UserDetailsService.
 * Carrega dados do cliente do banco de dados para autenticação.
 * 
 * Fluxo de autenticação:
 * 1. Usuário submete email/senha no formulário de login
 * 2. Spring Security chama loadUserByUsername(email)
 * 3. Este serviço busca o Cliente pelo email
 * 4. Retorna UserDetails com credenciais e authorities
 * 5. Spring Security valida a senha contra senhaCriptografada
 * 
 * RN0026: Dados obrigatórios do cliente (inclui contexto de autenticação)
 * RNF0012: Usa BCryptPasswordEncoder configurado em SecurityConfig
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private ClienteRepository clienteRepository;

    /**
     * Carrega os dados do usuário pelo email (usado como username).
     * 
     * @param email email do cliente (usado como identificador único)
     * @return UserDetails com credenciais e authorities
     * @throws UsernameNotFoundException se cliente não encontrado ou inativo
     */
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        // Busca cliente pelo email
        Cliente cliente = clienteRepository.findByEmail(email)
            .orElseThrow(() -> new UsernameNotFoundException(
                String.format("Cliente não encontrado com email: %s", email)
            ));

        // Valida se cliente está ativo
        if (cliente.getStatus() == StatusCliente.BLOQUEADO) {
            throw new UsernameNotFoundException(
                "Acesso bloqueado. Contacte o administrador do sistema."
            );
        }

        if (cliente.getStatus() == StatusCliente.INATIVO) {
            throw new UsernameNotFoundException(
                "Cliente inativo. Contacte o administrador do sistema."
            );
        }

        // Constrói lista de autoridades/roles do cliente
        List<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_CLIENTE"));

        // TODO: Implementar lógica para detectar admin (pode ser um campo na entidade)
        // Por exemplo: if (cliente.isAdmin()) { authorities.add(...); }

        // Retorna UserDetails do Spring Security
        return User.builder()
            .username(cliente.getEmail())  // email como username
            .password(cliente.getSenhaCriptografada())  // senha criptografada com BCrypt
            .authorities(authorities)  // roles/permissions
            .accountLocked(false)
            .accountExpired(false)
            .credentialsExpired(false)
            .disabled(false)
            .build();
    }

    /**
     * Carrega UserDetails pelo ID do cliente.
     * Método auxiliar útil para operações pós-autenticação.
     * 
     * @param clienteId ID do cliente
     * @return UserDetails
     */
    public UserDetails loadUserById(Long clienteId) {
        Cliente cliente = clienteRepository.findById(clienteId)
            .orElseThrow(() -> new UsernameNotFoundException(
                String.format("Cliente não encontrado com ID: %d", clienteId)
            ));

        List<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_CLIENTE"));

        return User.builder()
            .username(cliente.getEmail())
            .password(cliente.getSenhaCriptografada())
            .authorities(authorities)
            .accountLocked(false)
            .accountExpired(false)
            .credentialsExpired(false)
            .disabled(false)
            .build();
    }
}
