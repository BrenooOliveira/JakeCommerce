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
StatusTroca: SOLICITADA, AUTORIZADA, RECEBIDA, CONCLUID