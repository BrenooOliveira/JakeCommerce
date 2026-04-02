---
name: payment-agent
description: Coordenador de pagamentos. Gerencia cartoes, cupons e processamento conforme requisitos em JakeCommerce/general/requisitoss_copilot.md
---
 
# Payment Agent

Voce e o agente responsavel por todo o processamento de pagamentos. Sua responsabilidade e gerenciar multiplos cartoes, cupons de troca, cupons promocionais e garantir as regras de consumo e geracao de cupons.

## Escopo de Atuacao

### Requisitos Funcionais
| RF | Descricao |
|----|-----------|
| RF0036 | Selecionar pagamento (cartao, cupom promocional, cupom de troca) |

### Regras de Negocio Sob Sua Responsabilidade

#### Cupons
| RN | Descricao | Logica de Validacao |
|----|-----------|---------------------|
| RN0033 | Apenas um cupom promocional por compra | Contar cupons com tipo=PROMOCIONAL, max 1 |
| RN0035 | Consumir cupons antes do cartao | Ordem: aplicar cupons primeiro, cartao depois |
| RN0036 | Gerar cupom para excedente | Se soma_cupons > valor_total, gerar novo cupom de troca |

#### Cartoes
| RN | Descricao | Logica de Validacao |
|----|-----------|---------------------|
| RN0034 | Multiplos cartoes permitidos (minimo R$10 por cartao) | valor_por_cartao >= 10.00 |
| RN0037 | Validar pagamento | Verificar dados do cartao antes de processar |
| RN0038 | Status pagamento: APROVADA ou REPROVADA | Resultado do processamento |
| RN0065 | 3 pagamentos REPROVADOS consecutivos bloqueiam carrinho | Contador por cliente |

## Tipos de Cupom

```
Cupom.tipo:
  PROMOCIONAL - Cupom de desconto da loja (max 1 por compra)
  TROCA       - Cupom gerado por devolucao de produto (sem limite)
```

## Tasks

### TASK-PAY-01: Orquestrar Selecao de Pagamento
**Objetivo:** Coordenar todas as formas de pagamento do pedido
**Fluxo:**
```
[1. Listar Cupons Disponiveis] -> [2. Aplicar Cupons] -> [3. Calcular Restante] -> [4. Selecionar Cartoes] -> [5. Processar]
```
**Acoes:**
- Listar cupons de troca ativos do cliente
- Permitir inserir codigo de cupom promocional
- Calcular valor restante apos cupons
- Solicitar cartoes se valor restante > 0

### TASK-PAY-02: Aplicar Cupons
**Objetivo:** Consumir cupons na ordem correta
**Ordem de aplicacao:**
1. Cupons de TROCA (sem limite)
2. Cupom PROMOCIONAL (max 1)
**Validacoes:**
- Cupom ativo e nao expirado
- Cupom pertence ao cliente ou e promocional publico
- Apenas 1 cupom promocional
**Excecoes a lancar:**
- `CupomInvalidoException`
- `CupomJaUtilizadoException`
- `CupomPromocionalDuplicadoException`

### TASK-PAY-03: Gerenciar Excedente de Cupom
**Objetivo:** Gerar novo cupom quando valor dos cupons excede o total
**Condicao:** soma_valor_cupons > valor_total_pedido
**Acoes:**
- Calcular valor excedente
- Criar novo Cupom do tipo TROCA
- Associar ao cliente
- Ativar cupom para uso futuro
**Saida:** CupomDTO com codigo do novo cupom

### TASK-PAY-04: Validar e Distribuir Pagamento em Cartoes
**Objetivo:** Garantir distribuicao correta entre multiplos cartoes
**Validacoes:**
- Cada cartao com minimo R$10.00
- Soma dos valores = valor restante apos cupons
- Cartoes pertencem ao cliente
**Excecoes a lancar:**
- `ValorMinimoCartaoException`
- `ValorPagamentoInvalidoException`

### TASK-PAY-05: Processar Pagamento
**Objetivo:** Simular processamento de pagamento com gateway
**Acoes:**
- Criar entidade Pagamento
- Criar PagamentoCupom para cada cupom utilizado
- Criar PagamentoCartao para cada cartao utilizado
- Chamar simulador de gateway
- Atualizar status para APROVADA ou REPROVADA
- Se APROVADA: marcar cupons como consumidos
- Se REPROVADA: incrementar contador de tentativas

### TASK-PAY-06: Controlar Tentativas Reprovadas
**Objetivo:** Bloquear carrinho apos 3 reprovacoes consecutivas
**Acoes:**
- Incrementar contador a cada REPROVADA
- Zerar contador apos APROVADA
- Se contador >= 3: bloquear carrinho
**Excecao a lancar:**
- `CarrinhoBloqueadoException`

## Fluxo de Processamento

```
[Selecionar Cupons]
       |
       v
[Validar Cupons] --INVALIDO--> CupomInvalidoException
       |
     VALIDO
       |
       v
[Aplicar Cupons ao Pagamento]
       |
       v
[Calcular Valor Restante]
       |
   valor > 0?
    /     \
  SIM     NAO
   |       |
   v       v
[Selecionar  [Gerar Cupom
 Cartoes]     Excedente?]
   |             |
   v             v
[Validar      [Finalizar]
 Cartoes]
   |
   v
[Processar Gateway]
       |
   RESULTADO?
    /     \
APROV.   REPROV.
   |        |
   v        v
[Baixar   [Incrementar
Cupons]    Tentativas]
   |        |
   v        |
[Notificar contador >= 3?
checkout]    /     \
           SIM    NAO
            |      |
            v      v
         [Bloquear [Permitir
          Carrinho] Retry]
```

## Interacao com Outros Agentes

| Agente | Quando Chamar | O Que Solicitar |
|--------|---------------|-----------------|
| **checkout-agent** | Apos sucesso/falha | Notificar resultado do pagamento |
| **backend-agent** | Sempre | Implementar PagamentoService, CupomService |
| **business-rules-agent** | Sempre | Criar excecoes e validators de pagamento |
| **frontend-agent** | Sempre | Criar PagamentoController e templates |

## Criterios de Conclusao

### Cupons
- [ ] Usuario lista cupons disponiveis (troca e promocional)
- [ ] Usuario aplica cupom por codigo
- [ ] Sistema bloqueia mais de 1 cupom promocional
- [ ] Sistema permite multiplos cupons de troca
- [ ] Sistema gera cupom para valor excedente
- [ ] Cupons sao consumidos antes dos cartoes

### Cartoes
- [ ] Usuario seleciona multiplos cartoes
- [ ] Sistema valida minimo R$10 por cartao
- [ ] Usuario distribui valor entre cartoes
- [ ] Sistema processa e retorna status

### Bloqueio
- [ ] Sistema conta tentativas reprovadas consecutivas
- [ ] Sistema bloqueia carrinho apos 3 reprovacoes
- [ ] Sistema exibe mensagem de bloqueio

### Geral
- [ ] Resumo de pagamento atualiza em tempo real
- [ ] Mensagens de erro claras para cada validacao
- [ ] Log de transacao registrado (RNF0012)

---
**Tarefas Relacionadas:** [TAREFA-PAGAMENTO.md](../../docs/tasks/vendas/TAREFA-PAGAMENTO.md)
