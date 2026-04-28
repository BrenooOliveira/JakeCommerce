-- ============================================
-- JakeBooks E2E Test Data Seed Script
-- ============================================
-- Purpose: Populate database with test data for E2E tests
-- Usage: psql -U postgres -d jakebooks -f seed-test-data.sql
-- Note: Password will need to be hashed. See instructions below.

-- ============================================
-- CLEANUP: Remove existing test data
-- ============================================
DELETE FROM item_pedido WHERE pedido_id IN (SELECT id FROM pedido WHERE cliente_codigo = 'CLI001');
DELETE FROM pagamento_cartao WHERE pagamento_id IN (SELECT p.id FROM pagamento p WHERE p.pedido_id IN (SELECT id FROM pedido WHERE cliente_codigo = 'CLI001'));
DELETE FROM pagamento WHERE pedido_id IN (SELECT id FROM pedido WHERE cliente_codigo = 'CLI001');
DELETE FROM pedido WHERE cliente_codigo = 'CLI001';
DELETE FROM item_carrinho WHERE carrinho_id IN (SELECT id FROM carrinho WHERE cliente_codigo = 'CLI001');
DELETE FROM carrinho WHERE cliente_codigo = 'CLI001';
DELETE FROM cartao WHERE cliente_codigo = 'CLI001';
DELETE FROM endereco WHERE cliente_codigo = 'CLI001';
DELETE FROM cliente WHERE codigo = 'CLI001';

-- Clean up test book data
DELETE FROM preco_livro WHERE livro_codigo IN ('LIV001', 'LIV002', 'LIV003');
DELETE FROM estoque WHERE livro_codigo IN ('LIV001', 'LIV002', 'LIV003');
DELETE FROM livro WHERE codigo IN ('LIV001', 'LIV002', 'LIV003');

-- ============================================
-- INSERT: Test Customer (Cliente)
-- ============================================
-- Password for "senha123" hashed with BCrypt:
-- $2a$12$y2sqeZm8rjPmhSU0R7c1eOPFdKVRVEeLZGvD8xS1VvMN6XGjVNH1. (example)
-- To generate your own:
-- 1. Go to https://bcrypt-generator.com/
-- 2. Enter password: senha123
-- 3. Cost: 12
-- 4. Copy the generated hash
-- 5. Use it in the INSERT statement below

INSERT INTO cliente (codigo, nome, cpf, email, telefone, ranking, status, criado_em)
VALUES (
    'CLI001',
    'Cliente Teste E2E',
    '12345678901',
    'cliente@exemplo.com',
    '11987654321',
    1,
    'ATIVO',
    NOW()
);

-- NOTE: If the password field exists and requires the hashed value,
-- you'll need to update it after insertion because passwords are hashed
-- during user registration. For testing, you can either:
-- 1. Use Spring's PasswordEncoder to hash the password
-- 2. Update the password hash in the database directly
-- 3. Use the hashed value directly if you know the algorithm

-- Uncomment and run if password field exists in cliente table:
-- UPDATE cliente SET senha = '$2a$12$...' WHERE codigo = 'CLI001';

-- ============================================
-- INSERT: Test Addresses
-- ============================================
INSERT INTO endereco (
    cliente_codigo,
    nome_identificador,
    logradouro,
    numero,
    complemento,
    bairro,
    cidade,
    estado,
    cep,
    tipo_endereco,
    criado_em
) VALUES (
    'CLI001',
    'Casa',
    'Rua Principal',
    '123',
    'Apto 45',
    'Centro',
    'São Paulo',
    'SP',
    '01234-567',
    'ENTREGA',
    NOW()
);

-- Alternative delivery address
INSERT INTO endereco (
    cliente_codigo,
    nome_identificador,
    logradouro,
    numero,
    bairro,
    cidade,
    estado,
    cep,
    tipo_endereco,
    criado_em
) VALUES (
    'CLI001',
    'Trabalho',
    'Avenida Paulista',
    '1200',
    'Bela Vista',
    'São Paulo',
    'SP',
    '01311-100',
    'ENTREGA',
    NOW()
);

-- ============================================
-- INSERT: Test Payment Cards
-- ============================================
-- Test card numbers (DO NOT USE IN PRODUCTION):
-- VISA: 4111 1111 1111 1111
-- Mastercard: 5555 5555 5555 4444
-- Amex: 3782 822463 10005

INSERT INTO cartao (
    cliente_codigo,
    nome_impresso,
    numero,
    bandeira,
    mes_expiracao,
    ano_expiracao,
    status,
    preferencial,
    criado_em
) VALUES (
    'CLI001',
    'CLIENTE TESTE',
    '4111111111111111',
    'VISA',
    12,
    2025,
    'ATIVO',
    true,
    NOW()
);

INSERT INTO cartao (
    cliente_codigo,
    nome_impresso,
    numero,
    bandeira,
    mes_expiracao,
    ano_expiracao,
    status,
    preferencial,
    criado_em
) VALUES (
    'CLI001',
    'CLIENTE TESTE',
    '5555555555554444',
    'MASTERCARD',
    6,
    2025,
    'ATIVO',
    false,
    NOW()
);

-- ============================================
-- INSERT: Test Books (Livro)
-- ============================================

