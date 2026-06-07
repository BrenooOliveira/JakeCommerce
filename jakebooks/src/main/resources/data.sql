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

-- Inserir cupom simples para o fluxo de venda
INSERT INTO cupom (codigo, valor, tipo, ativo, data_criacao, data_validade, cliente_id) VALUES
('BEMVINDO10', 10.00, 'PROMOCIONAL', true, CURRENT_DATE, NULL, NULL);

-- Inserir Estoques
INSERT INTO estoque (quantidade, custo_atual, data_entrada) VALUES
(100, 25.00, '2024-01-15'),
(150, 18.00, '2024-01-20'),
(80, 45.00, '2024-02-01'),
(200, 12.00, '2024-02-10'),
(120, 35.00, '2024-02-15'),
(90, 28.00, '2024-03-01'),
(110, 55.00, '2024-03-10'),
(70, 38.50, '2024-03-15'),
-- Estoques adicionais para novos livros
(60, 42.00, '2024-04-01'),
(85, 32.00, '2024-04-10'),
(95, 22.00, '2024-04-15'),
(75, 48.00, '2024-05-01'),
(40, 65.00, '2024-05-10');

-- Inserir Livros
INSERT INTO livro (codigo, titulo, ano, edicao, isbn, numero_paginas, sinopse, dimensoes, codigo_barras, status, valor_venda, grupo_precificacao_id, editora_id, estoque_id) VALUES
('LIV001', 'Dom Casmurro', 1899, '1ª', '978-8535911770', 256, 'Romance clássico de Machado de Assis', '14x21cm', '9788535911770', 'ATIVO', 10.00, 2, 1, 1),
('LIV002', 'A Hora da Estrela', 1977, '1ª', '978-8532504630', 88, 'Novela poética de Clarice Lispector', '14x21cm', '9788532504630', 'ATIVO', 38.90, 2, 2, 2),
('LIV003', 'O Alquimista', 1988, '2ª', '978-8532515285', 224, 'Romance filosófico de Paulo Coelho', '14x21cm', '9788532515285', 'ATIVO', 65.00, 3, 3, 3),
('LIV004', 'Capitães da Areia', 1937, '1ª', '978-8501042459', 280, 'Romance de Jorge Amado', '14x21cm', '9788501042459', 'ATIVO', 52.80, 2, 4, 4),
('LIV005', 'Sentimento do Mundo', 1940, '1ª', '978-8525058848', 120, 'Coletânea de poesias de Drummond', '14x21cm', '9788525058848', 'ATIVO', 35.70, 2, 5, 5),
('LIV006', 'Memórias Póstumas de Brás Cubas', 1899, '1ª', '978-8535927429', 368, 'Obra-prima da literatura brasileira', '14x21cm', '9788535927429', 'ATIVO', 72.50, 3, 1, 6),
('LIV007', 'Grande Sertão Veredas', 1956, '1ª', '978-8532531156', 600, 'Épico do sertão de Guimarães Rosa', '14x21cm', '9788532531156', 'ATIVO', 89.90, 3, 2, 7),
('LIV008', 'Quincas Borba', 1891, '1ª', '978-8535904079', 285, 'Romance de Machado de Assis', '14x21cm', '9788535904079', 'ATIVO', 48.50, 2, 1, 8),
-- Novos livros para mais variedade
('LIV009', 'Vidas Secas', 1938, '1ª', '978-8501006752', 176, 'Romance de Graciliano Ramos sobre a seca nordestina', '14x21cm', '9788501006752', 'ATIVO', 42.90, 2, 4, 9),
('LIV010', 'Iracema', 1865, '1ª', '978-8572327428', 144, 'Romance indianista de José de Alencar', '14x21cm', '9788572327428', 'ATIVO', 28.50, 1, 1, 10),
('LIV011', 'O Cortiço', 1890, '1ª', '978-8544001165', 224, 'Romance naturalista de Aluísio Azevedo', '14x21cm', '9788544001165', 'ATIVO', 32.90, 2, 2, 11),
('LIV012', 'Macunaíma', 1928, '1ª', '978-8503009751', 208, 'Rapsódia modernista de Mário de Andrade', '14x21cm', '9788503009751', 'ATIVO', 55.00, 3, 3, 12),
('LIV013', 'A Moreninha', 1844, '1ª', '978-8572326766', 192, 'Romance romântico de Joaquim Manuel de Macedo', '14x21cm', '9788572326766', 'ATIVO', 25.90, 1, 5, 13);

