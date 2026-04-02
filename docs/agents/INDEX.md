# Agentes do JakeCommerce

## Visao Geral

Este documento descreve todos os agentes utilizados no sistema JakeCommerce, suas responsabilidades e como eles interagem entre si.

## Arquitetura de Agentes

```
                    +------------------+
                    |  checkout-agent  |  <-- Coordenador Principal
                    +------------------+
                           |
          +----------------+----------------+
          |                                 |
          v                                 v
   +---------------+               +----------------+
   | payment-agent |               | shipping-agent |
   +---------------+               +----------------+
          |                                 |
          +----------------+----------------+
                           |
          +----------------+----------------+----------------+
          |                |                |                |
          v                v                v                v
   +------------+  +-------------+  +----------------+  +--------------+
   |  backend   |  |  frontend   |  | business-rules |  |    domain    |
   |   agent    |  |    agent    |  |     agent      |  |    agent     |
   +------------+  +-------------+  +----------------+  +--------------+
```

## Agentes de Dominio (Coordenadores)

### checkout-agent
**Arquivo:** `.claude/agents/checkout-agent.md`
**Responsabilidade:** Orquestrar todo o fluxo de compra, desde a validacao do carrinho ate a finalizacao do pedido.

| Task | Descricao |
|------|-----------|
| TASK-CHK-01 | Orquestrar fluxo de checkout |
| TASK-CHK-02 | Validar pre-condicoes do carrinho |
| TASK-CHK-03 | Converter carrinho em pedido |
| TASK-CHK-04 | Coordenar baixa de estoque |
| TASK-CHK-05 | Gerenciar estado da transacao |

**RFs:** RF0033, RF0037
**RNs:** RN0031, RN0032, RN0063, RN0028

---

### payment-agent
**Arquivo:** `.claude/agents/payment-agent.md`
**Responsabilidade:** Gerenciar todas as formas de pagamento, incluindo multiplos cartoes, cupons de troca e promocionais.

| Task | Descricao |
|------|-----------|
| TASK-PAY-01 | Orquestrar selecao de pagamento |
| TASK-PAY-02 | Aplicar cupons |
| TASK-PAY-03 | Gerenciar excedente de cupom |
| TASK-PAY-04 | Validar e distribuir pagamento em cartoes |
| TASK-PAY-05 | Processar pagamento |
| TASK-PAY-06 | Controlar tentativas reprovadas |

**RFs:** RF0036
**RNs:** RN0033, RN0034, RN0035, RN0036, RN0037, RN0038, RN0065

---

### shipping-agent
**Arquivo:** `.claude/agents/shipping-agent.md`
**Responsabilidade:** Gerenciar endereco de entrega, calculo de frete e acompanhamento de status de entrega.

| Task | Descricao |
|------|-----------|
| TASK-SHP-01 | Listar enderecos de entrega |
| TASK-SHP-02 | Selecionar endereco de entrega |
| TASK-SHP-03 | Calcular frete |
| TASK-SHP-04 | Despachar pedido (Admin) |
| TASK-SHP-05 | Confirmar entrega (Admin) |
| TASK-SHP-06 | Listar pedidos por status (Admin) |

**RFs:** RF0034, RF0035, RF0038, RF0039
**RNs:** RN0022, RN0064, RN0039, RN0040

---

## Agentes de Camada (Implementacao)

### backend-agent
**Arquivo:** `.claude/agents/backend-agent.md`
**Responsabilidade:** Implementar Services, Repositories e DTOs.

**Pacotes:**
- `com.les.jakebooks.service` - Services com logica de negocio
- `com.les.jakebooks.repository` - Interfaces JpaRepository
- `com.les.jakebooks.dto` - Data Transfer Objects

---

### frontend-agent
**Arquivo:** `.claude/agents/frontend-agent.md`
**Responsabilidade:** Implementar Controllers e Templates Thymeleaf.

**Pacotes:**
- `com.les.jakebooks.controller` - Controllers Spring MVC
- `templates/` - Templates Thymeleaf
- `templates/fragments/` - Componentes reutilizaveis

---

### business-rules-agent
**Arquivo:** `.claude/agents/business-rules-agent.md`
**Responsabilidade:** Garantir regras de negocio, criar excecoes e validators.

**Pacotes:**
- `com.les.jakebooks.exception` - Excecoes customizadas
- `com.les.jakebooks.validator` - Validadores @Component

---

### domain-agent
**Arquivo:** `.claude/agents/agent-domain.md`
**Responsabilidade:** Manter modelo de dominio (entidades JPA).

**Pacotes:**
- `com.les.jakebooks.domain` - Entidades JPA

---

### review-agent
**Arquivo:** `.claude/agents/review-agent.md`
**Responsabilidade:** Revisar codigo para garantir conformidade com requisitos.

---

## Fluxo de Trabalho

### Para Implementar uma Nova Feature

1. **Coordenador** (checkout/payment/shipping-agent) define o escopo e tasks
2. **business-rules-agent** cria excecoes e validators necessarios
3. **backend-agent** implementa Services e Repositories
4. **frontend-agent** implementa Controllers e Templates
5. **review-agent** verifica conformidade com requisitos

### Ordem de Implementacao Recomendada

```
1. Excecoes e Validators (business-rules-agent)
         |
         v
2. Repository e Service (backend-agent)
         |
         v
3. Controller e Templates (frontend-agent)
         |
         v
4. Revisao (review-agent)
```

## Mapeamento Tasks x Requisitos

### Modulo de Vendas

| Requisito | Agente Responsavel | Tasks |
|-----------|-------------------|-------|
| RF0033 - Realizar compra | checkout-agent | CHK-01, CHK-02, CHK-03 |
| RF0034 - Calcular frete | shipping-agent | SHP-03 |
| RF0035 - Selecionar endereco | shipping-agent | SHP-01, SHP-02 |
| RF0036 - Selecionar pagamento | payment-agent | PAY-01, PAY-02, PAY-04, PAY-05 |
| RF0037 - Finalizar compra | checkout-agent | CHK-03, CHK-04, CHK-05 |
| RF0038 - Despachar produtos | shipping-agent | SHP-04 |
| RF0039 - Confirmar entrega | shipping-agent | SHP-05 |

## Documentos Relacionados

- [Tarefas de Vendas](../tasks/vendas/INDEX.md)
- [Requisitos do Sistema](../../general/requisitoss_copilot.md)
- [Guia de Regras de Negocio](../business-rules/BUSINESS-RULES-GUIDE.md)

---

**Atualizado em:** 2026-04-01
