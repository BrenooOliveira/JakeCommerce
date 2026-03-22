-- Dados Iniciais do Sistema

-- Inserir Grupos de Precificação
INSERT INTO grupo_precificacao (nome, percentual_margem) VALUES
('Economia', 20.00),
('Padrão', 30.00),
('Premium', 40.00);

-- Inserir Editoras
INSERT INTO editora (nome) VALUES
('Companhia das Letras'),
('Record'),
('Intrínseca'),
('Rocco'),
('Editora 34');

-- Inserir Autores
INSERT INTO autor (nome) VALUES
('Machado de Assis'),
('Clarice Lispector'),
('Paulo Coelho'),
('Jorge Amado'),
('Carlos Drummond de Andrade');

-- Inserir Categorias
INSERT INTO categoria (nome) VALUES
('Ficção'),
('Romance'),
('Poesia'),
('Conto'),
('Crônica');

-- Inserir Cupons (para teste)
INSERT INTO cupom (codigo, valor, tipo, ativo) VALUES
('BENVINDO10', 10.00, 'PROMOCIONAL', true),
('DESCONTO20', 20.00, 'PROMOCIONAL', true);

-- Inserir Estoques
INSERT INTO estoque (quantidade, custo_atual, data_entrada) VALUES
(100, 25.00, '2024-01-15'),
(150, 18.00, '2024-01-20'),
(80, 45.00, '2024-02-01'),
(200, 12.00, '2024-02-10'),
(120, 35.00, '2024-02-15'),
(90, 28.00, '2024-03-01'),
(110, 55.00, '2024-03-10'),
(70, 38.50, '2024-03-15');

-- Inserir Livros
INSERT INTO livro (codigo, titulo, ano, edicao, isbn, numero_paginas, sinopse, dimensoes, codigo_barras, status, valor_venda, grupo_precificacao_id, editora_id, estoque_id) VALUES
('LIV001', 'Dom Casmurro', 1899, '1ª', '978-8535911770', 256, 'Romance clássico de Machado de Assis', '14x21cm', '9788535911770', 'ATIVO', 45.50, 2, 1, 1),
('LIV002', 'A Hora da Estrela', 1977, '1ª', '978-8532504630', 88, 'Novela poética de Clarice Lispector', '14x21cm', '9788532504630', 'ATIVO', 38.90, 2, 2, 2),
('LIV003', 'O Alquimista', 1988, '2ª', '978-8532515285', 224, 'Romance filosófico de Paulo Coelho', '14x21cm', '9788532515285', 'ATIVO', 65.00, 3, 3, 3),
('LIV004', 'Capitães da Areia', 1937, '1ª', '978-8501042459', 280, 'Romance de Jorge Amado', '14x21cm', '9788501042459', 'ATIVO', 52.80, 2, 4, 4),
('LIV005', 'Sentimento do Mundo', 1940, '1ª', '978-8525058848', 120, 'Coletânea de poesias de Drummond', '14x21cm', '9788525058848', 'ATIVO', 35.70, 2, 5, 5),
('LIV006', 'Memórias Póstumas de Brás Cubas', 1899, '1ª', '978-8535927429', 368, 'Obra-prima da literatura brasileira', '14x21cm', '9788535927429', 'ATIVO', 72.50, 3, 1, 6),
('LIV007', 'Grande Sertão Veredas', 1956, '1ª', '978-8532531156', 600, 'Épico do sertão de Guimarães Rosa', '14x21cm', '9788532531156', 'ATIVO', 89.90, 3, 2, 7),
('LIV008', 'Quincas Borba', 1891, '1ª', '978-8535904079', 285, 'Romance de Machado de Assis', '14x21cm', '9788535904079', 'ATIVO', 48.50, 2, 1, 8);

-- Registrar relacionamentos livro-categoria
INSERT INTO livro_categoria (livro_id, categoria_id) VALUES
(1, 2), (2, 2), (3, 2), (4, 2), (5, 3), (6, 2), (7, 2), (8, 2);

-- Registrar relacionamentos livro-autor
INSERT INTO livro_autor (livro_id, autor_id) VALUES
(1, 1), (2, 2), (3, 3), (4, 4), (5, 5), (6, 1), (7, 4), (8, 1);

-- Inserir Clientes
-- ADMIN: email=admin@jakebooks.com | senha=Admin123@
-- CLIENTES: email=*@teste.com | senha=ClienteTeste@123
INSERT INTO cliente (codigo, nome, genero, data_nascimento, cpf, telefone, email, senha_criptografada, ranking, status, is_admin) VALUES
('ADMIN001', 'Administrador', 'OUTRO', '1990-01-01', '00000000000', '11999999999', 'admin@jakebooks.com', '$2a$12$i06IiML1RKTrMr7pkgmZ.eDy0eK/mKuaSaPGeM3hoTOGvRFFiDUyS', 0.0, 'ATIVO', true),
('CLI001', 'Ana Silva', 'F', '1990-05-15', '12345678901', '11987654321', 'ana@teste.com', '$2a$12$qKUpEwOLNQGJLZ4PNW3sluMYm/GhqvT5XfYqSLhCx6X.HZHPdAoaS', 4.5, 'ATIVO', false),
('CLI002', 'Bruno Costa', 'M', '1985-08-22', '12345678902', '11987654322', 'bruno@teste.com', '$2a$12$qKUpEwOLNQGJLZ4PNW3sluMYm/GhqvT5XfYqSLhCx6X.HZHPdAoaS', 3.8, 'ATIVO', false),
('CLI003', 'Carla Oliveira', 'F', '1992-12-10', '12345678903', '11987654323', 'carla@teste.com', '$2a$12$qKUpEwOLNQGJLZ4PNW3sluMYm/GhqvT5XfYqSLhCx6X.HZHPdAoaS', 4.2, 'ATIVO', false),
('CLI004', 'Breno Aves', '2', '1992-12-10', '12345678904', '11987654324', 'breno@teste.com', '$2a$12$MYli6cvDraEkUJZcChaVXu.zLDJYB0vUe2gcyierqZ6ZvaLn0pa7a', 4.4, 'ATIVO', false);

