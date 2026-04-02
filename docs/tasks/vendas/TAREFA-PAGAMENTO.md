# TAREFA-PAGAMENTO: Definir Forma de Pagamento

## Objetivo

Implementar o sistema de pagamento com suporte a multiplos cartoes, cupons de troca e cupons promocionais, aplicando todas as regras de negocio de consumo e geracao de cupons.

## Requisitos Funcionais

| RF | Descricao | Status |
|----|-----------|--------|
| RF0036 | Selecionar pagamento (cartao, cupom promocional, cupom de troca) | Pendente |

## Regras de Negocio

### Regras de Cupons

| RN | Descricao | Logica |
|----|-----------|--------|
| RN0033 | Apenas um cupom promocional por compra | Validar tipo=PROMOCIONAL, max 1 |
| RN0035 | Consumir cupons antes do cartao | Ordem: cupons primeiro |
| RN0036 | Gerar cupom para excedente | Se cupom > total, gerar novo cupom |

### Regras de Cartao

| RN | Descricao | Logica |
|----|-----------|--------|
| RN0034 | Multiplos cartoes permitidos (minimo R$10 por cartao) | Validar valor >= 10 por cartao |
| RN0037 | Validar pagamento | Verificar dados do cartao |
| RN0038 | Status pagamento: APROVADA ou REPROVADA | Simular gateway |
| RN0065 | 3 pagamentos REPROVADOS consecutivos bloqueiam carrinho | Contador por cliente |

### Tipos de Cupom

```
Cupom.tipo:
  - PROMOCIONAL: cupom de desconto da loja (max 1 por compra)
  - TROCA: cupom gerado por devolucao de produto (sem limite)
```

### Fluxo de Pagamento

```
[Selecionar Cupons] --> [Aplicar Cupons] --> [Calcular Restante]
                                                    |
                                    valor_restante > 0 ?
                                        |           |
                                       SIM         NAO
                                        |           |
                            [Selecionar Cartoes]  [Finalizar]
                                        |
                            [Processar Pagamento]
                                        |
                            APROVADA --> [Finalizar]
                            REPROVADA --> [Incrementar Contador]
                                              |
                                    contador >= 3 ?
                                        |
                                       SIM --> [Bloquear Carrinho]
```

## Tasks por Agente

### payment-agent (Coordenador)

**Tasks:**
1. Orquestrar selecao e aplicacao de formas de pagamento
2. Calcular distribuicao de valores entre cupons e cartoes
3. Gerenciar excedente de cupom
4. Controlar tentativas de pagamento

### backend-agent

**Entidades Envolvidas:**
- Pagamento, PagamentoCartao, PagamentoCupom
- Cupom
- Cartao

**Tasks:**

1. **PagamentoService** (`com.les.jakebooks.service`)
   ```java
   // Metodos a implementar
   PagamentoDTO processarPagamento(Long pedidoId, PagamentoRequestDTO request)
   void aplicarCupons(Pagamento pagamento, List<String> codigosCupons)
   void aplicarCartoes(Pagamento pagamento, List<PagamentoCartaoDTO> cartoes)
   BigDecimal calcularValorRestante(Pagamento pagamento)
   void validarMinimoCartao(BigDecimal valor)
   void validarCupomPromocionalUnico(List<Cupom> cupons)
   CupomDTO gerarCupomExcedente(Long clienteId, BigDecimal valor)
   StatusPagamento simularGateway(List<PagamentoCartaoDTO> cartoes)
   void incrementarTentativasReprovadas(Long clienteId)
   void verificarBloqueioCarrinho(Long clienteId)
   ```

2. **CupomService** (`com.les.jakebooks.service`)
   ```java
   CupomDTO buscarPorCodigo(String codigo)
   List<CupomDTO> listarCuponsCliente(Long clienteId)
   void validarCupomAtivo(Cupom cupom)
   void consumirCupom(Cupom cupom, BigDecimal valorUtilizado)
   CupomDTO criarCupomTroca(Long clienteId, BigDecimal valor)
   ```

3. **DTOs**
   ```java
   // Request
   PagamentoRequestDTO {
       List<String> codigosCupons
       List<PagamentoCartaoDTO> cartoes
   }

   PagamentoCartaoDTO {
       Long cartaoId
       BigDecimal valor
   }

   // Response
   PagamentoResultadoDTO {
       StatusPagamento status
       BigDecimal valorTotal
       BigDecimal valorCupons
       BigDecimal valorCartoes
       CupomDTO cupomExcedente // se houver
       String mensagem
   }
   ```

