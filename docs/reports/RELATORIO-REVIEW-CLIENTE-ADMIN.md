# Relatório de Revisão - Separação Cliente X Administrador

**Data:** 2026-03-18
**Revisado por:** Review Agent
**Projeto:** JakeCommerce - Sistema de E-commerce para Venda de Livros
**Objetivo:** Validar implementação de controle de acesso granular entre Cliente e Administrador

---

## 📊 RESUMO EXECUTIVO

A revisão identificou que o sistema **NÃO possui implementação completa** da separação Cliente x Administrador. Embora a infraestrutura de segurança (SecurityConfig) esteja corretamente configurada, faltam componentes críticos de backend e frontend.

**Status:** 🔴 **IMPLEMENTAÇÃO INCOMPLETA - REQUER AÇÃO IMEDIATA**

**Impacto:**
- Cliente comum pode executar operações administrativas
- Interface não diferencia perfis de usuário
- Requisitos funcionais RF0038, RF0039, RF0041, RF0042, RF0051, RF0055 não podem ser implementados

---

## ✅ IMPLEMENTAÇÕES CORRETAS

### 1. SecurityConfig (jakebooks/src/main/java/config/SecurityConfig.java)

**Status:** ✅ **CORRETO**

O arquivo de configuração de segurança está **bem implementado**:

- **Rotas públicas** corretamente definidas:
  - `/`, `/livros`, `/login`, `/clientes/novo`, recursos estáticos
  - Permitem acesso sem autenticação ✅

- **Rotas autenticadas** para clientes:
  - `/carrinho/**`, `/pedidos/**`, `/trocas/solicitar`, `/clientes/perfil`
  - Requerem autenticação mas não verificam role específico ✅

- **Rotas administrativas** protegidas:
  - `/admin/**`, `/estoque/**`, `/analise/**`, `/trocas/**`
  - Protegidas com `.hasRole("ADMIN")` ✅

- **Configuração de login**:
  - Formulário customizado com email/senha
  - BCryptPasswordEncoder com força 12 ✅

**Conclusão:** SecurityConfig está implementado conforme especificação.

### 2. Estrutura de Domínio

**Status:** ✅ **COMPLETO (exceto campo isAdmin)**

Todas as entidades do modelo de domínio especificado em AGENTS.md estão implementadas:
- Cliente, Endereco, Cartao
- Livro, Autor, Editora, Categoria
- Estoque, GrupoPrecificacao
- Carrinho, ItemCarrinho
- Pedido, ItemPedido
- Pagamento, PagamentoCartao, PagamentoCupom
- Cupom, Troca

**Único gap:** Campo `isAdmin` não existe na entidade Cliente (detalhado abaixo).

---

## ❌ PROBLEMAS ENCONTRADOS

### CRÍTICO #1: Entidade Cliente sem campo isAdmin

**Arquivo:** `jakebooks/src/main/java/com/les/jakebooks/domain/Cliente.java`
**Severidade:** 🔴 **CRÍTICO**
**Status:** ❌ **NÃO IMPLEMENTADO**

**Problema:**
- A entidade `Cliente` NÃO possui campo para identificar administradores
- Não há como distinguir cliente comum de administrador no nível de dados
- Impossível atribuir role ROLE_ADMIN durante autenticação

**Impacto:**
- CustomUserDetailsService não consegue determinar se usuário é admin
- Todos os usuários recebem apenas ROLE_CLIENTE
- Proteções de rota com `.hasRole("ADMIN")` NUNCA funcionarão
- RFs 038, 039, 041, 042, 051, 055 **não podem ser implementados**

**Evidência:**
```java
// Cliente.java (linha 28-184)
@Entity
@Table(name = "cliente")
public class Cliente {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String codigo;
    private String nome;
    // ... outros campos do modelo de domínio ...

    // ❌ NÃO EXISTE: private Boolean isAdmin;
}
```

**Solução:** TAREFA BR-01 (documento TAREFA-BACKEND-ADMIN.md)

---

### CRÍTICO #2: CustomUserDetailsService não implementa lógica de admin

**Arquivo:** `jakebooks/src/main/java/com/les/jakebooks/config/CustomUserDetailsService.java:71`
**Severidade:** 🔴 **CRÍTICO**
**Status:** ❌ **TODO NÃO RESOLVIDO**

