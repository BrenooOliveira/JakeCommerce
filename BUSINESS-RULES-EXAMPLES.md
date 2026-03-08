# Exemplos de Uso - Regras de Negócio

Este arquivo contém exemplos práticos de como usar a infraestrutura de regras de negócio em cada módulo do sistema.

---

## 1️⃣ Cadastro de Cliente

### Requisitos
- RN0026: Dados obrigatórios
- RN0028: Bloqueio após 3 pagamentos reprovados
- Senha forte obrigatória
- CPF válido

### Implementação no Service

```java
@Service
@Transactional
public class ClienteService {

    @Autowired
    private ClienteRepository clienteRepository;
    
    @Autowired
    private ClienteValidator clienteValidator;
    
    @Autowired
    private SenhaValidator senhaValidator;

    public Cliente cadastarCliente(ClienteDTO dto) {
        // 1. Validar dados obrigatórios (RN0026)
        clienteValidator.validarDadosObrigatorios(
            dto.getNome(), 
            dto.getCpf(), 
            dto.getEmail()
        );
        
        // 2. Validar CPF
        clienteValidator.validarCPF(dto.getCpf());
        
        // 3. Validar senha forte
        senhaValidator.validarSenha(dto.getSenha());
        
        // 4. Confirmar senha
        if (!dto.getSenha().equals(dto.getConfirmacaoSenha())) {
            throw new ValidacaoNegocioException("Senhas não coincidem");
        }
        
        // 5. Verificar duplicidade
        if (clienteRepository.findByCpf(dto.getCpf()).isPresent()) {
            throw new ValidacaoNegocioException("Já existe cliente com este CPF");
        }
        
        // 6. Criptografar senha
        String senhaCriptografada = CriptografiaUtil.criptografar(dto.getSenha());
        
        // 7. Criar cliente
        Cliente cliente = new Cliente();
        cliente.setCodigo(gerarCodigoCliente());
        cliente.setNome(dto.getNome());
        cliente.setCpf(dto.getCpf());
        cliente.setEmail(dto.getEmail());
        cliente.setTelefone(dto.getTelefone());
        cliente.setSenhaCriptografada(senhaCriptografada);
        cliente.setStatus(StatusCliente.ATIVO);
        cliente.setRanking(0);
        
        return clienteRepository.save(cliente);
    }

    private String gerarCodigoCliente() {
        return "CLI" + System.currentTimeMillis();
    }
}
```

### Endpoints

**POST /api/clientes**
```json
{
  "nome": "João Silva",
  "cpf": "123.456.789-00",
  "email": "joao@email.com",
  "telefone": "(11) 9999-9999",
  "senha": "Senha@123",
  "confirmacaoSenha": "Senha@123"
}
```

**Resposta de Sucesso (201):**
```json
{
  "codigo": "CLI1678286400000",
  "nome": "João Silva",
  "cpf": "123.456.789-00",
  "email": "joao@email.com"
}
```

**Resposta de Erro (422):**
```json
{
  "timestamp": "2026-03-08T10:30:45.123",
  "status": 422,
  "mensagem": "Senha não atende aos requisitos de segurança",
  "detalhes": "Senha deve conter pelo menos um caractere especial (!@#$%^&*)",
  "campo": "senha"
}
```

---

## 2️⃣ Cadastro de Livro

### Requisitos
- RN0011: Dados obrigatórios
- RN0013: Valor de venda baseado em margem
- RN0014: Redução abaixo da margem exige autorização

### Implementação no Service

