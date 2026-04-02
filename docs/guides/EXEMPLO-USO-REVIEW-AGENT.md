# 🔍 Exemplo de Uso - Review Agent

## 📖 Como Usar o Review Agent Configurado

O **review-agent** agora está configurado para revisar automaticamente a separação cliente X administrador. Aqui estão exemplos práticos de uso:

---

## 🎯 Cenário 1: Revisar Código Existente

### Comando:
```bash
# Via Claude Code ou CLI
@review-agent Revise o código atual e verifique a separação cliente X administrador
```

### O que o agent fará:
1. ✅ Verificar se entidade Cliente tem campo `isAdmin`
2. ✅ Verificar `CustomUserDetailsService` atribui roles corretamente
3. ✅ Verificar `SecurityConfig` protege rotas admin
4. ✅ Verificar controllers usam `@PreAuthorize` onde necessário
5. ✅ Verificar se `isAdmin` é passado corretamente ao model
6. ✅ Gerar relatório com problemas encontrados

### Saída esperada:
```markdown
# Relatório de Revisão - Separação Cliente X Administrador

## ❌ Problemas Encontrados

1. **Cliente.java:28** - Campo isAdmin não existe
2. **CustomUserDetailsService.java:72** - TODO não implementado, apenas ROLE_CLIENTE atribuída
3. **LivroController.java:82** - isAdmin hardcoded como false
4. **LivroController.java:267** - Método inativar() sem @PreAuthorize
5. **EstoqueController.java** - Nenhum método protegido com @PreAuthorize

## 🔧 Correções Necessárias

### Alta Prioridade:
- [ ] Adicionar campo isAdmin em Cliente.java
- [ ] Implementar lógica de ROLE_ADMIN em CustomUserDetailsService.java
- [ ] Criar classe SecurityUtil.java

### Média Prioridade:
- [ ] Adicionar @PreAuthorize em 15 métodos de controllers
- [ ] Substituir isAdmin hardcoded por SecurityUtil.isAdmin() em 8 controllers

### Baixa Prioridade:
- [ ] Adicionar validações extras em services
- [ ] Atualizar views para condicionar elementos por isAdmin

## 📋 Checklist de Implementação

Siga o guia: `GUIA-IMPLEMENTACAO-ADMIN.md`
```

---

## 🎯 Cenário 2: Revisar Após Implementar Mudanças

### Comando:
```bash
@review-agent Implementei as mudanças sugeridas. Revise novamente e confirme se está correto.
```

### O que o agent fará:
1. ✅ Re-executar todos os checks
2. ✅ Validar se campo isAdmin foi adicionado corretamente
3. ✅ Validar se SecurityUtil existe e é usado
4. ✅ Validar se @PreAuthorize está em todos os métodos admin
5. ✅ Marcar items do checklist como completos
6. ✅ Sugerir testes de validação

### Saída esperada:
```markdown
# Relatório de Revisão - Separação Cliente X Administrador

## ✅ Implementações Corretas

1. **Cliente.java:55** - Campo isAdmin adicionado corretamente
2. **CustomUserDetailsService.java:72-75** - ROLE_ADMIN atribuída quando isAdmin=true
3. **SecurityUtil.java** - Classe criada com métodos isAdmin() e getEmailUsuarioLogado()
4. **LivroController.java:267** - @PreAuthorize("hasRole('ADMIN')") presente
5. **EstoqueController.java** - Todos os métodos protegidos

## ✅ Checklist de Implementação

- [x] Passo 1: Adicionar campo isAdmin ✓
- [x] Passo 2: Criar migration ✓
- [x] Passo 3: Atualizar CustomUserDetailsService ✓
- [x] Passo 4: Criar SecurityUtil ✓
- [x] Passo 5: Habilitar @PreAuthorize ✓
- [x] Passo 6: Atualizar Controllers ✓
- [ ] Passo 7: Atualizar Views (opcional)
- [ ] Passo 8: Validações em Services (opcional)

## 🧪 Testes Recomendados

Execute os testes manuais descritos em `GUIA-IMPLEMENTACAO-ADMIN.md`:

### Teste 1: Login como Admin
```bash
# Acesse: http://localhost:8080/login
Email: admin@jakebooks.com
Senha: Admin@123

