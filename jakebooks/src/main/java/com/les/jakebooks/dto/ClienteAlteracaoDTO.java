package com.les.jakebooks.dto;

import com.les.jakebooks.domain.enums.StatusCliente;
import jakarta.validation.constraints.*;

import java.time.LocalDate;

/**
 * DTO para alteração de dados do cliente.
 * Não inclui CPF (imutável) nem senha (alterada por AlteraSenhaDTO).
 * RF0022: Alterar cliente
 */
public record ClienteAlteracaoDTO(
        /**
         * Nome completo do cliente
         */
        @NotBlank(message = "Nome é obrigatório")
        @Size(min = 3, max = 255, message = "Nome deve ter entre 3 e 255 caracteres")
        String nome,

        /**
         * Gênero do cliente
         */
        @NotBlank(message = "Gênero é obrigatório")
        @Size(max = 50, message = "Gênero deve ter no máximo 50 caracteres")
        String genero,

        /**
         * Data de nascimento
         */
        @NotNull(message = "Data de nascimento é obrigatória")
        @PastOrPresent(message = "Data de nascimento deve ser no passado")
        LocalDate dataNascimento,

        /**
         * Telefone de contato
         */
        @NotBlank(message = "Telefone é obrigatório")
        @Size(min = 10, max = 20, message = "Telefone deve ter entre 10 e 20 caracteres")
        String telefone,

        /**
         * Email (pode ser alterado)
         */
        @NotBlank(message = "Email é obrigatório")
        @Email(message = "Email deve ser válido")
        String email,

        /**
         * Status do cliente
         * RN0065: Cliente pode ser bloqueado após 3 pagamentos REPROVADOS
         */
        @NotNull(message = "Status é obrigatório")
        StatusCliente status
) {
}
