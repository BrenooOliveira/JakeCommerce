# TAREFAS BACKEND - Separação Cliente x Administrador

## 📋 CONTEXTO

A aplicação JakeCommerce precisa implementar controle de acesso granular entre **CLIENTE** (usuário comum) e **ADMINISTRADOR** (gestão do sistema). Este documento define as tarefas do **backend-agent** para garantir segurança e autorização correta.

## 🚨 PROBLEMAS IDENTIFICADOS

### 1. Entidade Cliente NÃO tem campo isAdmin
**Arquivo:** `jakebooks/src/main/java/com/les/jakebooks/domain/Cliente.java`
**Problema:** Não existe forma de diferenciar cliente comum de administrador.
**Impacto:** Impossível atribuir ROLE_ADMIN, todos os usuários têm apenas ROLE_CLIENTE.

### 2. CustomUserDetailsService não verifica admin
**Arquivo:** `jakebooks/src/main/java/com/les/jakebooks/config/CustomUserDetailsService.java:71`
**Problema:** Existe TODO para implementar lógica de admin, mas não está implementado.
**Impacto:** Todos os usuários autenticados recebem apenas ROLE_CLIENTE.

### 3. Não existe utility SecurityUtil
**Problema:** Controllers precisarão verificar se usuário logado é admin (para atributo `isAdmin` no Model).
**Impacto:** Controllers vão hardcode `model.addAttribute("isAdmin", false)` incorretamente.

### 4. Não existe script de migração/seed para admin
**Problema:** Banco de dados não tem coluna `is_admin` nem usuário administrador inicial.
**Impacto:** Impossível testar funcionalidades de administrador.

---

## ✅ TAREFAS - BACKEND AGENT

### TAREFA BR-01: Adicionar campo isAdmin na entidade Cliente

**Arquivo:** `jakebooks/src/main/java/com/les/jakebooks/domain/Cliente.java`

**Ação:**
1. Adicionar campo `private Boolean isAdmin;` após o campo `ranking` (após linha 50)
2. Inicializar no construtor padrão como `false`
3. Adicionar parâmetro no construtor com parâmetros
4. Criar getter `public Boolean getIsAdmin()`
5. Criar setter `public void setIsAdmin(Boolean isAdmin)`

**Código de exemplo:**
```java
// Após linha 50 (após ranking)
private Boolean isAdmin = false;

// Atualizar construtor vazio (linha 63)
public Cliente() {
    this.isAdmin = false;
}

// Atualizar construtor com parâmetros
public Cliente(String codigo, String nome, String genero, LocalDate dataNascimento, String cpf,
               String telefone, String email, String senhaCriptografada, Double ranking,
               StatusCliente status, Boolean isAdmin) {
    // ... campos existentes ...
    this.isAdmin = isAdmin != null ? isAdmin : false;
}

// Adicionar getters/setters
public Boolean getIsAdmin() {
    return isAdmin;
}

public void setIsAdmin(Boolean isAdmin) {
    this.isAdmin = isAdmin;
}
```

**Importante:**
- Este campo NÃO está no modelo de domínio original (AGENTS.md)
- É uma **extensão técnica necessária** para implementar RFs 038, 039, 041, 042, 051, 055 que especificam "(admin)"
- Documentar com comentário JavaDoc explicando a justificativa

**Critério de aceite:**
- [ ] Campo `isAdmin` adicionado com valor padrão `false`
- [ ] Getter e setter criados
- [ ] Construtores atualizados

---

### TAREFA BR-02: Implementar lógica de admin no CustomUserDetailsService

**Arquivo:** `jakebooks/src/main/java/com/les/jakebooks/config/CustomUserDetailsService.java`

**Ação:**
1. Remover TODO da linha 71
2. Implementar verificação de campo `isAdmin` do cliente
3. Atribuir `ROLE_ADMIN` quando `cliente.getIsAdmin() == true`
4. Sempre atribuir `ROLE_CLIENTE` para todos (admin também é cliente)

**Código:**
```java
// Substituir linhas 67-72 por:
// Constrói lista de autoridades/roles do cliente
List<GrantedAuthority> authorities = new ArrayList<>();
authorities.add(new SimpleGrantedAuthority("ROLE_CLIENTE"));

// Atribui ROLE_ADMIN se campo isAdmin for true
if (cliente.getIsAdmin() != null && cliente.getIsAdmin()) {
    authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
}
```

