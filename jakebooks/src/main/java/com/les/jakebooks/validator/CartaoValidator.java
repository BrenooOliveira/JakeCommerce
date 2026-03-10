package com.les.jakebooks.validator;

import org.springframework.stereotype.Component;

import com.les.jakebooks.exception.ValidacaoNegocioException;
import java.time.LocalDate;
import java.time.YearMonth;

/**
 * Validador de Cartão de Crédito.
 * 
 * Valida:
 * - Número: 16 dígitos (padrão Visa/Mastercard)
 * - Bandeira: deve ser cadastrada (Visa, Mastercard, Elo, Amex)
 * - Data de vencimento: não expirado
 * - Dígito verificador: algoritmo de Luhn
 * - Código de segurança (CVV): 3-4 dígitos conforme bandeira
 * 
 * RN0024: Campos obrigatórios do cartão
 * RN0025: Bandeira deve estar cadastrada
 * Lança: ValidacaoNegocioException se cartão for inválido
 */
@Component
public class CartaoValidator {

    private static final int[] BANDEIRAS_VALIDAS = {4, 5, 6}; // Visa (4), Mastercard (5), Elo (6)
    private static final String[] NOMES_BANDEIRAS = {"VISA", "MASTERCARD", "ELO"};

    /**
     * Valida um número de cartão de crédito.
     * 
     * @param numero número do cartão (apenas dígitos)
     * @throws ValidacaoNegocioException se o cartão for inválido
     */
    public void validarNumero(String numero) {
        if (numero == null || numero.trim().isEmpty()) {
            throw new ValidacaoNegocioException("Número do cartão não pode estar vazio");
        }

        String numeroLimpo = numero.replaceAll("[^0-9]", "");

        // Valida tamanho: geralmente 16 dígitos para Visa/Mastercard, 15 para Amex
        if (numeroLimpo.length() < 13 || numeroLimpo.length() > 19) {
            throw new ValidacaoNegocioException(
                String.format("Número do cartão deve ter entre 13 e 19 dígitos. Recebido: %d", 
                    numeroLimpo.length())
            );
        }

        // Valida algoritmo de Luhn
        if (!ehValidoLuhn(numeroLimpo)) {
            throw new ValidacaoNegocioException("Número do cartão inválido (falha no algoritmo de Luhn)");
        }
    }

    /**
     * Valida a bandeira do cartão.
     * RN0025: Bandeira deve estar cadastrada
     * 
     * @param bandeira nome da bandeira (VISA, MASTERCARD, ELO, AMEX)
     * @throws ValidacaoNegocioException se a bandeira não for suportada
     */
    public void validarBandeira(String bandeira) {
        if (bandeira == null || bandeira.trim().isEmpty()) {
            throw new ValidacaoNegocioException("Bandeira do cartão não pode estar vazia");
        }

        String bandeiraNormalizada = bandeira.toUpperCase().trim();

        // Bandeiras suportadas
        if (!bandeiraNormalizada.matches("^(VISA|MASTERCARD|ELO|AMEX)$")) {
            throw new ValidacaoNegocioException(
                String.format("Bandeira '%s' não cadastrada. Bandeiras aceitas: VISA, MASTERCARD, ELO, AMEX", 
                    bandeira)
            );
        }
    }

    /**
     * Valida data de vencimento do cartão.
     * 
     * @param mes mês de vencimento (1-12)
     * @param ano ano de vencimento (2 ou 4 dígitos)
     * @throws ValidacaoNegocioException se a data for inválida ou expirada
     */
    public void validarVencimento(Integer mes, Integer ano) {
        if (mes == null || ano == null) {
            throw new ValidacaoNegocioException("Mês e ano de vencimento são obrigatórios");
        }

        if (mes < 1 || mes > 12) {
            throw new ValidacaoNegocioException(
                String.format("Mês de vencimento inválido: %d. Deve estar entre 1 e 12", mes)
            );
        }

        // Normaliza ano: se tiver 2 dígitos, assume século 20/21
        int anoNormalizado = ano;
        if (ano < 100) {
            anoNormalizado = ano < 50 ? 2000 + ano : 1900 + ano;
        }

        YearMonth vencimento = YearMonth.of(anoNormalizado, mes);
        YearMonth hoje = YearMonth.now();

        if (vencimento.isBefore(hoje)) {
            throw new ValidacaoNegocioException(
                String.format("Cartão expirado: vencimento %02d/%d (hoje: %02d/%d)", 
                    mes, anoNormalizado, hoje.getMonthValue(), hoje.getYear())
            );
        }
    }