-- Registrar relacionamentos livro-categoria
INSERT INTO livro_categoria (livro_id, categoria_id) VALUES
(1, 2), (2, 2), (3, 2), (4, 2), (5, 3), (6, 2), (7, 2), (8, 2),
-- Novos livros
(9, 2), (10, 2), (11, 2), (12, 1), (12, 2), (13, 2);

-- Registrar relacionamentos livro-autor
INSERT INTO livro_autor (livro_id, autor_id) VALUES
(1, 1), (2, 2), (3, 3), (4, 4), (5, 5), (6, 1), (7, 4), (8, 1),
-- Novos livros (usando autores existentes para simplificar)
(9, 4), (10, 1), (11, 2), (12, 3), (13, 5);

-- Inserir Clientes
-- ADMIN: email=admin@jakebooks.com | senha=Admin123@
-- CLIENTES: email=*@teste.com | senha=ClienteTeste@123
INSERT INTO cliente (codigo, nome, genero, data_nascimento, cpf, telefone, email, senha_criptografada, ranking, status, is_admin, usuario_role) VALUES
('ADMIN001', 'Administrador', 'OUTRO', '1990-01-01', '00000000000', '11999999999', 'admin@jakebooks.com', '$2a$12$i06IiML1RKTrMr7pkgmZ.eDy0eK/mKuaSaPGeM3hoTOGvRFFiDUyS', 0.0, 'ATIVO', true, 'ROLE_ADMIN'),
('CLI001', 'Ana Silva', 'F', '1990-05-15', '12345678901', '11987654321', 'ana@teste.com', '$2a$12$qKUpEwOLNQGJLZ4PNW3sluMYm/GhqvT5XfYqSLhCx6X.HZHPdAoaS', 4.5, 'ATIVO', false, 'ROLE_CLIENTE'),
('CLI002', 'Bruno Costa', 'M', '1985-08-22', '12345678902', '11987654322', 'bruno@teste.com', '$2a$12$qKUpEwOLNQGJLZ4PNW3sluMYm/GhqvT5XfYqSLhCx6X.HZHPdAoaS', 3.8, 'ATIVO', false, 'ROLE_CLIENTE'),
('CLI003', 'Carla Oliveira', 'F', '1992-12-10', '12345678903', '11987654323', 'carla@teste.com', '$2a$12$qKUpEwOLNQGJLZ4PNW3sluMYm/GhqvT5XfYqSLhCx6X.HZHPdAoaS', 4.2, 'ATIVO', false, 'ROLE_CLIENTE'),
('CLI004', 'Breno Aves', '2', '1992-12-10', '12345678904', '11987654324', 'breno@teste.com', '$2a$12$MYli6cvDraEkUJZcChaVXu.zLDJYB0vUe2gcyierqZ6ZvaLn0pa7a', 4.4, 'ATIVO', false, 'ROLE_CLIENTE');

-- Inserir Endereços
-- Endereços do Admin (RN0021: cobrança obrigatório | RN0022: entrega obrigatório)
INSERT INTO endereco (nome_identificador, tipo_residencia, logradouro, numero, bairro, cep, cidade, estado, pais, tipo_endereco, cliente_id) VALUES
('Administração', 'COMERCIO', 'Rua Admin', '1', 'Centro', '01000-000', 'São Paulo', 'SP', 'Brasil', 'COBRANCA', 1),
('Administração', 'COMERCIO', 'Rua Admin', '1', 'Centro', '01000-000', 'São Paulo', 'SP', 'Brasil', 'ENTREGA', 1),
-- Endereços dos clientes
('Casa', 'APARTAMENTO', 'Rua A', 100, 'Centro', '01000-000', 'São Paulo', 'SP', 'Brasil', 'COBRANCA', 2),
('Casa', 'APARTAMENTO', 'Rua A', 100, 'Centro', '01000-000', 'São Paulo', 'SP', 'Brasil', 'ENTREGA', 2),
('Apto', 'APARTAMENTO', 'Rua B', 200, 'Zona Norte', '02000-000', 'São Paulo', 'SP', 'Brasil', 'COBRANCA', 3),
('Apto', 'APARTAMENTO', 'Rua B', 200, 'Zona Norte', '02000-000', 'São Paulo', 'SP', 'Brasil', 'ENTREGA', 3),
('Residência', 'CASA', 'Rua C', 300, 'Vila Nova', '03000-000', 'São Paulo', 'SP', 'Brasil', 'COBRANCA', 4),
('Residência', 'CASA', 'Rua C', 300, 'Vila Nova', '03000-000', 'São Paulo', 'SP', 'Brasil', 'ENTREGA', 4),
-- Endereços do Breno (cliente_id=5)
('Casa Principal', 'CASA', 'Av. Paulista', 1500, 'Bela Vista', '01310-100', 'São Paulo', 'SP', 'Brasil', 'COBRANCA', 5),
('Casa Principal', 'CASA', 'Av. Paulista', 1500, 'Bela Vista', '01310-100', 'São Paulo', 'SP', 'Brasil', 'ENTREGA', 5),
('Trabalho', 'COMERCIO', 'Rua Augusta', 2000, 'Consolação', '01304-001', 'São Paulo', 'SP', 'Brasil', 'ENTREGA', 5);

