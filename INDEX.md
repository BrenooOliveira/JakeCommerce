# 📑 Índice de Documentação Ativa

**Última atualização:** 2026-04-30  
**Status:** ✨ Sanitizada (legado separado)

---

## 🎯 Por Onde Começar

### Para Novos Desenvolvedores
1. **[CLAUDE.md](CLAUDE.md)** - Arquitetura, convenções e regras obrigatórias
2. **[README.md](README.md)** - Visão geral do projeto
3. **[docs/guides/FRONTEND-GUIDE.md](docs/guides/FRONTEND-GUIDE.md)** - Para frontend
4. **[docs/guides/GUIA-IMPLEMENTACAO-ADMIN.md](docs/guides/GUIA-IMPLEMENTACAO-ADMIN.md)** - Para features admin

### Para Revisor de Código
1. **[docs/business-rules/QUICK-REFERENCE.md](docs/business-rules/QUICK-REFERENCE.md)** - Validações rápidas
2. **[docs/reports/](docs/reports/)** - Status atual e conformidade
3. **[CLAUDE.md](CLAUDE.md)** - Architecture rules

### Para Product Owner / Stakeholders
1. **[general/requisitoss_copilot.md](general/requisitoss_copilot.md)** - Requisitos atuais
2. **[docs/reports/SUMARIO-EXECUTIVO.md](docs/reports/SUMARIO-EXECUTIVO.md)** - Sumário de status

---

## 📂 Estrutura de Documentação

### 🏗️ **Arquitetura & Padrões**
- **[CLAUDE.md](CLAUDE.md)** ⭐ - Fonte de verdade para arquitetura, camadas, convenções
- **[CONTRIBUTING.md](CONTRIBUTING.md)** - Diretrizes de contribuição
- **[README.md](README.md)** - Visão geral e quick start

### 📋 **Requisitos & Especificações**
- **[general/requisitoss_copilot.md](general/requisitoss_copilot.md)** - Requisitos para agentes
- **[general/requisitos.md](general/requisitos.md)** - Requisitos técnicos detalhados
- **[general/diagrams/](general/diagrams/)** - Diagramas de domínio

### 🤖 **Agentes Especializados**
- **[.claude/agents/](/:/.claude/agents/)** - Definições dos agentes:
  - `backend-agent.md` - Desenvolvimento Spring Boot
  - `frontend-agent.md` - Desenvolvimento frontend
  - `payment-agent.md` - Módulo de pagamentos
  - `shipping-agent.md` - Módulo de frete/entrega
  - `checkout-agent.md` - Fluxo de checkout
  - `business-rules-agent.md` - Validações de negócio
  - `review-agent.md` - Revisão de código
  - `unit-test-developer.md` - Desenvolvimento de testes
  - E outros agentes especializados...

- **[docs/agents/INDEX.md](docs/agents/INDEX.md)** - Índice e guia de uso dos agentes

### 📏 **Regras de Negócio**
- **[docs/business-rules/BUSINESS-RULES-GUIDE.md](docs/business-rules/BUSINESS-RULES-GUIDE.md)** - Guia completo
- **[docs/business-rules/QUICK-REFERENCE.md](docs/business-rules/QUICK-REFERENCE.md)** - Consulta rápida por RN
- **[docs/business-rules/BUSINESS-RULES-EXAMPLES.md](docs/business-rules/BUSINESS-RULES-EXAMPLES.md)** - Exemplos práticos
- **[docs/business-rules/EXCECOES-REFERENCE.md](docs/business-rules/EXCECOES-REFERENCE.md)** - Exceções customizadas
- **[docs/business-rules/HIERARQUIA-EXCECOES.md](docs/business-rules/HIERARQUIA-EXCECOES.md)** - Hierarquia de exceções

### ✅ **Tarefas & Features**
- **[docs/tasks/](docs/tasks/)** - Tarefas delegadas aos agentes:
  - `TAREFA-BR-*.md` - Tarefas de regras de negócio
  - `TAREFA-BACKEND-ADMIN.md` - Backend admin
  - `TAREFA-FRONTEND-ADMIN.md` - Frontend admin
  - Outras tarefas ativas...

### 📊 **Relatórios & Validação**
- **[docs/reports/SUMARIO-EXECUTIVO.md](docs/reports/SUMARIO-EXECUTIVO.md)** - Sumário para stakeholders
- **[docs/reports/RELATORIO-FINAL-REVIEW.md](docs/reports/RELATORIO-FINAL-REVIEW.md)** - Análise final
- **[docs/reports/ANALISE-VALIDACAO-R05.md](docs/reports/ANALISE-VALIDACAO-R05.md)** - Validação de módulos
- **[docs/reports/](docs/reports/)** - Outros relatórios de conformidade

