package com.les.jakebooks.repository;

import com.les.jakebooks.domain.Editora;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository para a entidade Editora.
 * RN0011: Dados obrigatórios conforme modelo.
 */
@Repository
public interface EditoraRepository extends JpaRepository<Editora, Long> {

    /**
     * Busca editora pelo nome único.
     */
    Optional<Editora> findByNome(String nome);
}
