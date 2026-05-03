# AGENTE: Implementação Completa de Compra e Pagamento

**Data**: 02/05/2026  
**Status**: Em Planejamento  
**Objetivo**: Implementar fluxos de compra, pagamento multi-cartão + cupom, e registro de endereço/cartão no checkout

---

## 1. CONTEXTO DO SISTEMA

### Arquitetura
JakeBooks segue **arquitetura em camadas estrita** (sem exceções):
- **Controller**: Recebe HTTP, chama Services, retorna views/redirects. ZERO lógica de negócio.
- **Service**: Toda lógica de negócio e transações (@Transactional)
- **Repository**: JpaRepository puro; JPQL se necessário
- **Domain**: JPA entities sem lógica de negócio
- **DTO**: Validações com @NotNull, @Pattern, @Min, @Size
- **Config**: Spring Security, beans, etc

### Stack
- **Java 21** (Maven)
- **Spring Boot + Spring Security** (sessão)
- **PostgreSQL** (ddl-auto=create-drop em dev)
- **Thymeleaf** (templates server-side)
- **Sem Lombok** (getters/setters explícitos para clareza)

### Padrões Obrigatórios
✅ **Sem Lombok**: declarar getters/setters explícitos  
✅ **@Transactional em Services**: garantidas transações  
✅ **LogTransacao**: registrar operações de escrita (RNF0012)  
✅ **DTOs com validações**: JSON/form data validados no boundary  
✅ **Sem lógica em Controllers**: business logic fica em Services  

---

## 2. FLUXOS A IMPLEMENTAR

### Fluxo A: Cliente Realizar Compra
```
[Visualizar Carrinho] 
  → [Validar Estoque] (RN0031, RN0032)
  → [Validar Min R$20] (RN0064)
  → [Validar Max 10 unidades/livro] (RN0063)
  → [Prosseguir para Checkout]
```

**Controllers Envolvidos:**
- `CarrinhoController`: GET /carrinho (listar itens)
- `PedidoController`: POST /checkout/validar (validação pré-pagamento)

**Services Envolvidos:**
- `CarrinhoService.validarEstoque()`: verifica quantidade disponível
- `CarrinhoService.validarValorMinimo()`: verifica R$20 mínimo
- `CarrinhoService.validarMaximoUnidades()`: verifica 10 unidades max/livro
- `PedidoService.criarPedidoDoCarrinho()`: cria Pedido com status EM_PROCESSAMENTO

---

### Fluxo B: Cliente Pagar (Combinações de Cartão + Cupom)
```
[Selecionar Pagamento] 
  → [Validar Cupom] (RN0033 - 1 cupom max)
  → [Consumir Cupom ANTES de Cartão] (RN0035)
  → [Validar Cartão(ões)] (RN0034 - mín R$10/cartão)
  → [Processar Pagamento]
  → [Validar Status] (APROVADA ou REPROVADA)
  → [SE APROVADA: Reduzir Estoque] (RN005x)
  → [SE REPROVADA: Contar Rejeição] (RN0065 - bloqueia após 3)
```

**Cenários de Pagamento:**
1. **1 Cartão**: R$100 inteiro em 1 cartão
2. **2+ Cartões**: R$50 cada cartão (mín R$10 cada) - RN0034
3. **Cupom + 1 Cartão**: Usar cupom R$30, cartão R$70 - RN0035
4. **Cupom + 2 Cartões**: Usar cupom R$20, cartão1 R$40, cartão2 R$40
5. **Cupom com Excedente**: Cupom R$100 para compra R$80 → gera cupom de R$20 - RN0036

**Controllers Envolvidos:**
- `PagamentoController`: POST /api/checkout/pagamento/processar
- `PagamentoController`: POST /api/checkout/pagamento/validar-cupom

