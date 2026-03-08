-- ============================================================================
-- SCHEMA SQL PARA JAKEBOOKS E-COMMERCE
-- ============================================================================
-- Este arquivo é uma referência do schema gerado automaticamente pelo Hibernate
-- baseado nas entidades JPA anotadas.
-- 
-- EM DESENVOLVIMENTO: O Hibernate cria esse schema automaticamente com 
-- spring.jpa.hibernate.ddl-auto=create-drop
--
-- EM PRODUÇÃO: Use este arquivo como base para criar manualmente o schema
-- com spring.jpa.hibernate.ddl-auto=validate
-- ============================================================================

-- Tabelas de Domínio (Sem dependências)

CREATE TABLE grupo_precificacao (
    id BIGSERIAL PRIMARY KEY,
    nome VARCHAR(255) NOT NULL,
    percentual_margem DECIMAL(19, 2) NOT NULL
);

CREATE TABLE editora (
    id BIGSERIAL PRIMARY KEY,
    nome VARCHAR(255) NOT NULL
);

CREATE TABLE autor (
    id BIGSERIAL PRIMARY KEY,
    nome VARCHAR(255) NOT NULL
);

CREATE TABLE categoria (
    id BIGSERIAL PRIMARY KEY,
    nome VARCHAR(255) NOT NULL
);

CREATE TABLE cupom (
    id BIGSERIAL PRIMARY KEY,
    codigo VARCHAR(100) NOT NULL UNIQUE,
    valor DECIMAL(19, 2) NOT NULL,
    tipo VARCHAR(50) NOT NULL,
    ativo BOOLEAN NOT NULL
);

-- Tabela de Estoque (Sem dependências)

CREATE TABLE estoque (
    id BIGSERIAL PRIMARY KEY,
    quantidade INT NOT NULL,
    custo_atual DECIMAL(19, 2) NOT NULL,
    data_entrada DATE NOT NULL
);

-- Tabela de Livro (Depende de grupo_precificacao, editora, estoque)

CREATE TABLE livro (
    id BIGSERIAL PRIMARY KEY,
    codigo VARCHAR(100) NOT NULL UNIQUE,
    titulo VARCHAR(255) NOT NULL,
    ano INT NOT NULL,
    edicao VARCHAR(50),
    isbn VARCHAR(50),
    numero_paginas INT,
    sinopse TEXT,
    dimensoes VARCHAR(100),
    codigo_barras VARCHAR(100),
    status VARCHAR(50) NOT NULL,
    valor_venda DECIMAL(19, 2) NOT NULL,
    grupo_precificacao_id BIGINT NOT NULL,
    editora_id BIGINT NOT NULL,
    estoque_id BIGINT NOT NULL UNIQUE,
    CONSTRAINT fk_livro_grupo_precificacao FOREIGN KEY (grupo_precificacao_id) REFERENCES grupo_precificacao(id),
    CONSTRAINT fk_livro_editora FOREIGN KEY (editora_id) REFERENCES editora(id),
    CONSTRAINT fk_livro_estoque FOREIGN KEY (estoque_id) REFERENCES estoque(id),
    INDEX idx_livro_codigo (codigo)
);

-- Tabela de Relacionamento Livro-Autor (ManyToMany)

CREATE TABLE livro_autor (
    livro_id BIGINT NOT NULL,
    autor_id BIGINT NOT NULL,
    PRIMARY KEY (livro_id, autor_id),
    CONSTRAINT fk_livro_autor_livro FOREIGN KEY (livro_id) REFERENCES livro(id) ON DELETE CASCADE,
    CONSTRAINT fk_livro_autor_autor FOREIGN KEY (autor_id) REFERENCES autor(id) ON DELETE CASCADE
);

-- Tabela de Relacionamento Livro-Categoria (ManyToMany)

