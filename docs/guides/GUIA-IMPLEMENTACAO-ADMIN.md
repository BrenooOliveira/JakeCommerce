# 🔐 Guia de Implementação - Separação Cliente X Administrador

## 📋 Resumo Executivo

Este documento descreve as implementações necessárias para separar corretamente as visões e permissões entre **Clientes** e **Administradores** no sistema JakeBooks.

**Status Atual**: ❌ Não implementado (todos os usuários são tratados como cliente)
**Prioridade**: 🔴 CRÍTICA (bloqueia funcionalidades admin)

---

## 🎯 Objetivos

1. ✅ Permitir que administradores acessem painel administrativo
2. ✅ Bloquear clientes de acessar funcionalidades admin
3. ✅ Exibir interface correta baseada no tipo de usuário
4. ✅ Garantir segurança em endpoints e services

---

## 📝 Checklist de Implementação

### ✅ PASSO 1: Adicionar campo isAdmin na entidade Cliente

**Arquivo**: `jakebooks/src/main/java/com/les/jakebooks/domain/Cliente.java`

**Adicionar:**
```java
@Entity
@Table(name = "cliente")
public class Cliente {

    // ... campos existentes ...

    @Column(name = "is_admin")
    private Boolean isAdmin = false;  // ← ADICIONAR ESTE CAMPO

    // ... relacionamentos ...

    // ADICIONAR getter/setter
    public Boolean getIsAdmin() {
        return isAdmin;
    }

    public void setIsAdmin(Boolean isAdmin) {
        this.isAdmin = isAdmin;
    }
}
```

**Justificativa**: Campo técnico necessário para autenticação. Não faz parte do modelo de negócio, mas é requisito de infraestrutura para implementar RFs que especificam "(admin)".

---

### ✅ PASSO 2: Criar migration de banco de dados

**Arquivo**: `jakebooks/src/main/resources/db/migration/V002__adicionar_campo_admin.sql` (ou similar)

```sql
-- Adicionar coluna is_admin à tabela cliente
ALTER TABLE cliente
ADD COLUMN is_admin BOOLEAN DEFAULT FALSE;

-- Criar usuário administrador padrão
-- NOTA: Senha BCrypt para "Admin@123" (força 12)
INSERT INTO cliente (
    codigo,
    nome,
    genero,
    data_nascimento,
    cpf,
    telefone,
    email,
    senha_criptografada,
    ranking,
    status,
    is_admin
) VALUES (
    'ADMIN001',
    'Administrador do Sistema',
    'Não informado',
    '1990-01-01',
    '00000000000',
    '00000000000',
    'admin@jakebooks.com',
    '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewY5GYyTQQI.3Qai',  -- Admin@123
    0.0,
    'ATIVO',
    TRUE
);
```

**Alternativa se não usar migrations**: Adicionar no script de inicialização ou `data.sql`.

---

### ✅ PASSO 3: Atualizar CustomUserDetailsService

**Arquivo**: `jakebooks/src/main/java/com/les/jakebooks/config/CustomUserDetailsService.java`

**Modificar o método `loadUserByUsername`:**

```java
@Override
public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
    // Busca cliente pelo email
    Cliente cliente = clienteRepository.findByEmail(email)
        .orElseThrow(() -> new UsernameNotFoundException(
            String.format("Cliente não encontrado com email: %s", email)
        ));

    // Valida se cliente está ativo
    if (cliente.getStatus() == StatusCliente.BLOQUEADO) {
        throw new UsernameNotFoundException(
            "Acesso bloqueado. Contacte o administrador do sistema."
        );
    }

    if (cliente.getStatus() == StatusCliente.INATIVO) {
        throw new UsernameNotFoundException(
            "Cliente inativo. Contacte o administrador do sistema."
        );
    }

    // Constrói lista de autoridades/roles do cliente
    List<GrantedAuthority> authorities = new ArrayList<>();
    authorities.add(new SimpleGrantedAuthority("ROLE_CLIENTE"));

    // ← ADICIONAR ESTA LÓGICA
    if (cliente.getIsAdmin() != null && cliente.getIsAdmin()) {
        authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
    }

    // Retorna UserDetails do Spring Security
    return User.builder()
        .username(cliente.getEmail())
        .password(cliente.getSenhaCriptografada())
        .authorities(authorities)
        .accountLocked(false)
        .accountExpired(false)
        .credentialsExpired(false)
        .disabled(false)
        .build();
}
```

**Fazer o mesmo no método `loadUserById`**.

---

### ✅ PASSO 4: Criar classe utilitária SecurityUtil