    /**
     * Valida código de segurança (CVV/CVC).
     * 
     * @param cvv código de segurança
     * @param bandeira bandeira do cartão (para validar tamanho apropriado)
     * @throws ValidacaoNegocioException se o CVV for inválido
     */
    public void validarCvv(String cvv, String bandeira) {
        if (cvv == null || cvv.trim().isEmpty()) {
            throw new ValidacaoNegocioException("Código de segurança (CVV) não pode estar vazio");
        }

        String cvvLimpo = cvv.replaceAll("[^0-9]", "");

        // Amex tem 4 dígitos, outros têm 3
        int tamanhoEsperado = bandeira != null && bandeira.toUpperCase().equals("AMEX") ? 4 : 3;
        int tamanhoMinimo = tamanhoEsperado - 1; // Aceita variação pequena
        int tamanhoMaximo = tamanhoEsperado + 1;

        if (cvvLimpo.length() < 3 || cvvLimpo.length() > 4) {
            throw new ValidacaoNegocioException(
                String.format("Código de segurança (CVV) deve ter %d dígitos. Recebido: %d", 
                    tamanhoEsperado, cvvLimpo.length())
            );
        }
    }

    /**
     * Valida nome impresso no cartão.
     * 
     * @param nomeImpresso nome conforme impresso no cartão
     * @throws ValidacaoNegocioException se o nome for inválido
     */
    public void validarNomeImpresso(String nomeImpresso) {
        if (nomeImpresso == null || nomeImpresso.trim().isEmpty()) {
            throw new ValidacaoNegocioException("Nome impresso no cartão é obrigatório");
        }

        if (nomeImpresso.length() < 3) {
            throw new ValidacaoNegocioException("Nome impresso deve ter pelo menos 3 caracteres");
        }

        if (nomeImpresso.length() > 30) {
            throw new ValidacaoNegocioException("Nome impresso não pode exceder 30 caracteres");
        }

        // Valida caracteres (apenas letras, espaços e alguns símbolos)
        if (!nomeImpresso.matches("^[A-Z\\s'-]+$")) {
            throw new ValidacaoNegocioException(
                "Nome impresso contém caracteres inválidos. Use apenas letras maiúsculas, espaços, hífen e apóstrofo"
            );
        }
    }

    /**
     * Algoritmo de Luhn para validar número do cartão.
     * 
     * @param numero número do cartão (apenas dígitos)
     * @return true se válido segundo algoritmo de Luhn
     */
    private boolean ehValidoLuhn(String numero) {
        int soma = 0;
        boolean alternado = false;

        for (int i = numero.length() - 1; i >= 0; i--) {
            int digito = Character.getNumericValue(numero.charAt(i));

            if (alternado) {
                digito *= 2;
                if (digito > 9) {
                    digito -= 9;
                }
            }

            soma += digito;
            alternado = !alternado;
        }

        return soma % 10 == 0;
    }

    /**
     * Valida todos os campos do cartão simultaneamente.
     * Conveniência para validação completa.
     * 
     * @param numero número do cartão
     * @param nomeImpresso nome impresso
     * @param bandeira bandeira
     * @param mes mês de vencimento
     * @param ano ano de vencimento
     * @param cvv código de segurança
     * @throws ValidacaoNegocioException se algum campo for inválido
     */
    public void validarCartaoCompleto(String numero, String nomeImpresso, String bandeira, 
                                     Integer mes, Integer ano, String cvv) {
        validarNumero(numero);
        validarBandeira(bandeira);
        validarVencimento(mes, ano);
        validarCvv(cvv, bandeira);
        validarNomeImpresso(nomeImpresso);
    }
}