-- Inserir Cartões
INSERT INTO cartao (numero, nome_impresso, bandeira, codigo_seguranca, preferencial, cliente_id) VALUES
('4111111111111111', 'ANA SILVA', 'VISA', '123', true, 2),
('5555555555554444', 'BRUNO COSTA', 'MASTERCARD', '456', true, 3),
('378282246310005', 'CARLA OLIVEIRA', 'AMEX', '789', true, 4),
-- Cartões do Breno (cliente_id=5)
('4539578763621486', 'BRENO AVES', 'VISA', '321', true, 5),
('5425233430109903', 'BRENO AVES', 'MASTERCARD', '654', false, 5);

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
('APROVADA', 384.20),
-- Mock para teste de despacho ADMIN (RF0038 / RN0039)
('APROVADA', 146.80);

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
('2024-05-25', 'ENTREGUE', 384.20, 20.00, 2, 4, 10),
('2026-04-06', 'EM_PROCESSAMENTO', 146.80, 15.00, 5, 10, 11);

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
(4, 45.50, 10, 1), (1, 38.90, 10, 2),
(2, 42.90, 11, 9), (1, 45.50, 11, 1);

-- Inserir Trocas
INSERT INTO troca (data_solicitacao, status, motivo, pedido_id) VALUES
('2024-01-20', 'SOLICITADA', 'Cliente solicitou troca por tamanho inadequado', 1),
('2024-02-18', 'AUTORIZADA', 'Troca autorizada após validação do atendimento', 2),
('2024-03-22', 'RECEBIDA', 'Produto recebido no centro de trocas para conferência', 3),
('2024-04-15', 'CONCLUIDA', 'Troca concluída com emissão de cupom para o cliente', 4);

-- ============================================================================
-- Base histórica gerada automaticamente: 13 meses (2025-05 → 2026-05)
-- 26 pedidos adicionais (2 por mês) com pagamentos e itens relacionados
-- Gerado em: 2026-06-03
-- Parâmetros: 2 pedidos/mês, clientes rotativos (ids 2..5), livros ids 1..13
-- ============================================================================

-- Inserir Pagamentos históricos (IDs 12..37)
INSERT INTO pagamento (status, valor_total) VALUES
('REPROVADA', 25.00),
('APROVADA', 162.80),
('APROVADA', 221.30),
('APROVADA', 125.60),
('APROVADA', 195.70),
('APROVADA', 351.90),
('APROVADA', 104.90),
('APROVADA', 159.90),
('APROVADA', 147.80),
('APROVADA', 77.00),
('APROVADA', 157.90),
('APROVADA', 175.90),
('APROVADA', 40.90),
('APROVADA', 78.90),
('APROVADA', 236.70),
('APROVADA', 150.00),
('APROVADA', 139.20),
('APROVADA', 343.70),
('APROVADA', 87.50),
('APROVADA', 248.30),
('REPROVADA', 177.80),
('APROVADA', 105.80),
('APROVADA', 109.30),
('APROVADA', 192.60),
('APROVADA', 70.00),
('APROVADA', 81.80);