**Arquivo**: `jakebooks/src/main/java/com/les/jakebooks/util/SecurityUtil.java` (criar pacote `util` se não existir)

```java
package com.les.jakebooks.util;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Classe utilitária para operações relacionadas à segurança.
 * Centraliza lógica de verificação de roles e usuário logado.
 */
public class SecurityUtil {

    /**
     * Verifica se o usuário logado possui role de ADMIN.
     *
     * @return true se admin, false caso contrário
     */
    public static boolean isAdmin() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return false;
        }
        return auth.getAuthorities().stream()
            .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }

    /**
     * Retorna o email (username) do usuário logado.
     *
     * @return email do usuário ou null se não autenticado
     */
    public static String getEmailUsuarioLogado() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return null;
        }
        return auth.getName();
    }

    /**
     * Verifica se o usuário está autenticado.
     *
     * @return true se autenticado, false caso contrário
     */
    public static boolean isAuthenticated() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null && auth.isAuthenticated()
            && !auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ANONYMOUS"));
    }
}
```

---

### ✅ PASSO 5: Habilitar @PreAuthorize no SecurityConfig

**Arquivo**: `jakebooks/src/main/java/com/les/jakebooks/config/SecurityConfig.java`

**Adicionar annotation na classe:**
```java
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)  // ← ADICIONAR ESTA LINHA
public class SecurityConfig {
    // ... resto do código ...
}
```

---

### ✅ PASSO 6: Atualizar Controllers

#### 6.1 Substituir isAdmin hardcoded

**Em TODOS os controllers**, substituir:
```java
// ANTES:
model.addAttribute("isAdmin", false);

// DEPOIS:
model.addAttribute("isAdmin", SecurityUtil.isAdmin());
```

**Importar:**
```java
import com.les.jakebooks.util.SecurityUtil;
```

**Arquivos afetados:**
- ✅ `LivroController.java`
- ✅ `ClienteController.java`
- ✅ `CarrinhoController.java`
- ✅ `PedidoController.java`
- ✅ `TrocaController.java`
- ✅ `EstoqueController.java`
- ✅ `AnaliseController.java`
- ✅ `HomeController.java`

#### 6.2 Adicionar @PreAuthorize em métodos admin

**LivroController.java:**
```java
@PreAuthorize("hasRole('ADMIN')")
@PostMapping("/{codigo}/inativar")
public String inativar(...) { ... }

@PreAuthorize("hasRole('ADMIN')")
@PostMapping("/{codigo}/ativar")
public String ativar(...) { ... }
```

**EstoqueController.java:**
```java
@PreAuthorize("hasRole('ADMIN')")
@GetMapping
public String listar(...) { ... }

@PreAuthorize("hasRole('ADMIN')")
@PostMapping("/entrada")
public String entrada(...) { ... }
```

**TrocaController.java:**
```java
// Solicitar troca: autenticado (cliente)
@GetMapping("/solicitar")
public String formularioSolicitacao(...) { ... }

// Resto: apenas admin
@PreAuthorize("hasRole('ADMIN')")
@GetMapping
public String listar(...) { ... }

@PreAuthorize("hasRole('ADMIN')")
@PostMapping("/{id}/autorizar")
public String autorizar(...) { ... }
```

**PedidoController.java:**
```java
@PreAuthorize("hasRole('ADMIN')")
@PostMapping("/{id}/despachar")
public String despachar(...) { ... }

@PreAuthorize("hasRole('ADMIN')")
@PostMapping("/{id}/confirmar-entrega")
public String confirmarEntrega(...) { ... }
```

**AnaliseController.java:**
```java
@PreAuthorize("hasRole('ADMIN')")
@GetMapping
public String dashboard(...) { ... }
```

**Importar:**
```java
import org.springframework.security.access.prepost.PreAuthorize;
```

---

### ✅ PASSO 7: Atualizar Views (opcional mas recomendado)

**navbar.html** - Diferenciar menu:
```html
<!-- Menu Admin -->
<li th:if="${isAdmin}" class="nav-item">
    <a class="nav-link" href="/admin/dashboard">
        <i class="bi bi-speedometer2"></i> Painel Admin
    </a>
</li>

<!-- Menu Cliente -->
<li th:unless="${isAdmin}" class="nav-item">
    <a class="nav-link" href="/pedidos/meus">
        <i class="bi bi-bag"></i> Meus Pedidos
    </a>
</li>
```