CREATE TABLE livro_categoria (
    livro_id BIGINT NOT NULL,
    categoria_id BIGINT NOT NULL,
    PRIMARY KEY (livro_id, categoria_id),
    CONSTRAINT fk_livro_categoria_livro FOREIGN KEY (livro_id) REFERENCES livro(id) ON DELETE CASCADE,
    CONSTRAINT fk_livro_categoria_categoria FOREIGN KEY (categoria_id) REFERENCES categoria(id) ON DELETE CASCADE
);

-- Tabela de Cliente

CREATE TABLE cliente (
    id BIGSERIAL PRIMARY KEY,
    codigo VARCHAR(100) NOT NULL UNIQUE,
    nome VARCHAR(255) NOT NULL,
    genero VARCHAR(50),
    data_nascimento DATE,
    cpf VARCHAR(20) NOT NULL UNIQUE,
    telefone VARCHAR(20),
    email VARCHAR(255) NOT NULL UNIQUE,
    senha_criptografada VARCHAR(255) NOT NULL,
    ranking DOUBLE PRECISION,
    status VARCHAR(50) NOT NULL,
    INDEX idx_cliente_id (id),
    INDEX idx_cliente_codigo (codigo),
    INDEX idx_cliente_cpf (cpf),
    INDEX idx_cliente_email (email)
);

-- Tabela de Endereco (Depende de cliente)

CREATE TABLE endereco (
    id BIGSERIAL PRIMARY KEY,
    nome_identificador VARCHAR(255),
    tipo_residencia VARCHAR(50) NOT NULL,
    logradouro VARCHAR(255) NOT NULL,
    numero INT NOT NULL,
    bairro VARCHAR(100) NOT NULL,
    cep VARCHAR(20) NOT NULL,
    cidade VARCHAR(100) NOT NULL,
    estado VARCHAR(50) NOT NULL,
    pais VARCHAR(100) NOT NULL,
    tipo_endereco VARCHAR(50) NOT NULL,
    cliente_id BIGINT NOT NULL,
    CONSTRAINT fk_endereco_cliente FOREIGN KEY (cliente_id) REFERENCES cliente(id) ON DELETE CASCADE,
    INDEX idx_endereco_cliente (cliente_id)
);

-- Tabela de Cartao (Depende de cliente)

CREATE TABLE cartao (
    id BIGSERIAL PRIMARY KEY,
    numero VARCHAR(50) NOT NULL UNIQUE,
    nome_impresso VARCHAR(255) NOT NULL,
    bandeira VARCHAR(50) NOT NULL,
    codigo_seguranca VARCHAR(10) NOT NULL,
    preferencial BOOLEAN NOT NULL,
    cliente_id BIGINT NOT NULL,
    CONSTRAINT fk_cartao_cliente FOREIGN KEY (cliente_id) REFERENCES cliente(id) ON DELETE CASCADE,
    INDEX idx_cartao_cliente (cliente_id)
);

-- Tabela de Carrinho (Depende de cliente)

CREATE TABLE carrinho (
    id BIGSERIAL PRIMARY KEY,
    data_criacao DATE NOT NULL,
    status VARCHAR(50) NOT NULL,
    data_expiracao DATE,
    cliente_id BIGINT NOT NULL UNIQUE,
    CONSTRAINT fk_carrinho_cliente FOREIGN KEY (cliente_id) REFERENCES cliente(id),
    INDEX idx_carrinho_cliente (cliente_id)
);

-- Tabela de ItemCarrinho (Depende de carrinho, livro)

CREATE TABLE item_carrinho (
    id BIGSERIAL PRIMARY KEY,
    quantidade INT NOT NULL,
    valor_unitario DECIMAL(19, 2) NOT NULL,
    carrinho_id BIGINT NOT NULL,
    livro_id BIGINT NOT NULL,
    CONSTRAINT fk_item_carrinho_carrinho FOREIGN KEY (carrinho_id) REFERENCES carrinho(id) ON DELETE CASCADE,
    CONSTRAINT fk_item_carrinho_livro FOREIGN KEY (livro_id) REFERENCES livro(id),
    INDEX idx_item_carrinho_carrinho (carrinho_id)
);

-- Tabela de Pedido (Depende de cliente, endereco)

