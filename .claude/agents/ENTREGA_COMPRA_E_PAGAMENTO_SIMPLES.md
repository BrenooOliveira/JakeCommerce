# AGENTE: Compra e Pagamento (Versão Simplificada)

**Data**: 02/05/2026  
**Contexto**: Trabalho acadêmico - foco na implementação prática, mínimo necessário

---

## 1. CONTEXTO RÁPIDO

**Stack**: Java 21 + Spring Boot + PostgreSQL + Thymeleaf  
**Padrão**: 5 camadas (Controller → Service → Repository → Domain → DTO)  
**Regra Ouro**: Domain é a fonte de verdade. Respeitar cardinalidades.

---

## 2. O QUE IMPLEMENTAR (3 FLUXOS)

### Fluxo A: Validar Carrinho → Criar Pedido
```
GET /carrinho 
  → Validar: estoque existe? valor ≥ R$20? max 10 unid/livro?
  → POST /checkout/validar
  → Cria Pedido(status=EM_PROCESSAMENTO)
  → Redireciona para /checkout/pagamento
```

**Classes Necessárias**:
- `CarrinhoService.validarCarrinho()`: verifica estoque, valor mínimo, limite unidades
- `PedidoService.criarDoPedidoCarrinho()`: cria Pedido do Carrinho
- `PedidoController`: GET /carrinho, POST /checkout/validar

---

### Fluxo B: Processar Pagamento (Múltiplos Cartões + Cupom)
```
POST /api/checkout/pagamento/processar
Entrada:
  - pedidoId: qual pedido pagar
  - cupomId (opcional): se tem cupom
  - cartoes[]: lista de {cartaoId, valor}

Lógica:
  1. Se cupom: valida (1 aprovado, saldo válido)
  2. Consome cupom (reduz valor, marca como usado)
  3. Valida cartões (múltiplos? mín R$10 cada)
  4. Simula aprovação/rejeição (PaymentGateway)
  5. SE APROVADA:
     - Cria Pagamento(status=APROVADA)
     - REDUZ ESTOQUE ← IMPORTANTE
     - Retorna sucesso
  6. SE REPROVADA:
     - Cria Pagamento(status=REPROVADA)
     - Incrementa contador rejeição (3x = bloqueia carrinho)
     - Retorna erro
```

**Classes Necessárias**:
- `PagamentoService.processar(pedido, cupom?, cartoes)`: orquestra fluxo
- `CupomService.consumir()`: reduz saldo cupom
- `EstoqueService.reduzir()`: reduz quantidade APÓS aprovação
- `PaymentGatewayService.simularAprovacao()`: mock 80% APROVADA
- `PagamentoController`: POST /api/checkout/pagamento/processar

---

### Fluxo C: Registrar Novo Cartão + Endereço (no Checkout)
```
Modal "Novo Cartão":
  POST /api/cartao/novo
  {
    "numero": "4532...",
    "nomeImpresso": "FULANO SILVA",
    "bandeira": "VISA",
    "codigoSeguranca": "123",
    "preferencial": false
  }
  → Valida: número válido? CVV 3-4 dígitos? bandeira existe?
  → Cria Cartao + associa ao Cliente
  → Retorna na lista de cartões

Modal "Novo Endereço":
  POST /api/endereco/novo
  {
    "nomeIdentificador": "Casa",
    "logradouro": "Rua X",
    "numero": "123",
    "bairro": "Bairro",
    "cep": "12345-678",
    "cidade": "São Paulo",
    "estado": "SP",
    "pais": "Brasil",
    "tipo": "ENTREGA"
  }
  → Valida: CEP formato? estado válido?
  → Cria Endereco + associa ao Cliente
  → Retorna na lista de endereços
```

**Classes Necessárias**:
- `CartaoService.registrar()`: cria Cartao
- `EnderecoService.registrar()`: cria Endereco
- `CartaoController`: POST /api/cartao/novo, GET /api/cartao/meus
- `EnderecoController`: POST /api/endereco/novo, GET /api/endereco/meus

---

## 3. PADRÃO: 5 CAMADAS

```
Controller → DTO (validações) → Service (lógica) → Repository → Domain (JPA)
```