```java
@Service
@Transactional
public class LivroService {

    @Autowired
    private LivroRepository livroRepository;
    
    @Autowired
    private LivroValidator livroValidator;

    public Livro cadastrarLivro(LivroDTO dto) {
        // 1. Validar dados obrigatórios (RN0011)
        livroValidator.validarDadosObrigatorios(
            dto.getCodigo(), 
            dto.getTitulo(), 
            dto.getIsbn()
        );
        
        // 2. Validar outros campos
        livroValidator.validarNumeroPaginas(dto.getNumeroPaginas());
        livroValidator.validarAno(dto.getAno());
        
        // 3. Validar valor de venda (RN0013, RN0014)
        Double custoBase = obterCustoBase(dto.getCodigo());
        Double percentualMargem = obterPercentualMargem(dto.getGrupoPrecificacaoId());
        
        livroValidator.validarValorVenda(
            custoBase, 
            percentualMargem, 
            dto.getValorVenda()
        );
        
        // 4. Criar livro
        Livro livro = new Livro();
        livro.setCodigo(dto.getCodigo());
        livro.setTitulo(dto.getTitulo());
        livro.setIsbn(dto.getIsbn());
        livro.setAno(dto.getAno());
        livro.setNumeroPaginas(dto.getNumeroPaginas());
        livro.setValorVenda(dto.getValorVenda());
        livro.setStatus(StatusLivro.ATIVO);
        
        return livroRepository.save(livro);
    }

    private Double obterCustoBase(String codigoLivro) {
        // Implementar lógica de obtenção do custo base
        return 0.0;
    }

    private Double obterPercentualMargem(Long grupoPrecificacaoId) {
        // Implementar lógica de obtenção da margem
        return 30.0;
    }
}
```

### Endpoints

**POST /api/livros**
```json
{
  "codigo": "JAVA001",
  "titulo": "Java Avançado",
  "isbn": "978-1-234567-89-0",
  "ano": 2023,
  "numeroPaginas": 450,
  "valorVenda": 89.90,
  "grupoPrecificacaoId": 1
}
```

**Caso de Erro (Valor abaixo da margem):**
```
Custo base: R$ 50,00
Margem: 30%
Valor mínimo: R$ 65,00
Valor informado: R$ 50,00

Response HTTP 422:
{
  "timestamp": "2026-03-08T10:30:45.123",
  "status": 422,
  "mensagem": "Violação de regra de negócio",
  "detalhes": "Valor de venda (50.00) está abaixo da margem mínima (65.00). Redução exige autorização.",
  "campo": "valorVenda"
}
```

---

## 3️⃣ Gerenciar Carrinho

### Requisitos
- RN0031: Validar estoque no carrinho
- RN0044: Bloqueio com aviso 5 min antes
- RN0063: Máximo 10 unidades por livro

### Implementação no Service