INSERT INTO livro (
    codigo,
    titulo,
    año,
    edicao,
    isbn,
    numero_paginas,
    sinopse,
    status,
    criado_em
) VALUES (
    'LIV001',
    'Test Book: Introduction to Java Programming',
    2024,
    1,
    '978-1234567890',
    500,
    'A comprehensive guide to Java programming for beginners and experienced developers. This test book covers all essential concepts.',
    'ATIVO',
    NOW()
);

INSERT INTO livro (
    codigo,
    titulo,
    año,
    edicao,
    isbn,
    numero_paginas,
    sinopse,
    status,
    criado_em
) VALUES (
    'LIV002',
    'Test Book: Spring Boot in Action',
    2023,
    2,
    '978-0987654321',
    450,
    'Master Spring Boot framework with practical examples and real-world applications.',
    'ATIVO',
    NOW()
);

INSERT INTO livro (
    codigo,
    titulo,
    año,
    edicao,
    isbn,
    numero_paginas,
    sinopse,
    status,
    criado_em
) VALUES (
    'LIV003',
    'Test Book: Database Design and SQL',
    2022,
    3,
    '978-1122334455',
    380,
    'Learn database design principles and advanced SQL techniques.',
    'ATIVO',
    NOW()
);

-- ============================================
-- INSERT: Stock (Estoque) for Test Books
-- ============================================

INSERT INTO estoque (
    livro_codigo,
    quantidade,
    custo_atual,
    data_entrada,
    criado_em
) VALUES (
    'LIV001',
    10,
    50.00,
    NOW(),
    NOW()
);

INSERT INTO estoque (
    livro_codigo,
    quantidade,
    custo_atual,
    data_entrada,
    criado_em
) VALUES (
    'LIV002',
    15,
    45.00,
    NOW(),
    NOW()
);

INSERT INTO estoque (
    livro_codigo,
    quantidade,
    custo_atual,
    data_entrada,
    criado_em
) VALUES (
    'LIV003',
    8,
    55.00,
    NOW(),
    NOW()
);

-- ============================================
-- INSERT: Book Pricing
-- ============================================
-- Note: Adjust prices according to your test requirements
-- RN0051: Price based on highest cost + margin

INSERT INTO preco_livro (
    livro_codigo,
    valor,
    data_vigencia,
    criado_em
) VALUES (
    'LIV001',
    150.00,
    NOW(),
    NOW()
);

INSERT INTO preco_livro (
    livro_codigo,
    valor,
    data_vigencia,
    criado_em
) VALUES (
    'LIV002',
    145.00,
    NOW(),
    NOW()
);

INSERT INTO preco_livro (
    livro_codigo,
    valor,
    data_vigencia,
    criado_em
) VALUES (
    'LIV003',
    160.00,
    NOW(),
    NOW()
);

-- ============================================
-- VERIFY: Inserted Data
-- ============================================

-- Verify customer
SELECT 'Cliente' AS tipo, COUNT(*) AS quantidade FROM cliente WHERE codigo = 'CLI001';

-- Verify addresses
SELECT 'Endereços' AS tipo, COUNT(*) AS quantidade FROM endereco WHERE cliente_codigo = 'CLI001';

-- Verify cards
SELECT 'Cartões' AS tipo, COUNT(*) AS quantidade FROM cartao WHERE cliente_codigo = 'CLI001';

-- Verify books
SELECT 'Livros' AS tipo, COUNT(*) AS quantidade FROM livro WHERE codigo IN ('LIV001', 'LIV002', 'LIV003');

-- Verify stock
SELECT 'Estoque' AS tipo, COUNT(*) AS quantidade FROM estoque WHERE livro_codigo IN ('LIV001', 'LIV002', 'LIV003');

-- Verify pricing
SELECT 'Preços' AS tipo, COUNT(*) AS quantidade FROM preco_livro WHERE livro_codigo IN ('LIV001', 'LIV002', 'LIV003');

-- ============================================
-- NOTES
-- ============================================
/*
1. PASSWORD HASHING:
   Since customer password is typically hashed during registration,
   you need to set the correct hashed value. Options:

   a) Generate hash online:
      - Go to https://bcrypt-generator.com/
      - Password: senha123
      - Cost: 12
      - Copy hash and run:

      UPDATE cliente SET senha = '$2a$12$...' WHERE codigo = 'CLI001';

   b) Or modify the registration process to insert hashed password

2. TEST CREDENTIALS:
   Email: cliente@exemplo.com
   Password: senha123

3. TEST CARD DETAILS:
   - All test cards use expiration 12/25 (adjust if needed)
   - These are valid test card numbers for development
   - DO NOT use in production environment

4. ORDER VALUE:
   - Book 1: R$ 150.00
   - Total with one book: R$ 150.00
   - This exceeds minimum order value (R$ 20.00)
   - Shipping is FREE (>= R$ 20.00)

5. ENSURE DATA INTEGRITY:
   - All foreign keys must exist
   - Status values must match enums in application
   - Codes must be unique
   - Dates should be in the past or NOW()

6. RESET TEST DATA:
   To clean up and restart tests, run the cleanup section first,
   or run this entire script again.
*/

-- ============================================
-- END OF SEED SCRIPT
-- ============================================
