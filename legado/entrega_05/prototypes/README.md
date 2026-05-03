# Protótipos de Telas - JakeCommerce

Este diretório contém protótipos interativos em HTML/CSS para cada tela principal do fluxo de compra do JakeCommerce, baseado no documento UseCase v1.0.0.

## 📋 Índice de Telas

### 1. **1-catalogo.html** - Catálogo de Livros
**Fluxo Relacionado:** Fluxo Principal (Passos 1-2)

Tela principal onde o cliente navega por livros e os adiciona ao carrinho. Inclui:
- Filtros de busca, categoria e ordenação
- Grid de livros com informações (título, autor, preço, disponibilidade)
- Indicador de quantidade de itens no carrinho
- Botões de adição com estados (ativo/desabilitado para itens indisponíveis)

**Regras de Negócio:**
- RN0031: Validar disponibilidade de estoque
- RN0061: Status ATIVO e estoque > 0

---

### 2. **2-carrinho.html** - Carrinho de Compras
**Fluxo Relacionado:** Fluxo Principal (Passos 2-5), FA01 (Carrinho Expirando)

Tela de visualização do carrinho com ajuste de quantidades e aplicação de cupons. Inclui:
- Tabela com itens do carrinho (produto, preço unitário, quantidade, subtotal)
- Botões de remover itens
- Campos de quantidade editáveis
- Seção de cupom promocional
- **Aviso visível:** "Sua sessão expira em 4 minutos" (FA01.1)
- Resumo com subtotal, desconto e total
- Botões de ação (Ir para Checkout, Continuar Comprando)

**Regras de Negócio:**
- RN0063: Máximo 10 unidades por livro
- RN0044: Aviso com 5 minutos para expiração

---

### 3. **3-checkout.html** - Finalização da Compra
**Fluxo Relacionado:** Fluxo Principal (Passos 6-11), FA03-FA05 (Pagamentos)

Tela estruturada em abas/passos para checkout. Inclui:
- **Passo 1 - Endereço de Entrega:**
  - Seleção de endereço cadastrado (RF0035)
  - Opção para adicionar novo endereço
  
- **Passo 2 - Modo de Entrega:**
  - Cálculo de frete (RF0034)
  - RN0064: Isenção de frete acima de R$ 20,00
  - Opções: Padrão e Expressa
  
- **Passo 3 - Cupons e Descontos:**
  - Campo para cupom promocional (FA04)
  - Checkboxes para cupons de troca (FA05)
  - RN0033: Apenas um cupom promocional
  
- **Passo 4 - Pagamento:**
  - Opções: Cartão de Crédito, PIX
  - Campos de cartão (número, validade, CVV, titular)
  - Opção para múltiplos cartões (FA03)
  - RN0034: Mínimo de R$ 10,00 por cartão
  
- **Resumo lateral sticky:**
  - Listagem de itens
  - Cálculo de totais em tempo real
  - Botão de finalização

**Regras de Negócio:**
- RN0032: Revalidação de estoque
- RN0034: Valor mínimo por cartão
- RN0035: Consumir cupons antes de cartões

---

### 4. **4-confirmacao.html** - Confirmação de Compra
**Fluxo Relacionado:** Fluxo Principal (Passo 15)