**Services Envolvidos:**
- `PagamentoService.processarPagamento()`: orquestra fluxo de pagamento
- `PagamentoService.consumirCupom()`: reduz saldo do cupom
- `PagamentoService.validarCartoes()`: valida múltiplos cartões
- `PagamentoService.gerarCupomExcedente()`: genera cupom para overpayment
- `CupomService.validarCupom()`: valida disponibilidade, tipo e valor
- `EstoqueService.reduzirEstoque()`: baixa estoque APÓS pagamento APROVADA
- `PaymentGateway`/Mock: simula decisão APROVADA/REPROVADA

**DTOs:**
```java
// Request
class PagamentoRequestDTO {
  Long pedidoId;
  Long cupomId;  // nullable
  List<PagamentoCartaoRequestDTO> cartoes;  // ≥1
}

class PagamentoCartaoRequestDTO {
  Long cartaoId;
  BigDecimal valor;  // ≥10
}

// Response
class PagamentoResponseDTO {
  Long pedidoId;
  StatusPagamento status;  // APROVADA ou REPROVADA
  BigDecimal valorTotal;
  List<PagamentoCartaoDTO> cartoes;
  PagamentoCupomDTO cupom;  // nullable
}
```

---

### Fluxo C: Registrar Novo Cartão + Endereço no Checkout
```
[Checkout em Andamento] 
  → [Usuário clica "Novo Cartão"]
  → [Modal/Form: Número, Nome, Bandeira, CVV]
  → [Validações: Formato cartão, CVV 3-4 dígitos, bandeira cadastrada] (RN0024, RN0025)
  → [Salvar no BD + Associar ao Cliente]
  → [Retornar para seleção de meio de pagamento]
  
  OU
  
  → [Usuário clica "Novo Endereço"]
  → [Modal/Form: Logradouro, Número, Bairro, CEP, Cidade, Estado, País]
  → [Validações: CEP formato, estado válido, tipo endereço] (RN0023)
  → [Validar que pelo menos 1 é de entrega] (RN0022)
  → [Salvar no BD + Associar ao Cliente]
  → [Retornar para seleção de endereço de entrega]
```

**Controllers Envolvidos:**
- `CartaoController`: POST /api/cartao/novo (criar no checkout)
- `EnderecoController`: POST /api/endereco/novo (criar no checkout)
- `CheckoutController`: GET /checkout (exibir form completo)

**Services Envolvidos:**
- `CartaoService.registrarCartao()`: cria novo Cartao, valida bandeira
- `EnderecoService.registrarEndereco()`: cria novo Endereco, valida CEP/estado
- `CartaoService.validarCartao()`: valida número, CVV, bandeira, duplicação
- `EnderecoService.validarEndereco()`: valida campos obrigatórios, estado

**DTOs:**
```java
// Novo Cartão
class CartaoRequestDTO {
  String numero;           // @Pattern("[0-9]{13,19}")
  String nomeImpresso;     // @NotNull, @Size(5, 50)
  String bandeira;         // @NotNull enum VISA, MASTERCARD, ELO, AMEX
  String codigoSeguranca;  // @Pattern("[0-9]{3,4}")
  Boolean preferencial;    // default false
}

// Novo Endereço
class EnderecoRequestDTO {
  String nomeIdentificador;  // @NotNull, "Casa", "Trabalho", etc
  String tipoResidencia;     // @NotNull enum CASA, APTO, COMERCIAL
  String logradouro;         // @NotNull
  String numero;             // @NotNull
  String bairro;             // @NotNull
  String cep;                // @Pattern("[0-9]{5}-?[0-9]{3}")
  String cidade;             // @NotNull
  String estado;             // @NotNull, @Size(2)
  String pais;               // @NotNull
  TipoEndereco tipo;         // COBRANCA, ENTREGA, AMBOS
}
```

---

## 3. REGRAS DE NEGÓCIO CRÍTICAS

