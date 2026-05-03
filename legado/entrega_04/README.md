# Entrega 04 - Modulo de Vendas

## Contexto

Esta entrega implementa o fluxo completo de vendas do JakeCommerce, cobrindo desde a realizacao da compra ate a confirmacao de entrega. O objetivo e permitir que clientes finalizem suas compras com multiplas formas de pagamento e acompanhem o status de entrega.

## Escopo da Entrega

### Funcionalidades

| ID | Funcionalidade | Descricao |
|----|----------------|-----------|
| F01 | Realizar Compra | Validar carrinho e criar pedido |
| F02 | Selecionar Endereco | Escolher endereco de entrega |
| F03 | Calcular Frete | Determinar valor e prazo de entrega |
| F04 | Selecionar Pagamento | Cartoes e cupons |
| F05 | Processar Pagamento | Validar e aprovar/reprovar |
| F06 | Gerenciar Cupons | Aplicar, consumir e gerar cupons |
| F07 | Finalizar Compra | Criar pedido e baixar estoque |
| F08 | Despachar Pedido | Admin envia produto |
| F09 | Confirmar Entrega | Admin confirma recebimento |

### Requisitos Funcionais Cobertos

| RF | Descricao | Status |
|----|-----------|--------|
| RF0033 | Realizar compra | Pendente |
| RF0034 | Calcular frete | Pendente |
| RF0035 | Selecionar endereco | Pendente |
| RF0036 | Selecionar pagamento | Pendente |
| RF0037 | Finalizar compra | Pendente |
| RF0038 | Despachar produtos | Pendente |
| RF0039 | Confirmar entrega | Pendente |

### Regras de Negocio Envolvidas

#### Compra e Estoque
| RN | Descricao |
|----|-----------|
| RN0031 | Validar estoque no carrinho |
| RN0032 | Validar estoque antes da finalizacao |
| RN0063 | Maximo 10 unidades do mesmo livro por pedido |
| RN0028 | Baixa estoque apenas apos pagamento APROVADO |

#### Pagamento
| RN | Descricao |
|----|-----------|
| RN0033 | Apenas um cupom promocional por compra |
| RN0034 | Multiplos cartoes permitidos (minimo R$10 por cartao) |
| RN0035 | Consumir cupons antes do cartao |
| RN0036 | Gerar cupom para excedente |
| RN0037 | Validar pagamento |
| RN0038 | Status pagamento: APROVADA ou REPROVADA |
| RN0065 | 3 pagamentos REPROVADOS consecutivos bloqueiam carrinho |

#### Frete e Entrega
| RN | Descricao |
|----|-----------|
| RN0022 | Pelo menos um endereco de entrega |
| RN0064 | Pedido minimo R$20 para frete gratis |
| RN0039 | Status transporte: EM_TRANSPORTE |
| RN0040 | Status entrega: ENTREGUE |

---

## Agentes Envolvidos

### Agentes Coordenadores

| Agente | Arquivo | Responsabilidade |
|--------|---------|------------------|
| **checkout-agent** | [checkout-agent.md](../../.claude/agents/checkout-agent.md) | Orquestrar fluxo de compra |
| **payment-agent** | [payment-agent.md](../../.claude/agents/payment-agent.md) | Processar pagamentos |
| **shipping-agent** | [shipping-agent.md](../../.claude/agents/shipping-agent.md) | Gerenciar frete e entrega |

### Agentes de Implementacao

| Agente | Arquivo | Responsabilidade |
|--------|---------|------------------|
| **backend-agent** | [backend-agent.md](../../.claude/agents/backend-agent.md) | Services e Repositories |
| **frontend-agent** | [frontend-agent.md](../../.claude/agents/frontend-agent.md) | Controllers e Templates |
| **business-rules-agent** | [business-rules-agent.md](../../.claude/agents/business-rules-agent.md) | Excecoes e Validators |
| **domain-agent** | [agent-domain.md](../../.claude/agents/agent-domain.md) | Entidades JPA |
| **review-agent** | [review-agent.md](../../.claude/agents/review-agent.md) | Revisao de codigo |

