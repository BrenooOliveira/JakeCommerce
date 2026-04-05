package com.les.jakebooks.services;

import com.les.jakebooks.domain.Cartao;
import com.les.jakebooks.dto.CartaoResumoDTO;
import com.les.jakebooks.repository.CartaoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service para gerenciar operações de Cartão.
 * RF0027: Cadastrar múltiplos cartões (um preferencial).
 * RN0024: Campos obrigatórios do cartão.
 * RN0025: Bandeira deve estar cadastrada.
 * RN0034: Múltiplos cartões permitidos (mínimo R$10 por cartão).
 */
@Service
@Transactional(readOnly = true)
public class CartaoService {

    @Autowired
    private CartaoRepository cartaoRepository;

    /**
     * Lista cartões ativos do cliente.
     * Conforme especificação TASK-PAY-04.
     *
     * @param clienteId ID do cliente
     * @return Lista de DTOs dos cartões ativos
     */
    public List<CartaoResumoDTO> listarCartoesAtivos(Long clienteId) {
        return cartaoRepository.findByClienteId(clienteId)
                .stream()
                .map(this::converterCartaoParaResumoDTO)
                .collect(Collectors.toList());
    }

    /**
     * Converte entidade Cartao para DTO resumido.
     * Método reutilizado do PagamentoService para consistência.
     *
     * @param cartao entidade a converter
     * @return DTO resumido do cartão
     */
    private CartaoResumoDTO converterCartaoParaResumoDTO(Cartao cartao) {
        // Mascarar número do cartão
        String numero = cartao.getNumero();
        String numeroMascarado = "**** **** **** " + numero.substring(numero.length() - 4);

        return new CartaoResumoDTO(
                cartao.getId(),
                numeroMascarado,
                cartao.getNomeImpresso(),
                cartao.getBandeira(),
                cartao.getPreferencial()
        );
    }
}