CREATE TABLE pedido (
    id BIGSERIAL PRIMARY KEY,
    data_criacao DATE NOT NULL,
    status VARCHAR(50) NOT NULL,
    valor_total DECIMAL(19, 2) NOT NULL,
    valor_frete DECIMAL(19, 2),
    cliente_id BIGINT NOT NULL,
    endereco_id BIGINT NOT NULL,
    pagamento_id BIGINT,
    CONSTRAINT fk_pedido_cliente FOREIGN KEY (cliente_id) REFERENCES cliente(id),
    CONSTRAINT fk_pedido_endereco FOREIGN KEY (endereco_id) REFERENCES endereco(id),
    INDEX idx_pedido_cliente (cliente_id),
    INDEX idx_pedido_status (status)
);

-- Tabela de ItemPedido (Depende de pedido, livro)

CREATE TABLE item_pedido (
    id BIGSERIAL PRIMARY KEY,
    quantidade INT NOT NULL,
    valor_unitario DECIMAL(19, 2) NOT NULL,
    pedido_id BIGINT NOT NULL,
    livro_id BIGINT NOT NULL,
    CONSTRAINT fk_item_pedido_pedido FOREIGN KEY (pedido_id) REFERENCES pedido(id) ON DELETE CASCADE,
    CONSTRAINT fk_item_pedido_livro FOREIGN KEY (livro_id) REFERENCES livro(id),
    INDEX idx_item_pedido_pedido (pedido_id)
);

-- Tabela de Pagamento

CREATE TABLE pagamento (
    id BIGSERIAL PRIMARY KEY,
    status VARCHAR(50) NOT NULL,
    valor_total DECIMAL(19, 2) NOT NULL,
    INDEX idx_pagamento_status (status)
);

-- Adicionar FK pagamento_id na table pedido após criar pagamento
ALTER TABLE pedido
ADD CONSTRAINT fk_pedido_pagamento FOREIGN KEY (pagamento_id) REFERENCES pagamento(id) ON DELETE SET NULL;

-- Tabela de PagamentoCartao (Depende de pagamento, cartao)

CREATE TABLE pagamento_cartao (
    id BIGSERIAL PRIMARY KEY,
    valor DECIMAL(19, 2) NOT NULL,
    pagamento_id BIGINT NOT NULL,
    cartao_id BIGINT NOT NULL,
    CONSTRAINT fk_pagamento_cartao_pagamento FOREIGN KEY (pagamento_id) REFERENCES pagamento(id) ON DELETE CASCADE,
    CONSTRAINT fk_pagamento_cartao_cartao FOREIGN KEY (cartao_id) REFERENCES cartao(id),
    INDEX idx_pagamento_cartao_pagamento (pagamento_id)
);

-- Tabela de PagamentoCupom (Depende de pagamento, cupom)

CREATE TABLE pagamento_cupom (
    id BIGSERIAL PRIMARY KEY,
    valor DECIMAL(19, 2) NOT NULL,
    pagamento_id BIGINT NOT NULL,
    cupom_id BIGINT NOT NULL,
    CONSTRAINT fk_pagamento_cupom_pagamento FOREIGN KEY (pagamento_id) REFERENCES pagamento(id) ON DELETE CASCADE,
    CONSTRAINT fk_pagamento_cupom_cupom FOREIGN KEY (cupom_id) REFERENCES cupom(id),
    INDEX idx_pagamento_cupom_pagamento (pagamento_id)
);

-- Tabela de Troca (Depende de pedido)

CREATE TABLE troca (
    id BIGSERIAL PRIMARY KEY,
    data_solicitacao DATE NOT NULL,
    status VARCHAR(50) NOT NULL,
    motivo TEXT,
    pedido_id BIGINT NOT NULL,
    CONSTRAINT fk_troca_pedido FOREIGN KEY (pedido_id) REFERENCES pedido(id) ON DELETE CASCADE,
    INDEX idx_troca_pedido (pedido_id),
    INDEX idx_troca_status (status)
);