| RN | Descrição | Implementação |
|---|---|---|
| **RN0031** | Validar estoque no carrinho | CarrinhoService.validarEstoque() |
| **RN0032** | Validar estoque antes de finalizar | PedidoService pré-pagamento |
| **RN0033** | 1 cupom promocional por compra | PagamentoService.validarUmCupom() |
| **RN0034** | Múltiplos cartões, mín R$10 cada | PagamentoService.validarCartoes() |
| **RN0035** | Consumir cupom ANTES de cartão | PagamentoService.processar() order |
| **RN0036** | Gerar cupom para excedente | CupomService.gerarCupomExcedente() |
| **RN005x** | Estoque reduzido APÓS APROVADA | EstoqueService na callback pagamento |
| **RN0063** | Máximo 10 unidades/livro | CarrinhoService.validarMaximoUnidades() |
| **RN0064** | Mínimo R$20 sem frete | PedidoService.validarValorMinimo() |
| **RN0065** | 3 rejeições bloqueiam carrinho | PagamentoService.contarRejeicoes() |
| **RN0023** | Campos obrigatórios endereço | EnderecoValidator + EnderecoRequestDTO |
| **RN0024** | Campos obrigatórios cartão | CartaoValidator + CartaoRequestDTO |
| **RN0025** | Bandeira cadastrada | CartaoService.validarBandeira() |
| **RN0021** | Mín 1 endereço cobrança | CartaoService ou ClienteService |
| **RN0022** | Mín 1 endereço entrega | EnderecoService.validarMinEntrarega() |
| **RNF0012** | Log transações | LogTransacao service |

---

## 4. ARQUITETURA DA SOLUÇÃO

### Estrutura de Pacotes
```
src/main/java/com/les/jakebooks/
├── controller/
│   ├── CarrinhoController.java (refactor: adicionar validações)
│   ├── PedidoController.java (refactor: nova rota pré-checkout)
│   ├── PagamentoController.java (nova: processar pagamento)
│   ├── CartaoController.java (nova: registrar cartão no checkout)
│   └── EnderecoController.java (nova: registrar endereço no checkout)
├── service/
│   ├── CarrinhoService.java (refactor: validações RN0031, RN0063, RN0064)
│   ├── PedidoService.java (refactor: criar pedido + validações)
│   ├── PagamentoService.java (nova: processamento de pagamento)
│   ├── CupomService.java (refactor: consumir + gerar cupom)
│   ├── EstoqueService.java (refactor: reduzir APÓS aprovação)
│   ├── CartaoService.java (refactor: adicionar validação + novo)
│   ├── EnderecoService.java (refactor: validações + novo)
│   └── PaymentGatewayService.java (nova: simula aprovação/rejeição)
├── repository/
│   ├── PagamentoCartaoRepository.java (novo)
│   ├── PagamentoCupomRepository.java (novo)
│   └── ... (revisar existentes)
├── domain/
│   ├── Pagamento.java (refactor: adicionar campos)
│   ├── PagamentoCartao.java (refactor/novo: compor com Cartao)
│   ├── PagamentoCupom.java (refactor/novo: compor com Cupom)
│   ├── Cupom.java (refactor: adicionar status, data_geracao)
│   └── ... (revisar relacionamentos)
├── dto/
│   ├── PagamentoRequestDTO.java (novo)
│   ├── PagamentoResponseDTO.java (novo)
│   ├── CartaoRequestDTO.java (novo)
│   ├── EnderecoRequestDTO.java (novo)
│   └── ... (refactor existentes)
└── exception/
    └── PagamentoException.java (novo: centralizar erros)

src/main/resources/
├── templates/
│   ├── checkout/
│   │   ├── carrinho.html (refactor: validações UI)
│   │   ├── pagamento.html (novo: form pagamento)
│   │   ├── novo-cartao-modal.html (novo)
│   │   └── novo-endereco-modal.html (novo)
│   └── ...
└── data.sql (refactor: dados de teste)

src/test/java/com/les/jakebooks/
├── service/
│   ├── PagamentoServiceTest.java (novo: 5+ testes)
│   ├── CupomServiceTest.java (refactor: testes cupom)
│   ├── EstoqueServiceTest.java (refactor: testes estoque pós-aprovação)
│   └── ...
├── controller/
│   ├── PagamentoControllerTest.java (novo: integration tests)
│   ├── CartaoControllerTest.java (novo)
│   ├── EnderecoControllerTest.java (novo)
│   └── ...
└── integration/
    └── CheckoutFlowIT.java (novo: E2E simples)
```