**Problema:**
- Linha 71: `// TODO: Implementar lógica para detectar admin`
- Sempre atribui apenas `ROLE_CLIENTE` (linha 69)
- NUNCA atribui `ROLE_ADMIN`

**Impacto:**
- Mesmo se existisse usuário admin no banco, não receberia role adequado
- SecurityConfig protege rotas admin, mas ninguém tem ROLE_ADMIN
- **Acesso a todas as rotas administrativas bloqueado para TODOS os usuários**

**Evidência:**
```java
// CustomUserDetailsService.java (linhas 67-72)
// Constrói lista de autoridades/roles do cliente
List<GrantedAuthority> authorities = new ArrayList<>();
authorities.add(new SimpleGrantedAuthority("ROLE_CLIENTE"));

// TODO: Implementar lógica para detectar admin (pode ser um campo na entidade)
// Por exemplo: if (cliente.isAdmin()) { authorities.add(...); }
```

**Solução:** TAREFA BR-02 (documento TAREFA-BACKEND-ADMIN.md)

---

### ALTO #3: SecurityUtil não existe

**Arquivo esperado:** `jakebooks/src/main/java/com/les/jakebooks/util/SecurityUtil.java`
**Severidade:** 🟠 **ALTO**
**Status:** ❌ **NÃO IMPLEMENTADO**

**Problema:**
- Não existe classe utilitária para verificar autorização
- Controllers precisarão hardcode `isAdmin = false` ou duplicar lógica

**Impacto:**
- Controllers não conseguem adicionar atributo `isAdmin` no Model
- Templates não conseguem renderizar condicionalmente elementos admin
- Risco de código duplicado e inconsistente

**Solução:** TAREFA BR-03 (documento TAREFA-BACKEND-ADMIN.md)

---

### ALTO #4: Banco de dados sem coluna is_admin

**Arquivos ausentes:**
- `jakebooks/src/main/resources/db/migration/V00X__add_is_admin_column.sql`
- `jakebooks/src/main/resources/db/seed/admin_seed.sql`

**Severidade:** 🟠 **ALTO**
**Status:** ❌ **NÃO IMPLEMENTADO**

**Problema:**
- Banco de dados não possui coluna `is_admin` na tabela `cliente`
- Não existe usuário administrador inicial cadastrado
- Impossível testar funcionalidades de admin

**Impacto:**
- Mesmo implementando campo isAdmin na entidade, banco não suporta
- Não há como criar usuário admin inicial
- **Sistema não pode ser testado com perfil de administrador**

**Solução:** TAREFA BR-04 (documento TAREFA-BACKEND-ADMIN.md)

---

### MÉDIO #5: Services não validam autorização

**Arquivos afetados:**
- `ClienteService.java`
- `LivroService.java`
- `EstoqueService.java`
- `TrocaService.java`
- `PedidoService.java`

**Severidade:** 🟡 **MÉDIO**
**Status:** ⚠️ **PARCIALMENTE IMPLEMENTADO**

**Problema:**
- Services não validam se operação requer privilégio de admin
- Validação de autorização delegada apenas ao SecurityConfig (rotas)
- Falta defense-in-depth (defesa em profundidade)

**Impacto Reduzido:**
- SecurityConfig já protege rotas, mitigando risco parcialmente
- Porém, falta validação em nível de negócio conforme boas práticas

**Recomendação:** TAREFA BR-05 (documento TAREFA-BACKEND-ADMIN.md)

---

### MÉDIO #6: Controllers sem @PreAuthorize

**Arquivos afetados:**
- `LivroController.java`
- `ClienteController.java`
- `EstoqueController.java`
- `PedidoController.java`
- `TrocaController.java`
- `AnaliseController.java`

**Severidade:** 🟡 **MÉDIO**
**Status:** ❌ **NÃO IMPLEMENTADO**

**Problema:**
- Métodos administrativos não têm annotation `@PreAuthorize("hasRole('ADMIN')")`
- Validação delegada apenas ao SecurityConfig (proteção de URL)
- Falta validação method-level (camada adicional de segurança)

**Impacto Reduzido:**
- SecurityConfig já protege rotas, mas sem defense-in-depth
- Vulnerável a bypass via URL manipulation em casos edge

**Solução:** TAREFA FR-01 (documento TAREFA-FRONTEND-ADMIN.md)

---

### MÉDIO #7: Controllers não adicionam isAdmin no Model

