package com.les.jakebooks.service;

import com.les.jakebooks.domain.Endereco;
import com.les.jakebooks.dto.EnderecoDTO;
import com.les.jakebooks.exception.AcessoNegadoException;
import com.les.jakebooks.exception.EnderecoEntregaNaoEncontradoException;
import com.les.jakebooks.exception.RecursoNaoEncontradoException;
import com.les.jakebooks.domain.enums.TipoEndereco;
import com.les.jakebooks.repository.EnderecoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service para gerenciar operações de Endereço.
 * RF0035: Selecionar endereço de entrega.
 * RN0022: Pelo menos um endereço de entrega é obrigatório.
 * RN0023: Campos obrigatórios do endereço.
 */
@Service
public class EnderecoService {

    @Autowired
    private EnderecoRepository enderecoRepository;

    /**
     * Lista todos os endereços de entrega do cliente.
     * RF0035: Selecionar endereço de entrega.
     * RN0022: Pelo menos um endereço de entrega é obrigatório.
     *
     * @param clienteId ID do cliente
     * @return Lista de EnderecoDTO ordenada por ID (endereços mais recentes primeiro)
     * @throws EnderecoEntregaNaoEncontradoException se nenhum endereço de entrega encontrado
     */
    public List<EnderecoDTO> listarEnderecosEntrega(Long clienteId) {
        // Busca endereços com tipo ENTREGA
        List<Endereco> enderecosEntrega = enderecoRepository
                .findByClienteIdAndTipoEndereco(clienteId, TipoEndereco.ENTREGA);

        // Busca endereços com tipo AMBOS (também podem ser usados para entrega)
        List<Endereco> enderecosAmbos = enderecoRepository
                .findByClienteIdAndTipoEndereco(clienteId, TipoEndereco.AMBOS);

        // Combina as duas listas
        List<Endereco> todosEnderecos = new java.util.ArrayList<>(enderecosEntrega);
        todosEnderecos.addAll(enderecosAmbos);

        // Valida RN0022: Pelo menos um endereço de entrega é obrigatório
        if (todosEnderecos.isEmpty()) {
            throw new EnderecoEntregaNaoEncontradoException(
                    "Cliente deve ter pelo menos um endereço de entrega cadastrado"
            );
        }

        // Converte para DTO com endereço formatado
        return todosEnderecos.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Seleciona endereço para entrega do checkout atual.
     * RF0035: Selecionar endereço de entrega
     *
     * Valida que:
     * - Endereço existe
     * - Endereço pertence ao cliente
     * - Endereço é do tipo ENTREGA ou AMBOS
     *
     * @param clienteId ID do cliente
     * @param enderecoId ID do endereço selecionado
     * @return EnderecoDTO do endereço selecionado
     * @throws RecursoNaoEncontradoException se endereço não existe
     * @throws AcessoNegadoException se endereço não pertence ao cliente
     * @throws EnderecoEntregaNaoEncontradoException se endereço não é do tipo ENTREGA
     */
    public EnderecoDTO selecionarEnderecoEntrega(Long clienteId, Long enderecoId) {
        // Buscar endereço
        Endereco endereco = enderecoRepository.findById(enderecoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Endereço não encontrado"
                ));

        // Validar que endereço pertence ao cliente
        if (!endereco.getCliente().getId().equals(clienteId)) {
            throw new AcessoNegadoException(
                    "Endereço não pertence ao cliente"
            );
        }

        // Validar que endereço é do tipo ENTREGA ou AMBOS
        if (endereco.getTipoEndereco() != TipoEndereco.ENTREGA
                && endereco.getTipoEndereco() != TipoEndereco.AMBOS) {
            throw new EnderecoEntregaNaoEncontradoException(
                    "Endereço selecionado não é do tipo entrega"
            );
        }

        return toDTO(endereco);
    }

    /**
     * Converte entidade Endereco para EnderecoDTO com endereço formatado.
     *
     * @param endereco entidade a converter
     * @return DTO do endereço com campo enderecoFormatado preenchido
     */
    private EnderecoDTO toDTO(Endereco endereco) {
        String enderecoFormatado = formatarEndereco(endereco);

        return new EnderecoDTO(
                endereco.getId(),
                endereco.getNomeIdentificador(),
                endereco.getTipoResidencia(),
                endereco.getLogradouro(),
                endereco.getNumero(),
                endereco.getBairro(),
                endereco.getCep(),
                endereco.getCidade(),
                endereco.getEstado(),
                endereco.getPais(),
                endereco.getTipoEndereco(),
                enderecoFormatado
        );
    }

    /**
     * Formata o endereço para exibição.
     * Formato: Logradouro, Número - Bairro, Cidade - Estado, CEP
     * Exemplo: Rua das Flores, 123 - Centro, São Paulo - SP, 01234-567
     *
     * @param endereco endereço a formatar
     * @return endereço formatado
     */
    private String formatarEndereco(Endereco endereco) {
        StringBuilder sb = new StringBuilder();

        sb.append(endereco.getLogradouro())
                .append(", ")
                .append(endereco.getNumero())
                .append(" - ")
                .append(endereco.getBairro())
                .append(", ")
                .append(endereco.getCidade())
                .append(" - ")
                .append(endereco.getEstado())
                .append(", ")
                .append(endereco.getCep());

        return sb.toString();
    }
}
