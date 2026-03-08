package com.les.jakebooks.repository;

import com.les.jakebooks.domain.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository para a entidade Categoria.
 * RN0012: Livro pode ter múltiplas categorias.
 */
@Repository
public interface CategoriaRepository extends JpaRepository<Categoria, Long> {

    /**
     * Busca categoria pelo nome único.
     */
    Optional<Categoria> findByNome(String nome);

    /**
     * Verifica se existe categoria "FORA DE MERCADO".
     * RN0016: Inativação automática categoria FORA DE MERCADO.
     */
    Optional<Categoria> findByNomeContainingIgnoreCase(String nome);
}
