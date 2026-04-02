# Tarefas do Modulo de Vendas

## Visao Geral

Este modulo cobre todo o fluxo de vendas eletronicas do sistema JakeCommerce, desde a realizacao da compra ate a entrega do produto.

## Tarefas

| Tarefa | Arquivo | Descricao | RFs Relacionados |
|--------|---------|-----------|------------------|
| **Compra** | [TAREFA-COMPRA.md](./TAREFA-COMPRA.md) | Fluxo completo de realizacao de compra | RF0033, RF0037 |
| **Pagamento** | [TAREFA-PAGAMENTO.md](./TAREFA-PAGAMENTO.md) | Formas de pagamento (cartoes, cupons) | RF0036 |
| **Entrega e Frete** | [TAREFA-ENTREGA-FRETE.md](./TAREFA-ENTREGA-FRETE.md) | Calculo de frete, selecao de endereco, despacho | RF0034, RF0035, RF0038, RF0039 |

## Fluxo Geral

```
[Carrinho] --> [Selecionar Endereco] --> [Calcular Frete] --> [Selecionar Pagamento] --> [Finalizar Compra] --> [Despacho] --> [Entrega]
```

## Regras de Negocio Envolvidas

### Validacoes Pre-Compra
- **RN0031**: Validar estoque no carrinho
- **RN0032**: Validar estoque antes da finalizacao
- **RN0063**: Maximo 10 unidades do mesmo livro por pedido
- **RN0064**: Pedido minimo R$20 para frete gratis

### Pagamento
- **RN0033**: Apenas um cupom promocional por compra
- **RN0034**: Multiplos cartoes permitidos (minimo R$10 por cartao)
- **RN0035**: Consumir cupons antes do cartao
- **RN0036**: Gerar cupom para excedente
- **RN0037**: Validar pagamento
- **RN0038**: Status pagamento: APROVADA ou REPROVADA
- **RN0065**: 3 pagamentos REPROVADOS consecutivos bloqueiam carrinho

### Entrega
- **RN0039**: Status transporte: EM_TRANSPORTE
- **RN0040**: Status entrega: ENTREGUE

## Agentes Responsaveis

### Agentes Coordenadores (Dominio)

| Agente | Arquivo | Responsabilidade | Tasks |
|--------|---------|------------------|-------|
| **checkout-agent** | [checkout-agent.md](../../../.claude/agents/checkout-agent.md) | Coordenacao do fluxo de checkout | TASK-CHK-01 a CHK-05 |
| **payment-agent** | [payment-agent.md](../../../.claude/agents/payment-agent.md) | Processamento de pagamentos | TASK-PAY-01 a PAY-06 |
| **shipping-agent** | [shipping-agent.md](../../../.claude/agents/shipping-agent.md) | Calculo de frete e gestao de entregas | TASK-SHP-01 a SHP-06 |

### Agentes de Implementacao

| Agente | Arquivo | Responsabilidade |
|--------|---------|------------------|
| **backend-agent** | [backend-agent.md](../../../.claude/agents/backend-agent.md) | Services e Repositories |
| **frontend-agent** | [frontend-agent.md](../../../.claude/agents/frontend-agent.md) | Controllers e Templates |
| **business-rules-agent** | [business-rules-agent.md](../../../.claude/agents/business-rules-agent.md) | Validacoes e excecoes |

### Documentacao de Agentes

Para visao completa dos agentes e suas interacoes, consulte: [INDEX de Agentes](../../agents/INDEX.md)

---

**Atualizado em:** 2026-04-01
