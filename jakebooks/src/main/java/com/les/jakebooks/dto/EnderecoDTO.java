package com.les.jakebooks.dto;

import com.les.jakebooks.model.enums.TipoEndereco;
import com.les.jakebooks.model.enums.TipoResidencia;
import jakarta.validation.constraints.*;

/**
 * DTO para cadastro/edição de endereço de cliente.
 * RF0026: Cadastrar múltiplos endereços
 * RN0023: Campos obrigatórios do endereço
 * RN0021: Pelo menos um endereço de cobrança é obrigatório
 * RN0022: Pelo menos um endereço de entrega é obrigatório
 */
public record EnderecoDTO(
        /**
         * Identificador único
         */
        Long id,

        /**
         * Nome identificador (Ex: "Casa", "Trabalho", "Cobrança")
         * RN0023: Campos obrigatórios
         */
        @NotBlank(message = "Nome identificador é obrigatório")
        @Size(min = 1, max = 100, message = "Nome identificador deve ter entre 1 e 100 caracteres")
        String nomeIdentificador,

        /**
         * Tipo de residência
         * RN0023: Campos obrigatórios
         */
        @NotNull(message = "Tipo de residência é obrigatório")
        TipoResidencia tipoResidencia,

        /**
         * Logradouro (rua, avenida, etc)
         * RN0023: Campos obrigatórios
         */
        @NotBlank(message = "Logradouro é obrigatório")
        @Size(min = 1, max = 255, message = "Logradouro deve ter entre 1 e 255 caracteres")
        String logradouro,

        /**
         * Número
         * RN0023: Campos obrigatórios
         */
        @NotNull(message = "Número é obrigatório")
        @Positive(message = "Número deve ser positivo")
        Integer numero,

        /**
         * Bairro
         * RN0023: Campos obrigatórios
         */
        @NotBlank(message = "Bairro é obrigatório")
        @Size(min = 1, max = 100, message = "Bairro deve ter entre 1 e 100 caracteres")
        String bairro,

        /**
         * CEP (código de endereçamento postal)
         * RN0023: Campos obrigatórios
         * Formato: 00000-000
         */
        @NotBlank(message = "CEP é obrigatório")
        @Pattern(regexp = "\\d{5}-\\d{3}", message = "CEP deve estar no formato: 00000-000")
        String cep,

        /**
         * Cidade
         * RN0023: Campos obrigatórios
         */
        @NotBlank(message = "Cidade é obrigatória")
        @Size(min = 1, max = 100, message = "Cidade deve ter entre 1 e 100 caracteres")
        String cidade,

        /**
         * Estado (sigla: SP, RJ, etc)
         * RN0023: Campos obrigatórios
         */
        @NotBlank(message = "Estado é obrigatório")
        @Size(min = 2, max = 2, message = "Estado deve ter exatamente 2 caracteres")
        String estado,

        /**
         * País
         * RN0023: Campos obrigatórios
         */
        @NotBlank(message = "País é obrigatório")
        @Size(min = 1, max = 100, message = "País deve ter entre 1 e 100 caracteres")
        String pais,

        /**
         * Tipo de endereço (cobrança, entrega ou ambos)
         * RN0021: Pelo menos um endereço de cobrança
         * RN0022: Pelo menos um endereço de entrega
         */
        @NotNull(message = "Tipo de endereço é obrigatório")
        TipoEndereco tipoEndereco,

        /**
         * Endereço formatado para exibição
         * Gerado automaticamente a partir dos outros campos
         */
        String enderecoFormatado
) {
}
