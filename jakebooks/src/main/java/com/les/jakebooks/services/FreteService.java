package com.les.jakebooks.services;

import com.les.jakebooks.domain.Endereco;
import com.les.jakebooks.dto.FreteDTO;
import com.les.jakebooks.exception.RecursoNaoEncontradoException;
import com.les.jakebooks.model.enums.RegiaoFrete;
import com.les.jakebooks.repository.EnderecoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/**
 * Service para cálculo de frete.
 * RF0034: Calcular frete
 * RN0064: Pedido mínimo R$20 para frete grátis
 */
@Service
public class FreteService {

    @Autowired
    private EnderecoRepository enderecoRepository;

    // Constantes de valores de frete
    private static final BigDecimal VALOR_MINIMO_FRETE_GRATIS = new BigDecimal("20.00");
    private static final BigDecimal FRETE_MESMA_CIDADE = new BigDecimal("5.00");
    private static final BigDecimal FRETE_MESMO_ESTADO = new BigDecimal("10.00");
    private static final BigDecimal FRETE_OUTRO_ESTADO = new BigDecimal("15.00");

    // CEP de origem da loja (simulação acadêmica)
    // 01310-100 = Avenida Paulista, São Paulo - SP
    private static final String CEP_ORIGEM = "01310100";

    /**
     * Calcula frete baseado no endereço de entrega e valor do pedido.
     * RF0034: Calcular frete
     * RN0064: Pedido mínimo R$20 para frete grátis
     *
     * @param enderecoId ID do endereço de entrega
     * @param valorPedido Valor total do pedido (sem frete)
     * @return FreteDTO com valor, descrição, prazo e flag de grátis
     * @throws RecursoNaoEncontradoException se endereço não existe
     */
    public FreteDTO calcularFrete(Long enderecoId, BigDecimal valorPedido) {
        // Buscar endereço de entrega
        Endereco endereco = enderecoRepository.findById(enderecoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Endereço não encontrado"
                ));

        // RN0064: Frete grátis para pedidos >= R$20
        if (valorPedido.compareTo(VALOR_MINIMO_FRETE_GRATIS) >= 0) {
            int prazoGratis = calcularPrazo(endereco.getCep());
            return new FreteDTO(
                    BigDecimal.ZERO,
                    "Frete Grátis",
                    prazoGratis,
                    true
            );
        }

        // Calcular frete por região
        RegiaoFrete regiao = identificarRegiao(endereco.getCep());

        return switch (regiao) {
            case MESMA_CIDADE -> new FreteDTO(
                    FRETE_MESMA_CIDADE,
                    "Entrega Local",
                    3,
                    false
            );
            case MESMO_ESTADO -> new FreteDTO(
                    FRETE_MESMO_ESTADO,
                    "Entrega Estadual",
                    7,
                    false
            );
            case OUTRO_ESTADO -> new FreteDTO(
                    FRETE_OUTRO_ESTADO,
                    "Entrega Nacional",
                    15,
                    false
            );
        };
    }

    /**
     * Identifica a região de frete baseada no CEP de destino.
     * Critérios (simulação acadêmica):
     * - Mesma cidade: mesmo prefixo de 3 dígitos do CEP
     * - Mesmo estado: primeiro dígito do CEP igual
     * - Outro estado: demais casos
     *
     * @param cepDestino CEP do endereço de entrega
     * @return RegiaoFrete identificada
     */
    private RegiaoFrete identificarRegiao(String cepDestino) {
        // Remover formatação do CEP (hífen, pontos, etc)
        String cepDestinoLimpo = cepDestino.replaceAll("\\D", "");
        String cepOrigemLimpo = CEP_ORIGEM.replaceAll("\\D", "");

        // Validar tamanho mínimo do CEP
        if (cepDestinoLimpo.length() < 5) {
            return RegiaoFrete.OUTRO_ESTADO;
        }

        // Extrair prefixos
        String prefixoOrigem = cepOrigemLimpo.substring(0, 3);  // 013
        String prefixoDestino = cepDestinoLimpo.substring(0, 3);

        // Mesma cidade: mesmo prefixo de 3 dígitos
        if (prefixoOrigem.equals(prefixoDestino)) {
            return RegiaoFrete.MESMA_CIDADE;
        }

        // Mesmo estado: primeiro dígito igual (simplificação académica)
        // Em SP, CEPs começam com 0 ou 1
        if (cepOrigemLimpo.charAt(0) == cepDestinoLimpo.charAt(0)) {
            return RegiaoFrete.MESMO_ESTADO;
        }

        return RegiaoFrete.OUTRO_ESTADO;
    }

    /**
     * Calcula prazo estimado de entrega baseado na região.
     *
     * @param cep CEP de destino
     * @return prazo em dias úteis
     */
    private int calcularPrazo(String cep) {
        RegiaoFrete regiao = identificarRegiao(cep);
        return switch (regiao) {
            case MESMA_CIDADE -> 3;
            case MESMO_ESTADO -> 7;
            case OUTRO_ESTADO -> 15;
        };
    }
}