```java
@Service
@Transactional
public class CarrinhoService {

    @Autowired
    private CarrinhoRepository carrinhoRepository;
    
    @Autowired
    private ItemCarrinhoRepository itemRepository;
    
    @Autowired
    private EstoqueValidator estoqueValidator;
    
    @Autowired
    private CarrinhoValidator carrinhoValidator;

    public Carrinho adicionarAoCarrinho(String codigoCliente, String codigoLivro, Integer quantidade) {
        // 1. Obter carrinho ou criar novo
        Carrinho carrinho = obterCarrinhoAberto(codigoCliente);
        
        // 2. Validar quantidade (RN0063)
        carrinhoValidator.validarQuantidadeCarrinho(quantidade);
        
        // 3. Verificar estoque (RN0031)
        Integer quantidadeDisponivel = obterQuantidadeEmEstoque(codigoLivro);
        estoqueValidator.validarQuantidadeDisponivel(
            codigoLivro, 
            quantidade, 
            quantidadeDisponivel
        );
        
        // 4. Verificar expiração próxima (RN0044)
        Integer minutosFaltando = calcularMinutosParaExpirar(carrinho);
        try {
            carrinhoValidator.validarExpiracaoProxima(minutosFaltando);
        } catch (ValidacaoNegocioException ex) {
            // Log de aviso - não bloqueia
            System.out.println("AVISO: " + ex.getMessage());
        }
        
        // 5. Adicionar item
        ItemCarrinho item = new ItemCarrinho();
        item.setCarrinho(carrinho);
        item.setCodigoLivro(codigoLivro);
        item.setQuantidade(quantidade);
        item.setValorUnitario(obterValorVenda(codigoLivro));
        
        itemRepository.save(item);
        
        // 6. Renovar expiração do carrinho
        carrinho.setDataExpiracao(LocalDateTime.now().plusMinutes(30));
        
        return carrinhoRepository.save(carrinho);
    }

    private Integer obterQuantidadeEmEstoque(String codigoLivro) {
        // Implementar
        return 5;
    }

    private Integer calcularMinutosParaExpirar(Carrinho carrinho) {
        Duration duracao = Duration.between(LocalDateTime.now(), carrinho.getDataExpiracao());
        return (int) duracao.toMinutes();
    }

    private Double obterValorVenda(String codigoLivro) {
        // Implementar
        return 89.90;
    }

    private Carrinho obterCarrinhoAberto(String codigoCliente) {
        return carrinhoRepository.findByClienteAndStatus(codigoCliente, StatusCarrinho.ABERTO)
            .orElseGet(() -> criarNovoCarrinho(codigoCliente));
    }

    private Carrinho criarNovoCarrinho(String codigoCliente) {
        Carrinho carrinho = new Carrinho();
        carrinho.setCodigoCliente(codigoCliente);
        carrinho.setDataCriacao(LocalDateTime.now());
        carrinho.setDataExpiracao(LocalDateTime.now().plusMinutes(30));
        carrinho.setStatus(StatusCarrinho.ABERTO);
        return carrinhoRepository.save(carrinho);
    }
}
```

### Endpoints

**POST /api/carrinho/adicionar**
```json
{
  "codigoCliente": "CLI1234567890",
  "codigoLivro": "JAVA001",
  "quantidade": 3
}
```

**Caso de Erro (Estoque insuficiente):**
```
Response HTTP 422:
{
  "timestamp": "2026-03-08T10:30:45.123",
  "status": 422,
  "mensagem": "Estoque insuficiente",
  "detalhes": "Livro: JAVA001 | Solicitado: 15 | Disponível: 5",
  "campo": null
}
```

**Caso de Erro (Quantidade acima do limite):**
```
Response HTTP 422:
{
  "timestamp": "2026-03-08T10:30:45.123",
  "status": 422,
  "mensagem": "Violação de regra de negócio",
  "detalhes": "Máximo 10 unidades do mesmo livro por pedido. Quantidade informada: 15",
  "campo": null
}
```

---

## 4️⃣ Processar Pagamento

### Requisitos
- RN0033: Um cupom por compra
- RN0037: Validar pagamento
- RN0065: Bloqueio após 3 reprovações
- RN0064: Valor mínimo R$ 20,00 sem frete

### Implementação no Service

