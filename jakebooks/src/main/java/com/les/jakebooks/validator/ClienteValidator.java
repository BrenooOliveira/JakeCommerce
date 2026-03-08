package com.les.jakebooks.validator;

import org.springframework.stereotype.Component;

import com.les.jakebooks.exception.ValidacaoNegocioException;

/**
 * Validador de Cliente.
 * 
 * Requisitos:
 * - RN0021: Pelo menos um endereço de cobrança
 * - RN0022: Pelo menos um endereço de entrega
 * - RN0023: Campos obrigatórios do endereço
 * - RN0024: Campos obrigatórios do cartão
 * - RN0025: Bandeira deve estar cadastrada
 * - RN0026: Dados obrigatórios do cliente
 * - RN0027: Cliente possui ranking numérico
 * - RN0028: Baixa estoque apenas após pagamento aprovado
 */
@Component
public class ClienteValidator {

    /**
     * Valida dados obrigatórios do cliente.
     * RN0026: Dados obrigatórios do cliente
     * 
     * @param nome nome do cliente
     * @param cpf CPF do cliente
     * @param email email do cliente
     * @throws ValidacaoNegocioException se algum campo obrigatório estiver vazio
     */
    public void validarDadosObrigatorios(String nome, String cpf, String email) {
        if (nome == null || nome.trim().isEmpty()) {
            throw new ValidacaoNegocioException("Nome do cliente é obrigatório");
        }

        if (cpf == null || cpf.trim().isEmpty()) {
            throw new ValidacaoNegocioException("CPF do cliente é obrigatório");
        }

        if (email == null || email.trim().isEmpty()) {
            throw new ValidacaoNegocioException("Email do cliente é obrigatório");
        }

        if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            throw new ValidacaoNegocioException("Email inválido: " + email);
        }
    }

    /**
     * Valida CPF do cliente.
     * 
     * @param cpf CPF a validar
     * @throws ValidacaoNegocioException se o CPF for inválido
     */
    public void validarCPF(String cpf) {
        if (cpf == null || cpf.isEmpty()) {
            throw new ValidacaoNegocioException("CPF não pode estar vazio");
        }

        // Remove caracteres especiais
        String cpfLimpo = cpf.replaceAll("\\D", "");

        if (cpfLimpo.length() != 11) {
            throw new ValidacaoNegocioException("CPF deve conter 11 dígitos");
        }
    }

    /**
     * Valida dados obrigatórios de endereço.
     * RN0023: Campos obrigatórios do endereço
     * 
     * @param logradouro logradouro
     * @param numero número
     * @param bairro bairro
     * @param cep CEP
     * @param cidade cidade
     * @param estado estado
     * @throws ValidacaoNegocioException se algum campo obrigatório estiver vazio
     */
    public void validarDadosEndereco(String logradouro, String numero, String bairro, 
                                     String cep, String cidade, String estado) {
        if (logradouro == null || logradouro.trim().isEmpty()) {
            throw new ValidacaoNegocioException("Logradouro é obrigatório");
        }

        if (numero == null || numero.trim().isEmpty()) {
            throw new ValidacaoNegocioException("Número é obrigatório");
        }

        if (bairro == null || bairro.trim().isEmpty()) {
            throw new ValidacaoNegocioException("Bairro é obrigatório");
        }

        if (cep == null || cep.trim().isEmpty()) {
            throw new ValidacaoNegocioException("CEP é obrigatório");
        }

        if (cidade == null || cidade.trim().isEmpty()) {
            throw new ValidacaoNegocioException("Cidade é obrigatória");
        }

        if (estado == null || estado.trim().isEmpty()) {
            throw new ValidacaoNegocioException("Estado é obrigatório");
        }
    }

    /**
     * Valida dados obrigatórios de cartão.
     * RN0024: Campos obrigatórios do cartão
     * RN0025: Bandeira deve estar cadastrada
     * 
     * @param numero número do cartão
     * @param nomeImpresso nome impresso no cartão
     * @param codigoSeguranca código de segurança
     * @param bandeira bandeira do cartão
     * @throws ValidacaoNegocioException se algum campo obrigatório estiver vazio ou inválido
     */
    public void validarDadosCarta(String numero, String nomeImpresso, String codigoSeguranca, String bandeira) {
        if (numero == null || numero.trim().isEmpty()) {
            throw new ValidacaoNegocioException("Número do cartão é obrigatório");
        }

        if (numero.replaceAll("\\D", "").length() < 13) {
            throw new ValidacaoNegocioException("Número do cartão inválido");
        }

        if (nomeImpresso == null || nomeImpresso.trim().isEmpty()) {
            throw new ValidacaoNegocioException("Nome impresso no cartão é obrigatório");
        }

        if (codigoSeguranca == null || codigoSeguranca.trim().isEmpty()) {
            throw new ValidacaoNegocioException("Código de segurança é obrigatório");
        }

        if (codigoSeguranca.replaceAll("\\D", "").length() < 3) {
            throw new ValidacaoNegocioException("Código de segurança inválido");
        }

        if (bandeira == null || bandeira.trim().isEmpty()) {
            throw new ValidacaoNegocioException("Bandeira do cartão é obrigatória");
        }

        validarBandeiraConhecida(bandeira);
    }

    /**
     * Valida se a bandeira é conhecida.
     * RN0025: Bandeira deve estar cadastrada
     * 
     * @param bandeira bandeira a validar
     * @throws ValidacaoNegocioException se a bandeira não estiver cadastrada
     */
    public void validarBandeiraConhecida(String bandeira) {
        // Integrar com banco de dados quando houver tabela de bandeiras
        String[] bandeirasValidas = {"VISA", "MASTERCARD", "ELO", "AMEX", "DINERS"};
        boolean isValid = false;

        for (String validaBandeira : bandeirasValidas) {
            if (validaBandeira.equalsIgnoreCase(bandeira)) {
                isValid = true;
                break;
            }
        }

        if (!isValid) {
            throw new ValidacaoNegocioException("Bandeira '" + bandeira + "' não é válida");
        }
    }

    /**
     * Valida ranking do cliente (valor numérico).
     * RN0027: Cliente possui ranking numérico
     * 
     * @param ranking ranking a validar
     * @throws ValidacaoNegocioException se o ranking for inválido
     */
    public void validarRanking(Integer ranking) {
        if (ranking == null || ranking < 0) {
            throw new ValidacaoNegocioException("Ranking deve ser um valor numérico não negativo");
        }
    }
}
