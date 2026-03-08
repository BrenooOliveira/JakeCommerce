package com.les.jakebooks.repository;

import com.les.jakebooks.domain.Autor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository para a entidade Autor.
 * RN0011: Dados obrigatórios conforme modelo.
 */
@Repository
public interface AutorRepository extends JpaRepository<Autor, Long> {

    /**
     * Busca autor pelo nome.
     */
    Optional<Autor> findByNome(String nome);
}
