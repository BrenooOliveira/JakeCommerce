package com.les.jakebooks.repository;

import com.les.jakebooks.domain.Livro;
import com.les.jakebooks.domain.enums.StatusLivro;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository para a entidade Livro.
 * RN0011: Dados obrigatórios conforme modelo.
 * RN0012: Livro pode ter múltiplas categorias.
 */
@Repository
public interface LivroRepository extends JpaRepository<Livro, Long> {

    /**
     * Busca livros por status.
     * RN0016: Inativação automática categoria FORA DE MERCADO.
     */
    List<Livro> findByStatus(StatusLivro status);

    /**
     * Busca livros por categoria.
     * RN0012: Livro pode ter múltiplas categorias.
     */
    List<Livro> findByCategoriasId(Long categoriaId);

    /**
     * Filtro combinado para busca de livros.
     * Busca por título, autor, status e categoria.
     */
    @Query("SELECT DISTINCT l FROM Livro l " +
           "LEFT JOIN l.autores a " +
           "LEFT JOIN l.categorias c " +
           "WHERE (:titulo IS NULL OR LOWER(l.titulo) LIKE LOWER(CONCAT('%', :titulo, '%'))) " +
           "AND (:autorId IS NULL OR a.id = :autorId) " +
           "AND (:status IS NULL OR l.status = :status) " +
           "AND (:categoriaId IS NULL OR c.id = :categoriaId)")
    List<Livro> buscarComFiltros(
            @Param("titulo") String titulo,
            @Param("autorId") Long autorId,
            @Param("status") StatusLivro status,
            @Param("categoriaId") Long categoriaId
    );

    /**
     * Busca livro pelo ISBN único.
     * RF0011: Código único obrigatório.
     */
    Livro findByIsbn(String isbn);

    /**
     * Busca livro pelo código único.
     * RF0011: Código único obrigatório.
     */
    Livro findByCodigo(String codigo);

    /**
     * Busca livro pelo código de barras.
     */
    Livro findByCodigoBarras(String codigoBarras);
}