**Aplicar também no método `loadUserById()` (linhas 99-100):**
```java
// Substituir linhas 99-100 por:
List<GrantedAuthority> authorities = new ArrayList<>();
authorities.add(new SimpleGrantedAuthority("ROLE_CLIENTE"));

if (cliente.getIsAdmin() != null && cliente.getIsAdmin()) {
    authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
}
```

**Critério de aceite:**
- [ ] Lógica de admin implementada em `loadUserByUsername()`
- [ ] Lógica de admin implementada em `loadUserById()`
- [ ] Admin recebe `ROLE_ADMIN` + `ROLE_CLIENTE`
- [ ] Cliente comum recebe apenas `ROLE_CLIENTE`

---

### TAREFA BR-03: Criar utility SecurityUtil para helpers de segurança

**Arquivo:** `jakebooks/src/main/java/com/les/jakebooks/util/SecurityUtil.java` (novo)

**Ação:**
1. Criar pacote `com.les.jakebooks.util` se não existir
2. Criar classe `SecurityUtil` com métodos estáticos:
   - `isAdmin()`: verifica se usuário logado tem ROLE_ADMIN
   - `getEmailUsuarioLogado()`: retorna email do usuário autenticado
   - `isAuthenticated()`: verifica se há usuário autenticado

**Código completo:**
```java
package com.les.jakebooks.util;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Utilitário para operações relacionadas a segurança e autenticação.
 * Fornece métodos estáticos para verificar autorização e obter dados do usuário logado.
 */
public class SecurityUtil {

    /**
     * Verifica se o usuário atualmente autenticado possui a role ADMIN.
     *
     * @return true se o usuário tem ROLE_ADMIN, false caso contrário
     */
    public static boolean isAdmin() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return false;
        }
        return auth.getAuthorities().stream()
            .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"));
    }

    /**
     * Retorna o email (username) do usuário atualmente autenticado.
     *
     * @return email do usuário logado, ou null se não autenticado
     */
    public static String getEmailUsuarioLogado() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return null;
        }
        return auth.getName();
    }

    /**
     * Verifica se existe um usuário autenticado no contexto atual.
     *
     * @return true se há usuário autenticado, false caso contrário
     */
    public static boolean isAuthenticated() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null && auth.isAuthenticated()
            && !"anonymousUser".equals(auth.getPrincipal());
    }
}
```

**Critério de aceite:**
- [ ] Classe `SecurityUtil` criada no pacote `com.les.jakebooks.util`
- [ ] Método `isAdmin()` implementado e testado
- [ ] Método `getEmailUsuarioLogado()` implementado
- [ ] Método `isAuthenticated()` implementado

---

### TAREFA BR-04: Criar script SQL de migração e seed

**Arquivos:**
- `jakebooks/src/main/resources/db/migration/V002__add_is_admin_column.sql` (novo)
- `jakebooks/src/main/resources/db/seed/admin_seed.sql` (novo)

**Ação 1 - Migração (adicionar coluna is_admin):**

```sql
-- V002__add_is_admin_column.sql
-- Adiciona coluna is_admin na tabela cliente para separação de perfis

ALTER TABLE cliente ADD COLUMN IF NOT EXISTS is_admin BOOLEAN DEFAULT FALSE;

-- Define como false para todos os clientes existentes
UPDATE cliente SET is_admin = FALSE WHERE is_admin IS NULL;

-- Comentário de justificativa
COMMENT ON COLUMN cliente.is_admin IS 'Campo técnico para autorização. Não faz parte do modelo de domínio, mas é necessário para implementar RFs 038, 039, 041, 042, 051, 055.';
```

**Ação 2 - Seed (criar usuário admin inicial):**