---

## 5. TAREFAS DE IMPLEMENTAÇÃO

### Fase 1: Setup & Domain (2-3 horas)
- [ ] 1.1 Revisar `Pagamento.java`, `Cupom.java`, `Cartao.java`, `Endereco.java`
- [ ] 1.2 Validar relacionamentos 1:N e N:N
- [ ] 1.3 Adicionar campos faltantes (e.g., `Cupom.ativo`, `Cupom.dataGeracao`, `Cartao.codigoSeguranca`)
- [ ] 1.4 Criar repositories `PagamentoCartaoRepository`, `PagamentoCupomRepository`
- [ ] 1.5 Atualizar `data.sql`: inserir cartões, endereços, cupons de teste

### Fase 2: Services (Núcleo de Negócio) (4-5 horas)
- [ ] 2.1 **CarrinhoService**: refactor `validarEstoque()`, `validarValorMinimo()`, `validarMaximoUnidades()`
- [ ] 2.2 **PedidoService**: refactor `criarPedidoDoCarrinho()`, adicionar validações pré-checkout
- [ ] 2.3 **PagamentoService** (novo):
  - [ ] 2.3.1 `processarPagamento(pedido, cupom?, cartoes)` → orquestra fluxo
  - [ ] 2.3.2 `validarCupom(cupom)` → apenas 1, válido, tipo certo
  - [ ] 2.3.3 `consumirCupom()` → reduz valor/marca gasto
  - [ ] 2.3.4 `validarCartoes()` → múltiplos, mín R$10 cada
  - [ ] 2.3.5 `contarRejeicoes()` → 3 consecutivas bloqueiam
- [ ] 2.4 **CupomService**: refactor `validarDisponibilidade()`, novo `gerarCupomExcedente()`
- [ ] 2.5 **EstoqueService**: refactor `reduzirEstoque()` → APENAS após `StatusPagamento.APROVADA`
- [ ] 2.6 **CartaoService**: refactor `registrarCartao()`, novo validar bandeira
- [ ] 2.7 **EnderecoService**: novo `registrarEndereco()`, validações CEP/estado
- [ ] 2.8 **PaymentGatewayService** (novo): simula decisão (80% APROVADA, 20% REPROVADA)
- [ ] 2.9 **LogTransacaoService**: usado em todas operações de escrita

### Fase 3: DTOs & Validações (2 horas)
- [ ] 3.1 Criar `PagamentoRequestDTO`, `PagamentoResponseDTO`
- [ ] 3.2 Criar `PagamentoCartaoRequestDTO`
- [ ] 3.3 Criar `CartaoRequestDTO` com @Pattern, @NotNull
- [ ] 3.4 Criar `EnderecoRequestDTO` com validações de CEP
- [ ] 3.5 Adicionar validators customizados se necessário (@Validator)

### Fase 4: Controllers & Templates (3 horas)
- [ ] 4.1 **CarrinhoController**: refactor GET /carrinho com validações
- [ ] 4.2 **PedidoController**: novo POST /checkout/validar
- [ ] 4.3 **PagamentoController** (novo): 
  - [ ] 4.3.1 POST /api/checkout/pagamento/validar-cupom
  - [ ] 4.3.2 POST /api/checkout/pagamento/processar
  - [ ] 4.3.3 GET /api/checkout/pagamento/status/{pedidoId}
- [ ] 4.4 **CartaoController** (novo):
  - [ ] 4.4.1 POST /api/cartao/novo (checkout context)
  - [ ] 4.4.2 GET /api/cliente/cartoes (listar meus cartões)
