package com.les.jakebooks.dto;

import com.les.jakebooks.domain.enums.StatusCliente;
import jakarta.validation.constraints.*;

import java.time.LocalDate;

/**
 * DTO para cadastro de novo cliente.
 * Contém todos os campos obrigatórios para criar uma conta.
 * RF0021: Cadastrar cliente
 * RN0026: Dados obrigatórios do cliente
 * RNF0012: Senha forte (mínimo 8 caracteres, maiúsculas, minúsculas e especiais)
 */
public record ClienteCadastroDTO(
        /**
         * Nome completo do cliente
         * RN0026: Dados obrigatórios
         */
        @NotBlank(message = "Nome é obrigatório")
        @Size(min = 3, max = 255, message = "Nome deve ter entre 3 e 255 caracteres")
        String nome,

        /**
         * Gênero do cliente (M/F/Outro)
         */
        @NotBlank(message = "Gênero é obrigatório")
        @Size(max = 50, message = "Gênero deve ter no máximo 50 caracteres")
        String genero,

        /**
         * Data de nascimento
         * RN0026: Dados obrigatórios
         */
        @NotNull(message = "Data de nascimento é obrigatória")
        @PastOrPresent(message = "Data de nascimento deve ser no passado")
        LocalDate dataNascimento,

        /**
         * CPF único e obrigatório
         * RN0026: Dados obrigatórios
         * Formato: 000.000.000-00
         */
        @NotBlank(message = "CPF é obrigatório")
        @Pattern(regexp = "\\d{3}\\.\\d{3}\\.\\d{3}-\\d{2}", message = "CPF deve estar no formato: 000.000.000-00")
        String cpf,

        /**
         * Telefone de contato
         * RN0026: Dados obrigatórios
         */
        @NotBlank(message = "Telefone é obrigatório")
        @Size(min = 10, max = 20, message = "Telefone deve ter entre 10 e 20 caracteres")
        String telefone,

        /**
         * Email único do cliente
         * RN0026: Dados obrigatórios
         */
        @NotBlank(message = "Email é obrigatório")
        @Email(message = "Email deve ser válido")
        String email,

        /**
         * Senha de acesso
         * RNF0012: Mínimo 8 caracteres, maiúsculas, minúsculas e especiais
         */
        @NotBlank(message = "Senha é obrigatória")
        @Size(min = 8, max = 100, message = "Senha deve ter entre 8 e 100 caracteres")
        @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]+$",
                message = "Senha deve conter: minúsculas, maiúsculas, números e caracteres especiais (@$!%*?&)")
        String senha,

        /**
         * Confirmação de senha (deve ser idêntica a senha)
         */
        @NotBlank(message = "Confirmação de senha é obrigatória")
        String confirmacaoSenha
) {
}