**Arquivos afetados:** Todos os controllers

**Severidade:** 🟡 **MÉDIO**
**Status:** ❌ **NÃO IMPLEMENTADO**

**Problema:**
- Controllers não adicionam `model.addAttribute("isAdmin", ...)`
- Templates não podem renderizar condicionalmente elementos admin

**Impacto:**
- Sidebar administrativa aparece para todos os usuários
- Botões de ações admin visíveis para cliente comum
- **Experiência de usuário ruim e confusa**

**Solução:** TAREFA FR-02 (documento TAREFA-FRONTEND-ADMIN.md)

---

### BAIXO #8: Templates sem lógica condicional isAdmin

**Arquivos afetados:**
- `templates/fragments/layout.html`
- `templates/fragments/navbar.html`
- `templates/fragments/sidebar.html`
- `templates/livros/lista.html`
- `templates/clientes/lista.html`
- Outros templates

**Severidade:** 🟢 **BAIXO**
**Status:** ❌ **NÃO IMPLEMENTADO**

**Problema:**
- Templates não usam `th:if="${isAdmin == true}"` para diferenciar perfis
- Elementos admin exibidos para todos os usuários
- Sidebar aparece mesmo para cliente comum

**Impacto:**
- Usuário vê botões que não pode usar (clicando neles retorna 403)
- Experiência de usuário degradada

**Solução:** TAREFA FR-03 (documento TAREFA-FRONTEND-ADMIN.md)

---

### BAIXO #9: Página 403 genérica

**Arquivo:** `templates/error/403.html`
**Severidade:** 🟢 **BAIXO**
**Status:** ⚠️ **MELHORÁVEL**

**Problema:**
- Página 403 pode estar genérica ou não customizada

**Impacto:**
- Cliente que acessa rota admin vê erro genérico
- Falta orientação sobre como proceder

**Solução:** TAREFA FR-04 (documento TAREFA-FRONTEND-ADMIN.md)

---

## 🔧 CORREÇÕES APLICADAS

**NENHUMA** correção foi aplicada nesta revisão.

Este relatório identifica os problemas e delega as correções aos agentes especializados:

- **Backend Agent:** Responsável por TAREFA BR-01 a BR-05
- **Frontend Agent:** Responsável por TAREFA FR-01 a FR-04

**Próximos Passos:**
1. Backend Agent deve executar TAREFA-BACKEND-ADMIN.md
2. Frontend Agent deve executar TAREFA-FRONTEND-ADMIN.md (após backend)
3. Review Agent deve validar implementação após conclusão

---

## ⚠️ OBSERVAÇÕES CRÍTICAS

### Sobre o campo isAdmin

O campo `isAdmin` **NÃO está presente no modelo de domínio** especificado em `AGENTS.md`.

**Justificativa Técnica:**

Esta é uma **extensão técnica necessária** para implementar os seguintes Requisitos Funcionais que explicitamente especificam "(admin)":

- **RF0038**: Despachar produtos (EM TRANSPORTE)
- **RF0039**: Confirmar entrega (ENTREGUE)
- **RF0041**: Autorizar troca
- **RF0042**: Visualizar trocas (admin)
- **RF0051**: Entrada em estoque
- **RF0055**: Analisar histórico por período comparando produtos ou categorias

**Conclusão:**

Sem um mecanismo técnico para distinguir administradores de clientes comuns, é **impossível** implementar esses requisitos funcionais conforme especificado.

O campo `isAdmin` deve ser:
- Documentado como **extensão técnica** fora do modelo de negócio
- Justificado pela necessidade de implementar RFs específicos
- Adicionado com valor padrão `false` para não impactar clientes existentes

**Aprovação Necessária:**

Este é um desvio do modelo de domínio documentado. Recomenda-se:
1. Documentar a decisão em `CHANGELOG.md` ou arquivo de decisões arquiteturais
2. Adicionar comentário JavaDoc na entidade justificando a adição
3. Informar stakeholders sobre a extensão técnica

---

## 🧪 TESTES RECOMENDADOS

Após implementação das correções pelos agentes backend e frontend, executar:

### Teste 1: Criação de Admin no Banco de Dados
```sql
-- Verificar que script de seed criou admin
SELECT codigo, nome, email, is_admin, status
FROM cliente
WHERE email = 'admin@jakebooks.com';

-- Deve retornar:
-- codigo: ADMIN001
-- nome: Administrador
-- email: admin@jakebooks.com
-- is_admin: TRUE
-- status: ATIVO
```

