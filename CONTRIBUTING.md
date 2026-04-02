# 🤝 Guia de Contribuição - JakeCommerce

## 📚 Navegando pela Documentação

Antes de começar, familiarize-se com a estrutura de documentação:

1. **Leia primeiro:** [`docs/README.md`](docs/README.md) - Guia de navegação
2. **Consulte:** [`docs/INDEX.md`](docs/INDEX.md) - Índice rápido por necessidade
3. **Especificação:** [`AGENTS.md`](AGENTS.md) - Requisitos completos do sistema

## 🎯 Por Onde Começar

### Backend Developer
1. Leia [`AGENTS.md`](AGENTS.md) (especificação)
2. Consulte [`docs/business-rules/BUSINESS-RULES-GUIDE.md`](docs/business-rules/BUSINESS-RULES-GUIDE.md)
3. Verifique tarefas pendentes em [`docs/tasks/`](docs/tasks/)

### Frontend Developer
1. Leia [`AGENTS.md`](AGENTS.md) (especificação)
2. Consulte [`docs/guides/FRONTEND-GUIDE.md`](docs/guides/FRONTEND-GUIDE.md)
3. Verifique tarefas pendentes em [`docs/tasks/`](docs/tasks/)

### Revisor de Código
1. Consulte [`docs/reports/`](docs/reports/) para status atual
2. Valide contra [`docs/business-rules/`](docs/business-rules/)
3. Verifique conformidade com [`AGENTS.md`](AGENTS.md)

## 📝 Atualizando Documentação

### Quando criar novos documentos

| Tipo | Onde criar | Quando |
|------|-----------|--------|
| **Tarefa para agente** | `docs/tasks/TAREFA-*.md` | Ao delegar trabalho |
| **Relatório de revisão** | `docs/reports/RELATORIO-*.md` | Após validações |
| **Guia de implementação** | `docs/guides/GUIA-*.md` | Para documentar processos |
| **Regra de negócio** | `docs/business-rules/` | Ao adicionar/modificar RNs |

### Manutenção obrigatória

Ao fazer mudanças significativas, SEMPRE atualizar:
1. [`docs/general/CHANGELOG.md`](docs/general/CHANGELOG.md) - Registrar mudança
2. [`docs/general/INDICE-DOCUMENTACAO.md`](docs/general/INDICE-DOCUMENTACAO.md) - Se adicionar documento novo

## 🔍 Convenções

### Nomenclatura de Arquivos

| Prefixo | Uso | Exemplo |
|---------|-----|---------|
| `TAREFA-` | Tarefas delegadas | `TAREFA-BACKEND-ADMIN.md` |
| `RELATORIO-` | Relatórios de revisão | `RELATORIO-REVIEW-CLIENTE-ADMIN.md` |
| `SUMARIO-` | Resumos executivos | `SUMARIO-EXECUTIVO.md` |
| `GUIA-` | Guias de implementação | `GUIA-IMPLEMENTACAO-ADMIN.md` |

### Estrutura de Documentos

Todo documento deve ter:
```markdown
# Título

**Data:** YYYY-MM-DD
**Status:** (em andamento/concluído/bloqueado)

## Objetivo
[Breve descrição]

## [Conteúdo]
...
```

## 🚦 Fluxo de Trabalho

### 1. Pegar uma tarefa
```bash
# Ver tarefas disponíveis
ls docs/tasks/

# Ler tarefa específica
cat docs/tasks/TAREFA-BACKEND-ADMIN.md
```

### 2. Implementar
- Siga padrões em [`docs/guides/`](docs/guides/)
- Valide regras em [`docs/business-rules/`](docs/business-rules/)
- Consulte [`AGENTS.md`](AGENTS.md) para requisitos

### 3. Documentar
- Atualize [`docs/general/CHANGELOG.md`](docs/general/CHANGELOG.md)
- Crie relatório em [`docs/reports/`](docs/reports/) se necessário

### 4. Revisar
- Revise contra checklist em [`docs/reports/RELATORIO-REVIEW-CLIENTE-ADMIN.md`](docs/reports/RELATORIO-REVIEW-CLIENTE-ADMIN.md)
- Valide regras de negócio

## 📞 Dúvidas?

- **Documentação:** Consulte [`docs/README.md`](docs/README.md)
- **Requisitos:** Consulte [`AGENTS.md`](AGENTS.md)
- **Regras de negócio:** Consulte [`docs/business-rules/BUSINESS-RULES-GUIDE.md`](docs/business-rules/BUSINESS-RULES-GUIDE.md)

---

**Estrutura criada em:** 2026-03-21
**Mantida por:** Review Agent