-- Inserir Pedidos históricos (13 meses cobrindo 2025-05 a 2026-05)
INSERT INTO pedido (data_criacao, status, valor_total, valor_frete, cliente_id, endereco_id, pagamento_id) VALUES
('2025-05-05 10:00', 'ENTREGUE', 25.00, 15.00, 2, 4, 12),
('2025-05-15 15:00', 'ENTREGUE', 162.80, 20.00, 3, 6, 13),
('2025-06-05 10:00', 'ENTREGUE', 221.30, 15.00, 4, 8, 14),
('2025-06-15 15:00', 'ENTREGUE', 125.60, 20.00, 5, 10, 15),
('2025-07-05 10:00', 'ENTREGUE', 195.70, 15.00, 2, 4, 16),
('2025-07-15 15:00', 'ENTREGUE', 351.90, 20.00, 3, 6, 17),
('2025-08-05 10:00', 'ENTREGUE', 104.90, 15.00, 4, 8, 18),
('2025-08-15 15:00', 'EM_PROCESSAMENTO', 159.90, 20.00, 5, 10, 19),
('2025-09-05 10:00', 'ENTREGUE', 147.80, 15.00, 2, 4, 20),
('2025-09-15 15:00', 'ENTREGUE', 77.00, 20.00, 3, 6, 21),
('2025-10-05 10:00', 'ENTREGUE', 157.90, 15.00, 4, 8, 22),
('2025-10-15 15:00', 'ENTREGUE', 175.90, 20.00, 5, 10, 23),
('2025-11-05 10:00', 'ENTREGUE', 40.90, 15.00, 2, 4, 24),
('2025-11-15 15:00', 'ENTREGUE', 78.90, 20.00, 3, 6, 25),
('2025-12-05 10:00', 'ENTREGUE', 236.70, 15.00, 4, 8, 26),
('2025-12-15 15:00', 'ENTREGUE', 150.00, 20.00, 5, 10, 27),
('2026-01-05 10:00', 'ENTREGUE', 139.20, 15.00, 2, 4, 28),
('2026-01-15 15:00', 'ENTREGUE', 343.70, 20.00, 3, 6, 29),
('2026-02-05 10:00', 'ENTREGUE', 87.50, 15.00, 4, 8, 30),
('2026-02-15 15:00', 'ENTREGUE', 248.30, 20.00, 5, 10, 31),
('2026-03-05 10:00', 'ENTREGUE', 177.80, 15.00, 2, 4, 32),
('2026-03-15 15:00', 'ENTREGUE', 105.80, 20.00, 3, 6, 33),
('2026-04-05 10:00', 'ENTREGUE', 109.30, 15.00, 4, 8, 34),
('2026-04-15 15:00', 'EM_TRANSPORTE', 192.60, 20.00, 5, 10, 35),
('2026-05-05 10:00', 'ENTREGUE', 70.00, 15.00, 2, 4, 36),
('2026-05-15 15:00', 'ENTREGUE', 81.80, 20.00, 3, 6, 37);

-- Inserir ItemPedidos históricos
INSERT INTO item_pedido (quantidade, valor_unitario, pedido_id, livro_id) VALUES
(1, 10.00, 12, 1),
(2, 38.90, 13, 2), (1, 65.00, 13, 3),
(1, 65.00, 14, 3), (2, 52.80, 14, 4), (1, 35.70, 14, 5),
(2, 52.80, 15, 4),
(1, 35.70, 16, 5), (2, 72.50, 16, 6),
(2, 72.50, 17, 6), (1, 89.90, 17, 7), (2, 48.50, 17, 8),
(1, 89.90, 18, 7),
(2, 48.50, 19, 8), (1, 42.90, 19, 9),
(1, 42.90, 20, 9), (2, 28.50, 20, 10), (1, 32.90, 20, 11),
(2, 28.50, 21, 10),
(1, 32.90, 22, 11), (2, 55.00, 22, 12),
(2, 55.00, 23, 12), (1, 25.90, 23, 13), (2, 10.00, 23, 1),
(1, 25.90, 24, 13),
(2, 10.00, 25, 1), (1, 38.90, 25, 2),
(1, 38.90, 26, 2), (2, 65.00, 26, 3), (1, 52.80, 26, 4),
(2, 65.00, 27, 3),
(1, 52.80, 28, 4), (2, 35.70, 28, 5),
(2, 35.70, 29, 5), (1, 72.50, 29, 6), (2, 89.90, 29, 7),
(1, 72.50, 30, 6),
(2, 89.90, 31, 7), (1, 48.50, 31, 8),
(1, 48.50, 32, 8), (2, 42.90, 32, 9), (1, 28.50, 32, 10),
(2, 42.90, 33, 9),
(1, 28.50, 34, 10), (2, 32.90, 34, 11),
(2, 32.90, 35, 11), (1, 55.00, 35, 12), (2, 25.90, 35, 13),
(1, 55.00, 36, 12),
(2, 25.90, 37, 13), (1, 10.00, 37, 1);