```sql
-- admin_seed.sql
-- Cria usuário administrador inicial para o sistema

-- Senha padrão do admin: Admin123@
-- Hash BCrypt (força 12): $2a$12$i06IiML1RKTrMr7pkgmZ.eDy0eK/mKuaSaPGeM3hoTOGvRFFiDUyS

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
    'Administrador',
    'OUTRO',
    '1990-01-01',
    '00000000000',
    '11999999999',
    'admin@jakebooks.com',
    '$2a$12$i06IiML1RKTrMr7pkgmZ.eDy0eK/mKuaSaPGeM3hoTOGvRFFiDUyS',
    0.0,
    'ATIVO',
    TRUE
) ON CONFLICT (email) DO NOTHING;

-- Criar endereço de cobrança obrigatório para admin (RN0021)
INSERT INTO endereco (
    cliente_id,
    nome_identificador,
    tipo_residencia,
    logradouro,
    numero,
    bairro,
    cep,
    cidade,
    estado,
    pais,
    tipo_endereco
) SELECT
    c.id,
    'Endereço Principal',
    'COMERCIAL',
    'Rua Admin',
    '1',
    'Centro',
    '01000-000',
    'São Paulo',
    'SP',
    'Brasil',
    'COBRANCA'
FROM cliente c
WHERE c.email = 'admin@jakebooks.com'
ON CONFLICT DO NOTHING;

-- Criar endereço de entrega obrigatório para admin (RN0022)
INSERT INTO endereco (
    cliente_id,
    nome_identificador,
    tipo_residencia,
    logradouro,
    numero,
    bairro,
    cep,
    cidade,
    estado,
    pais,
    tipo_endereco
) SELECT
    c.id,
    'Endereço Entrega',
    'COMERCIAL',
    'Rua Admin',
    '1',
    'Centro',
    '01000-000',
    'São Paulo',
    'SP',
    'Brasil',
    'ENTREGA'
FROM cliente c
WHERE c.email = 'admin@jakebooks.com'
ON CONFLICT DO NOTHING;
```

**Importante:**
- Se o projeto usa Flyway, usar prefixo `V00X__`
- Se o projeto usa Liquibase, adaptar para formato XML/YAML
- Se não usa ferramenta de migração, executar script manualmente
- **ATENÇÃO:** Ajustar o hash BCrypt da senha! O exemplo acima é ilustrativo

**Gerar hash BCrypt correto:**
```java
// Executar este código Java para gerar hash:
BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);
String hash = encoder.encode("Admin123@");
System.out.println(hash);
```

**Critério de aceite:**
- [ ] Script de migração cria coluna `is_admin`
- [ ] Script de seed cria usuário admin com email `admin@jakebooks.com`
- [ ] Senha do admin está criptografada com BCrypt (força 12)
- [ ] Admin tem `is_admin = TRUE`
- [ ] Admin tem endereços de cobrança e entrega (RN0021, RN0022)

---

### TAREFA BR-05: Validar autorização em Services críticos

**Arquivos afetados:**
- `ClienteService.java`
- `LivroService.java`
- `EstoqueService.java`
- `TrocaService.java`
- `PedidoService.java`

**Ação:**
Adicionar validações de autorização em operações sensíveis que requerem privilégio de admin.

**Exemplo - ClienteService.inativar():**
```java
package com.les.jakebooks.service;

import com.les.jakebooks.util.SecurityUtil;
import com.les.jakebooks.exception.ValidacaoNegocioException;

public void inativar(String codigo) {
    // Validar autorização: apenas admin pode inativar outros clientes
    if (!SecurityUtil.isAdmin()) {
        throw new ValidacaoNegocioException(
            "Acesso negado. Apenas administradores podem inativar clientes."
        );
    }

    // ... resto da lógica ...
}
```

**Métodos que DEVEM ter validação de admin:**

**ClienteService:**
- `inativar(String codigo)` - apenas admin pode inativar outros clientes
- `listarTodos()` - apenas admin pode listar todos os clientes

**LivroService:**
- `inativar(String codigo, String motivo)` - apenas admin (RN0015)
- `ativar(String codigo, String justificativa)` - apenas admin (RN0017)
- `autorizarReducaoPreco(...)` - apenas admin (RN0014)

**EstoqueService:**
- `registrarEntrada(...)` - apenas admin (RF0051)

**TrocaService:**
- `listarTodas()` - apenas admin (RF0042)
- `autorizarTroca(Long trocaId)` - apenas admin (RF0041)
- `confirmarRecebimento(Long trocaId)` - apenas admin (RF0043)

**PedidoService:**
- `despachar(Long pedidoId)` - apenas admin (RF0038)
- `confirmarEntrega(Long pedidoId)` - apenas admin (RF0039)
- `listarTodos()` - apenas admin

**IMPORTANTE:**
- Usar `SecurityUtil.isAdmin()` para verificar
- Lançar `ValidacaoNegocioException` com mensagem clara
- Fazer validação NO INÍCIO do método, antes de qualquer lógica

