package com.les.jakebooks.service;

import com.les.jakebooks.domain.Cliente;
import com.les.jakebooks.domain.Cupom;
import com.les.jakebooks.domain.enums.TipoCupom;
import com.les.jakebooks.dto.CupomDTO;
import com.les.jakebooks.exception.CupomInvalidoException;
import com.les.jakebooks.repository.ClienteRepository;
import com.les.jakebooks.repository.CupomRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CupomServiceTest {

    @Mock
    private CupomRepository cupomRepository;

    @Mock
    private ClienteRepository clienteRepository;

    @Mock
    private LogService logService;

    @InjectMocks
    private CupomService cupomService;

    @AfterEach
    void limparContextoSeguranca() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void deveValidarCupomDeTrocaDoClienteLogadoNoCheckout() {
        String email = "breno@teste.com";
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                email,
                "senha",
                List.of(new SimpleGrantedAuthority("ROLE_CLIENTE")));
        SecurityContextHolder.getContext().setAuthentication(authentication);

        Cliente cliente = new Cliente();
        cliente.setId(10L);
        cliente.setEmail(email);

        Cupom cupom = new Cupom();
        cupom.setCodigo("TROCA-ABC123");
        cupom.setValor(new BigDecimal("25.00"));
        cupom.setTipo(TipoCupom.TROCA);
        cupom.setAtivo(true);
        cupom.setCliente(cliente);
        cupom.setDataValidade(LocalDate.now().plusDays(10));

        when(cupomRepository.findByCodigo("TROCA-ABC123")).thenReturn(Optional.of(cupom));
        when(clienteRepository.findByEmail(email)).thenReturn(Optional.of(cliente));

        CupomDTO dto = cupomService.validarCupomCheckout("troca-abc123");

        assertEquals("TROCA-ABC123", dto.codigo());
        assertEquals(new BigDecimal("25.00"), dto.valor());
        assertEquals(TipoCupom.TROCA, dto.tipo());
    }

    @Test
    void deveRejeitarCupomDeTrocaDeOutroCliente() {
        String email = "breno@teste.com";
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                email,
                "senha",
                List.of(new SimpleGrantedAuthority("ROLE_CLIENTE")));
        SecurityContextHolder.getContext().setAuthentication(authentication);

        Cliente clienteLogado = new Cliente();
        clienteLogado.setId(10L);
        clienteLogado.setEmail(email);

        Cliente outroCliente = new Cliente();
        outroCliente.setId(11L);
        outroCliente.setEmail("outra@teste.com");

        Cupom cupom = new Cupom();
        cupom.setCodigo("TROCA-XYZ999");
        cupom.setValor(new BigDecimal("40.00"));
        cupom.setTipo(TipoCupom.TROCA);
        cupom.setAtivo(true);
        cupom.setCliente(outroCliente);
        cupom.setDataValidade(LocalDate.now().plusDays(10));

        when(cupomRepository.findByCodigo("TROCA-XYZ999")).thenReturn(Optional.of(cupom));
        when(clienteRepository.findByEmail(email)).thenReturn(Optional.of(clienteLogado));

        CupomInvalidoException exception = assertThrows(
                CupomInvalidoException.class,
                () -> cupomService.validarCupomCheckout("TROCA-XYZ999"));

        assertEquals("NAO_PERTENCE", exception.getMotivoInvalid());
        assertEquals("TROCA-XYZ999", exception.getCodigoCupom());
    }
}
