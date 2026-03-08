package com.les.jakebooks.validator;

import org.springframework.stereotype.Component;

import com.les.jakebooks.exception.PagamentoReprovadoException;
import com.les.jakebooks.exception.ValidacaoNegocioException;

/**
 * Validador de Pagamento.
 * 
 * Requisitos:
 * - RN0033: Apenas um cupom promocional por compra
 * - RN0034: Múltiplos cartões permitidos (mínimo 10 por cartão)
 * - RN0035: Consumir cupons antes do cartão
 * - RN0036: Gerar cupom para excedente
 * - RN0037: Validar pagamento
 * - RN0038: Status pagamento: APROVADA ou REPROVADA
 */
@Component
public class PagamentoValidator {

    /**
     * Valida se apenas um cupom promocional foi informado.
     * RN0033: Apenas um cupom promocional por compra
     * 
     * @param numeroCuponsPromocionais número de cupons promocionais informados
     * @throws ValidacaoNegocioException se mais de um cupom promocional foi fornecido
     */
    public void validarUnicoCupomPromocional(Integer numeroCuponsPromocionais) {
        if (numeroCuponsPromocionais != null && numeroCuponsPromocionais > 1) {
            throw new ValidacaoNegocioException("Apenas um cupom promocional é permitido por compra");
        }
    }

    /**
     * Valida se o valor mínimo do pedido foi atingido.
     * RN0064: Pedido mínimo 20 sem frete
     * 
     * @param valorPedido valor total do pedido
     * @param temFrete se o pedido tem frete
     * @throws ValidacaoNegocioException se o pedido for menor que o mínimo sem frete
     */
    public void validarValorMinimoPedido(Double valorPedido, boolean temFrete) {
        if (!temFrete && (valorPedido == null || valorPedido < 20.0)) {
            throw new ValidacaoNegocioException(
                String.format("Valor mínimo do pedido sem frete é R$ 20,00. Valor atual: R$ %.2f", 
                    valorPedido != null ? valorPedido : 0.0)
            );
        }
    }

    /**
     * Valida se o limite de tentativas de pagamento foi atingido.
     * RN0065: 3 pagamentos REPROVADOS consecutivos bloqueiam carrinho
     * 
     * @param tentativasReprovadas número de tentativas reprovadas consecutivas
     * @throws ValidacaoNegocioException se o cliente bloqueado por excesso de tentativas falhas
     */
    public void validarLimiteTentativasReprovadas(Integer tentativasReprovadas) {
        if (tentativasReprovadas != null && tentativasReprovadas >= 3) {
            throw new ValidacaoNegocioException(
                "Carrinho bloqueado: 3 tentativas de pagamento reprovadas consecutivas"
            );
        }
    }

    /**
     * Valida dados básicos de pagamento.
     * RN0037: Validar pagamento
     * 
     * @param valorTotal valor total a pagar
     * @param codigoPedido código do pedido
     * @throws ValidacaoNegocioException se algum dado for inválido
     */
    public void validarDadosPagamento(Double valorTotal, String codigoPedido) {
        if (codigoPedido == null || codigoPedido.trim().isEmpty()) {
            throw new ValidacaoNegocioException("Código do pedido é obrigatório");
        }

        if (valorTotal == null || valorTotal <= 0) {
            throw new ValidacaoNegocioException("Valor do pagamento deve ser maior que zero");
        }
    }

    /**
     * Valida status de pagamento permitido.
     * RN0038: Status pagamento: APROVADA ou REPROVADA
     * 
     * @param status status do pagamento
     * @throws ValidacaoNegocioException se o status for inválido
     */
    public void validarStatusPagamento(String status) {
        if (status == null || (!status.equals("PENDENTE") && !status.equals("APROVADA") && !status.equals("REPROVADA"))) {
            throw new ValidacaoNegocioException(
                "Status de pagamento inválido. Valores permitidos: PENDENTE, APROVADA, REPROVADA"
            );
        }
    }

    /**
     * Valida se o pagamento foi aprovado.
     * RN0028: Baixa estoque apenas após pagamento aprovado
     * 
     * @param statusPagamento status do pagamento
     * @throws PagamentoReprovadoException se o pagamento não foi aprovado
     */
    public void validarPagamentoAprovado(String statusPagamento, String codigoPedido) {
        if (!"APROVADA".equals(statusPagamento)) {
            throw new PagamentoReprovadoException(
                "Pagamento não foi aprovado. Status: " + statusPagamento,
                codigoPedido,
                null
            );
        }
    }
}
