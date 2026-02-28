``` mermaid
classDiagram

%% =========================
%% LIVRO E DOMÍNIO
%% =========================

class Livro {
    +codigo: String
    +titulo: String
    +ano: int
    +edicao: String
    +isbn: String
    +numeroPaginas: int
    +sinopse: String
    +dimensoes: String
    +codigoBarras: String
    +status: StatusLivro
    +valorVenda: decimal
}

class Autor {
    +id: Long
    +nome: String
}

class Categoria {
    +id: Long
    +nome: String
}

class Editora {
    +id: Long
    +nome: String
}

class GrupoPrecificacao {
    +id: Long
    +nome: String
    +percentualMargem: decimal
}

class Estoque {
    +id: Long
    +quantidade: int
    +custoAtual: decimal
    +dataEntrada: Date
}

Livro "1" --> "1" GrupoPrecificacao
Livro "1" --> "1" Editora
Livro "1" --> "*" Autor
Livro "1" --> "*" Categoria
Livro "1" --> "1" Estoque


%% =========================
%% CLIENTE
%% =========================

class Cliente {
    +codigo: String
    +nome: String
    +genero: String
    +dataNascimento: Date
    +cpf: String
    +telefone: String
    +email: String
    +senhaCriptografada: String
    +ranking: int
    +status: StatusCliente
}

class Endereco {
    +id: Long
    +nomeIdentificador: String
    +tipoResidencia: String
    +logradouro: String
    +numero: String
    +bairro: String
    +cep: String
    +cidade: String
    +estado: String
    +pais: String
    +tipoEndereco: TipoEndereco
}

class Cartao {
    +id: Long
    +numero: String
    +nomeImpresso: String
    +bandeira: String
    +codigoSeguranca: String
    +preferencial: boolean
}

Cliente "1" --> "*" Endereco
Cliente "1" --> "*" Cartao


%% =========================
%% CARRINHO E VENDA
%% =========================

class Carrinho {
    +id: Long
    +dataCriacao: DateTime
    +status: StatusCarrinho
    +dataExpiracao: DateTime
}

class ItemCarrinho {
    +id: Long
    +quantidade: int
    +valorUnitario: decimal
}

Cliente "1" --> "1" Carrinho
Carrinho "1" --> "*" ItemCarrinho
ItemCarrinho "*" --> "1" Livro


%% =========================
%% PEDIDO
%% =========================

class Pedido {
    +id: Long
    +dataCriacao: DateTime
    +status: StatusPedido
    +valorTotal: decimal
    +valorFrete: decimal
}

class ItemPedido {
    +id: Long
    +quantidade: int
    +valorUnitario: decimal
}

class Pagamento {
    +id: Long
    +status: StatusPagamento
    +valorTotal: decimal
}

class PagamentoCartao {
    +valor: decimal
}

class PagamentoCupom {
    +valor: decimal
}

class Cupom {
    +codigo: String
    +valor: decimal
    +tipo: TipoCupom
    +ativo: boolean
}

Cliente "1" --> "*" Pedido
Pedido "1" --> "*" ItemPedido
ItemPedido "*" --> "1" Livro
Pedido "1" --> "1" Endereco
Pedido "1" --> "1" Pagamento

Pagamento "1" --> "*" PagamentoCartao
Pagamento "1" --> "*" PagamentoCupom
PagamentoCupom "*" --> "1" Cupom


%% =========================
%% TROCA
%% =========================

class Troca {
    +id: Long
    +dataSolicitacao: DateTime
    +status: StatusTroca
    +motivo: String
}

Pedido "1" --> "*" Troca


```