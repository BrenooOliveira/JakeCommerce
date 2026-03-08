package com.les.jakebooks.dto;

import jakarta.validation.constraints.*;

/**
 * DTO para alteração de senha do cliente.
 * RF0028: Alterar apenas senha
 * RNF0012: Senha forte (mínimo 8 caracteres, maiúsculas, minúsculas e especiais)
 */
public record AlteraSenhaDTO(
        /**
         * Senha atual do cliente (para validação)
         */
        @NotBlank(message = "Senha atual é obrigatória")
        String senhaAtual,

        /**
         * Nova senha desejada
         * RNF0012: Mínimo 8 caracteres, maiúsculas, minúsculas e especiais
         */
        @NotBlank(message = "Nova senha é obrigatória")
        @Size(min = 8, max = 100, message = "Senha deve ter entre 8 e 100 caracteres")
        @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]+$",
                message = "Senha deve conter: minúsculas, maiúsculas, números e caracteres especiais (@$!%*?&)")
        String novaSenha,

        /**
         * Confirmação da nova senha (deve ser idêntica a novaSenha)
         */
        @NotBlank(message = "Confirmação de nova senha é obrigatória")
        String confirmacaoNovaSenha
) {
}
