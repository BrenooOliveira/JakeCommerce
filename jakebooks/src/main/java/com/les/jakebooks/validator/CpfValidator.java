package com.les.jakebooks.validator;

import org.springframework.stereotype.Component;

import com.les.jakebooks.exception.ValidacaoNegocioException;

/**
 * Validador de CPF.
 * 
 * Valida:
 * - Formato: apenas dígitos
 * - Tamanho: exatamente 11 dígitos
 * - Dígitos verificadores: primeiro e segundo dígito verificador
 * 
 * RN0026: Dados obrigatórios do cliente (inclui validação de CPF)
 * Lança: ValidacaoNegocioException se CPF inválido
 */
@Component
public class CpfValidator {

    private static final int TAMANHO_CPF = 11;

    /**
     * Valida um número de CPF.
     * 
     * @param cpf CPF a validar (pode conter formatação ou não)
     * @throws ValidacaoNegocioException se o CPF for inválido
     */
    public void validarCpf(String cpf) {
        if (cpf == null || cpf.trim().isEmpty()) {
            throw new ValidacaoNegocioException("CPF não pode estar vazio");
        }

        // Remove formatação comum (XXX.XXX.XXX-XX)
        String cpfLimpo = cpf.replaceAll("[^0-9]", "");

        if (cpfLimpo.length() != TAMANHO_CPF) {
            throw new ValidacaoNegocioException(
                String.format("CPF deve conter exatamente %d dígitos. Recebido: %d", 
                    TAMANHO_CPF, cpfLimpo.length())
            );
        }

        // Verifica se é uma sequência de dígitos iguais (caso especial inválido)
        if (saoTodosDigitosIguais(cpfLimpo)) {
            throw new ValidacaoNegocioException("CPF inválido: todos os dígitos são iguais");
        }

        // Calcula e valida primeiro dígito verificador
        int primeiroDigito = calcularDigitoVerificador(cpfLimpo.substring(0, 9), 10);
        if (Integer.parseInt(cpfLimpo.substring(9, 10)) != primeiroDigito) {
            throw new ValidacaoNegocioException("CPF inválido: primeiro dígito verificador incorreto");
        }

        // Calcula e valida segundo dígito verificador
        int segundoDigito = calcularDigitoVerificador(cpfLimpo.substring(0, 10), 11);
        if (Integer.parseInt(cpfLimpo.substring(10, 11)) != segundoDigito) {
            throw new ValidacaoNegocioException("CPF inválido: segundo dígito verificador incorreto");
        }
    }

    /**
     * Calcula o dígito verificador usando o algoritmo oficial da Receita Federal.
     * 
     * @param sequencia sequência de dígitos para calcular verificador
     * @param multiplicadorInicial multiplicador inicial (10 ou 11)
     * @return dígito verificador calculado
     */
    private int calcularDigitoVerificador(String sequencia, int multiplicadorInicial) {
        int soma = 0;
        int multiplicador = multiplicadorInicial;

        for (char c : sequencia.toCharArray()) {
            soma += Character.getNumericValue(c) * multiplicador;
            multiplicador--;
        }

        int resto = soma % 11;
        return resto < 2 ? 0 : 11 - resto;
    }

    /**
     * Verifica se todos os dígitos do CPF são iguais.
     * 
     * @param cpf CPF limpo (apenas dígitos)
     * @return true se todos os dígitos são iguais, false caso contrário
     */
    private boolean saoTodosDigitosIguais(String cpf) {
        return cpf.matches("(\\d)\\1{" + (TAMANHO_CPF - 1) + "}");
    }

    /**
     * Valida formato do CPF (retorna true/false sem lançar exceção).
     * Útil para pré-validação em formulários.
     * 
     * @param cpf CPF a validar
     * @return true se CPF é válido
     */
    public boolean ehValido(String cpf) {
        try {
            validarCpf(cpf);
            return true;
        } catch (ValidacaoNegocioException e) {
            return false;
        }
    }
}