- [ ] 4.5 **EnderecoController** (novo):
  - [ ] 4.5.1 POST /api/endereco/novo (checkout context)
  - [ ] 4.5.2 GET /api/cliente/enderecos (listar meus endereços)
- [ ] 4.6 **Templates Thymeleaf**:
  - [ ] 4.6.1 `templates/checkout/carrinho.html`: refactor com validações
  - [ ] 4.6.2 `templates/checkout/pagamento.html` (novo): form completo
  - [ ] 4.6.3 `templates/checkout/novo-cartao-modal.html` (novo)
  - [ ] 4.6.4 `templates/checkout/novo-endereco-modal.html` (novo)
  - [ ] 4.6.5 JavaScript: AJAX para validação em tempo real

### Fase 5: Testes (4-5 horas)
- [ ] 5.1 **PagamentoServiceTest**: 
  - [ ] 5.1.1 Teste: 1 cartão + sucesso
  - [ ] 5.1.2 Teste: múltiplos cartões + sucesso
  - [ ] 5.1.3 Teste: cupom + cartão (RN0035)
  - [ ] 5.1.4 Teste: cupom excedente
  - [ ] 5.1.5 Teste: rejeição 3x → bloqueio
- [ ] 5.2 **CupomServiceTest**: novo `gerarCupomExcedente()`
- [ ] 5.3 **EstoqueServiceTest**: estoque reduzido APÓS aprovação
- [ ] 5.4 **CartaoServiceTest**: novo `registrarCartao()`, validar bandeira
- [ ] 5.5 **EnderecoServiceTest**: novo `registrarEndereco()`, validações
- [ ] 5.6 **PagamentoControllerTest** (integration): @MockMvc
- [ ] 5.7 **CartaoControllerTest** (integration): POST novo cartão
- [ ] 5.8 **EnderecoControllerTest** (integration): POST novo endereço
- [ ] 5.9 **CheckoutFlowIT** (E2E): fluxo completo (em dev, usar `TestRestTemplate`)
- [ ] 5.10 IronOut: mínimo 80% cobertura jacoco

### Fase 6: Integração & Revisão (2 horas)
- [ ] 6.1 Rodar `mvn clean test` → todos passam
- [ ] 6.2 Rodar `mvn test jacoco:report` → verificar cobertura
- [ ] 6.3 Revisar: RNs críticas implementadas?
- [ ] 6.4 Revisar: arquitetura em camadas respeitada?
- [ ] 6.5 Revisar: sem Lombok? sem lógica em Controller?
- [ ] 6.6 Revisar: LogTransacao em operações escrita?
- [ ] 6.7 Revisar: enum StatusPagamento APENAS APROVADA/REPROVADA?
- [ ] 6.8 Revisar: estoque reduzido APÓS aprovação?
- [ ] 6.9 Criar commit com mensagem clara (e.g., `:sparkles: implementa fluxo completo de compra e pagamento`)

---

## 6. EXEMPLO DE FLUXO INTEGRADO