```java
@Service
@Transactional
public class PagamentoService {

    @Autowired
    private PagamentoValidator pagamentoValidator;
    
    @Autowired
    private ClienteRepository clienteRepository;

    public Pagamento processarPagamento(PagamentoDTO dto) {
        // 1. Validar dados de pagamento (RN0037)
        pagamentoValidator.validarDadosPagamento(
            dto.getValorTotal(), 
            dto.getCodigoPedido()
        );
        
        // 2. Validar cupom (RN0033)
        if (dto.getCupons() != null) {
            Long numeroCupons = dto.getCupons().stream()
                .filter(c -> "PROMOCIONAL".equals(c.getTipo()))
                .count();
            pagamentoValidator.validarUnicoCupomPromocional(numeroCupons.intValue());
        }
        
        // 3. Validar valor mínimo (RN0064)
        pagamentoValidator.validarValorMinimoPedido(
            dto.getValorTotal(), 
            dto.isTemFrete()
        );
        
        // 4. Verificar limite de tentativas do cliente (RN0065)
        Cliente cliente = clienteRepository.findByCodigo(dto.getCodigoCliente())
            .orElseThrow(() -> new RecursoNaoEncontradoException("Cliente não encontrado"));
        
        Integer tentativasReprovadas = obterTentativasReprovadas(dto.getCodigoPedido());
        pagamentoValidator.validarLimiteTentativasReprovadas(tentativasReprovadas);
        
        // 5. Processar pagamento
        Pagamento pagamento = new Pagamento();
        pagamento.setCodigoPedido(dto.getCodigoPedido());
        pagamento.setValorTotal(dto.getValorTotal());
        pagamento.setStatus(StatusPagamento.PENDENTE);
        pagamento.setDataProcessamento(LocalDateTime.now());
        
        // Simular processamento
        boolean aprovado = simularProcessamentoBancario(dto);
        
        if (aprovado) {
            pagamento.setStatus(StatusPagamento.APROVADA);
            Cliente c = clienteRepository.findByCodigo(dto.getCodigoCliente()).get();
            c.setRanking(c.getRanking() + 10); // Aumentar ranking
            clienteRepository.save(c);
        } else {
            pagamento.setStatus(StatusPagamento.REPROVADA);
            Integer novasTentativas = tentativasReprovadas + 1;
            
            if (novasTentativas >= 3) {
                Cliente c = clienteRepository.findByCodigo(dto.getCodigoCliente()).get();
                c.setStatus(StatusCliente.BLOQUEADO);
                clienteRepository.save(c);
                
                throw new ClienteBloqueadoException(
                    "Seu cliente foi bloqueado após 3 tentativas de pagamento reprovadas",
                    dto.getCodigoCliente(),
                    "3 pagamentos reprovados consecutivos"
                );
            }
            
            throw new PagamentoReprovadoException(
                "Pagamento reprovado. Tente novamente.",
                dto.getCodigoPedido(),
                novasTentativas
            );
        }
        
        return pagamento;
    }

    private Integer obterTentativasReprovadas(String codigoPedido) {
        // Implementar busca no banco
        return 0;
    }

    private boolean simularProcessamentoBancario(PagamentoDTO dto) {
        // Simular resposta do banco
        return Math.random() > 0.2; // 80% chance de aprovação
    }
}
```

### Endpoints

**POST /api/pagamentos/processar**
```json
{
  "codigoPedido": "PED001",
  "codigoCliente": "CLI001",
  "valorTotal": 250.00,
  "temFrete": false,
  "cupons": [
    {
      "codigo": "DESC10",
      "tipo": "PROMOCIONAL",
      "valor": 25.00
    }
  ]
}
```

**Caso de Erro (Valor abaixo do mínimo):**
```
Response HTTP 422:
{
  "timestamp": "2026-03-08T10:30:45.123",
  "status": 422,
  "mensagem": "Violação de regra de negócio",
  "detalhes": "Valor mínimo do pedido sem frete é R$ 20,00. Valor atual: R$ 15,50",
  "campo": null
}
```

**Caso de Erro (Cliente bloqueado):**
```
Response HTTP 403:
{
  "timestamp": "2026-03-08T10:30:45.123",
  "status": 403,
  "mensagem": "Operação não permitida - Cliente bloqueado",
  "detalhes": "Seu cliente foi bloqueado após 3 tentativas de pagamento reprovadas",
  "campo": "3 pagamentos reprovados consecutivos"
}
```

---

## 5️⃣ Solicitar Troca

### Requisitos
- RN0043: Apenas pedidos ENTREGUES
- Gerar cupom de troca

### Implementação no Service

