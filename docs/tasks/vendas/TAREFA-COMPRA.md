# TAREFA-COMPRA: Realizar uma Compra

## Objetivo

Implementar o fluxo completo de realizacao de compra, desde a validacao do carrinho ate a finalizacao do pedido com status EM_PROCESSAMENTO.

## Requisitos Funcionais

| RF | Descricao | Status |
|----|-----------|--------|
| RF0033 | Realizar compra | Pendente |
| RF0037 | Finalizar compra (status inicial: EM_PROCESSAMENTO) | Pendente |

## Regras de Negocio

### Validacoes Obrigatorias

| RN | Descricao | Validacao |
|----|-----------|-----------|
| RN0031 | Validar estoque no carrinho | Antes de iniciar checkout |
| RN0032 | Validar estoque antes da finalizacao | Momento da finalizacao |
| RN0063 | Maximo 10 unidades do mesmo livro por pedido | Por item no carrinho |
| RN0064 | Pedido minimo R$20 para frete gratis | Calculo no total |
| RN0028 | Baixa estoque apenas apos pagamento APROVADO | Pos-pagamento |

### Fluxo de Estados

```
Carrinho.ABERTO --> Pedido.EM_PROCESSAMENTO --> [Pagamento]
                                                    |
                                        Pagamento.APROVADA --> Baixa Estoque
                                        Pagamento.REPROVADA --> Manter Estoque
```

## Tasks por Agente

### checkout-agent (Coordenador)

**Tasks:**
1. Orquestrar fluxo completo de checkout
2. Validar pre-condicoes do carrinho
3. Coordenar chamadas entre payment-agent e shipping-agent
4. Garantir consistencia transacional

### backend-agent

**Entidades Envolvidas:**
- Carrinho, ItemCarrinho
- Pedido, ItemPedido
- Estoque

**Tasks:**

1. **CompraService** (`com.les.jakebooks.service`)
   ```java
   // Metodos a implementar
   void iniciarCheckout(Long clienteId)
   PedidoDTO finalizarCompra(Long carrinhoId, CheckoutDTO checkout)
   void validarCarrinho(Long carrinhoId)
   void validarEstoqueDisponivel(Long carrinhoId)
   void validarLimiteItens(Long carrinhoId)
   void converterCarrinhoEmPedido(Long carrinhoId)
   ```

2. **EstoqueService** (atualizacao)
   ```java
   void baixarEstoque(Long livroId, int quantidade)
   boolean verificarDisponibilidade(Long livroId, int quantidade)
   ```

3. **DTOs**
   ```java
   CheckoutDTO: enderecoEntregaId, pagamentoDTO
   PedidoResumoDTO: itens, valorTotal, valorFrete, status
   ```

4. **Repository**
   ```java
   // CarrinhoRepository
   Optional<Carrinho> findByClienteIdAndStatus(Long clienteId, StatusCarrinho status)

   // PedidoRepository
   List<Pedido> findByClienteIdOrderByDataCriacaoDesc(Long clienteId)
   ```

### business-rules-agent

**Tasks:**

1. **Excecoes Customizadas**
   ```java
   // com.les.jakebooks.exception
   EstoqueInsuficienteException extends ValidacaoNegocioException
   LimiteItensExcedidoException extends ValidacaoNegocioException
   CarrinhoVazioException extends ValidacaoNegocioException
   PedidoMinimoException extends ValidacaoNegocioException
   ```

2. **Validadores**
   ```java
   @Component
   public class CompraValidator {
       void validarEstoque(Carrinho carrinho)
       void validarLimiteItens(ItemCarrinho item)
       void validarPedidoMinimo(BigDecimal valorTotal)
   }
   ```

3. **Constantes de Negocio**
   ```java
   public class CompraConstants {
       public static final int MAX_ITENS_MESMO_LIVRO = 10;
       public static final BigDecimal PEDIDO_MINIMO_FRETE_GRATIS = new BigDecimal("20.00");
   }
   ```

### frontend-agent

**Tasks:**

1. **CheckoutController** (`com.les.jakebooks.controller`)
   ```java
   @GetMapping("/checkout")
   String exibirCheckout(Model model, Principal principal)

   @PostMapping("/checkout/finalizar")
   String finalizarCompra(@ModelAttribute CheckoutDTO dto, RedirectAttributes ra)
   ```

2. **Templates**
   - `templates/checkout/index.html` - Tela principal de checkout
   - `templates/checkout/resumo.html` - Resumo do pedido
   - `templates/checkout/confirmacao.html` - Confirmacao pos-compra
   - `templates/fragments/checkout-steps.html` - Steps do checkout

3. **Componentes UI**
   - Wizard de checkout (steps)
   - Resumo do carrinho
   - Selecao de endereco (integrar com shipping-agent)
   - Selecao de pagamento (integrar com payment-agent)
   - Botao finalizar com validacao JS

## Criterios de Aceite

- [ ] Usuario consegue iniciar checkout a partir do carrinho
- [ ] Sistema valida estoque de todos os itens antes de prosseguir
- [ ] Sistema bloqueia compra com mais de 10 unidades do mesmo livro
- [ ] Sistema cria pedido com status EM_PROCESSAMENTO
- [ ] Sistema converte itens do carrinho para itens do pedido
- [ ] Sistema altera status do carrinho para FINALIZADO
- [ ] Baixa de estoque ocorre apenas apos pagamento APROVADO
- [ ] Erros de validacao exibem mensagens claras ao usuario
- [ ] Log de transacao registrado conforme RNF0012

## Dependencias

- **TAREFA-PAGAMENTO.md**: Processamento do pagamento
- **TAREFA-ENTREGA-FRETE.md**: Selecao de endereco e calculo de frete

## Sequencia de Implementacao

1. Backend: Excecoes e Validators (business-rules-agent)
2. Backend: Repository e Service (backend-agent)
3. Frontend: Controller e Templates (frontend-agent)
4. Integracao: Testes E2E (checkout-agent)

---

**Criado em:** 2026-03-31
**Ultima atualizacao:** 2026-03-31