-- Inserir Endereços
-- Endereços do Admin (RN0021: cobrança obrigatório | RN0022: entrega obrigatório)
INSERT INTO endereco (nome_identificador, tipo_residencia, logradouro, numero, bairro, cep, cidade, estado, pais, tipo_endereco, cliente_id) VALUES
('Administração', 'COMERCIAL', 'Rua Admin', '1', 'Centro', '01000-000', 'São Paulo', 'SP', 'Brasil', 'COBRANCA', 1),
('Administração', 'COMERCIAL', 'Rua Admin', '1', 'Centro', '01000-000', 'São Paulo', 'SP', 'Brasil', 'ENTREGA', 1),
-- Endereços dos clientes
('Casa', 'APARTAMENTO', 'Rua A', 100, 'Centro', '01000-000', 'São Paulo', 'SP', 'Brasil', 'COBRANCA', 2),
('Casa', 'APARTAMENTO', 'Rua A', 100, 'Centro', '01000-000', 'São Paulo', 'SP', 'Brasil', 'ENTREGA', 2),
('Apto', 'APARTAMENTO', 'Rua B', 200, 'Zona Norte', '02000-000', 'São Paulo', 'SP', 'Brasil', 'COBRANCA', 3),
('Apto', 'APARTAMENTO', 'Rua B', 200, 'Zona Norte', '02000-000', 'São Paulo', 'SP', 'Brasil', 'ENTREGA', 3),
('Residência', 'CASA', 'Rua C', 300, 'Vila Nova', '03000-000', 'São Paulo', 'SP', 'Brasil', 'COBRANCA', 4),
('Residência', 'CASA', 'Rua C', 300, 'Vila Nova', '03000-000', 'São Paulo', 'SP', 'Brasil', 'ENTREGA', 4);

-- Inserir Cartões
INSERT INTO cartao (numero, nome_impresso, bandeira, codigo_seguranca, preferencial, cliente_id) VALUES
('4111111111111111', 'ANA SILVA', 'VISA', '123', true, 2),
('5555555555554444', 'BRUNO COSTA', 'MASTERCARD', '456', true, 3),
('378282246310005', 'CARLA OLIVEIRA', 'AMEX', '789', true, 4);

-- Inserir Pagamentos (APROVADOS para vendas válidas)
INSERT INTO pagamento (status, valor_total) VALUES
('APROVADA', 168.50),
('APROVADA', 203.40),
('APROVADA', 312.75),
('APROVADA', 157.60),
('APROVADA', 245.90),
('APROVADA', 189.70),
('APROVADA', 428.40),
('APROVADA', 296.30),
('APROVADA', 521.00),
('APROVADA', 384.20);

-- Inserir Pedidos (com datas variadas para teste de período)
INSERT INTO pedido (data_criacao, status, valor_total, valor_frete, cliente_id, endereco_id, pagamento_id) VALUES
('2024-01-10', 'ENTREGUE', 168.50, 15.00, 2, 4, 1),
('2024-01-15', 'ENTREGUE', 203.40, 15.00, 3, 6, 2),
('2024-02-05', 'ENTREGUE', 312.75, 20.00, 4, 8, 3),
('2024-02-20', 'ENTREGUE', 157.60, 15.00, 2, 4, 4),
('2024-03-10', 'ENTREGUE', 245.90, 20.00, 3, 6, 5),
('2024-03-18', 'ENTREGUE', 189.70, 15.00, 4, 8, 6),
('2024-04-05', 'ENTREGUE', 428.40, 25.00, 2, 4, 7),
('2024-04-22', 'ENTREGUE', 296.30, 15.00, 3, 6, 8),
('2024-05-08', 'ENTREGUE', 521.00, 30.00, 4, 8, 9),
('2024-05-25', 'ENTREGUE', 384.20, 20.00, 2, 4, 10);

-- Inserir ItemPedidos (com produtos variados)
INSERT INTO item_pedido (quantidade, valor_unitario, pedido_id, livro_id) VALUES
(2, 45.50, 1, 1), (1, 38.90, 1, 2),
(1, 65.00, 2, 3), (2, 52.80, 2, 4),
(2, 45.50, 3, 1), (1, 89.90, 3, 7),
(3, 38.90, 4, 2),
(1, 72.50, 5, 6), (2, 48.50, 5, 8),
(2, 35.70, 6, 5),
(3, 65.00, 7, 3), (2, 48.50, 7, 8),
(2, 89.90, 8, 7),
(3, 72.50, 9, 6), (2, 52.80, 9, 4),
(4, 45.50, 10, 1), (1, 38.90, 10, 2);
