package com.les.jakebooks.repository;

import com.les.jakebooks.domain.Estoque;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository para a entidade Estoque.
 * RN0051: Entrada exige produto, quantidade, custo, fornecedor e data.
 * RN0061: Não permitir quantidade zero.
 * RN0062: Todo item deve possuir custo.
 */
@Repository
public interface EstoqueRepository extends JpaRepository<Estoque, Long> {

    /**
     * Busca estoque por livro.
     */
    Estoque findByLivroId(Long livroId);
}
