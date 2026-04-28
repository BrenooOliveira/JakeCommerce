# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Quick Commands

All commands assume you're in the `jakebooks/` directory.

### Build & Compilation
```bash
# Clean and compile with Maven
mvn clean compile

# Build the project (compile + package)
mvn clean package

# Skip tests during build
mvn clean package -DskipTests
```

### Running the Application
```bash
# Run the Spring Boot application
mvn spring-boot:run

# Run with custom database credentials via environment variables
DB_HOST=localhost DB_PORT=5432 DB_NAME=jakebooks DB_USER=postgres DB_PASSWORD=postgres mvn spring-boot:run
```

### Testing
```bash
# Run all tests
mvn test

# Run a specific test class
mvn test -Dtest=TestClassName

# Run a specific test method
mvn test -Dtest=TestClassName#testMethodName

# Run tests with coverage report
mvn test jacoco:report
```

### Database Setup
```bash
# Create the PostgreSQL database
psql -U postgres -c "CREATE DATABASE jakebooks ENCODING 'UTF8';"

# Verify connection
psql -U postgres -h localhost -d jakebooks
```

The application uses Hibernate's `ddl-auto=create-drop` in development, so the schema is created automatically on startup. Initial data is loaded from `src/main/resources/data.sql`.

---

## Architecture Overview

JakeBooks follows **strict layered architecture** (no exceptions):

### Layer Responsibilities
- **Controller** (`com.les.jakebooks.controller`): Receives HTTP requests, calls Services, returns views/redirects. **Zero business logic**.
- **Service** (`com.les.jakebooks.service`): All business logic and transactions. Marked `@Transactional` when needed.
- **Repository** (`com.les.jakebooks.repository`): JpaRepository interfaces only. JPQL/native queries when required.
- **Domain** (`com.les.jakebooks.domain`): JPA entities with zero business logic.
- **DTO** (`com.les.jakebooks.dto`): Data transport between layers.
- **Config** (`com.les.jakebooks.config`): Spring configuration (security, beans, etc.).
- **Exception** (`com.les.jakebooks.exception`): Custom exceptions.
- **Validator** (`com.les.jakebooks.validator`): Business rule validators.
- **Util** (`com.les.jakebooks.util`): Utility helpers.

### Package Naming Convention
- Root: `com.les.jakebooks`
- Subpackages as described above
- Attributes and comments in Portuguese
- No Lombok (explicit getters/setters for clarity)

---

## Domain Model (Source of Truth)

The domain model is **the source of truth**. Never add fields or entities outside this specification.

### Key Entities
- **Livro** (Book): titulo, año, edicao, isbn, numeroPaginas, sinopse, dimensoes, codigoBarras, status, valorVenda
- **Cliente**: codigo, nome, cpf, telefone, email, ranking, status
- **Pedido**: dataCriacao, status, valorTotal, valorFrete
- **Carrinho**: dataCriacao, status, dataExpiracao
- **Pagamento**: status, valorTotal (supports multiple PagamentoCartao + PagamentoCupom)
- **Estoque**: quantidade, custoAtual, dataEntrada
- **Cupom**: codigo, valor, tipo (PROMOCIONAL, TROCA)

### Status Enums (Never Create Others)
- **StatusLivro**: ATIVO, INATIVO
- **StatusCliente**: ATIVO, INATIVO, BLOQUEADO
- **StatusPedido**: EM_PROCESSAMENTO, EM_TRANSPORTE, ENTREGUE, EM_TROCA, TROCADO
- **StatusPagamento**: PENDENTE, APROVADA, REPROVADA
- **StatusCarrinho**: ABERTO, EXPIRADO, FINALIZADO
- **StatusTroca**: SOLICITADA, AUTORIZADA, RECEBIDA, CONCLUIDA

All status enums are in `com.les.jakebooks.domain.enums`.

---

## Key Business Rules

### Stock & Inventory (RN005x)
- Stock deduction happens **only after payment APPROVED** (not during checkout)
- Restock occurs only via product exchange (Troca)
- Every item must have a cost; consider **highest** cost for sale price calculation
- No quantity zero allowed

### Cart & Checkout (RN003x / RN006x)
- Maximum 10 units per book per order (RN0063)
- Minimum order value 20 without shipping (RN0064)
- Cart blocking: warn 5 minutes before expiration, then remove items (RN0044)
- 3 consecutive payment REJECTIONS block the entire cart (RN0065)
- Multiple cards allowed, minimum 10 per card (RN0034)

### Payment (RN003x)
- One promotional coupon per purchase (RN0033)
- Consume coupons **before** card payment (RN0035)
- Generate coupon for overpayment (RN0036)
- Status: APROVADA or REPROVADA only

### Books (RN001x)
- Unique code required
- Price based on pricing group margin
- Manual deactivation requires reason
- Automatic deactivation for category "FORA DE MERCADO"
- Activation requires justification

---

## Strict Implementation Rules

1. **Respect cardinalité mappings** in the domain model—do not modify relationships.
2. **Consistency guarantee**: Cart → Pedido → Pagamento → Estoque must stay aligned.
3. **Logging**: All write operations must be logged via LogTransacao (data, user, timestamp, changes).
4. **No custom statuses**: Only use the defined enum values.
5. **No field injection beyond model**: Do not add unmapped attributes to entities.
6. **Validation at boundaries**: Validate user input in Controllers/DTOs; trust internal calls.

---

## Testing Strategy

- Tests use Spring Test + Spring Security Test (dependencies already in pom.xml).
- Test scope: unit tests for Services/Utilities; integration tests for endpoints with `@WebMvcTest` or `@SpringBootTest`.
- Mock repositories for unit tests; use `TestRestTemplate` or `MockMvc` for controllers.
- Never commit tests that mock the database if integration data matters—use `@DataJpaTest` with an in-memory database if needed.

---

## Useful References

- **Specification Document**: [AGENTS.md](AGENTS.md) — Complete requirements, RFs, RNs, and status enums.
- **Business Rules Guide**: [Guia de Regras de Negócio](docs/business-rules/BUSINESS-RULES-GUIDE.md)
- **Executive Summary**: [Sumário Executivo](docs/reports/SUMARIO-EXECUTIVO.md)
- **Database Schema**: [src/main/resources/schema.sql](jakebooks/src/main/resources/schema.sql)
- **Initial Data**: [src/main/resources/data.sql](jakebooks/src/main/resources/data.sql)

---

## Development Notes

- **Java Version**: 21 (set in pom.xml)
- **PostgreSQL**: Required; connection configured via environment variables in `application.properties`
- **Thymeleaf**: Used for server-side rendering; templates in `src/main/resources/templates/`
- **Spring Security**: Configured for session-based auth; no Lombok used for clarity
- **Dialect**: Portuguese for domain language; exceptions and business logic explained in inline comments