```
1. Cliente acessa /carrinho
   → CarrinhoController.verCarrinho()
   → Exibe itens + somatório

2. Cliente clica "Prosseguir para Checkout"
   → POST /checkout/validar
   → PedidoService.criarPedidoDoCarrinho()
     - Valida: RN0031, RN0032, RN0063, RN0064
     - Cria Pedido(status=EM_PROCESSAMENTO, valorTotal, valorFrete)
   → Redireciona para /checkout/pagamento

3. Cliente em /checkout/pagamento vê:
   - Resumo do Pedido (itens, valor)
   - Se tem cupom: input para número do cupom
   - Lista de cartões salvos
   - Botão "Registrar novo cartão" (modal)
   - Se precisa alterar endereço: lista + botão "Novo endereço" (modal)

4. Cliente clica "Novo Cartão"
   → Modal abre com form (número, nome, bandeira, CVV)
   → Validacoes client-side + server-side (CartaoRequestDTO)
   → CartaoController.registrarCartao()
   → CartaoService.registrarCartao()
     - Valida: bandeira existe, número formato, CVV 3-4 dígitos
     - Cria Cartao + associa a Cliente
   → Modal retorna com novo cartão na lista

5. Cliente seleciona: Cupom (R$30) + Cartão A (R$70)
   → POST /api/checkout/pagamento/processar
   ```json
   {
     "pedidoId": 123,
     "cupomId": 456,
     "cartoes": [
       { "cartaoId": 789, "valor": 70.00 }
     ]
   }
   ```

6. PagamentoController.processar()
   → PagamentoService.processarPagamento()
     a) Valida cupom (1 apenas, tipo certo, valor ≤ total)
     b) Consome cupom (reduz valor, marca data)
     c) Valida cartões (múltiplos, mín R$10 cada)
     d) Chama PaymentGatewayService.autorizar(cartoes)
        - Simula: 80% APROVADA, 20% REPROVADA
     e) SE APROVADA:
        - Cria Pagamento(status=APROVADA)
        - Cria PagamentoCartao (vínculo)
        - Cria PagamentoCupom (vínculo)
        - EstoqueService.reduzirEstoque() para cada item
        - LogTransacao registra tudo
        - Retorna PagamentoResponseDTO(status=APROVADA)
     f) SE REPROVADA:
        - Incrementa contador de rejeições
        - SE 3 rejeições: bloqueia Carrinho (status=EXPIRADO)
        - Cria Pagamento(status=REPROVADA)
        - LogTransacao registra tudo
        - Retorna PagamentoResponseDTO(status=REPROVADA, mensagem=razão)

7. Cliente vê resultado
   → SE APROVADA: "Compra realizada! Pedido #123"
      - Redireciona para /pedido/123
   → SE REPROVADA: "Pagamento recusado. Tente outro cartão ou cupom"
      - Fica em /checkout/pagamento (pre-filled com dados)

```

---

## 7. VALIDAÇÕES NO CHECKOUT (RN/RNF)

| Campo | Validação | RN | Cenário |
|---|---|---|---|
| Valor Mínimo | ≥ R$20 | RN0064 | Sem frete |
| Unidades/Livro | ≤ 10 | RN0063 | Por livro |
| Estoque | Qtd ≥ carrinho | RN0031, RN0032 | Pré-pagamento |
| Cupom | 1 apenas, ativo | RN0033 | Seleção cupom |
| Cupom | Tipo: PROMOCIONAL ou TROCA | RN0033 | Validação cupom |
| Cupom | Consumir ANTES de cartão | RN0035 | Order de processamento |
| Cartão | ≥ R$10 por cartão | RN0034 | Múltiplos cartões |
| Cartão | Número válido (13-19 dígitos) | RN0024 | Formato |
| Cartão | CVV 3-4 dígitos | RN0024 | Formato |
| Cartão | Bandeira existe | RN0025 | Validação BD |
| Endereço | Campos obrigatórios | RN0023 | Novo endereço |
| Endereço | CEP formato correto | RN0023 | Novo endereço |
| Endereço | Estado válido (ENUM) | RN0023 | Novo endereço |
| Rejeição | ≤ 2 REPROVADAS consecutivas | RN0065 | Contagem |
| Estoque | Reduzido APÓS APROVADA | RN005x | Callback |
| Cupom | Gerar excedente (overpay) | RN0036 | Cenário cupom |

---

## 8. CRITÉRIO DE ACEITAÇÃO

### Checklist de Implementação
- [ ] **Domain**: Todos os campos presentes (Pagamento, PagamentoCartao, PagamentoCupom, Cupom, Cartao, Endereco)
- [ ] **Services**: Todos 9 services criados/refatorados com lógica correta
- [ ] **DTOs**: Validações (@NotNull, @Pattern, etc) presentes em todos DTOs
- [ ] **Controllers**: 5 controllers (Carrinho, Pedido, Pagamento, Cartao, Endereco) sem lógica
- [ ] **Repositories**: PagamentoCartaoRepository, PagamentoCupomRepository criados
- [ ] **Templates**: Thymeleaf templates para carrinho, pagamento, modais novos
- [ ] **Tests**: Mínimo 15 test classes (unit + integration), ≥80% cobertura

