package com.les.jakebooks.repository;

import com.les.jakebooks.domain.Endereco;
import com.les.jakebooks.domain.enums.TipoEndereco;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository para a entidade Endereco.
 * RN0021: Pelo menos um endereço de cobrança é obrigatório.
 * RN0022: Pelo menos um endereço de entrega é obrigatório.
 * RN0023: Campos obrigatórios do endereço.
 */
@Repository
public interface EnderecoRepository extends JpaRepository<Endereco, Long> {

    /**
     * Busca endereços de um cliente.
     */
    List<Endereco> findByClienteId(Long clienteId);

    /**
     * Busca endereços de um cliente por tipo.
     * RF0026: Cadastrar múltiplos endereços.
     */
    List<Endereco> findByClienteIdAndTipoEndereco(Long clienteId, TipoEndereco tipoEndereco);

    /**
     * Busca um endereço principal (cobrança) de um cliente.
     */
    Endereco findByClienteIdAndNomeIdentificadorAndTipoEndereco(Long clienteId, String nomeIdentificador, TipoEndereco tipoEndereco);

    /**
     * Busca um endereço específico de um cliente.
     */
    Optional<Endereco> findByIdAndClienteId(Long id, Long clienteId);
}