### 📖 **Guias de Implementação**
- **[docs/guides/FRONTEND-GUIDE.md](docs/guides/FRONTEND-GUIDE.md)** - Padrões frontend
- **[docs/guides/GUIA-IMPLEMENTACAO-ADMIN.md](docs/guides/GUIA-IMPLEMENTACAO-ADMIN.md)** - Implementação admin
- **[docs/guides/EXEMPLO-USO-REVIEW-AGENT.md](docs/guides/EXEMPLO-USO-REVIEW-AGENT.md)** - Como usar review-agent

### 📝 **Documentação Geral**
- **[docs/general/INDICE-DOCUMENTACAO.md](docs/general/INDICE-DOCUMENTACAO.md)** - Índice anterior (manter para referência)
- **[docs/general/CHANGELOG.md](docs/general/CHANGELOG.md)** - Histórico de mudanças

### 🧪 **Testes & QA**
- **[jakebooks/src/test/README_E2E_TESTS.md](jakebooks/src/test/README_E2E_TESTS.md)** - Testes end-to-end
- Selenium test suite em `jakebooks/src/test/selenium/`

---

## 🗂️ Documentação Legada

Documentação obsoleta ou de fases anteriores está em **[legado/](legado/README.md)**:
- Entregas passadas (entrega_04, entrega_05)
- Especificações antigas (AGENTS-deprecated.md)
- Fases anteriores (DVP, apresentações)

**Consulte legado apenas para** contexto histórico, não para implementação atual.

---

## 🔍 Busca Rápida por Tipo

| Tipo | Documento |
|------|-----------|
| **Validação RN0033** | `docs/business-rules/QUICK-REFERENCE.md` |
| **Exceção customizada** | `docs/business-rules/EXCECOES-REFERENCE.md` |
| **Exemplo de implementação** | `docs/business-rules/BUSINESS-RULES-EXAMPLES.md` |
| **Padrão Frontend** | `docs/guides/FRONTEND-GUIDE.md` |
| **Status do projeto** | `docs/reports/SUMARIO-EXECUTIVO.md` |
| **Como usar Agent X** | `docs/agents/INDEX.md` |
| **Requisitos completos** | `general/requisitoss_copilot.md` |
| **Testes E2E** | `jakebooks/src/test/README_E2E_TESTS.md` |

---

## 📚 Referência Rápida

### Arquitetura
```
Controller → Service → Repository → Entity
          → DTO ← (transfer)
          → Validator (business rules)
```

### Pacotes Obrigatórios
```
com.les.jakebooks.
├── domain/          (Entities, Enums)
├── repository/      (JPA interfaces)
├── service/         (Business logic, @Transactional)
├── controller/      (HTTP endpoints, zero business logic)
├── dto/             (Data transfer objects)
├── config/          (Spring configuration)
├── exception/       (Custom exceptions)
├── validator/       (Business rule validators)
└── util/            (Utilities)
```

### Status Enums (Never Create Others)
- `StatusLivro`: ATIVO, INATIVO
- `StatusCliente`: ATIVO, INATIVO, BLOQUEADO
- `StatusPedido`: EM_PROCESSAMENTO, EM_TRANSPORTE, ENTREGUE, EM_TROCA, TROCADO
- `StatusPagamento`: PENDENTE, APROVADA, REPROVADA
- `StatusCarrinho`: ABERTO, EXPIRADO, FINALIZADO
- `StatusTroca`: SOLICITADA, AUTORIZADA, RECEBIDA, CONCLUIDA

---

## 🚀 Workflow Típico

### Adding a New Feature
1. Ler requisito em `general/requisitoss_copilot.md`
2. Consultar `docs/business-rules/` para validações
3. Verificar `docs/guides/` para padrões
4. Implementar respeitando `CLAUDE.md`
5. Submeter para review, verificar `docs/reports/`

### Debugging a Business Rule
1. Procurar em `docs/business-rules/QUICK-REFERENCE.md`
2. Ver exemplo em `docs/business-rules/BUSINESS-RULES-EXAMPLES.md`
3. Verificar exceção em `docs/business-rules/EXCECOES-REFERENCE.md`

### Asking an Agent to Work
1. Consultar `docs/agents/INDEX.md`
2. Ler requisito em `docs/tasks/`
3. Delegar informações completas ao agent

---

**🔗 Documentação relacionada:** [docs/README.md](docs/README.md) | [CLAUDE.md](CLAUDE.md) | [legado/README.md](legado/README.md)
