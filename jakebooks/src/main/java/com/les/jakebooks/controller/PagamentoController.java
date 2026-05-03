package com.les.jakebooks.controller;

import com.les.jakebooks.domain.Pagamento;
import com.les.jakebooks.dto.PagamentoRequestDTO;
import com.les.jakebooks.dto.PagamentoResponseDTO;
import com.les.jakebooks.dto.PagamentoCartaoDadosDTO;
import com.les.jakebooks.dto.ClienteDetalheDTO;
import com.les.jakebooks.service.PagamentoService;
import com.les.jakebooks.service.ClienteService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Endpoints REST para processamento de pagamento no checkout.
 */
@RestController
@RequestMapping("/api/checkout/pagamento")
public class PagamentoController {

    @Autowired
    private PagamentoService pagamentoService;

    @Autowired
    private ClienteService clienteService;

    @PostMapping("/processar")
    public ResponseEntity<PagamentoResponseDTO> processar(
            HttpSession session,
            @Valid @RequestBody PagamentoRequestDTO request) {

                String codigoCliente = obterCodigoClienteAutenticado(session);
        ClienteDetalheDTO cliente = clienteService.buscarPorCodigo(codigoCliente);

        Map<Long, BigDecimal> cartoesValores = request.cartoes().stream()
                .collect(Collectors.toMap(PagamentoCartaoDadosDTO::cartaoId, PagamentoCartaoDadosDTO::valor));

        Pagamento pagamento = pagamentoService.processar(
                request.pedidoId(),
                request.cupomId(),
                cartoesValores,
                cliente.id()
        );

        return ResponseEntity.ok(PagamentoResponseDTO.from(pagamento));
    }

        private String obterCodigoClienteAutenticado(HttpSession session) {
                String codigoCliente = (String) session.getAttribute("codigoClienteAutenticado");
                if (codigoCliente == null || codigoCliente.isBlank()) {
                        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Cliente não autenticado");
                }
                return codigoCliente;
        }
}