Tela de confirmação pós-compra com informações completas. Inclui:
- **Banner de sucesso** com ícone animado
- **Informações do Pedido:**
  - Número do pedido (#2024010123)
  - Status: EM PROCESSAMENTO 📦
  - Data e hora
  
- **Cards informativos (3 colunas):**
  - Endereço de entrega
  - Método de pagamento
  - Previsão de entrega
  
- **Seção de Itens:**
  - Listagem com ícone, nome, quantidade e preço
  
- **Totalizadores:**
  - Subtotal, frete, desconto, total pago
  
- **Caixa de notificação:**
  - Próximas ações (email, SMS no despacho, acompanhamento)
  
- **Botões de ação:**
  - Acompanhar Pedido
  - Voltar ao Catálogo

**Observação:** O status inicial é "EM PROCESSAMENTO" conforme RN0037

---

### 5. **5-admin-pedidos.html** - Painel Admin - Gestão de Pedidos
**Fluxo Relacionado:** FA06 (Despacho), FA07 (Confirmação de Entrega)

Painel administrativo para gerenciar pedidos em processamento. Inclui:
- **Estatísticas (3 cards):**
  - Pedidos em Processamento (12)
  - Pedidos em Transporte (5)
  - Trocas Pendentes (3)
  
- **Filtros:**
  - Status (Todos, Em Processamento, Em Transporte, Entregue)
  - Data
  - Busca por número/cliente
  
- **Tabela de Pedidos:**
  - Colunas: ID, Cliente, Total, Data, Status, Ações
  - Badges de status com cor (amarelo=processamento, azul=transporte, verde=entregue)
  - Botão "Despachar" abre modal com detalhes
  - Botão "Confirmar Entrega" para pedidos em transporte
  
- **Modal de Despacho:**
  - Exibe ID do pedido e cliente
  - Lista de itens a despachar
  - Campo para código de rastreamento (opcional)
  - Botões: Confirmar Despacho, Cancelar

**Fluxos Implementados:**
- FA06: Despachar produtos (status → EM TRANSPORTE)
- FA07: Confirmar entrega (status → ENTREGUE)

---

### 6. **6-meus-pedidos.html** - Meus Pedidos (Cliente)
**Fluxo Relacionado:** FA07-FA09 (Solicitar Trocas), FA11 (Cancelamento)

Página de visualização de pedidos do cliente. Inclui:
- **Abas de Filtro:**
  - Todos os Pedidos
  - Em Processamento
  - Em Transporte
  - Entregues
  
- **Cards de Pedidos (4 exemplos diferentes):**
  1. **EM PROCESSAMENTO** - Status inicial, botão "Rastrear"
  2. **EM TRANSPORTE** - Rastreamento com código
  3. **ENTREGUE** - Botão "Solicitar Troca" (habilita FA08/FA09)
  4. **EM TROCA** - Status de solicitação de troca em andamento
  
- **Estrutura de Card:**
  - Cabeçalho com número, status, data
  - Lista de itens com ícone, nome, quantidade e preço
  - Footer com total e ações
  
- **Modal de Troca:**
  - Opções: Troca Total vs Parcial
  - Seletor de motivo (dano, defeito, produto errado, etc.)
  - Campo de detalhes
  - Botões: Solicitar Troca, Cancelar

**Regras de Negócio:**
- RN0043: Troca em status ENTREGUE
- RN0041: Transição para EM TROCA

---

### 7. **7-admin-trocas.html** - Painel Admin - Gestão de Trocas
**Fluxo Relacionado:** FA08-FA09 (Solicitar Troca), RF0041-RF0044 (Autorizar, Confirmar Recebimento, Gerar Cupom)

Painel administrativo para gerenciar solicitações de troca. Inclui:
- **Estatísticas (3 cards):**
  - Trocas Pendentes (5)
  - Trocas Autorizadas (8)
  - Trocas Concluídas (23)
  
- **Filtros:**
  - Status (Pendente, Autorizada, Concluída, Rejeitada)
  - Data
  
- **Tabela de Trocas:**
  - Colunas: ID Troca, Pedido, Cliente, Tipo (Total/Parcial), Data Solicitação, Status, Ações
  - Badges de status
  - Botões: Visualizar (abre modal)
  
- **Modal de Detalhes da Troca:**
  - **Informações Gerais:** ID, Pedido, Cliente, Email, Tipo, Data
  - **Motivo com Box Destacado:** Ex. "Livro danificado"
  - **Itens para Devolução:** Lista com nome, autor, quantidade, valor
  - **Valores:** Total de itens, valor do cupom a gerar
  - **Botões de Ação:**
    - ✓ Autorizar Troca (RF0041)
    - ✗ Rejeitar
    - Fechar
  
**Fluxos Implementados:**
- RF0041: Autorizar troca
- RF0043: Confirmar recebimento (botão em trocas autorizadas)
- RF0044: Gerar cupom de troca
- RF0042: Visualizar trocas pendentes

---

### 8. **8-avisos-erros.html** - Avisos, Erros e Notificações
**Fluxo Relacionado:** FA01, FA02, FE01-FE20 (Fluxos de Exceção)

Página de exemplos de diferentes tipos de notificações. Organizado por kategoria:

#### **⏰ AVISOS (Warnings)**
1. **Sessão Expirando** (FA01.1)
   - Ícone de relógio
   - Mensagem: "Você tem 5 minutos..."
   - Botões: "Ir para Checkout", "Estender Sessão"

2. **Estoque Limitado** (FA10)
   - Lista de itens com estoque baixo
   - Recomendação de acelerar compra

3. **Item Removido** (FA01.4)
   - Aviso quando item é removido por outro cliente

#### **❌ ERROS (Errors)**
1. **Pagamento Reprovado** (FA02.1-FA02.3)
   - Motivos listados
   - Opções: Tentar outro cartão, Cancelar

2. **Carrinho Bloqueado** (FA02.5, FE07)
   - 3 reprovações consecutivas
   - Instruções de suporte

3. **Estoque Insuficiente** (FE03, FE04)
   - Lista de itens com problema
   - Quantidades disponíveis
   - Ação: Ajustar carrinho

4. **Cliente Bloqueado** (FE01.3)
   - Account blocked message
   - Instruções de suporte

5. **Cupom Inválido** (FE10)
   - Motivos: não existe, expirou, já usado
   - Opção de tentar outro

6. **Carrinho Expirado** (FE08)
   - Lista de itens removidos
   - Botão para retornar ao catálogo

#### **✅ SUCESSOS (Success)**
1. **Compra Confirmada** (Passo 15)
   - Número do pedido
   - Informações de confirmação

2. **Cupom Aplicado** (FA04.3)
   - Valor do cupom e desconto

3. **Troca Autorizada** (FA08.5)
   - Instrução sobre devolução
   - Botão para rótulo de envio

#### **ℹ️ INFORMAÇÕES (Info)**
1. **Pedido Despachado** (FA06.3)
   - Código de rastreamento
   - Botão de rastreamento

2. **Endereço Não Cadastrado** (FE09)
   - Ação: Cadastrar

3. **Carrinho Vazio** (FE06.2b)
   - Ação: Ir para Catálogo

---

## 🎯 Fluxos Cobertos

| Fluxo | Telas | Status |
|-------|-------|--------|
| **Principal** (Registrar Pedido) | 1,2,3,4 | ✅ Completo |
| **FA01** (Carrinho Expirado) | 2, 8 | ✅ Implementado |
| **FA02** (Pagamento Reprovado) | 8 | ✅ Implementado |
| **FA03** (Múltiplos Cartões) | 3, 8 | ✅ Implementado |
| **FA04** (Cupom Promocional) | 2,3,8 | ✅ Implementado |
| **FA05** (Cupom de Troca) | 3,8 | ✅ Implementado |
| **FA06** (Despacho) | 5 | ✅ Implementado |
| **FA07** (Confirmar Entrega) | 5 | ✅ Implementado |
| **FA08/FA09** (Solicitar Troca) | 6,7 | ✅ Implementado |
| **FA10** (Estoque Insuficiente) | 8 | ✅ Implementado |
| **FA11** (Cancelar Pedido) | - | ⚠️ Referência no modal |

---

## 🎨 Design Patterns

### **Cores Utilizadas:**
- **Primária:** #2c3e50 (Azul escuro - header)
- **Sucesso:** #27ae60 (Verde)
- **Aviso:** #ffc107 (Amarelo)
- **Erro:** #e74c3c (Vermelho)
- **Info:** #3498db (Azul)

### **Componentes Reutilizáveis:**
- Badges de status
- Buttons (Primary, Secondary, Outline)
- Cards/Notifications
- Modals
- Tabelas
- Inputs e Selects

### **Responsividade:**
- Prototípos otimizados para desktop
- Podem ser adaptados para mobile com media queries

---

## 📸 Como Usar

1. Abra cada arquivo HTML em um navegador
2. Interaja com os botões e modals
3. Use para screenshots no documento
4. Customizar cores/estilos conforme necessário

---

## ✏️ Notas de Implementação

- Os protótipos usam **HTML semântico + CSS puro** (sem frameworks)
- Inclui **JavaScript mínimo** para interatividade de modals
- **Não inclui autenticação** - assumem usuário já autenticado
- **Não fazem chamadas HTTP** - são apenas UI
- **Completamente **self-contained** - cada arquivo é independente
- Podem ser integrados a um backend alterando os event listeners JS

---

## 🔄 Integração com Backend

Para integração com um backend real, será necessário:
1. Adicionar chamadas AJAX/Fetch nas buttons
2. Implementar validação de servidor
3. Adicionar tratamento de erros dinâmico
4. Implementar form submission com CSRF tokens
5. Adicionar carregamento de dados dinâmicos

---

**Data de Criação:** 13/04/2026  
**Versão:** 1.0  
**Projeto:** JakeCommerce - Entrega 05