---

## Tasks por Agente

### shipping-agent (6 tasks)

| Task | Arquivo | Descricao | Prioridade |
|------|---------|-----------|------------|
| TASK-SHP-01 | [TASK-SHP-01.md](./tasks/TASK-SHP-01.md) | Listar enderecos de entrega | Alta |
| TASK-SHP-02 | [TASK-SHP-02.md](./tasks/TASK-SHP-02.md) | Selecionar endereco de entrega | Alta |
| TASK-SHP-03 | [TASK-SHP-03.md](./tasks/TASK-SHP-03.md) | Calcular frete | Alta |
| TASK-SHP-04 | [TASK-SHP-04.md](./tasks/TASK-SHP-04.md) | Despachar pedido (Admin) | Media |
| TASK-SHP-05 | [TASK-SHP-05.md](./tasks/TASK-SHP-05.md) | Confirmar entrega (Admin) | Media |
| TASK-SHP-06 | [TASK-SHP-06.md](./tasks/TASK-SHP-06.md) | Listar pedidos por status (Admin) | Media |

### payment-agent (6 tasks)

| Task | Arquivo | Descricao | Prioridade |
|------|---------|-----------|------------|
| TASK-PAY-01 | [TASK-PAY-01.md](./tasks/TASK-PAY-01.md) | Orquestrar selecao de pagamento | Alta |
| TASK-PAY-02 | [TASK-PAY-02.md](./tasks/TASK-PAY-02.md) | Aplicar cupons | Alta |
| TASK-PAY-03 | [TASK-PAY-03.md](./tasks/TASK-PAY-03.md) | Gerenciar excedente de cupom | Media |
| TASK-PAY-04 | [TASK-PAY-04.md](./tasks/TASK-PAY-04.md) | Validar e distribuir pagamento em cartoes | Alta |
| TASK-PAY-05 | [TASK-PAY-05.md](./tasks/TASK-PAY-05.md) | Processar pagamento | Alta |
| TASK-PAY-06 | [TASK-PAY-06.md](./tasks/TASK-PAY-06.md) | Controlar tentativas reprovadas | Media |

### checkout-agent (5 tasks)

| Task | Arquivo | Descricao | Prioridade |
|------|---------|-----------|------------|
| TASK-CHK-01 | [TASK-CHK-01.md](./tasks/TASK-CHK-01.md) | Orquestrar fluxo de checkout | Alta |
| TASK-CHK-02 | [TASK-CHK-02.md](./tasks/TASK-CHK-02.md) | Validar pre-condicoes do carrinho | Alta |
| TASK-CHK-03 | [TASK-CHK-03.md](./tasks/TASK-CHK-03.md) | Converter carrinho em pedido | Alta |
| TASK-CHK-04 | [TASK-CHK-04.md](./tasks/TASK-CHK-04.md) | Coordenar baixa de estoque | Alta |
| TASK-CHK-05 | [TASK-CHK-05.md](./tasks/TASK-CHK-05.md) | Gerenciar estado da transacao | Media |

---

## Ordem de Implementacao

### Fase 1: Shipping (Endereco e Frete)

```
1.1 business-rules-agent: Excecoes de endereco
1.2 backend-agent: EnderecoService, FreteService
1.3 frontend-agent: EnderecoController, templates
```

**Tasks:** TASK-SHP-01, TASK-SHP-02, TASK-SHP-03

### Fase 2: Payment (Pagamento)

```
2.1 business-rules-agent: Excecoes de pagamento
2.2 backend-agent: PagamentoService, CupomService
2.3 frontend-agent: PagamentoController, templates
```

**Tasks:** TASK-PAY-01, TASK-PAY-02, TASK-PAY-04, TASK-PAY-05

### Fase 3: Checkout (Integracao)

```
3.1 backend-agent: CompraService, PedidoService
3.2 frontend-agent: CheckoutController, wizard
3.3 Integracao shipping + payment + checkout
```

**Tasks:** TASK-CHK-01, TASK-CHK-02, TASK-CHK-03, TASK-CHK-04

