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
import com.les.jakebooks.domain.enums.StatusCliente;
import com.les.jakebooks.repository.ClienteRepository;

import java.util.ArrayList;
import java.util.List;

/**
 * Implementação customizada de UserDetailsService.
 * Carrega dados do cliente do banco de dados para autenticação.
 *
 * O papel vem de {@link Cliente#getUsuarioRole()} ou, se ausente, de {@link Cliente#getIsAdmin()}.
 * Administradores recebem ROLE_CLIENTE e ROLE_ADMIN (área administrativa + fluxo de cliente).
 *
 * RN0026: Dados obrigatórios do cliente (inclui contexto de autenticação)
 * RNF0012: Usa BCryptPasswordEncoder configurado em SecurityConfig
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    private static final String ROLE_CLIENTE = "ROLE_CLIENTE";
    private static final String ROLE_ADMIN = "ROLE_ADMIN";

    @Autowired
    private ClienteRepository clienteRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Cliente cliente = clienteRepository.findByEmail(email)
            .orElseThrow(() -> new UsernameNotFoundException(
                String.format("Cliente não encontrado com email: %s", email)
            ));

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

        List<GrantedAuthority> authorities = montarAuthorities(cliente);

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

    /**
     * Carrega UserDetails pelo ID do cliente.
     */
    public UserDetails loadUserById(Long clienteId) {
        Cliente cliente = clienteRepository.findById(clienteId)
            .orElseThrow(() -> new UsernameNotFoundException(
                String.format("Cliente não encontrado com ID: %d", clienteId)
            ));

        List<GrantedAuthority> authorities = montarAuthorities(cliente);

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

    private static List<GrantedAuthority> montarAuthorities(Cliente cliente) {
        String papel = cliente.getUsuarioRole();
        if (papel == null || papel.isBlank()) {
            papel = Boolean.TRUE.equals(cliente.getIsAdmin()) ? ROLE_ADMIN : ROLE_CLIENTE;
        }

        List<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority(ROLE_CLIENTE));
        if (ROLE_ADMIN.equals(papel)) {
            authorities.add(new SimpleGrantedAuthority(ROLE_ADMIN));
        }
        return authorities;
    }
}