```java
@Service
@Transactional
public class TrocaService {

    @Autowired
    private TrocaRepository trocaRepository;
    
    @Autowired
    private PedidoRepository pedidoRepository;
    
    @Autowired
    private TrocaValidator trocaValidator;
    
    @Autowired
    private CupomService cupomService;

    public Troca solicitarTroca(TrocaDTO dto) {
        // 1. Obter pedido
        Pedido pedido = pedidoRepository.findById(dto.getCodigoPedido())
            .orElseThrow(() -> new RecursoNaoEncontradoException("Pedido não encontrado"));
        
        // 2. Validar status do pedido (RN0043)
        trocaValidator.validarStatusPedidoParaTroca(
            pedido.getStatus().toString(), 
            dto.getCodigoPedido()
        );
        
        // 3. Validar dados obrigatórios
        trocaValidator.validarDadosObrigatorios(
            dto.getCodigoPedido(), 
            dto.getMotivo()
        );
        
        // 4. Criar troca
        Troca troca = new Troca();
        troca.setPedido(pedido);
        troca.setMotivo(dto.getMotivo());
        troca.setDataSolicitacao(LocalDateTime.now());
        troca.setStatus(StatusTroca.SOLICITADA);
        
        Troca trocaSalva = trocaRepository.save(troca);
        
        // 5. Atualizar status do pedido
        pedido.setStatus(StatusPedido.EM_TROCA);
        pedidoRepository.save(pedido);
        
        return trocaSalva;
    }

    public Troca autorizarTroca(String codigoTroca) {
        Troca troca = trocaRepository.findById(codigoTroca)
            .orElseThrow(() -> new RecursoNaoEncontradoException("Troca não encontrada"));
        
        // Validar status
        if (!troca.getStatus().equals(StatusTroca.SOLICITADA)) {
            throw new TrocaNaoPermitidaException(
                "Troca não pode ser autorizada. Status atual: " + troca.getStatus(),
                troca.getPedido().getCodigo(),
                "Status não permite autorização"
            );
        }
        
        // Autorizar
        troca.setStatus(StatusTroca.AUTORIZADA);
        Troca trocaAutorizada = trocaRepository.save(troca);
        
        // Gerar cupom de troca (valor do pedido)
        Cupom cupomTroca = cupomService.gerarCupomTroca(
            troca.getPedido().getValorTotal(),
            troca.getPedido().getCliente()
        );
        
        return trocaAutorizada;
    }
}
```

### Endpoints

**POST /api/trocas/solicitar**
```json
{
  "codigoPedido": "PED001",
  "motivo": "Livro danificado durante o transporte"
}
```

**Caso de Erro (Pedido não entregue):**
```
Response HTTP 422:
{
  "timestamp": "2026-03-08T10:30:45.123",
  "status": 422,
  "mensagem": "Troca não permitida",
  "detalhes": "Pedido PED001 não pode ser trocado. Status atual: EM_TRANSPORTE. Apenas pedidos ENTREGUES podem solicitar troca.",
  "campo": "Pedido não está entregue"
}
```

---

## 📊 Resumo de Testes

Para cada serviço, sempre criar testes para:

1. ✅ Caso de sucesso (happy path)
2. ✅ Validação obrigatória
3. ✅ Regras de negócio
4. ✅ Combinações de erros

Exemplo:

```java
@SpringBootTest
public class ClienteServiceTest {
    
    @Autowired
    private ClienteService clienteService;
    
    @Test
    public void testCadastroComSucesso() {
        ClienteDTO dto = new ClienteDTO(/* dados válidos */);
        Cliente cliente = clienteService.cadastrarCliente(dto);
        assertNotNull(cliente.getCodigo());
    }
    
    @Test
    public void testSenhaFraca() {
        ClienteDTO dto = new ClienteDTO(/* senha fraca */);
        assertThrows(
            SenhaInseguraException.class, 
            () -> clienteService.cadastrarCliente(dto)
        );
    }
    
    @Test
    public void testCPFDuplicado() {
        // Cadastrar cliente
        // Tentar cadastrar outro com mesmo CPF
        assertThrows(
            ValidacaoNegocioException.class,
            () -> clienteService.cadastrarCliente(dto)
        );
    }
}
```

---

**Última atualização:** 08 de março de 2026  
**Versão:** 1.0