**Critério:** ✅ Admin existe com `is_admin = TRUE`

---

### Teste 2: Login como Admin e Verificação de Roles
1. Iniciar aplicação
2. Acessar `/login`
3. Fazer login com `admin@jakebooks.com` / `Admin@2024`
4. Debug no CustomUserDetailsService:
   ```
   authorities: [ROLE_CLIENTE, ROLE_ADMIN]
   ```

**Critério:** ✅ Admin recebe ROLE_ADMIN + ROLE_CLIENTE

---

### Teste 3: Login como Cliente Comum
1. Criar cliente comum (sem isAdmin)
2. Fazer login
3. **Verificar:**
   - [ ] Sidebar NÃO aparece
   - [ ] Navbar NÃO exibe links admin
   - [ ] Acesso a `/admin/pedidos` retorna página 403 personalizada
   - [ ] Acesso a `/estoque` retorna página 403
   - [ ] Listagem de livros NÃO exibe botões "Editar", "Inativar"

**Critério:** ✅ Cliente comum não vê nem acessa funcionalidades admin

---

### Teste 4: Login como Admin
1. Fazer login com `admin@jakebooks.com`
2. **Verificar:**
   - [ ] Sidebar aparece no lado esquerdo
   - [ ] Navbar exibe: "Gerenciar Pedidos", "Estoque", "Trocas", "Análises"
   - [ ] Acesso a `/admin/pedidos` bem-sucedido
   - [ ] Acesso a `/estoque` bem-sucedido
   - [ ] Listagem de livros exibe botões "Editar", "Inativar", "Ativar"
   - [ ] Pode despachar pedidos
   - [ ] Pode autorizar trocas

**Critério:** ✅ Admin tem acesso completo a funcionalidades administrativas

---

### Teste 5: Tentativa de Bypass (Segurança)

Como **cliente comum**, tentar bypass via requisições diretas:

```bash
# POST para inativar livro
curl -X POST http://localhost:8080/livros/LIVRO001/inativar

# POST para inativar cliente
curl -X POST http://localhost:8080/clientes/CLI001/inativar

# POST para autorizar troca
curl -X POST http://localhost:8080/trocas/1/autorizar

# GET para análises
curl http://localhost:8080/analise
```

**Critério:** ✅ Todas as requisições retornam **302 Redirect** ou **403 Forbidden**

---

### Teste 6: SecurityUtil

Em qualquer controller após login como admin:

```java
boolean isAdmin = SecurityUtil.isAdmin(); // deve retornar true
String email = SecurityUtil.getEmailUsuarioLogado(); // "admin@jakebooks.com"
boolean isAuth = SecurityUtil.isAuthenticated(); // true
```

**Critério:** ✅ SecurityUtil funciona corretamente

---

## 📋 CONFORMIDADE COM REQUISITOS

| Requisito | Descrição | Status | Observação |
|-----------|-----------|--------|------------|
| RF0038 | Despachar produtos (admin) | ❌ | Rota protegida mas ninguém tem ROLE_ADMIN |
| RF0039 | Confirmar entrega (admin) | ❌ | Rota protegida mas ninguém tem ROLE_ADMIN |
| RF0041 | Autorizar troca (admin) | ❌ | Rota protegida mas ninguém tem ROLE_ADMIN |
| RF0042 | Visualizar trocas (admin) | ❌ | Rota protegida mas ninguém tem ROLE_ADMIN |
| RF0051 | Entrada em estoque (admin) | ❌ | Rota protegida mas ninguém tem ROLE_ADMIN |
| RF0055 | Analisar histórico (admin) | ❌ | Rota protegida mas ninguém tem ROLE_ADMIN |
| RNF0012 | Log de transações | ⚠️ | Não verificado nesta revisão |

**Conclusão:** Nenhum RF que especifica "(admin)" pode ser implementado no estado atual.

---

## 📊 ESTATÍSTICAS

| Categoria | Quantidade |
|-----------|------------|
| **Problemas Críticos** | 2 |
| **Problemas Altos** | 2 |
| **Problemas Médios** | 3 |
| **Problemas Baixos** | 2 |
| **Total de Issues** | 9 |
| | |
| **Arquivos Afetados** | 15+ |
| **Tarefas Backend** | 5 (BR-01 a BR-05) |
| **Tarefas Frontend** | 4 (FR-01 a FR-04) |