-- Fim da geração histórica automática

-- ============================================================================
-- Histórico complementar para reforçar o dashboard analítico
-- Cobertura adicional: 2025-04 -> 2026-05
-- 14 pedidos extras, todos APROVADA e com itens totalizando o valor do pedido
-- ============================================================================

INSERT INTO pagamento (status, valor_total) VALUES
('APROVADA', 104.50),
('APROVADA', 91.40),
('APROVADA', 132.00),
('APROVADA', 117.80),
('APROVADA', 137.30),
('APROVADA', 103.50),
('APROVADA', 157.30),
('APROVADA', 146.80),
('APROVADA', 113.30),
('APROVADA', 177.80),
('APROVADA', 91.40),
('APROVADA', 161.30),
('APROVADA', 133.80),
('APROVADA', 123.80);

INSERT INTO pedido (data_criacao, status, valor_total, valor_frete, cliente_id, endereco_id, pagamento_id) VALUES
('2025-04-08 10:00', 'ENTREGUE', 104.50, 15.00, 2, 4, 38),
('2025-05-12 11:00', 'ENTREGUE', 91.40, 20.00, 3, 6, 39),
('2025-06-09 14:00', 'ENTREGUE', 132.00, 15.00, 4, 8, 40),
('2025-07-11 09:30', 'ENTREGUE', 117.80, 20.00, 5, 10, 41),
('2025-08-07 16:00', 'ENTREGUE', 137.30, 15.00, 2, 4, 42),
('2025-09-12 10:30', 'ENTREGUE', 103.50, 20.00, 3, 6, 43),
('2025-10-09 12:20', 'ENTREGUE', 157.30, 15.00, 4, 8, 44),
('2025-11-14 15:45', 'ENTREGUE', 146.80, 20.00, 5, 10, 45),
('2025-12-11 08:15', 'ENTREGUE', 113.30, 15.00, 2, 4, 46),
('2026-01-08 13:05', 'ENTREGUE', 177.80, 20.00, 3, 6, 47),
('2026-02-13 11:40', 'ENTREGUE', 91.40, 15.00, 4, 8, 48),
('2026-03-10 17:10', 'ENTREGUE', 161.30, 20.00, 5, 10, 49),
('2026-04-18 09:20', 'ENTREGUE', 133.80, 15.00, 2, 4, 50),
('2026-05-09 10:50', 'ENTREGUE', 123.80, 20.00, 3, 6, 51);

INSERT INTO item_pedido (quantidade, valor_unitario, pedido_id, livro_id) VALUES
(1, 42.90, 38, 9), (1, 35.70, 38, 5), (1, 25.90, 38, 13),
(1, 42.90, 39, 9), (1, 48.50, 39, 8),
(1, 55.00, 40, 12), (1, 48.50, 40, 8), (1, 28.50, 40, 10),
(1, 65.00, 41, 3), (1, 52.80, 41, 4),
(1, 72.50, 42, 6), (1, 38.90, 42, 2), (1, 25.90, 42, 13),
(1, 55.00, 43, 12), (1, 48.50, 43, 8),
(1, 89.90, 44, 7), (1, 38.90, 44, 2), (1, 28.50, 44, 10),
(1, 65.00, 45, 3), (1, 42.90, 45, 9), (1, 38.90, 45, 2),
(1, 48.50, 46, 8), (1, 38.90, 46, 2), (1, 25.90, 46, 13),
(1, 89.90, 47, 7), (1, 55.00, 47, 12), (1, 32.90, 47, 11),
(1, 48.50, 48, 8), (1, 42.90, 48, 9),
(1, 89.90, 49, 7), (1, 42.90, 49, 9), (1, 28.50, 49, 10),
(1, 65.00, 50, 3), (1, 42.90, 50, 9), (1, 25.90, 50, 13),
(1, 65.00, 51, 3), (1, 32.90, 51, 11), (1, 25.90, 51, 13);

