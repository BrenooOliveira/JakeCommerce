package com.les.jakebooks.dto;

import com.les.jakebooks.domain.enums.StatusCliente;

import java.time.LocalDate;
import java.util.List;

/**
 * DTO para exibição completa dos dados do cliente.
 * Inclui endereços e cartões associados.
 * RF0024: Consultar cliente
 * RF0025: Consultar transações do cliente
 */
public record ClienteDetalheDTO(
        /**
         * Identificador único (gerado)
         */
        Long id,

        /**
         * Código único do cliente
         */
        String codigo,

        /**
         * Nome do cliente
         */
        String nome,

        /**
         * Gênero
         */
        String genero,

        /**
         * Data de nascimento
         */
        LocalDate dataNascimento,

        /**
         * CPF (mascarado parcialmente na resposta)
         */
        String cpf,

        /**
         * Telefone
         */
        String telefone,

        /**
         * Email
         */
        String email,

        /**
         * Ranking numérico do cliente
         * RN0027: Cliente possui ranking numérico
         */
        Double ranking,

        /**
         * Status do cliente
         */
        StatusCliente status,

        /**
         * Lista de endereços cadastrados
         * RN0021: Pelo menos um endereço de cobrança é obrigatório
         * RN0022: Pelo menos um endereço de entrega é obrigatório
         */
        List<EnderecoDTO> enderecos,

        /**
         * Lista de cartões cadastrados
         * RF0027: Cadastrar múltiplos cartões (um preferencial)
         */
        List<CartaoDTO> cartoes,

        /**
         * Indica se cliente possui privilégios administrativos
         */
        Boolean isAdmin
) {
}
