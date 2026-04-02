# TAREFA-ENTREGA-FRETE: Definir Entrega e Frete

## Objetivo

Implementar o sistema de selecao de endereco de entrega, calculo de frete e gestao do status de entrega dos pedidos.

## Requisitos Funcionais

| RF | Descricao | Status |
|----|-----------|--------|
| RF0034 | Calcular frete | Pendente |
| RF0035 | Selecionar endereco | Pendente |
| RF0038 | Despachar produtos (EM_TRANSPORTE) | Pendente |
| RF0039 | Confirmar entrega (ENTREGUE) | Pendente |

## Regras de Negocio

| RN | Descricao | Logica |
|----|-----------|--------|
| RN0022 | Pelo menos um endereco de entrega | Validar tipo_endereco = ENTREGA |
| RN0064 | Pedido minimo R$20 para frete gratis | valorTotal >= 20 ? frete = 0 |
| RN0039 | Status transporte: EM_TRANSPORTE | Admin despacha |
| RN0040 | Status entrega: ENTREGUE | Admin confirma |

### Calculo de Frete (Simulacao)

```
Se valorPedido >= R$20.00:
    frete = R$0.00 (GRATIS)
Senao:
    frete = calcularPorRegiao(cep)

calcularPorRegiao(cep):
    - Mesma cidade: R$5.00
    - Mesmo estado: R$10.00
    - Outro estado: R$15.00
    - (Simulacao simplificada para fins academicos)
```

### Fluxo de Estados do Pedido

```
[Pagamento APROVADO] --> EM_PROCESSAMENTO
                              |
                        [Admin: Despachar]
                              |
                        EM_TRANSPORTE
                              |
                        [Admin: Confirmar Entrega]
                              |
                          ENTREGUE
                              |
                        [Cliente pode solicitar troca]
```

## Tasks por Agente

### shipping-agent (Coordenador)

**Tasks:**
1. Calcular frete baseado no endereco
2. Gerenciar selecao de endereco de entrega
3. Coordenar fluxo de status de entrega
4. Notificar mudancas de status (futuro)

### backend-agent

**Entidades Envolvidas:**
- Endereco
- Pedido (valorFrete, status)

**Tasks:**

1. **FreteService** (`com.les.jakebooks.service`)
   ```java
   // Metodos a implementar
   FreteDTO calcularFrete(Long enderecoId, BigDecimal valorPedido)
   BigDecimal calcularFretePorRegiao(String cep)
   boolean isFreteGratis(BigDecimal valorPedido)
   List<OpcaoFreteDTO> listarOpcoesEntrega(Long enderecoId)
   ```

2. **EnderecoService** (atualizacao)
   ```java
   List<EnderecoDTO> listarEnderecosEntrega(Long clienteId)
   EnderecoDTO buscarEnderecoEntrega(Long clienteId, Long enderecoId)
   void validarEnderecoEntrega(Endereco endereco)
   ```

3. **PedidoService** (atualizacao)
   ```java
   void despacharPedido(Long pedidoId)
   void confirmarEntrega(Long pedidoId)
   void atualizarStatusPedido(Long pedidoId, StatusPedido status)
   List<PedidoDTO> listarPedidosParaDespacho()
   List<PedidoDTO> listarPedidosEmTransporte()
   ```

4. **DTOs**
   ```java
   FreteDTO {
       BigDecimal valor
       String descricao
       int prazoEstimadoDias
       boolean gratis
   }

   OpcaoFreteDTO {
       String tipo // ECONOMICO, NORMAL, EXPRESSO
       BigDecimal valor
       int prazoEstimadoDias
   }

   EnderecoEntregaDTO {
       Long id
       String nomeIdentificador
       String enderecoCompleto
       String cep
       boolean selecionado
   }
   ```

5. **Repository**
   ```java
   // EnderecoRepository
   List<Endereco> findByClienteIdAndTipoEndereco(Long clienteId, TipoEndereco tipo)

   // PedidoRepository
   List<Pedido> findByStatus(StatusPedido status)
   List<Pedido> findByStatusIn(List<StatusPedido> statuses)
   ```

### business-rules-agent

**Tasks:**

1. **Excecoes Customizadas**
   ```java
   // com.les.jakebooks.exception
   EnderecoEntregaNaoEncontradoException extends ValidacaoNegocioException
   StatusPedidoInvalidoException extends ValidacaoNegocioException
   TransicaoStatusInvalidaException extends ValidacaoNegocioException
   ```

2. **Validadores**
   ```java
   @Component
   public class EntregaValidator {
       void validarEnderecoEntrega(Endereco endereco)
       void validarTransicaoStatus(StatusPedido atual, StatusPedido novo)
       void validarPedidoParaDespacho(Pedido pedido)
       void validarPedidoParaEntrega(Pedido pedido)
   }
   ```

