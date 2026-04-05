package com.les.jakebooks.repository;

import com.les.jakebooks.domain.Estoque;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository para a entidade Estoque.
 * RN0051: Entrada exige produto, quantidade, custo, fornecedor e data.
 * RN0061: Não permitir quantidade zero.
 * RN0062: Todo item deve possuir custo.
 * TASK-CHK-04: Lock pessimista para evitar race conditions na baixa de estoque.
 */
@Repository
public interface EstoqueRepository extends JpaRepository<Estoque, Long> {

    /**
     * Busca estoque por livro.
     */
    Estoque findByLivroId(Long livroId);

    /**
     * Busca estoque por livro com lock pessimista.
     * TASK-CHK-04: Previne race conditions durante baixa de estoque.
     * O lock pessimista garante que apenas uma transacao por vez
     * pode ler e modificar o estoque deste livro.
     *
     * @param livroId ID do livro
     * @return Optional com o estoque (vazio se nao encontrado)
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT e FROM Estoque e WHERE e.livro.id = :livroId")
    Optional<Estoque> findByLivroIdWithLock(@Param("livroId") Long livroId);
}