Validações:
- [ ] Sidebar aparece
- [ ] Menu Admin visível
- [ ] Acesso a /estoque funciona
- [ ] Acesso a /analise funciona
```

### Teste 2: Login como Cliente
```bash
# Acesse: http://localhost:8080/login
Email: cliente@test.com
Senha: Cliente@123

Validações:
- [ ] Sidebar NÃO aparece
- [ ] Menu Admin NÃO visível
- [ ] Acesso a /estoque retorna 403
- [ ] Acesso a /carrinho funciona
```

### Teste 3: Endpoints Protegidos
```bash
# Sem autenticação
curl http://localhost:8080/estoque
# Esperado: 302 redirect para /login

# Com cliente
curl -u cliente@test.com:senha http://localhost:8080/estoque
# Esperado: 403 Forbidden

# Com admin
curl -u admin@test.com:Admin@123 http://localhost:8080/estoque
# Esperado: 200 OK
```

## ⚡ Próximos Passos

1. Executar testes manuais listados acima
2. Criar testes automatizados de segurança
3. Documentar credenciais de admin no README
```

---

## 🎯 Cenário 3: Validar Controller Específico

### Comando:
```bash
@review-agent Revise apenas o LivroController.java e verifique se está conforme as regras de separação admin
```

### O que o agent fará:
1. ✅ Focar análise no LivroController.java
2. ✅ Verificar cada método e suas annotations
3. ✅ Verificar uso de SecurityUtil.isAdmin()
4. ✅ Validar quais métodos devem ter @PreAuthorize
5. ✅ Sugerir correções específicas

### Saída esperada:
```markdown
# Revisão - LivroController.java

## Métodos Públicos (OK - sem proteção necessária)
✅ listar() - GET /livros
✅ detalhe() - GET /livros/{codigo}

## Métodos Autenticados (OK - .authenticated())
✅ formularioNovo() - GET /livros/novo (se for para cliente cadastrar wishlist)

## ❌ Métodos Admin SEM @PreAuthorize (CORRIGIR)
❌ inativar() - POST /livros/{codigo}/inativar
❌ ativar() - POST /livros/{codigo}/ativar
❌ criar() - POST /livros
❌ atualizar() - POST /livros/{codigo}

## ❌ Uso de isAdmin Hardcoded (CORRIGIR)
❌ Linha 82: model.addAttribute("isAdmin", false);
❌ Linha 101: model.addAttribute("isAdmin", false);
❌ Linha 159: model.addAttribute("isAdmin", false);
❌ Linha 209: model.addAttribute("isAdmin", false);

## 🔧 Correções Sugeridas

### 1. Adicionar @PreAuthorize
```java
@PreAuthorize("hasRole('ADMIN')")
@PostMapping("/{codigo}/inativar")
public String inativar(...) { ... }

@PreAuthorize("hasRole('ADMIN')")
@PostMapping("/{codigo}/ativar")
public String ativar(...) { ... }

@PreAuthorize("hasRole('ADMIN')")
@PostMapping
public String criar(...) { ... }

@PreAuthorize("hasRole('ADMIN')")
@PostMapping("/{codigo}")
public String atualizar(...) { ... }
```

### 2. Substituir isAdmin hardcoded
```java
// ANTES:
model.addAttribute("isAdmin", false);

// DEPOIS:
model.addAttribute("isAdmin", SecurityUtil.isAdmin());
```

Aplicar em linhas: 82, 101, 159, 209
```

---

## 🎯 Cenário 4: Revisão de Segurança Completa

### Comando:
```bash
@review-agent Execute uma revisão de segurança completa do sistema, focando em cliente X admin
```

### O que o agent fará:
1. ✅ Verificar TODOS os controllers
2. ✅ Verificar TODOS os services
3. ✅ Verificar SecurityConfig
4. ✅ Verificar CustomUserDetailsService
5. ✅ Verificar entidades relacionadas
6. ✅ Verificar routes e endpoints expostos
7. ✅ Gerar relatório completo com matriz de acesso