**livros/lista.html** - Mostrar botões admin apenas para admin:
```html
<a th:if="${isAdmin}"
   th:href="@{/livros/novo}"
   class="btn btn-primary">
    <i class="bi bi-plus"></i> Novo Livro
</a>

<button th:if="${isAdmin}"
        type="button"
        class="btn btn-danger"
        data-bs-toggle="modal"
        data-bs-target="#inativarModal">
    <i class="bi bi-x-circle"></i> Inativar
</button>
```

---

### ✅ PASSO 8: Validações em Services (camada extra de segurança)

**Exemplo em ClienteService.java:**
```java
public void inativar(String codigo) {
    // Validação dupla de segurança
    if (!SecurityUtil.isAdmin()) {
        throw new ValidacaoNegocioException(
            "Apenas administradores podem inativar clientes"
        );
    }

    Cliente cliente = buscarPorCodigoOuFalhar(codigo);
    cliente.setStatus(StatusCliente.INATIVO);
    clienteRepository.save(cliente);
}
```

**Aplicar em operações sensíveis:**
- Inativar/ativar livros
- Inativar clientes
- Autorizar trocas
- Despachar pedidos
- Entrada em estoque

---

## 🧪 Testes de Validação

### Teste 1: Criar usuário admin
```bash
# No banco de dados ou via script:
INSERT INTO cliente (codigo, nome, email, senha_criptografada, is_admin, status, ...)
VALUES ('ADMIN001', 'Admin', 'admin@test.com', '$2a$12$...', TRUE, 'ATIVO', ...);
```

### Teste 2: Login como admin
1. Acessar `/login`
2. Email: `admin@jakebooks.com`
3. Senha: `Admin@123`
4. Verificar que:
   - ✅ Sidebar aparece
   - ✅ Links admin visíveis
   - ✅ Pode acessar `/estoque`

### Teste 3: Login como cliente
1. Criar cliente comum (isAdmin = FALSE)
2. Fazer login
3. Tentar acessar `/estoque`
4. Deve retornar **403 Forbidden**

### Teste 4: Validação de endpoints
```bash
# Sem autenticação
curl http://localhost:8080/admin/livros
# Esperado: redirect 302 para /login

# Com cliente comum
curl -u cliente@test.com:senha http://localhost:8080/estoque
# Esperado: 403 Forbidden

# Com admin
curl -u admin@test.com:Admin@123 http://localhost:8080/estoque
# Esperado: 200 OK
```

---

## 📊 Resumo de Mudanças

| Componente | Arquivo | Ação |
|------------|---------|------|
| **Entidade** | `Cliente.java` | Adicionar campo `isAdmin` |
| **Migration** | `V002__adicionar_campo_admin.sql` | Criar script SQL |
| **Security** | `CustomUserDetailsService.java` | Atribuir ROLE_ADMIN |
| **Utility** | `SecurityUtil.java` | Criar classe nova |
| **Config** | `SecurityConfig.java` | Adicionar `@EnableMethodSecurity` |
| **Controllers** | Todos os `*Controller.java` | Substituir isAdmin + @PreAuthorize |
| **Services** | Services críticos | Adicionar validações |
| **Views** | Templates Thymeleaf | Condicionar elementos por isAdmin |

---

## ⚠️ Notas Importantes

### Sobre o Modelo de Domínio
O campo `isAdmin` **NÃO está no modelo de domínio original** (requisitos). Esta é uma **extensão técnica necessária** para:
- Implementar autenticação e autorização
- Diferenciar perfis de acesso
- Atender RFs que especificam "(admin)"

**RFs que requerem admin:**
- RF0041: Autorizar troca (admin)
- RF0042: Visualizar trocas (admin)
- RF0038: Despachar produtos (admin)
- RF0039: Confirmar entrega (admin)
- RF0051: Entrada em estoque (admin)
- RF0055: Analisar histórico (admin)

### Senhas BCrypt
Para gerar senha BCrypt (força 12):
```java
String senha = "Admin@123";
String hash = new BCryptPasswordEncoder(12).encode(senha);
System.out.println(hash);
```

### Alternativas de Implementação
Se não quiser adicionar campo em Cliente:
1. **Criar entidade Usuario** separada (mais complexo)
2. **Usar email específico** como `admin@*` (gambiarra, não recomendado)
3. **Criar tabela roles separada** (over-engineering para este caso)

**Recomendação**: Adicionar campo `isAdmin` é a solução mais simples e eficaz.

---

## 🎯 Conclusão

Após implementar todos os passos:
✅ Administradores terão acesso completo
✅ Clientes terão acesso limitado
✅ Interface se adaptará ao tipo de usuário
✅ Segurança garantida em múltiplas camadas

**Responsável pela revisão**: `review-agent`
**Documentação de referência**: `review-agent.md`
