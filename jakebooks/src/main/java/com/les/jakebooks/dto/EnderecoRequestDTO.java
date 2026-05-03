package com.les.jakebooks.dto;

import com.les.jakebooks.domain.enums.TipoEndereco;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Request para cadastro de novo endereço no checkout.
 */
public record EnderecoRequestDTO(
        @NotBlank(message = "Nome identificador é obrigatório")
        @Size(min = 2, max = 100, message = "Nome identificador deve ter entre 2 e 100 caracteres")
        String nomeIdentificador,

        @NotBlank(message = "Logradouro é obrigatório")
        @Size(min = 3, max = 255, message = "Logradouro deve ter entre 3 e 255 caracteres")
        String logradouro,

        @NotBlank(message = "Número é obrigatório")
        @Size(min = 1, max = 20, message = "Número deve ter entre 1 e 20 caracteres")
        String numero,

        @NotBlank(message = "Bairro é obrigatório")
        @Size(min = 2, max = 100, message = "Bairro deve ter entre 2 e 100 caracteres")
        String bairro,

        @NotBlank(message = "CEP é obrigatório")
        @Pattern(regexp = "\\d{5}-\\d{3}", message = "CEP deve estar no formato 00000-000")
        String cep,

        @NotBlank(message = "Cidade é obrigatória")
        @Size(min = 2, max = 100, message = "Cidade deve ter entre 2 e 100 caracteres")
        String cidade,

        @NotBlank(message = "Estado é obrigatório")
        @Pattern(
                regexp = "AC|AL|AP|AM|BA|CE|DF|ES|GO|MA|MT|MS|MG|PA|PB|PR|PE|PI|RJ|RN|RS|RO|RR|SC|SP|SE|TO",
                message = "Estado deve ser uma UF válida"
        )
        String estado,

        @NotBlank(message = "País é obrigatório")
        @Size(min = 2, max = 100, message = "País deve ter entre 2 e 100 caracteres")
        String pais,

        @NotNull(message = "Tipo de endereço é obrigatório")
        TipoEndereco tipo
) {
}