### Saída esperada:
```markdown
# Relatório de Segurança Completo

## 📊 Matriz de Acesso - Endpoints

| Endpoint | Método | Público | Cliente | Admin | Status |
|----------|--------|---------|---------|-------|--------|
| `/` | GET | ✅ | ✅ | ✅ | ✅ OK |
| `/livros` | GET | ✅ | ✅ | ✅ | ✅ OK |
| `/livros/{id}` | GET | ✅ | ✅ | ✅ | ✅ OK |
| `/livros/novo` | GET | ❌ | ❌ | ✅ | ⚠️ Verificar |
| `/livros` | POST | ❌ | ❌ | ✅ | ❌ SEM @PreAuthorize |
| `/carrinho/**` | * | ❌ | ✅ | ✅ | ✅ OK |
| `/pedidos` | GET | ❌ | ✅ próprios | ✅ todos | ⚠️ Validar service |
| `/estoque/**` | * | ❌ | ❌ | ✅ | ✅ OK |
| `/analise/**` | * | ❌ | ❌ | ✅ | ✅ OK |
| `/trocas/solicitar` | GET | ❌ | ✅ | ✅ | ✅ OK |
| `/trocas/**` | * | ❌ | ❌ | ✅ | ✅ OK |

## 🔐 Análise de Segurança por Camada

### Camada 1: SecurityConfig (Firewall)
✅ Rotas públicas corretas
✅ Rotas admin protegidas com hasRole("ADMIN")
✅ CSRF habilitado
✅ Autenticação configurada

### Camada 2: Controllers (@PreAuthorize)
❌ 12 métodos admin SEM @PreAuthorize
⚠️ 8 controllers com isAdmin hardcoded

### Camada 3: Services (Validações)
⚠️ Validações de negócio presentes mas sem verificação de role
⚠️ Métodos sensíveis não validam SecurityUtil.isAdmin()

### Camada 4: Views (UI)
⚠️ Elementos admin condicionais mas isAdmin sempre false
⚠️ Sidebar nunca exibida

## 🚨 Vulnerabilidades Identificadas

### CRÍTICAS (Corrigir Imediatamente)
1. **Escalação de Privilégios**: Clientes podem criar livros via POST direto
2. **Falta @PreAuthorize**: 12 endpoints admin sem proteção método-level

### ALTAS (Corrigir em Breve)
3. **isAdmin Hardcoded**: Interface admin nunca exibida
4. **Falta ROLE_ADMIN**: Nenhum usuário pode ser admin

### MÉDIAS (Corrigir Eventualmente)
5. **Services sem Validação**: Segunda camada de defesa ausente
6. **Log de Auditoria**: Operações admin não rastreadas

## 📋 Plano de Ação

**Fase 1 - Crítico (1 dia)**
- [ ] Implementar campo isAdmin
- [ ] Atribuir ROLE_ADMIN em UserDetailsService
- [ ] Adicionar @PreAuthorize em todos métodos admin

**Fase 2 - Alto (2 dias)**
- [ ] Criar SecurityUtil
- [ ] Substituir isAdmin hardcoded
- [ ] Testar acesso admin e cliente

**Fase 3 - Médio (3 dias)**
- [ ] Adicionar validações em services
- [ ] Implementar auditoria
- [ ] Testes automatizados de segurança
```

---

## 🔄 Ciclo de Revisão Contínua

### Uso Recomendado:

1. **Antes de commit**: Revisar mudanças
```bash
@review-agent Revise as mudanças que fiz e valide conformidade admin
```

2. **Após merge**: Revisar integração
```bash
@review-agent Revise a branch main após merge
```

3. **Antes de deploy**: Revisão completa
```bash
@review-agent Revisão de segurança completa antes de deploy
```

4. **Periódica**: Auditoria mensal
```bash
@review-agent Auditoria de segurança mensal - foco em cliente X admin
```

---

## 💡 Dicas de Uso

### ✅ Boas Práticas
- Use o agent após cada funcionalidade implementada
- Solicite revisões específicas por arquivo quando necessário
- Implemente as correções sugeridas antes de seguir em frente
- Execute os testes recomendados pelo agent

### ❌ Evite
- Acumular muitas mudanças antes de revisar
- Ignorar warnings de média prioridade repetidamente
- Fazer deploy sem executar revisão de segurança
- Modificar código sem re-executar review após mudanças

---

## 📚 Documentos Relacionados

- `review-agent.md` - Configuração completa do agent
- `GUIA-IMPLEMENTACAO-ADMIN.md` - Passo-a-passo de implementação
- `requisitoss_copilot.md` - Requisitos funcionais

---

## 🎯 Resultado Esperado

Após usar o review-agent configurado:
- ✅ Identificação automática de problemas de segurança
- ✅ Checklist claro de correções
- ✅ Validação contínua de conformidade
- ✅ Documentação de gaps e vulnerabilidades
- ✅ Sistema seguro com separação cliente X admin funcionando