4. **Repository**
   ```java
   // CupomRepository
   Optional<Cupom> findByCodigoAndAtivoTrue(String codigo)
   List<Cupom> findByClienteIdAndAtivoTrueAndTipo(Long clienteId, TipoCupom tipo)

   // PagamentoRepository
   Optional<Pagamento> findByPedidoId(Long pedidoId)
   ```

### business-rules-agent

**Tasks:**

1. **Excecoes Customizadas**
   ```java
   // com.les.jakebooks.exception
   CupomInvalidoException extends ValidacaoNegocioException
   CupomJaUtilizadoException extends ValidacaoNegocioException
   CupomPromocionalDuplicadoException extends ValidacaoNegocioException
   ValorMinimoCartaoException extends ValidacaoNegocioException
   PagamentoReprovadoException extends ValidacaoNegocioException
   CarrinhoBloqueadoException extends ValidacaoNegocioException
   ```

2. **Validadores**
   ```java
   @Component
   public class PagamentoValidator {
       void validarCupomPromocionalUnico(List<Cupom> cupons)
       void validarMinimoCartao(BigDecimal valor)
       void validarTentativasReprovadas(int tentativas)
       void validarValorTotalPagamento(BigDecimal pago, BigDecimal total)
   }
   ```

3. **Constantes de Negocio**
   ```java
   public class PagamentoConstants {
       public static final BigDecimal VALOR_MINIMO_CARTAO = new BigDecimal("10.00");
       public static final int MAX_TENTATIVAS_REPROVADAS = 3;
   }
   ```

### frontend-agent

**Tasks:**

1. **PagamentoController** (`com.les.jakebooks.controller`)
   ```java
   @GetMapping("/checkout/pagamento")
   String exibirPagamento(Model model, Principal principal)

   @PostMapping("/checkout/pagamento/aplicar-cupom")
   String aplicarCupom(@RequestParam String codigo, RedirectAttributes ra)

   @PostMapping("/checkout/pagamento/processar")
   String processarPagamento(@ModelAttribute PagamentoRequestDTO dto, RedirectAttributes ra)
   ```

2. **Templates**
   - `templates/checkout/pagamento.html` - Tela de selecao de pagamento
   - `templates/fragments/cupom-form.html` - Formulario de cupom
   - `templates/fragments/cartao-select.html` - Selecao de cartoes
   - `templates/fragments/pagamento-resumo.html` - Resumo de valores

3. **Componentes UI**
   - Lista de cupons disponiveis do cliente
   - Campo para inserir codigo de cupom promocional
   - Selecao de cartoes cadastrados
   - Input de valor por cartao
   - Adicionar mais cartoes dinamicamente (JS)
   - Resumo: valor total, cupons aplicados, restante no cartao
   - Feedback de pagamento aprovado/reprovado

4. **Validacao Frontend (JS)**
   ```javascript
   // Validacoes a implementar
   - Validar valor minimo R$10 por cartao
   - Validar soma dos valores = total restante
   - Impedir mais de 1 cupom promocional
   - Atualizar resumo em tempo real
   ```

## Criterios de Aceite

### Cupons
- [ ] Usuario consegue listar cupons disponiveis (troca e promocional)
- [ ] Usuario consegue aplicar cupom por codigo
- [ ] Sistema bloqueia mais de 1 cupom promocional
- [ ] Sistema permite multiplos cupons de troca
- [ ] Sistema gera cupom para valor excedente
- [ ] Cupons sao consumidos antes dos cartoes

### Cartoes
- [ ] Usuario consegue selecionar multiplos cartoes
- [ ] Sistema valida minimo R$10 por cartao
- [ ] Usuario consegue distribuir valor entre cartoes
- [ ] Sistema processa pagamento e retorna status

### Bloqueio
- [ ] Sistema conta tentativas reprovadas consecutivas
- [ ] Sistema bloqueia carrinho apos 3 reprovacoes
- [ ] Sistema exibe mensagem de bloqueio

### Geral
- [ ] Resumo de pagamento atualiza em tempo real
- [ ] Mensagens de erro claras para cada validacao
- [ ] Log de transacao registrado

## Dependencias

- **TAREFA-COMPRA.md**: Fluxo geral de checkout
- **Cartao**: Cartoes cadastrados pelo cliente
- **Cupom**: Cupons disponiveis/gerados

## Sequencia de Implementacao

1. Backend: Excecoes e Validators (business-rules-agent)
2. Backend: CupomService e PagamentoService (backend-agent)
3. Frontend: Controller e Templates (frontend-agent)
4. Integracao: Testes de fluxo completo (payment-agent)

---

**Criado em:** 2026-03-31
**Ultima atualizacao:** 2026-03-31
