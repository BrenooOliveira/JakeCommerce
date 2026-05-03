package com.les.jakebooks.controller;

import com.les.jakebooks.dto.CartaoRequestDTO;
import com.les.jakebooks.dto.CartaoResumoDTO;
import com.les.jakebooks.dto.ClienteDetalheDTO;
import com.les.jakebooks.service.CartaoService;
import com.les.jakebooks.service.ClienteService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

/**
 * Endpoints REST para gestão de cartões no checkout.
 */
@RestController
@RequestMapping("/api/cartao")
public class CartaoController {

    @Autowired
    private CartaoService cartaoService;

    @Autowired
    private ClienteService clienteService;

    @PostMapping("/novo")
    public ResponseEntity<List<CartaoResumoDTO>> novo(
            HttpSession session,
            @Valid @RequestBody CartaoRequestDTO request) {

        String codigoCliente = obterCodigoClienteAutenticado(session);
        ClienteDetalheDTO cliente = clienteService.buscarPorCodigo(codigoCliente);

        cartaoService.registrarCartao(
                request.numero(),
                request.nomeImpresso(),
                request.bandeira(),
                request.codigoSeguranca(),
                request.preferencial(),
                cliente.id()
        );

        List<CartaoResumoDTO> cartoes = cartaoService.listarCartoesAtivos(cliente.id());
        return ResponseEntity.ok(cartoes);
    }

    @GetMapping("/meus")
    public ResponseEntity<List<CartaoResumoDTO>> meus(HttpSession session) {
        String codigoCliente = obterCodigoClienteAutenticado(session);
        ClienteDetalheDTO cliente = clienteService.buscarPorCodigo(codigoCliente);

        List<CartaoResumoDTO> cartoes = cartaoService.listarCartoesAtivos(cliente.id());
        return ResponseEntity.ok(cartoes);
    }

    private String obterCodigoClienteAutenticado(HttpSession session) {
        String codigoCliente = (String) session.getAttribute("codigoClienteAutenticado");
        if (codigoCliente == null || codigoCliente.isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Cliente não autenticado");
        }
        return codigoCliente;
    }
}
