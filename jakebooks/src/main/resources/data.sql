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