---

## ✅ PLANO DE AÇÃO

### Fase 1: BACKEND (Prioridade Máxima) 🔴

**Responsável:** Backend Agent
**Documento:** `TAREFA-BACKEND-ADMIN.md`

**Tarefas Críticas:**
1. ✅ **BR-01**: Adicionar campo `isAdmin` na entidade Cliente
2. ✅ **BR-02**: Implementar lógica de admin no CustomUserDetailsService
3. ✅ **BR-03**: Criar utility SecurityUtil
4. ✅ **BR-04**: Criar scripts SQL (migração + seed admin)

**Tarefas Recomendadas:**
5. ✅ **BR-05**: Validar autorização em Services críticos

**Estimativa:** Alta prioridade - Bloqueia todo o resto

---

### Fase 2: FRONTEND (Após Backend) 🟠

**Responsável:** Frontend Agent
**Documento:** `TAREFA-FRONTEND-ADMIN.md`
**Dependência:** BR-01, BR-02, BR-03 devem estar concluídos

**Tarefas:**
1. ✅ **FR-01**: Adicionar @PreAuthorize em todos os controllers
2. ✅ **FR-02**: Adicionar `isAdmin` no Model de todos os controllers
3. ✅ **FR-03**: Atualizar templates Thymeleaf (layout, navbar, sidebar, páginas)
4. ✅ **FR-04**: Criar página 403 personalizada

**Estimativa:** Depende de backend completo

---

### Fase 3: VALIDAÇÃO (Final) 🟢

**Responsável:** Review Agent

**Tarefas:**
1. Executar bateria de testes (6 testes descritos acima)
2. Verificar checklist de conformidade
3. Validar contra requisitos funcionais
4. Gerar relatório final de aceite

---

## 📄 DOCUMENTAÇÃO GERADA

Como resultado desta revisão, foram gerados os seguintes documentos:

1. **TAREFA-BACKEND-ADMIN.md**
   - Caminho: `/home/breno-oliveira/Documentos/gitRepositories/JakeCommerce/TAREFA-BACKEND-ADMIN.md`
   - Conteúdo: Tarefas BR-01 a BR-05 detalhadas com código de exemplo
   - Alvo: Backend Agent

2. **TAREFA-FRONTEND-ADMIN.md**
   - Caminho: `/home/breno-oliveira/Documentos/gitRepositories/JakeCommerce/TAREFA-FRONTEND-ADMIN.md`
   - Conteúdo: Tarefas FR-01 a FR-04 detalhadas com código de exemplo
   - Alvo: Frontend Agent

3. **RELATORIO-REVIEW-CLIENTE-ADMIN.md** (este documento)
   - Caminho: `/home/breno-oliveira/Documentos/gitRepositories/JakeCommerce/RELATORIO-REVIEW-CLIENTE-ADMIN.md`
   - Conteúdo: Análise completa, problemas, plano de ação
   - Alvo: Stakeholders, Product Owner, Revisores Técnicos

---

## 🎯 CONCLUSÃO

A revisão identificou que o sistema **JakeCommerce não possui separação funcional entre Cliente e Administrador**, embora a infraestrutura de segurança esteja corretamente configurada.

**Situação Atual:**
- ✅ SecurityConfig protege rotas corretamente
- ❌ Nenhum usuário pode obter ROLE_ADMIN
- ❌ Funcionalidades administrativas inacessíveis
- ❌ Interface não diferencia perfis

**Impacto nos Requisitos:**
- **6 Requisitos Funcionais** (RF0038, RF0039, RF0041, RF0042, RF0051, RF0055) **não podem ser implementados**

**Próximas Etapas:**
1. **Backend Agent** deve implementar TAREFA-BACKEND-ADMIN.md (BR-01 a BR-05)
2. **Frontend Agent** deve implementar TAREFA-FRONTEND-ADMIN.md (FR-01 a FR-04)
3. **Review Agent** deve validar implementação com bateria de testes

**Prazo Recomendado:**
- Alta prioridade devido ao bloqueio de RFs críticos
- Backend e frontend devem trabalhar sequencialmente (backend primeiro)

---

**Aprovado por:** Review Agent
**Data:** 2026-03-18
**Versão do Documento:** 1.0