**Critério de aceite:**
- [ ] Todos os métodos listados acima validam `SecurityUtil.isAdmin()`
- [ ] Métodos lançam exceção apropriada quando não autorizado
- [ ] Cliente comum NÃO consegue executar operações de admin

---

## 📝 OBSERVAÇÕES IMPORTANTES

### Justificativa Técnica

O campo `isAdmin` **NÃO** está presente no modelo de domínio especificado em `AGENTS.md`. Essa é uma **extensão técnica necessária** para implementar os seguintes Requisitos Funcionais:

- **RF0038**: Despachar produtos (admin)
- **RF0039**: Confirmar entrega (admin)
- **RF0041**: Autorizar troca (admin)
- **RF0042**: Visualizar trocas (admin)
- **RF0051**: Entrada em estoque (admin)
- **RF0055**: Analisar histórico (admin)

Sem um mecanismo de distinguir administradores de clientes comuns, é **impossível** implementar esses requisitos conforme especificado.

### Documentação

Adicionar comentário JavaDoc na classe `Cliente`:

```java
/**
 * Campo técnico para controle de autorização.
 *
 * Este campo NÃO faz parte do modelo de domínio original, mas é uma
 * extensão técnica necessária para implementar requisitos funcionais
 * que especificam operações exclusivas de administradores:
 * - RF0038, RF0039, RF0041, RF0042, RF0051, RF0055
 *
 * Valor padrão: false (cliente comum)
 * Valor true: concede privilégios administrativos (ROLE_ADMIN)
 */
private Boolean isAdmin = false;
```

---

## 🧪 TESTES DE VALIDAÇÃO

Após implementar todas as tarefas, executar os seguintes testes:

### Teste 1: Criação de usuário admin
```bash
# Executar script SQL de seed
# Verificar no banco que existe cliente com is_admin = true
SELECT * FROM cliente WHERE email = 'admin@jakebooks.com';
```

### Teste 2: Login como admin
1. Iniciar aplicação
2. Acessar `/login`
3. Fazer login com `admin@jakebooks.com` / `Admin@2024`
4. Verificar que autenticação foi bem-sucedida

### Teste 3: Roles do admin
```java
// Debug no CustomUserDetailsService.loadUserByUsername()
// Verificar que authorities contém:
// - ROLE_CLIENTE
// - ROLE_ADMIN
```

### Teste 4: SecurityUtil
```java
// Em qualquer controller após login como admin:
boolean isAdmin = SecurityUtil.isAdmin(); // deve retornar true
String email = SecurityUtil.getEmailUsuarioLogado(); // deve retornar "admin@jakebooks.com"
```

### Teste 5: Validação de service
1. Fazer login como cliente comum
2. Tentar chamar `clienteService.inativar("CLIENTE_QUALQUER")`
3. Verificar que lança `ValidacaoNegocioException`

---

## ✅ CHECKLIST FINAL - BACKEND AGENT

Antes de considerar as tarefas concluídas, verificar:

- [ ] **BR-01**: Campo `isAdmin` adicionado na entidade `Cliente`
- [ ] **BR-01**: Getters/setters criados
- [ ] **BR-01**: Construtores atualizados
- [ ] **BR-02**: CustomUserDetailsService atribui ROLE_ADMIN quando isAdmin = true
- [ ] **BR-02**: CustomUserDetailsService sempre atribui ROLE_CLIENTE
- [ ] **BR-03**: Classe `SecurityUtil` criada com métodos estáticos
- [ ] **BR-03**: Método `isAdmin()` funcional
- [ ] **BR-03**: Método `getEmailUsuarioLogado()` funcional
- [ ] **BR-04**: Script de migração cria coluna `is_admin`
- [ ] **BR-04**: Script de seed cria usuário admin inicial
- [ ] **BR-04**: Senha do admin está criptografada (BCrypt força 12)
- [ ] **BR-05**: Services críticos validam `SecurityUtil.isAdmin()`
- [ ] **BR-05**: Exceções apropriadas lançadas quando não autorizado
- [ ] Todos os testes de validação passando
- [ ] Documentação JavaDoc explicando campo `isAdmin`

---

## 📚 REFERÊNCIAS

- **AGENTS.md**: Especificação completa do sistema
- **review-agent.md**: Checklist de revisão e validação
- **SecurityConfig.java**: Configuração de segurança (já está correto)
- Spring Security Documentation: https://docs.spring.io/spring-security/reference/