**Exemplo PagamentoService**:
```java
@Service
@Transactional
public class PagamentoService {
    
    @Autowired private PagamentoRepository pagamentoRepo;
    @Autowired private CupomService cupomService;
    @Autowired private EstoqueService estoqueService;
    @Autowired private PaymentGatewayService gatewayService;
    
    public PagamentoResponseDTO processar(Long pedidoId, Long cupomId, 
                                          List<PagamentoCartaoRequestDTO> cartoesReq) {
        // 1. Buscar pedido
        Pedido pedido = pedidoRepo.findById(pedidoId);
        
        // 2. Consumir cupom (se houver)
        BigDecimal valorCupom = BigDecimal.ZERO;
        if (cupomId != null) {
            valorCupom = cupomService.consumir(cupomId);
        }
        
        // 3. Validar cartões (múltiplos, mín R$10 cada)
        validarCartoes(cartoesReq);
        BigDecimal valorCartoes = cartoesReq.stream()
            .map(c -> c.getValor())
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        // 4. Chamar PaymentGateway (simula aprovação)
        boolean aprovada = gatewayService.simularAprovacao();
        
        // 5. Processar resultado
        Pagamento pagamento = new Pagamento();
        pagamento.setPedido(pedido);
        pagamento.setValorTotal(valorCupom.add(valorCartoes));
        
        if (aprovada) {
            pagamento.setStatus(StatusPagamento.APROVADA);
            
            // REDUZIR ESTOQUE (IMPORTANTE: só após aprovação!)
            for (ItemPedido item : pedido.getItens()) {
                estoqueService.reduzir(item.getLivro().getId(), item.getQuantidade());
            }
        } else {
            pagamento.setStatus(StatusPagamento.REPROVADA);
            // Incrementar rejeição (pode bloquear cart se 3x)
        }
        
        pagamentoRepo.save(pagamento);
        
        return new PagamentoResponseDTO(pagamento);
    }
    
    private void validarCartoes(List<PagamentoCartaoRequestDTO> cartoes) {
        if (cartoes == null || cartoes.isEmpty()) {
            throw new PagamentoException("Nenhum cartão informado");
        }
        for (PagamentoCartaoRequestDTO c : cartoes) {
            if (c.getValor().compareTo(BigDecimal.TEN) < 0) {
                throw new PagamentoException("Mínimo R$10 por cartão");
            }
        }
    }
}
```

---

## 4. ESTRUTURA DE PACOTES (MINIMALISTA)

```
jakebooks/src/main/java/com/les/jakebooks/

controller/
├── CarrinhoController.java (já existe, adicionar validações)
├── PedidoController.java (já existe, adicionar POST /checkout/validar)
├── PagamentoController.java ← NOVO (processar pagamento)
├── CartaoController.java ← NOVO (registrar cartão)
└── EnderecoController.java ← NOVO (registrar endereço)

service/
├── CarrinhoService.java (refactor: validações)
├── PedidoService.java (refactor: criar do carrinho)
├── PagamentoService.java ← NOVO (CORE - processamento pagamento)
├── CupomService.java (refactor: consumir cupom)
├── EstoqueService.java (refactor: reduzir após aprovação)
├── CartaoService.java (refactor: registrar novo)
├── EnderecoService.java (novo: registrar novo)
└── PaymentGatewayService.java ← NOVO (simula aprovação/rejeição)

repository/ (apenas interfaces JpaRepository)
├── PagamentoRepository.java
├── PagamentoCartaoRepository.java
└── (outros já existem)

domain/ (JPA entities)
├── Pagamento.java (refactor se necessário)
├── Cupom.java (adicionar campo "ativo")
├── Cartao.java (garantir relacionamento com Cliente)
└── (outros revisados)

dto/
├── PagamentoRequestDTO.java ← NOVO
├── PagamentoResponseDTO.java ← NOVO
├── CartaoRequestDTO.java ← NOVO
└── EnderecoRequestDTO.java ← NOVO

resources/templates/checkout/
├── carrinho.html (refactor: exibir validações)
├── pagamento.html ← NOVO (form principal pagamento)
├── novo-cartao-modal.html ← NOVO (modal registrar cartão)
└── novo-endereco-modal.html ← NOVO (modal registrar endereço)
```

---

## 5. TAREFAS (ORDEM RECOMENDADA)

### Task 1: Domain Review (30 min)
- [ ] Abrir `Pagamento.java` → garantir campos necessários
- [ ] Abrir `Cupom.java` → adicionar campo `ativo` (boolean)
- [ ] Abrir `Cartao.java` → garantir relacionamento com `Cliente`
- [ ] Abrir `Endereco.java` → garantir campos CEP, estado
- [ ] Abrir `Estoque.java` → verificar se tem `quantidade`

### Task 2: Services - Núcleo (3-4 horas)
- [ ] **PagamentoService** (novo): método `processar()` com lógica completa
- [ ] **PaymentGatewayService** (novo): simula 80% APROVADA, 20% REPROVADA
- [ ] **CupomService**: refactor método `consumir(cupomId)` → reduz valor + marca "ativo=false"
- [ ] **EstoqueService**: refactor `reduzir()` → APENAS chamado após APROVADA
- [ ] **CarrinhoService**: refactor `validarCarrinho()` → estoque, valor mín R$20, max 10/livro
- [ ] **PedidoService**: refactor `criarDoCarrinho()` → cria Pedido com status EM_PROCESSAMENTO
- [ ] **CartaoService**: novo método `registrar()` → cria + valida bandeira
- [ ] **EnderecoService**: novo método `registrar()` → cria + valida CEP/estado

