/**
 * ======================================================================
 * TEST DATA FIXTURES
 * ======================================================================
 *
 * Contains pre-configured test data for the order placement tests.
 * These fixtures assume that a test customer with addresses and cards
 * is already seeded in the database via data.sql or test setup.
 */

export const testCustomer = {
  email: 'cliente@jakebooks.com',
  password: 'Senha123!',
  codigo: 'CLI001',
  nome: 'João Silva',
  cpf: '123.456.789-00',
  telefone: '(11) 98765-4321'
};

export const testBook = {
  codigo: 'LIV001',
  titulo: 'Clean Code',
  author: 'Robert C. Martin',
  isbn: '978-0132350884',
  valorVenda: 89.90,
  status: 'ATIVO'
};

export const testAddress = {
  id: 1,
  nomeIdentificador: 'Casa',
  tipoResidencia: 'Apartamento',
  logradouro: 'Rua Principal',
  numero: '123',
  bairro: 'Centro',
  cidade: 'São Paulo',
  estado: 'SP',
  pais: 'Brasil',
  cep: '01310-100',
  tipoEndereco: 'ENTREGA'
};

export const testCard = {
  id: 1,
  numero: '4111111111111111',
  nomeImpresso: 'JOAO SILVA',
  bandeira: 'VISA',
  codigoSeguranca: '123',
  preferencial: true
};

/**
 * Business rule validations embedded in fixtures
 */
export const businessRules = {
  minOrderValue: 20.00,           // RN0064: Minimum R$ 20
  maxQuantityPerBook: 10,         // RN0063: Maximum 10 units
  minPaymentPerCard: 10.00,       // RN0034: Minimum R$ 10 per card
  cartExpirationMinutes: 1440,    // 24 hours
  freeShippingThreshold: 20.00,   // R$ 20 or more = free shipping
  shippingCost: 15.00             // R$ 15 for orders below threshold
};

/**
 * Expected values for order confirmation
 */
export const expectedOrderValues = {
  status: 'EM_PROCESSAMENTO',
  paymentStatus: 'APROVADA',
  itemCount: 1,
  totalValue: testBook.valorVenda + businessRules.freeShippingCost
};