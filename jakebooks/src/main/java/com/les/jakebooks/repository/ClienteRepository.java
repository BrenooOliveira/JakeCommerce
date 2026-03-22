package com.les.jakebooks.repository;

import com.les.jakebooks.domain.Cliente;
import com.les.jakebooks.model.enums.StatusCliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository para a entidade Cliente.
 * RN0026: Dados obrigatórios do cliente.
 * RN0027: Cliente possui ranking numérico.
 */
@Repository
public interface ClienteRepository extends JpaRepository<Cliente, Long> {

    /**
     * Busca cliente pelo CPF único.
     * Requerimento de negócio: CPF deve ser único.
     */
    Optional<Cliente> findByCpf(String cpf);

    /**
     * Busca cliente pelo email único.
     */
    Optional<Cliente> findByEmail(String email);

    /**
     * Busca cliente pelo código único.
     * RF0024: Consultar cliente.
     */
    Optional<Cliente> findByCodigo(String codigo);

    /**
     * Busca cliente pelo código com relacionamentos carregados (endereços e cartões).
     * Usa LEFT JOIN FETCH para evitar N+1 queries e problemas de lazy loading.
     * IMPORTANTE: Duas queries separadas para evitar produto cartesiano.
     */
    @Query("SELECT DISTINCT c FROM Cliente c " +
           "LEFT JOIN FETCH c.enderecos " +
           "WHERE c.codigo = :codigo")
    Optional<Cliente> findByCodigoComEnderecos(@Param("codigo") String codigo);

    /**
     * Busca cliente pelo código com cartões carregados.
     * Segunda query para evitar produto cartesiano entre endereços e cartões.
     */
    @Query("SELECT DISTINCT c FROM Cliente c " +
           "LEFT JOIN FETCH c.cartoes " +
           "WHERE c.codigo = :codigo")
    Optional<Cliente> findByCodigoComCartoes(@Param("codigo") String codigo);

    /**
     * Busca cliente pelo código com TODOS os relacionamentos.
     * Usa duas queries em batch para evitar produto cartesiano.
     */
    @Query("SELECT DISTINCT c FROM Cliente c " +
           "LEFT JOIN FETCH c.enderecos " +
           "LEFT JOIN FETCH c.cartoes " +
           "WHERE c.codigo = :codigo")
    Optional<Cliente> findByCodigoComRelacionamentos(@Param("codigo") String codigo);

    /**
     * Busca cliente pelo email com relacionamentos carregados.
     * Usa JOIN FETCH para evitar N+1 queries.
     */
    @Query("SELECT DISTINCT c FROM Cliente c " +
           "LEFT JOIN FETCH c.enderecos " +
           "LEFT JOIN FETCH c.cartoes " +
           "WHERE c.email = :email")
    Optional<Cliente> findByEmailComRelacionamentos(@Param("email") String email);

    /**
     * Busca todos os clientes ativos.
     */
    List<Cliente> findByStatus(StatusCliente status);

    /**
     * Busca clientes bloqueados.
     * RN0065: 3 pagamentos REPROVADOS consecutivos bloqueiam cliente.
     */
    List<Cliente> findByStatusOrderByRankingDesc(StatusCliente status);
}
