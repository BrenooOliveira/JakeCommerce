package com.les.jakebooks.controller;

import com.les.jakebooks.domain.enums.TipoResidencia;
import com.les.jakebooks.dto.ClienteDetalheDTO;
import com.les.jakebooks.dto.EnderecoDTO;
import com.les.jakebooks.dto.EnderecoRequestDTO;
import com.les.jakebooks.service.ClienteService;
import com.les.jakebooks.service.EnderecoService;
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
 * Endpoints REST para gestão de endereços no checkout.
 */
@RestController
@RequestMapping("/api/endereco")
public class EnderecoController {

    @Autowired
    private EnderecoService enderecoService;

    @Autowired
    private ClienteService clienteService;

    @PostMapping("/novo")
    public ResponseEntity<List<EnderecoDTO>> novo(
            HttpSession session,
            @Valid @RequestBody EnderecoRequestDTO request) {

        String codigoCliente = obterCodigoClienteAutenticado(session);

        EnderecoDTO enderecoDTO = new EnderecoDTO(
                null,
                request.nomeIdentificador(),
                TipoResidencia.CASA,
                request.logradouro(),
                request.numero(),
                request.bairro(),
                request.cep(),
                request.cidade(),
                request.estado(),
                request.pais(),
                request.tipo(),
                null
        );

        clienteService.adicionarEndereco(codigoCliente, enderecoDTO);

        ClienteDetalheDTO cliente = clienteService.buscarPorCodigo(codigoCliente);
        List<EnderecoDTO> enderecos = enderecoService.listarEnderecosEntrega(cliente.id());
        return ResponseEntity.ok(enderecos);
    }

    @GetMapping("/meus")
    public ResponseEntity<List<EnderecoDTO>> meus(HttpSession session) {
        String codigoCliente = obterCodigoClienteAutenticado(session);
        ClienteDetalheDTO cliente = clienteService.buscarPorCodigo(codigoCliente);

        List<EnderecoDTO> enderecos = enderecoService.listarEnderecosEntrega(cliente.id());
        return ResponseEntity.ok(enderecos);
    }

    private String obterCodigoClienteAutenticado(HttpSession session) {
        String codigoCliente = (String) session.getAttribute("codigoClienteAutenticado");
        if (codigoCliente == null || codigoCliente.isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Cliente não autenticado");
        }
        return codigoCliente;
    }
}
