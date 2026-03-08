Você está desenvolvendo um sistema de e-commerce para venda de livros como trabalho acadêmico.

STACK OBRIGATÓRIA:
- Java 21 + Spring Boot 3
- Spring Data JPA + Hibernate
- Thymeleaf + Bootstrap 5
- PostgreSQL
- Maven

ARQUITETURA (sem exceções):
- Controller: recebe requisição, chama Service, retorna view/redirect. ZERO lógica de negócio.
- Service: toda lógica de negócio e regras aqui. Anota @Transactional quando necessário.
- Repository: apenas interfaces JpaRepository + queries JPQL/nativas quando necessário.
- Entity: mapeamento JPA puro, sem lógica.
- DTO: transporte de dados entre camadas.

CONVENÇÕES:
- Pacote raiz: com.livraria
- Subpacotes: .domain, .repository, .service, .controller, .dto, .config, .exception
- Idioma dos atributos: português (conforme modelo de domínio)
- Idioma dos comentários: português
- Sem Lombok (use getters/setters explícitos para clareza acadêmica)

MODELO DE DOMÍNIO (fonte da verdade — nunca adicionar campos fora deste modelo):
Livro: codigo, titulo, ano, edicao, isbn, numeroPaginas, sinopse, dimensoes, codigoBarras, status, valorVenda
GrupoPrecificacao: id, nome, percentualMargem
Estoque: id, quantidade, custoAtual, dataEntrada
Editora: id, nome (+ relacionamentos)
Autor: id, nome (+ relacionamentos)
Categoria: id, nome
Cliente: codigo, nome, genero, dataNascimento, cpf, telefone, email, senhaCriptografada, ranking, status
Endereco: nomeIdentificador, tipoResidencia, logradouro, numero, bairro, cep, cidade, estado, pais, tipoEndereco
Cartao: numero, nomeImpresso, bandeira, codigoSeguranca, preferencial
Carrinho: dataCriacao, status, dataExpiracao
ItemCarrinho: quantidade, valorUnitario → Livro
Pedido: dataCriacao, status, valorTotal, valorFrete → Endereco, Pagamento, [ItemPedido], [Troca]
ItemPedido: quantidade, valorUnitario → Livro
Pagamento: status, valorTotal → [PagamentoCartao], [PagamentoCupom]
PagamentoCartao: valor
PagamentoCupom: valor → Cupom
Cupom: codigo, valor, tipo, ativo
Troca: dataSolicitacao, status, motivo

STATUS PERMITIDOS (nunca criar outros):
StatusLivro: ATIVO, INATIVO
StatusCliente: ATIVO, INATIVO, BLOQUEADO
StatusCarrinho: ABERTO, EXPIRADO, FINALIZADO
StatusPedido: EM_PROCESSAMENTO, EM_TRANSPORTE, ENTREGUE, EM_TROCA, TROCADO
StatusPagamento: PENDENTE, APROVADA, REPROVADA
StatusTroca: SOLICITADA, AUTORIZADA, RECEBIDA, CONCLUIDA


3. REQUISITOS FUNCIONAIS

3.1 Cadastro de Livros

RF0011 Cadastrar livro
RF0012 Inativar livro manualmente
RF0013 Inativar livro automaticamente
RF0014 Alterar livro
RF0015 Consultar livros com filtros combinados
RF0016 Ativar livro

3.2 Cadastro de Clientes

RF0021 Cadastrar cliente
RF0022 Alterar cliente
RF0023 Inativar cliente
RF0024 Consultar cliente
RF0025 Consultar transações do cliente
RF0026 Cadastrar múltiplos endereços
RF0027 Cadastrar múltiplos cartões (um preferencial)
RF0028 Alterar apenas senha

3.3 Vendas Eletrônicas

RF0031 Gerenciar carrinho
RF0032 Definir quantidade no carrinho
RF0033 Realizar compra
RF0034 Calcular frete
RF0035 Selecionar endereço
RF0036 Selecionar pagamento (cartão, cupom promocional, cupom de troca)
RF0037 Finalizar compra (status inicial: EM PROCESSAMENTO)
RF0038 Despachar produtos (EM TRANSPORTE)
RF0039 Confirmar entrega (ENTREGUE)
RF0040 Solicitar troca
RF0041 Autorizar troca
RF0042 Visualizar trocas (admin)
RF0043 Confirmar recebimento de troca
RF0044 Gerar cupom de troca