3. **Transicoes de Status Validas**
   ```java
   public class StatusPedidoTransicao {
       // Transicoes permitidas
       EM_PROCESSAMENTO -> EM_TRANSPORTE (despachar)
       EM_TRANSPORTE -> ENTREGUE (confirmar entrega)
       ENTREGUE -> EM_TROCA (solicitar troca)
       EM_TROCA -> TROCADO (concluir troca)
   }
   ```

### frontend-agent

**Tasks:**

1. **EnderecoController** (atualizacao)
   ```java
   @GetMapping("/checkout/endereco")
   String exibirSelecaoEndereco(Model model, Principal principal)

   @PostMapping("/checkout/endereco/selecionar")
   String selecionarEndereco(@RequestParam Long enderecoId, RedirectAttributes ra)
   ```

2. **FreteController** (`com.les.jakebooks.controller`)
   ```java
   @GetMapping("/checkout/frete")
   String exibirOpcoesFrete(Model model)

   @PostMapping("/checkout/frete/calcular")
   @ResponseBody
   FreteDTO calcularFrete(@RequestParam Long enderecoId, @RequestParam BigDecimal valorPedido)
   ```

3. **AdminPedidoController** (atualizacao)
   ```java
   @GetMapping("/admin/pedidos/despacho")
   String listarPedidosParaDespacho(Model model)

   @PostMapping("/admin/pedidos/{id}/despachar")
   String despacharPedido(@PathVariable Long id, RedirectAttributes ra)

   @GetMapping("/admin/pedidos/transporte")
   String listarPedidosEmTransporte(Model model)

   @PostMapping("/admin/pedidos/{id}/confirmar-entrega")
   String confirmarEntrega(@PathVariable Long id, RedirectAttributes ra)
   ```

4. **Templates**

   **Cliente:**
   - `templates/checkout/endereco.html` - Selecao de endereco
   - `templates/checkout/frete.html` - Opcoes de frete
   - `templates/fragments/endereco-card.html` - Card de endereco
   - `templates/fragments/frete-opcoes.html` - Lista de opcoes de frete

   **Admin:**
   - `templates/admin/pedidos/despacho.html` - Lista para despacho
   - `templates/admin/pedidos/transporte.html` - Em transporte
   - `templates/fragments/pedido-status-actions.html` - Acoes de status

5. **Componentes UI**

   **Cliente:**
   - Lista de enderecos de entrega cadastrados
   - Botao para adicionar novo endereco
   - Exibicao do frete calculado
   - Badge "FRETE GRATIS" quando aplicavel
   - Resumo do endereco selecionado

   **Admin:**
   - Tabela de pedidos pendentes de despacho
   - Botao "Despachar" por pedido
   - Tabela de pedidos em transporte
   - Botao "Confirmar Entrega" por pedido
   - Filtros por data/status

## Criterios de Aceite

### Selecao de Endereco
- [ ] Usuario visualiza todos os enderecos de entrega cadastrados
- [ ] Usuario consegue selecionar endereco para entrega
- [ ] Sistema valida que endereco e do tipo ENTREGA
- [ ] Usuario pode adicionar novo endereco durante checkout

### Calculo de Frete
- [ ] Sistema calcula frete automaticamente ao selecionar endereco
- [ ] Frete gratis para pedidos >= R$20
- [ ] Sistema exibe valor e prazo estimado
- [ ] Valor do frete e adicionado ao pedido

### Gestao de Entrega (Admin)
- [ ] Admin visualiza pedidos aguardando despacho
- [ ] Admin consegue despachar pedido (muda para EM_TRANSPORTE)
- [ ] Admin visualiza pedidos em transporte
- [ ] Admin consegue confirmar entrega (muda para ENTREGUE)
- [ ] Sistema valida transicoes de status

### Geral
- [ ] Log de todas as mudancas de status
- [ ] Mensagens claras de sucesso/erro
- [ ] Tempo de resposta < 1 segundo

## Dependencias

- **TAREFA-COMPRA.md**: Fluxo de checkout
- **Endereco**: Enderecos cadastrados pelo cliente
- **Pedido**: Pedidos criados

## Sequencia de Implementacao

1. Backend: Excecoes e Validators (business-rules-agent)
2. Backend: FreteService e atualizacoes em PedidoService (backend-agent)
3. Frontend: Controllers e Templates do cliente (frontend-agent)
4. Frontend: Controllers e Templates do admin (frontend-agent)
5. Integracao: Testes E2E (shipping-agent)

---

**Criado em:** 2026-03-31
**Ultima atualizacao:** 2026-03-31
