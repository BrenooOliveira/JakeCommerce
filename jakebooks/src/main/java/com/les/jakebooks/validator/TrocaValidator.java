package com.les.jakebooks.validator;

import org.springframework.stereotype.Component;

import com.les.jakebooks.exception.TrocaNaoPermitidaException;
import com.les.jakebooks.exception.ValidacaoNegocioException;

/**
 * Validador de Troca.
 * 
 * Requisitos:
 * - RN0043: Apenas pedidos ENTREGUES podem solicitar troca
 */
@Component
public class TrocaValidator {

    /**
     * Valida se o pedido pode solicitar troca.
     * RN0043: Apenas pedidos ENTREGUES podem solicitar troca
     * 
     * @param statusPedido status atual do pedido
     * @param codigoPedido código do pedido
     * @throws TrocaNaoPermitidaException se o pedido não estiver entregue
     */
    public void validarStatusPedidoParaTroca(String statusPedido, String codigoPedido) {
        if (statusPedido == null || !statusPedido.equals("ENTREGUE")) {
            throw new TrocaNaoPermitidaException(
                String.format("Pedido %s não pode ser trocado. Status atual: %s. Apenas pedidos ENTREGUES podem solicitar troca.",
                    codigoPedido, statusPedido),
                codigoPedido,
                "Pedido não está entregue"
            );
        }
    }

    /**
     * Valida dados obrigatórios da troca.
     * 
     * @param codigoPedido código do pedido
     * @param motivo motivo da troca
     * @throws ValidacaoNegocioException se algum dado obrigatório estiver vazio
     */
    public void validarDadosObrigatorios(String codigoPedido, String motivo) {
        if (codigoPedido == null || codigoPedido.trim().isEmpty()) {
            throw new ValidacaoNegocioException("Código do pedido é obrigatório");
        }

        if (motivo == null || motivo.trim().isEmpty()) {
            throw new ValidacaoNegocioException("Motivo da troca é obrigatório");
        }
    }

    /**
     * Valida status válido da troca.
     * 
     * @param status status a validar
     * @throws ValidacaoNegocioException se o status for inválido
     */
    public void validarStatusTroca(String status) {
        String[] statusValidos = {"SOLICITADA", "AUTORIZADA", "RECEBIDA", "CONCLUIDA"};
        boolean isValid = false;

        for (String validStatus : statusValidos) {
            if (validStatus.equals(status)) {
                isValid = true;
                break;
            }
        }

        if (!isValid) {
            throw new ValidacaoNegocioException(
                "Status de troca inválido. Valores permitidos: SOLICITADA, AUTORIZADA, RECEBIDA, CONCLUIDA"
            );
        }
    }
}