### Fase 4: Admin (Gestao de Entregas)

```
4.1 backend-agent: Endpoints admin
4.2 frontend-agent: Templates admin
```

**Tasks:** TASK-SHP-04, TASK-SHP-05, TASK-SHP-06

### Fase 5: Refinamentos

```
5.1 TASK-PAY-03: Gerenciar excedente de cupom
5.2 TASK-PAY-06: Controlar tentativas reprovadas
5.3 TASK-CHK-05: Gerenciar estado da transacao
```

---

## Fluxo Completo

```
[Cliente]
    |
    v
[Carrinho.ABERTO]
    |
    v
[Iniciar Checkout] --> checkout-agent
    |
    +---> [Validar Carrinho] --> TASK-CHK-02
    |
    +---> [Selecionar Endereco] --> shipping-agent (TASK-SHP-01, SHP-02)
    |
    +---> [Calcular Frete] --> shipping-agent (TASK-SHP-03)
    |
    +---> [Selecionar Pagamento] --> payment-agent (TASK-PAY-01, PAY-02, PAY-04)
    |
    +---> [Processar Pagamento] --> payment-agent (TASK-PAY-05)
    |
    v
[Pagamento.APROVADA?]
    |
  SIM --> [Baixar Estoque] --> TASK-CHK-04
    |           |
    |           v
    |      [Pedido.EM_PROCESSAMENTO]
    |           |
    |           v
    |      [Carrinho.FINALIZADO]
    |
  NAO --> [Incrementar Tentativas] --> TASK-PAY-06
    |
    v
[Permitir Retry ou Bloquear]

---

[Admin]
    |
    v
[Despachar] --> TASK-SHP-04 --> Pedido.EM_TRANSPORTE
    |
    v
[Confirmar Entrega] --> TASK-SHP-05 --> Pedido.ENTREGUE
```

---

## Criterios de Aceite da Entrega

### Cliente
- [ ] Consegue iniciar checkout a partir do carrinho
- [ ] Consegue selecionar endereco de entrega
- [ ] Visualiza valor e prazo do frete
- [ ] Consegue aplicar cupons de troca
- [ ] Consegue aplicar cupom promocional (max 1)
- [ ] Consegue pagar com multiplos cartoes
- [ ] Recebe cupom se valor de cupons exceder total
- [ ] Visualiza pedido finalizado com status EM_PROCESSAMENTO

### Admin
- [ ] Consegue visualizar pedidos por status
- [ ] Consegue despachar pedidos
- [ ] Consegue confirmar entregas

### Sistema
- [ ] Valida estoque antes e durante checkout
- [ ] Bloqueia mais de 10 unidades do mesmo livro
- [ ] Baixa estoque apenas apos pagamento APROVADO
- [ ] Bloqueia carrinho apos 3 pagamentos reprovados
- [ ] Registra log de todas as operacoes (RNF0012)
- [ ] Tempo de resposta < 1 segundo (RNF0011)

---

## Dependencias Tecnicas

### Entidades JPA Necessarias
- Pedido, ItemPedido
- Pagamento, PagamentoCartao, PagamentoCupom
- Cupom
- Endereco (atualizar com tipo ENTREGA)

### Services a Criar
- CompraService
- PedidoService
- PagamentoService
- CupomService
- FreteService

### Excecoes a Criar
- CarrinhoVazioException
- EstoqueInsuficienteException
- LimiteItensExcedidoException
- EnderecoEntregaNaoEncontradoException
- CupomInvalidoException
- CupomPromocionalDuplicadoException
- ValorMinimoCartaoException
- CarrinhoBloqueadoException
- TransicaoStatusInvalidaException

---

## Documentos Relacionados

- [Requisitos do Sistema](../../general/requisitoss_copilot.md)
- [INDEX de Agentes](../agents/INDEX.md)
- [Tarefas de Vendas](../tasks/vendas/INDEX.md)

---

**Criado em:** 2026-04-01
**Ultima atualizacao:** 2026-04-01
