# 🔍 Índice Rápido de Documentação

## 📁 Por Categoria

### Business Rules (Regras de Negócio)
```
docs/business-rules/
├── BUSINESS-RULES-GUIDE.md           ⭐ Guia completo
├── BUSINESS-RULES-EXAMPLES.md        💡 Exemplos práticos
├── BUSINESS-RULES-IMPLEMENTATION.md  🔧 Guia de implementação
├── BUSINESS-RULES-SUMMARY.md         📋 Resumo
├── EXCECOES-REFERENCE.md             ⚠️ Referência de exceções
├── HIERARQUIA-EXCECOES.md            🗂️ Hierarquia de exceções
└── QUICK-REFERENCE.md                ⚡ Consulta rápida
```

### Tasks (Tarefas)
```
docs/tasks/
├── TAREFA-BR-02.md                   📝 Business Rules #2
├── TAREFA-BR-03.md                   📝 Business Rules #3
├── TAREFA-BR-04.md                   📝 Business Rules #4
├── TAREFA-BACKEND-ADMIN.md           🔴 Backend: Cliente x Admin
└── TAREFA-FRONTEND-ADMIN.md          🟠 Frontend: Cliente x Admin
```

### Reports (Relatórios)
```
docs/reports/
├── ANALISE-VALIDACAO-R05.md          📊 Validação RF0055
├── RELATORIO-FINAL-REVIEW.md         ✅ Review Final
├── RELATORIO-REVIEW-CLIENTE-ADMIN.md 🔐 Review Cliente/Admin
├── SUMARIO-EXECUTIVO.md              🎯 Sumário Executivo
└── SUMARIO-CLIENTE-ADMIN.md          📋 Sumário Cliente/Admin
```

### Guides (Guias)
```
docs/guides/
├── FRONTEND-GUIDE.md                 🎨 Guia Frontend
├── GUIA-IMPLEMENTACAO-ADMIN.md       👤 Implementação Admin
└── EXEMPLO-USO-REVIEW-AGENT.md       🤖 Review Agent
```

### General (Geral)
```
docs/general/
├── INDICE-DOCUMENTACAO.md            📚 Índice completo
└── CHANGELOG.md                      📝 Histórico de mudanças
```

---

## 🎯 Acesso Rápido por Necessidade

### "Preciso implementar uma validação de negócio"
→ `business-rules/BUSINESS-RULES-GUIDE.md`
→ `business-rules/BUSINESS-RULES-EXAMPLES.md`

### "Preciso saber quais exceções lançar"
→ `business-rules/EXCECOES-REFERENCE.md`
→ `business-rules/HIERARQUIA-EXCECOES.md`

### "Preciso implementar separação Cliente/Admin"
→ `tasks/TAREFA-BACKEND-ADMIN.md` (backend)
→ `tasks/TAREFA-FRONTEND-ADMIN.md` (frontend)

### "Preciso saber o status do projeto"
→ `reports/SUMARIO-EXECUTIVO.md`
→ `reports/RELATORIO-FINAL-REVIEW.md`

### "Preciso seguir padrões de frontend"
→ `guides/FRONTEND-GUIDE.md`

### "Preciso consultar regras rapidamente"
→ `business-rules/QUICK-REFERENCE.md`

### "Preciso ver histórico de mudanças"
→ `general/CHANGELOG.md`

---

## 📖 Documentos Essenciais (Raiz do Projeto)

```
/
├── README.md                         📘 README principal
├── AGENTS.md                         📕 Especificação completa
└── docs/                             📚 Toda documentação técnica
```

---

## 🔗 Links Diretos

### Mais Acessados

| Documento | Caminho | Uso |
|-----------|---------|-----|
| **Especificação Completa** | `/AGENTS.md` | Requisitos, RFs, RNs |
| **Guia de Regras** | `business-rules/BUSINESS-RULES-GUIDE.md` | Implementar validações |
| **Tarefas Backend Admin** | `tasks/TAREFA-BACKEND-ADMIN.md` | Separação perfis backend |
| **Tarefas Frontend Admin** | `tasks/TAREFA-FRONTEND-ADMIN.md` | Separação perfis frontend |
| **Sumário Executivo** | `reports/SUMARIO-EXECUTIVO.md` | Status geral |
| **Referência de Exceções** | `business-rules/EXCECOES-REFERENCE.md` | Lançar exceções |

### Por Módulo

| Módulo | Documentos Principais |
|--------|----------------------|
| **Cliente/Admin** | `tasks/TAREFA-BACKEND-ADMIN.md`<br>`tasks/TAREFA-FRONTEND-ADMIN.md`<br>`reports/RELATORIO-REVIEW-CLIENTE-ADMIN.md` |
| **Business Rules** | `business-rules/BUSINESS-RULES-GUIDE.md`<br>`business-rules/EXCECOES-REFERENCE.md` |
| **Análise (RF0055)** | `reports/ANALISE-VALIDACAO-R05.md` |
| **Frontend** | `guides/FRONTEND-GUIDE.md` |

---

## 🏷️ Tags Rápidas

Use `grep` para buscar por tags nos documentos:

```bash
# Buscar por requisito funcional específico
grep -r "RF0055" docs/

# Buscar por regra de negócio específica
grep -r "RN0028" docs/

# Buscar por tarefa específica
grep -r "BR-01" docs/

# Buscar por exceção específica
grep -r "ValidacaoNegocioException" docs/
```

---

**Atualizado em:** 2026-03-21