3.4 Controle de Estoque

RF0051 Entrada em estoque
RF0052 Calcular valor de venda
RF0053 Baixa automática após venda
RF0054 Reentrada via troca

3.5 Análise

RF0055 Analisar histórico por período comparando produtos ou categorias

4. REQUISITOS NÃO FUNCIONAIS

RNF0011 Tempo de resposta máximo: 1 segundo
RNF0012 Log de transações com data, hora, usuário e dados alterados

Cadastro de Livros:

Código único obrigatório
Script inicial deve cadastrar domínios (autor, editora etc.)

Cadastro de Clientes:

Senha forte (mínimo 8 caracteres, maiúsculas, minúsculas e especiais)
Confirmação de senha
Senha criptografada
Código único de cliente

Vendas:

Exibir itens removidos do carrinho por expiração

Análise:

Exibição em gráfico de linhas

5. REGRAS DE NEGÓCIO

5.1 Livro

RN0011 Dados obrigatórios conforme modelo
RN0012 Livro pode ter múltiplas categorias
RN0013 Valor de venda baseado na margem do grupo
RN0014 Redução abaixo da margem exige autorização
RN0015 Inativação manual exige motivo
RN0016 Inativação automática categoria FORA DE MERCADO
RN0017 Ativação exige justificativa

5.2 Cliente

RN0021 Pelo menos um endereço de cobrança
RN0022 Pelo menos um endereço de entrega
RN0023 Campos obrigatórios do endereço
RN0024 Campos obrigatórios do cartão
RN0025 Bandeira deve estar cadastrada
RN0026 Dados obrigatórios do cliente
RN0027 Cliente possui ranking numérico
RN0028 Baixa estoque apenas após pagamento aprovado

5.3 Venda

RN0031 Validar estoque no carrinho
RN0032 Validar estoque antes da finalização
RN0033 Apenas um cupom promocional por compra
RN0034 Múltiplos cartões permitidos (mínimo 10 por cartão)
RN0035 Consumir cupons antes do cartão
RN0036 Gerar cupom para excedente
RN0037 Validar pagamento
RN0038 Status pagamento: APROVADA ou REPROVADA
RN0039 Status transporte: EM TRANSPORTE
RN0040 Status entrega: ENTREGUE
RN0041 Pedido em troca: EM TROCA
RN0042 Após troca: TROCADO
RN0043 Apenas pedidos ENTREGUES podem solicitar troca
RN0044 Bloqueio carrinho com aviso 5 minutos antes
RN0045 Remover item desbloqueado
RN0063 Máximo 10 unidades do mesmo livro por pedido
RN0064 Pedido mínimo 20 sem frete
RN0065 3 pagamentos REPROVADOS consecutivos bloqueiam carrinho

5.4 Estoque

RN0051 Entrada exige produto, quantidade, custo, fornecedor e data
RN005x Considerar maior custo para cálculo de venda
RN0061 Não permitir quantidade zero
RN0062 Todo item deve possuir custo
RNF0064 Não permitir registro sem data

6. DIRETRIZES

1. Sempre validar fluxos contra regras de negócio antes de sugerir código.
2. Nunca criar novos status além dos definidos.
3. Nunca criar novos campos além dos presentes no modelo.
4. Sempre respeitar cardinalidades do diagrama.
5. Garantir consistência entre carrinho, pedido, pagamento e estoque.
6. Considerar que baixa de estoque ocorre apenas após pagamento APROVADO.
7. Aplicar limites de compra e pedido mínimo antes de finalizar pedido.
8. Garantir rastreabilidade via log em operações de escrita.
9. Em caso de troca:

    Validar status ENTREGUE
    Alterar status corretamente
    Gerar cupom quando aplicável

Este documento deve ser tratado como especificação oficial do sistema.