### Task 3: DTOs (1 hora)
- [ ] Criar `PagamentoRequestDTO`: pedidoId, cupomId (nullable), cartoes[]
- [ ] Criar `PagamentoResponseDTO`: pedidoId, status (APROVADA/REPROVADA), valorTotal
- [ ] Criar `CartaoRequestDTO`: numero, nomeImpresso, bandeira, codigoSeguranca
- [ ] Criar `EnderecoRequestDTO`: logradouro, numero, bairro, cep, cidade, estado, pais, tipo

### Task 4: Controllers (1.5 hora)
- [ ] **PagamentoController**: POST /api/checkout/pagamento/processar
- [ ] **CartaoController**: POST /api/cartao/novo, GET /api/cartao/meus
- [ ] **EnderecoController**: POST /api/endereco/novo, GET /api/endereco/meus
- [ ] Refactor **CarrinhoController**: GET /carrinho com validações
- [ ] Refactor **PedidoController**: POST /checkout/validar

### Task 5: Templates Thymeleaf (1.5 hora)
- [ ] **pagamento.html** (novo): form completo com 3 abas (cupom, cartões, endereço)
- [ ] **novo-cartao-modal.html** (novo): form registrar cartão
- [ ] **novo-endereco-modal.html** (novo): form registrar endereço
- [ ] Refactor **carrinho.html**: exibir botão "Prosseguir para Checkout"

### Task 6: Validações Form (30 min)
- [ ] Adicionar JavaScript básico: validar número cartão, CVV, CEP
- [ ] Adicionar @Pattern, @NotNull nas DTOs
- [ ] Adicionar mensagens de erro no Thymeleaf

### Task 7: Teste Manual (no servidor) (30 min)
- [ ] `mvn spring-boot:run` na pasta jakebooks/
- [ ] Acessar /carrinho → validar estoque, valor mínimo
- [ ] Clicar "Novo Cartão" → registrar 1 cartão teste
- [ ] Selecionar cartão e processar pagamento → verificar APROVADA
- [ ] Checar estoque foi reduzido
- [ ] Tentar 3 pagamentos com rejeição → verificar bloqueio

---

## 6. REGRAS DE OURO (NÃO ESQUECER!)

| Regra | Por Quê |
|---|---|
| **Estoque reduz APÓS APROVADA** | Não reduza durante checkout, só após confirmação |
| **Cupom ANTES de Cartão** | Order importa: consumir cupom first, depois descontar do cartão |
| **StatusPagamento: APROVADA ou REPROVADA** | Apenas 2 enums, não criar outros status |
| **@Transactional em Services** | Garante atomicidade das transações |
| **Sem lógica em Controller** | Controller = despachante, Service = lógica |
| **DTOs com validações** | Validar input no boundary (Controller recebe DTO validado) |

---

## 7. VERIFICAÇÃO FINAL

- [ ] `mvn clean compile` → sem erros
- [ ] `mvn spring-boot:run` → servidor inicia
- [ ] `/carrinho` → exibe itens com validações
- [ ] `/checkout/pagamento` → exibe form pagamento completo
- [ ] POST pagamento com 1 cartão → APROVADA com estoque reduzido
- [ ] POST pagamento com 2 cartões → APROVADA
- [ ] POST pagamento com cupom → APROVADA com cupom consumido
- [ ] Novo cartão registrado → aparece na lista
- [ ] Novo endereço registrado → aparece na lista
- [ ] 3 rejeições consecutivas → carrinho bloqueado

---

## 8. TAMANHO ESPERADO

- **6 Services** (novos/refatorados): ~500 linhas
- **5 Controllers**: ~300 linhas
- **4 DTOs**: ~150 linhas
- **4 Templates HTML**: ~400 linhas
- **Domain**: ~100 linhas (pequenos ajustes)

**Total**: ~1.450 linhas de código (muito viável)

---

## 9. REFERÊNCIAS

- **Requisitos**: `/home/breno-oliveira/Documentos/gitRepositories/JakeCommerce/general/requisitoss_copilot.md`
- **Arquitetura**: `/home/breno-oliveira/Documentos/gitRepositories/JakeCommerce/CLAUDE.md`
- **Projeto**: `jakebooks/`

---

**Começar por**: Task 1 (Domain Review) → Task 2 (Services Core) → Tasks 3-6

**Tempo estimado**: 7-8 horas de trabalho prático (sem testes)
