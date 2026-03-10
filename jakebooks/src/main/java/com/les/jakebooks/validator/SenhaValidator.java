package com.les.jakebooks.validator;

import org.springframework.stereotype.Component;

import com.les.jakebooks.exception.SenhaFracaException;

/**
 * Validador de senhas.
 * 
 * Requisitos:
 * - Mínimo 8 caracteres
 * - Pelo menos uma letra maiúscula
 * - Pelo menos uma letra minúscula
 * - Pelo menos um caractere especial
 * 
 * Regra de negócio: Senha forte obrigatória para clientes
 * Lança: SenhaFracaException (NegocioException child)
 */
@Component
public class SenhaValidator {

    private static final int TAMANHO_MINIMO = 8;
    private static final String REGEX_SENHA = "^(?=.*[A-Z])(?=.*[a-z])(?=.*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?])(?=.{8,}).*$";

    /**
     * Valida se a senha atende aos requisitos de segurança.
     * 
     * @param senha a senha a validar
     * @throws SenhaFracaException se a senha não atender aos requisitos
     */
    public void validarSenha(String senha) {
        if (senha == null || senha.isEmpty()) {
            throw new SenhaFracaException("Senha não pode estar vazia");
        }

        if (senha.length() < TAMANHO_MINIMO) {
            String motivo = String.format("Mínimo %d caracteres. Tamanho atual: %d", 
                TAMANHO_MINIMO, senha.length());
            throw new SenhaFracaException(
                String.format("Senha deve ter no mínimo %d caracteres. Tamanho atual: %d", 
                    TAMANHO_MINIMO, senha.length()),
                motivo
            );
        }

        if (!temLetraMaiuscula(senha)) {
            throw new SenhaFracaException(
                "Senha deve conter pelo menos uma letra maiúscula (A-Z)",
                "Falta letra maiúscula"
            );
        }

        if (!temLetraMinuscula(senha)) {
            throw new SenhaFracaException(
                "Senha deve conter pelo menos uma letra minúscula (a-z)",
                "Falta letra minúscula"
            );
        }

        if (!temCaractereEspecial(senha)) {
            throw new SenhaFracaException(
                "Senha deve conter pelo menos um caractere especial (!@#$%^&*)",
                "Falta caractere especial"
            );
        }
    }

    /**
     * Verifica se a senha contém letra maiúscula.
     */
    private boolean temLetraMaiuscula(String senha) {
        return senha.matches(".*[A-Z].*");
    }

    /**
     * Verifica se a senha contém letra minúscula.
     */
    private boolean temLetraMinuscula(String senha) {
        return senha.matches(".*[a-z].*");
    }

    /**
     * Verifica se a senha contém caractere especial.
     */
    private boolean temCaractereEspecial(String senha) {
        return senha.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?].*");
    }
}
