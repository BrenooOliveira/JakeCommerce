package com.les.jakebooks.service;

import com.les.jakebooks.domain.Cartao;
import com.les.jakebooks.domain.PagamentoCartao;
import com.les.jakebooks.domain.enums.BandeiraCartao;
import com.les.jakebooks.domain.enums.StatusPagamentoCartao;
import com.les.jakebooks.exception.ValidacaoNegocioException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

/**
 * Service para simular processamento de gateway de pagamento.
 * PAY-05: Simular decisão de aprovação/rejeição de cartões.
 * RN0034: Validar múltiplos cartões.
 * RN0024: Validar formato do cartão.
 * RN0025: Validar bandeira cadastrada.
 *
 * Ambiente de testes: 90% aprovação, com lógica baseada em dígito final do cartão
 */
@Service
@Transactional
public class PaymentGatewayService {

    /**
     * Simula aprovação de pagamento com 80% de chance de sucesso.
     * Fluxo acadêmico simplificado para decisão geral do gateway.
     *
     * @return true quando aprovado, false quando reprovado
     */
    public boolean simularAprovacao() {
        return ThreadLocalRandom.current().nextDouble() < 0.8;
    }

    /**
     * Simula processamento de um cartão individual.
     * PAY-05: Retorna status baseado em lógica simulada.
     *
     * Lógica (académica):
     * - Cartões com final par: sempre aprovam
     * - Cartões com final ímpar: 80% aprovação
     *
     * @param pagamentoCartao dados do cartão a processar
     * @return status da transação (APROVADO ou REPROVADO)
     * @throws ValidacaoNegocioException se cartão inválido
     */
    public StatusPagamentoCartao simularProcessamento(PagamentoCartao pagamentoCartao) {
        if (pagamentoCartao == null || pagamentoCartao.getCartao() == null) {
            throw new ValidacaoNegocioException("Cartão não pode ser nulo");
        }

        Cartao cartao = pagamentoCartao.getCartao();
        BigDecimal valor = pagamentoCartao.getValor();

        // Validar cartão antes de processar
        validarCartao(cartao);

        // Validar valor
        if (valor == null || valor.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidacaoNegocioException("Valor deve ser maior que zero");
        }

        // Simular processamento
        return simularGatewayPorCartao(cartao);
    }

    /**
     * Processa múltiplos cartões em sequência.
     * RN0034: Validar múltiplos cartões.
     *
     * @param pagamentosCartao lista de cartões a processar
     * @return lista de statuses (na mesma ordem)
     */
    public List<StatusPagamentoCartao> processarMultiplosCartoes(List<PagamentoCartao> pagamentosCartao) {
        if (pagamentosCartao == null || pagamentosCartao.isEmpty()) {
            return List.of();
        }

        return pagamentosCartao.stream()
                .map(this::simularProcessamento)
                .collect(Collectors.toList());
    }

    /**
     * Valida formato básico do cartão.
     * RN0024: Validar campos obrigatórios.
     * RN0025: Validar bandeira cadastrada.
     *
     * @param cartao cartão a validar
     * @throws ValidacaoNegocioException se validação falhar
     */
    public void validarCartao(Cartao cartao) {
        if (cartao == null) {
            throw new ValidacaoNegocioException("Cartão não pode ser nulo");
        }

        // Validar número (13-19 dígitos)
        if (cartao.getNumero() == null || cartao.getNumero().isBlank()) {
            throw new ValidacaoNegocioException("Número do cartão é obrigatório");
        }

        if (!cartao.getNumero().matches("\\d{13,19}")) {
            throw new ValidacaoNegocioException(
                    "Número do cartão inválido. Deve conter entre 13 e 19 dígitos");
        }

        // Validar CVV (3-4 dígitos)
        if (cartao.getCodigoSeguranca() == null || cartao.getCodigoSeguranca().isBlank()) {
            throw new ValidacaoNegocioException("Código de segurança é obrigatório");
        }

        if (!cartao.getCodigoSeguranca().matches("\\d{3,4}")) {
            throw new ValidacaoNegocioException(
                    "Código de segurança inválido. Deve conter 3 ou 4 dígitos");
        }

        // Validar nome impresso
        if (cartao.getNomeImpresso() == null || cartao.getNomeImpresso().isBlank()) {
            throw new ValidacaoNegocioException("Nome impresso é obrigatório");
        }

        if (cartao.getNomeImpresso().length() < 5 || cartao.getNomeImpresso().length() > 50) {
            throw new ValidacaoNegocioException(
                    "Nome impresso deve conter entre 5 e 50 caracteres");
        }

        // Validar bandeira (RN0025)
        if (cartao.getBandeira() == null) {
            throw new ValidacaoNegocioException("Bandeira é obrigatória");
        }

        validarBandeira(cartao.getBandeira());
    }

    /**
     * Valida se a bandeira está cadastrada no sistema.
     * RN0025: Bandeira deve estar cadastrada.
     *
     * @param bandeira bandeira a validar
     * @throws ValidacaoNegocioException se bandeira não permitida
     */
    public void validarBandeira(BandeiraCartao bandeira) {
        if (bandeira == null) {
            throw new ValidacaoNegocioException("Bandeira não pode ser nula");
        }

        // Bandeiras permitidas: VISA, MASTERCARD, ELO, AMEX (definidas no ENUM)
        // Se chegou aqui, é válida pois é do tipo ENUM
        try {
            BandeiraCartao.valueOf(bandeira.name());
        } catch (IllegalArgumentException e) {
            throw new ValidacaoNegocioException(
                    "Bandeira '" + bandeira + "' não é permitida");
        }
    }

    /**
     * Simula decisão do gateway baseado no dígito final do cartão.
     * Lógica (académica para testes):
     * - Final par: 100% aprovação
     * - Final ímpar: 80% aprovação
     *
     * @param cartao cartão cujo número será analisado
     * @return APROVADO ou REPROVADO
     */
    private StatusPagamentoCartao simularGatewayPorCartao(Cartao cartao) {
        String numero = cartao.getNumero();
        String ultimoDigito = numero.substring(numero.length() - 1);
        int digito = Integer.parseInt(ultimoDigito);

        // Cartões com final par: sempre aprovam
        if (digito % 2 == 0) {
            return StatusPagamentoCartao.APROVADO;
        }

        // Cartões com final ímpar: 80% de chance de aprovação
        double randomValue = Math.random();
        return randomValue < 0.8 ?
                StatusPagamentoCartao.APROVADO :
                StatusPagamentoCartao.REPROVADO;
    }

    /**
     * Verifica se todos os cartões foram aprovados.
     *
     * @param pagamentosCartao lista de pagamentos com cartão
     * @return true se todos aprovados, false caso contrário
     */
    public boolean todosCartõesAprovados(List<PagamentoCartao> pagamentosCartao) {
        if (pagamentosCartao == null || pagamentosCartao.isEmpty()) {
            return false;
        }

        return pagamentosCartao.stream()
                .allMatch(pc -> pc.getStatus() == StatusPagamentoCartao.APROVADO);
    }

    /**
     * Conta quantos cartões foram aprovados.
     *
     * @param pagamentosCartao lista de pagamentos com cartão
     * @return quantidade de aprovações
     */
    public long contarAprovacoes(List<PagamentoCartao> pagamentosCartao) {
        if (pagamentosCartao == null || pagamentosCartao.isEmpty()) {
            return 0;
        }

        return pagamentosCartao.stream()
                .filter(pc -> pc.getStatus() == StatusPagamentoCartao.APROVADO)
                .count();
    }
}