### Validação Funcional
- [ ] **RN0031**: Validar estoque no carrinho → passa
- [ ] **RN0032**: Validar estoque antes de finalizar → passa
- [ ] **RN0033**: 1 cupom por compra → passa
- [ ] **RN0034**: Múltiplos cartões, mín R$10 cada → passa
- [ ] **RN0035**: Consumir cupom ANTES de cartão → passa
- [ ] **RN0036**: Gerar cupom excedente → passa
- [ ] **RN005x**: Estoque reduzido APÓS APROVADA → passa
- [ ] **RN0063**: Máximo 10 unidades/livro → passa
- [ ] **RN0064**: Mínimo R$20 → passa
- [ ] **RN0065**: 3 rejeições bloqueiam carrinho → passa
- [ ] **RN0023, RN0024, RN0025**: Novo cartão/endereço validados → passa
- [ ] **RNF0012**: Todas operações escrita em LogTransacao → passa
- [ ] **Padrão**: Sem Lombok, @Transactional em Services, Controllers sem lógica → passa
- [ ] **Build**: `mvn clean package -DskipTests` sucesso
- [ ] **Tests**: `mvn clean test` todos passam
- [ ] **Coverage**: `mvn test jacoco:report` ≥ 80%

---

## 9. REFERÊNCIAS

### Requisitos Gerais
📄 [requisitoss_copilot.md](../../general/requisitoss_copilot.md) — Modelo de domínio, RFs, RNs  
📄 [CLAUDE.md](../../CLAUDE.md) — Arquitetura, padrões, stack

### Documentação Anterior (Entrega 07)
📄 [Entrega 07 - Agentes](./entrega_07_agentes.md) — 4 agentes especializados definidos  
📄 [Entrega 07 - Prompt Mestre](./entrega_07_prompt_mestre.md) — 18 cenários, 7 camadas architetura

### Banco de Dados
📄 [src/main/resources/schema.sql](../jakebooks/src/main/resources/schema.sql) — Schema DDL  
📄 [src/main/resources/data.sql](../jakebooks/src/main/resources/data.sql) — Dados iniciais

### Código Existente
📁 [jakebooks/src/main/java/com/les/jakebooks/](../jakebooks/src/main/java/com/les/jakebooks/) — Implementação atual

---

## 10. NOTAS IMPORTANTES

### ⚠️ Armadilhas Comuns
1. **Lombok**: Não usar. Declarar getters/setters explícitos sempre.
2. **Lógica em Controller**: Colocar TUDO em Service. Controller = despachante.
3. **@Transactional**: Marcar métodos Service que alteram estado.
4. **Estoque**: Reduzir APÓS `StatusPagamento.APROVADA`, não antes.
5. **Enums**: Usar APENAS os definidos em `requisitoss_copilot.md`.
6. **Cupom**: CONSUMIR ANTES de cartão na ordem de processamento.
7. **Rejeição**: Contar consecutivas; resetar se usuário tenta outro método.
8. **LogTransacao**: Registrar user, timestamp, o que mudou.

### 📝 Padrão de Commit
```
:sparkles: implementa fluxo completo de compra e pagamento

- Agente: Checkout & Payment
- Fase 1-6: domain, services, dtos, controllers, templates, tests
- RNs: 0031, 0032, 0033, 0034, 0035, 0036, 005x, 0063, 0064, 0065, 0023, 0024, 0025
- Tests: 15+ classes, ≥80% coverage
- Sem Lombok, @Transactional em Services, Controllers sem lógica
```

---

**Data de Revisão**: 02/05/2026  
**Versão**: 1.0  
**Status**: Pronto para Implementação
