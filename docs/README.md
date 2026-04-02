# 📚 Documentação do Projeto JakeCommerce

Esta pasta contém toda a documentação técnica e organizacional do projeto, categorizada por tipo.

## 📁 Estrutura de Pastas

### 🔧 `business-rules/`
Documentação completa sobre regras de negócio e exceções.

**Arquivos:**
- `BUSINESS-RULES-GUIDE.md` - Guia completo de regras de negócio
- `BUSINESS-RULES-EXAMPLES.md` - Exemplos práticos de implementação
- `BUSINESS-RULES-IMPLEMENTATION.md` - Guia de implementação técnica
- `BUSINESS-RULES-SUMMARY.md` - Resumo das principais regras
- `EXCECOES-REFERENCE.md` - Referência de exceções customizadas
- `HIERARQUIA-EXCECOES.md` - Hierarquia e estrutura de exceções
- `QUICK-REFERENCE.md` - Referência rápida para consultas

**Quando consultar:**
- Implementando validações de negócio
- Tratando exceções no sistema
- Entendendo regras específicas (RN00XX)

---

### ✅ `tasks/`
Tarefas delegadas aos agentes especializados (backend, frontend, business-rules).

**Arquivos:**
- `TAREFA-BR-02.md` - Tarefa de regras de negócio #2
- `TAREFA-BR-03.md` - Tarefa de regras de negócio #3
- `TAREFA-BR-04.md` - Tarefa de regras de negócio #4
- `TAREFA-BACKEND-ADMIN.md` - Tarefas backend para separação cliente/admin
- `TAREFA-FRONTEND-ADMIN.md` - Tarefas frontend para separação cliente/admin

**Quando consultar:**
- Implementando funcionalidades específicas
- Delegando trabalho a agentes
- Verificando escopo de tarefas

---

### 📊 `reports/`
Relatórios de revisão, validação e análise do sistema.

**Arquivos:**
- `ANALISE-VALIDACAO-R05.md` - Validação do módulo de análise (RF0055)
- `RELATORIO-FINAL-REVIEW.md` - Relatório final da fase de review
- `RELATORIO-REVIEW-CLIENTE-ADMIN.md` - Revisão da separação cliente/admin
- `SUMARIO-EXECUTIVO.md` - Sumário executivo da fase de review
- `SUMARIO-CLIENTE-ADMIN.md` - Sumário da implementação cliente/admin

**Quando consultar:**
- Verificando status do projeto
- Entendendo decisões arquiteturais
- Validando conformidade com requisitos
- Apresentando progresso para stakeholders

---

### 📖 `guides/`
Guias de implementação e uso de funcionalidades específicas.

**Arquivos:**
- `FRONTEND-GUIDE.md` - Guia de desenvolvimento frontend
- `GUIA-IMPLEMENTACAO-ADMIN.md` - Guia de implementação do módulo admin
- `EXEMPLO-USO-REVIEW-AGENT.md` - Como usar o review-agent

**Quando consultar:**
- Desenvolvendo novas features
- Seguindo padrões do projeto
- Entendendo fluxos de trabalho

---

### 📄 `general/`
Documentação geral e controle de mudanças.

**Arquivos:**
- `INDICE-DOCUMENTACAO.md` - Índice geral da documentação
- `CHANGELOG.md` - Histórico de mudanças do projeto

**Quando consultar:**
- Navegando pela documentação
- Verificando histórico de mudanças
- Entendendo evolução do projeto

---

## 🗂️ Documentação Raiz do Projeto

Alguns documentos permanecem na raiz do repositório por serem essenciais:

- `/README.md` - README principal do projeto
- `/AGENTS.md` - Especificação completa do sistema e requisitos
- `/jakebooks/README.md` - README do módulo Spring Boot

---

## 🔍 Como Navegar

### Por Tipo de Atividade

| Atividade | Onde Procurar |
|-----------|--------------|
| Implementar validação de negócio | `business-rules/` |
| Verificar tarefas pendentes | `tasks/` |
| Ver status do projeto | `reports/` |
| Aprender padrões do projeto | `guides/` |
| Verificar mudanças recentes | `general/CHANGELOG.md` |
| Entender requisitos | `/AGENTS.md` (raiz) |

### Por Papel no Projeto

| Papel | Documentos Principais |
|-------|----------------------|
| **Backend Developer** | `tasks/TAREFA-BACKEND-ADMIN.md`<br>`business-rules/BUSINESS-RULES-GUIDE.md` |
| **Frontend Developer** | `tasks/TAREFA-FRONTEND-ADMIN.md`<br>`guides/FRONTEND-GUIDE.md` |
| **Tech Lead** | `reports/RELATORIO-REVIEW-CLIENTE-ADMIN.md`<br>`general/INDICE-DOCUMENTACAO.md` |
| **Product Owner** | `reports/SUMARIO-EXECUTIVO.md`<br>`/AGENTS.md` |
| **QA/Tester** | `reports/ANALISE-VALIDACAO-R05.md`<br>`business-rules/BUSINESS-RULES-EXAMPLES.md` |

---

## 📝 Convenções de Nomenclatura

### Prefixos de Arquivos

- **TAREFA-** → Documentos de tarefas delegadas
- **RELATORIO-** → Relatórios de revisão/validação
- **SUMARIO-** → Resumos executivos
- **GUIA-** → Guias de implementação
- **BUSINESS-RULES-** → Documentação de regras de negócio

### Sufixos Comuns

- **-ADMIN** → Relacionado ao módulo administrativo
- **-GUIDE** → Guia/tutorial
- **-REFERENCE** → Referência técnica
- **-EXAMPLES** → Exemplos práticos

---

## 🔄 Manutenção da Documentação

### Quando Criar Novos Documentos

1. **Tarefas** → Criar em `tasks/` quando delegar trabalho a agentes
2. **Relatórios** → Criar em `reports/` após revisões ou validações
3. **Guias** → Criar em `guides/` para documentar processos novos
4. **Regras** → Atualizar em `business-rules/` quando RNs mudarem

### Atualizando Documentos Existentes

- Sempre atualizar `CHANGELOG.md` ao fazer mudanças significativas
- Manter `INDICE-DOCUMENTACAO.md` sincronizado
- Versionar documentos importantes (adicionar data/versão no topo)

---

## 🚀 Quick Start

**Novos desenvolvedores:**
1. Leia `/README.md` (raiz do projeto)
2. Leia `/AGENTS.md` (especificação completa)
3. Navegue por `guides/` conforme sua área (backend/frontend)
4. Consulte `business-rules/` ao implementar validações

**Revisores de código:**
1. Verifique `reports/` para status atual
2. Consulte `business-rules/` para validar regras
3. Revise `tasks/` para entender escopo

**Product Owners:**
1. Leia `reports/SUMARIO-EXECUTIVO.md`
2. Consulte `/AGENTS.md` para requisitos
3. Verifique `general/CHANGELOG.md` para mudanças

---

## 📞 Suporte

Para dúvidas sobre:
- **Estrutura de documentação** → Consulte este README
- **Requisitos do sistema** → `/AGENTS.md`
- **Implementação específica** → Documentos em `guides/` ou `tasks/`
- **Status do projeto** → Documentos em `reports/`

---

**Última atualização:** 2026-03-21
**Estrutura criada por:** Review Agent
