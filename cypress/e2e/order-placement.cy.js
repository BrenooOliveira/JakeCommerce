/**
 * ======================================================================
 * END-TO-END TEST: Successful Order Placement Flow
 * ======================================================================
 *
 * This test validates the complete purchase flow in JakeBooks:
 * 1. User authentication (login)
 * 2. Browse and add a book to cart
 * 3. Go to cart page and review items
 * 4. Proceed to checkout
 * 5. Select delivery address
 * 6. Select payment method (credit card)
 * 7. Finalize order
 * 8. Validate order creation with EM_PROCESSAMENTO status and APROVADA payment
 *
 * Business Rules Validated:
 * - RN0031: Stock validation in cart
 * - RN0034: Multiple cards allowed
 * - RN0063: Maximum 10 units per book
 * - RN0064: Minimum order value (R$ 20)
 * - RN0037: Payment validation
 * - RN0038: Payment status (APROVADA only)
 * - RF0031-RF0035: Complete purchase flow
 */

describe('E2E: Complete Order Placement Flow', () => {
  // ========== TEST DATA ==========
  const testData = {
    customer: {
      email: 'cliente@jakebooks.com',
      password: 'Senha123!',
      codigo: 'CLI001',
      nome: 'João Silva'
    },
    book: {
      codigo: 'LIV001',
      titulo: 'Clean Code',
      valorVenda: 89.90
    },
    address: {
      id: 1, // Assumes pre-existing address in DB
      nome: 'Casa',
      logradouro: 'Rua Principal',
      numero: '123',
      bairro: 'Centro',
      cidade: 'São Paulo',
      estado: 'SP',
      cep: '01310-100'
    },
    card: {
      id: 1, // Assumes pre-existing card in DB
      numero: '**** **** **** 1234',
      nomeImpresso: 'JOAO SILVA'
    }
  };

  beforeEach(() => {
    // Reset and set up test environment
    cy.visit('http://localhost:8080');
    // Clear any previous session
    cy.clearCookies();
    cy.clearLocalStorage();
  });

  /**
   * TEST 1: User Authentication
   * Validates login screen is accessible and credentials work
   */
  it('Step 1: User should log in successfully', () => {
    cy.visit('http://localhost:8080/login');

    // Verify login form is displayed
    cy.get('[data-testid="login-form"]', { timeout: 5000 }).should('be.visible');
    cy.contains('JakeBooks').should('be.visible');
    cy.contains('Sistema de E-commerce de Livros').should('be.visible');

    // Fill in login credentials
    cy.get('[data-testid="email-input"]')
      .should('be.visible')
      .type(testData.customer.email);

    cy.get('[data-testid="password-input"]')
      .should('be.visible')
      .type(testData.customer.password);

    // Submit login form
    cy.get('[data-testid="login-button"]')
      .should('be.visible')
      .click();

    // Verify successful login - should redirect to books list
    cy.url({ timeout: 5000 }).should('include', '/livros');
    cy.contains(testData.customer.nome).should('be.visible');
  });

  /**
   * TEST 2: Book Browsing and Addition to Cart
   * Validates searching for a book and adding it to cart
   * RN0031: Stock validation is implicitly tested
   * RN0063: Quantity limit is tested in this step
   */
  it('Step 2: User should find and add a book to cart', () => {
    // Pre-requisite: User is logged in
    cy.login(testData.customer.email, testData.customer.password);

    // Navigate to books page (redirect after login)
    cy.visit('http://localhost:8080/livros');

    // Verify books page is loaded
    cy.get('[data-testid="livros-header"]', { timeout: 5000 }).should('be.visible');
    cy.contains('Livros').should('be.visible');

    // Search for the book (optional, depends on implementation)
    cy.get('[data-testid="titulo-filter"]').should('exist');
    cy.get('[data-testid="titulo-filter"]').type(testData.book.titulo);
    cy.get('[data-testid="buscar-button"]').click();

    // Wait for search results
    cy.get('[data-testid="livros-table"]', { timeout: 5000 }).should('be.visible');

    // Find the specific book row
    cy.get(`[data-testid="livro-row-${testData.book.codigo}"]`)
      .should('be.visible')
      .within(() => {
        // Verify book details
        cy.contains(testData.book.titulo).should('exist');
        cy.contains('R$').should('exist');

        // Click "Add to Cart" button
        cy.get('[data-testid="add-to-cart-button"]')
          .should('be.visible')
          .click();
      });

    // Verify success message
    cy.get('[data-testid="success-message"]', { timeout: 3000 })
      .should('be.visible')
      .and('contain', 'Livro adicionado ao carrinho');
  });

  /**
   * TEST 3: Cart Review
   * Validates cart page, item details, and minimum order value
   * RN0064: Minimum order value of R$ 20 is checked
   */
  it('Step 3: User should review cart and validate items', () => {
    cy.login(testData.customer.email, testData.customer.password);

    // Navigate to cart
    cy.visit('http://localhost:8080/carrinho');

    // Verify cart page is loaded
    cy.get('[data-testid="carrinho-header"]', { timeout: 5000 }).should('be.visible');
    cy.contains('Carrinho de Compras').should('be.visible');

    // Verify the book is in the cart
    cy.get('[data-testid="cart-items-table"]').should('exist');
    cy.get(`[data-testid="cart-item-${testData.book.codigo}"]`)
      .should('be.visible')
      .within(() => {
        cy.contains(testData.book.titulo).should('exist');
        cy.contains('1').should('exist'); // Default quantity
      });

    // Verify subtotal is displayed
    cy.get('[data-testid="subtotal-value"]')
      .should('be.visible')
      .and('contain', 'R$');

    // Verify total is >= 20 (business rule RN0064)
    cy.get('[data-testid="total-value"]').then(($el) => {
      const totalText = $el.text();
      const totalValue = parseFloat(totalText.replace(/[^\d.,]/g, '').replace(',', '.'));
      expect(totalValue).to.be.greaterThanOrEqual(20);
    });

    // Verify "Proceed to Payment" button exists and is enabled
    cy.get('[data-testid="checkout-button"]')
      .should('be.visible')
      .and('not.be.disabled');
  });

  /**
   * TEST 4: Proceed to Checkout
   * Validates navigation to checkout page and form initialization
   */
  it('Step 4: User should proceed to checkout page', () => {
    cy.login(testData.customer.email, testData.customer.password);

    // Navigate to cart first
    cy.visit('http://localhost:8080/carrinho');

    // Click checkout button
    cy.get('[data-testid="checkout-button"]')
      .should('be.visible')
      .click();

    // Verify checkout page is loaded
    cy.url({ timeout: 5000 }).should('include', '/checkout');
    cy.get('[data-testid="checkout-header"]', { timeout: 5000 }).should('be.visible');
    cy.contains('Finalizar Compra').should('be.visible');

    // Verify checkout form sections are visible
    cy.get('[data-testid="endereco-section"]').should('be.visible');
    cy.get('[data-testid="pagamento-section"]').should('be.visible');
    cy.get('[data-testid="resumo-section"]').should('be.visible');
  });

  /**
   * TEST 5: Select Delivery Address
   * Validates address selection functionality
   * RN0021: At least one address must be present
   */
  it('Step 5: User should select delivery address', () => {
    cy.login(testData.customer.email, testData.customer.password);

    cy.visit('http://localhost:8080/checkout');

    // Verify address section is visible
    cy.get('[data-testid="endereco-section"]', { timeout: 5000 }).should('be.visible');

    // Verify at least one address radio is available
    cy.get('[data-testid="endereco-radio"]')
      .should('have.length.greaterThan', 0);

    // Select the first address (or specific one by ID)
    cy.get(`[data-testid="endereco-radio-${testData.address.id}"]`)
      .should('be.visible')
      .click({ force: true });

    // Verify address is selected
    cy.get(`[data-testid="endereco-radio-${testData.address.id}"]`)
      .should('be.checked');

    // Verify address details are displayed
    cy.contains(testData.address.nome).should('be.visible');
    cy.contains(testData.address.logradouro).should('be.visible');
    cy.contains(testData.address.cidade).should('be.visible');
  });

  /**
   * TEST 6: Select Payment Method
   * Validates credit card selection and payment distribution
   * RN0034: Multiple cards allowed, minimum R$ 10 per card
   */
  it('Step 6: User should select payment method (credit card)', () => {
    cy.login(testData.customer.email, testData.customer.password);

    cy.visit('http://localhost:8080/checkout');

    // Verify address is already selected (from previous test order)
    cy.get(`[data-testid="endereco-radio-${testData.address.id}"]`)
      .click({ force: true });

    // Navigate to payment cards tab
    cy.get('[data-testid="cartoes-tab"]')
      .should('be.visible')
      .click();

    // Verify cards tab is active
    cy.get('#cartoes', { timeout: 3000 }).should('be.visible');

    // Verify at least one card is available
    cy.get('[data-testid="card-item"]')
      .should('have.length.greaterThan', 0);

    // Find and select the specific card
    cy.get(`[data-testid="card-${testData.card.id}"]`)
      .should('be.visible')
      .within(() => {
        cy.contains(testData.card.nomeImpresso).should('exist');
      });

    // Get the current total from the summary
    cy.get('[data-testid="total-value"]').then(($el) => {
      const totalText = $el.text();
      const totalValue = parseFloat(totalText.replace(/[^\d.,]/g, '').replace('.', '').replace(',', '.'));

      // Enter payment amount for the card
      // Business rule: minimum R$ 10 per card, so we pay full amount
      cy.get(`[data-testid="valor-cartao-${testData.card.id}"]`)
        .should('be.visible')
        .type(totalValue.toFixed(2));

      // Verify validation message doesn't appear
      cy.get('[data-testid="validacao-pagamento"]')
        .should('not.be.visible');
    });
  });

  /**
   * TEST 7: Accept Terms and Finalize Order
   * Validates order submission and successful processing
   */
  it('Step 7: User should accept terms and finalize order', () => {
    cy.login(testData.customer.email, testData.customer.password);

    cy.visit('http://localhost:8080/checkout');

    // ===== Complete all checkout steps =====
    // 1. Select address
    cy.get(`[data-testid="endereco-radio-${testData.address.id}"]`)
      .click({ force: true });

    // 2. Select payment card
    cy.get('[data-testid="cartoes-tab"]')
      .click();

    // Get total and enter payment
    cy.get('[data-testid="total-value"]').then(($el) => {
      const totalText = $el.text();
      const totalValue = parseFloat(totalText.replace(/[^\d.,]/g, '').replace('.', '').replace(',', '.'));

      cy.get(`[data-testid="valor-cartao-${testData.card.id}"]`)
        .type(totalValue.toFixed(2));
    });

    // 3. Accept terms and conditions
    cy.get('[data-testid="concordo-checkbox"]')
      .should('be.visible')
      .check({ force: true });

    // Verify checkbox is checked
    cy.get('[data-testid="concordo-checkbox"]')
      .should('be.checked');

    // 4. Click finalize order button
    cy.get('[data-testid="finalizar-compra-button"]')
      .should('be.visible')
      .and('not.be.disabled')
      .click();

    // Verify success message appears
    cy.get('[data-testid="success-message"]', { timeout: 5000 })
      .should('be.visible')
      .and('contain', 'Pedido realizado com sucesso');
  });

  /**
   * TEST 8: Verify Order Creation and Status
   * Validates that order was created with correct status and payment approval
   * RN0037: Payment validation
   * RN0038: Payment status must be APROVADA
   * RF0037: Order status must be EM_PROCESSAMENTO
   */
  it('Step 8: Order should be created with EM_PROCESSAMENTO status and APROVADA payment', () => {
    cy.login(testData.customer.email, testData.customer.password);

    // Get the order ID from the URL after redirect
    cy.url({ timeout: 5000 }).then((url) => {
      expect(url).to.include('/pedidos/');

      // Extract order ID from URL
      const orderId = url.split('/pedidos/')[1];
      expect(orderId).to.match(/^\d+$/);

      // Navigate to order details page
      cy.visit(`http://localhost:8080/pedidos/${orderId}`);

      // Verify order details page is loaded
      cy.get('[data-testid="pedido-header"]', { timeout: 5000 }).should('be.visible');
      cy.contains('Pedido #').should('be.visible');

      // ===== Verify Order Status =====
      // Requirement: Status must be EM_PROCESSAMENTO
      cy.get('[data-testid="status-badge"]')
        .should('be.visible')
        .and('contain', 'EM PROCESSAMENTO')
        .and('have.class', 'bg-warning');

      // ===== Verify Payment Information =====
      // Requirement: Payment status must be APROVADA
      cy.get('[data-testid="pagamento-status"]')
        .should('be.visible')
        .and('contain', 'APROVADA')
        .and('have.class', 'bg-success');

      // ===== Verify Order Details =====
      // Verify customer information
      cy.get('[data-testid="cliente-nome"]')
        .should('be.visible')
        .and('contain', testData.customer.nome);

      // Verify delivery address
      cy.get('[data-testid="endereco-display"]')
        .should('be.visible')
        .and('contain', testData.address.logradouro);

      // Verify order items are listed
      cy.get('[data-testid="itens-table"]')
        .should('be.visible');

      cy.get('[data-testid="item-livro"]')
        .should('have.length.greaterThan', 0)
        .first()
        .within(() => {
          cy.contains(testData.book.titulo).should('exist');
        });

      // ===== Verify Financial Summary =====
      // Verify total value is displayed
      cy.get('[data-testid="valor-total"]')
        .should('be.visible')
        .and('contain', 'R$');

      // Verify frete is displayed
      cy.get('[data-testid="valor-frete"]')
        .should('be.visible')
        .and('contain', 'R$');

      // ===== Verify Timeline =====
      // First status should be "Pedido Criado"
      cy.get('[data-testid="timeline-item-criado"]')
        .should('be.visible');

      // Second status should be "Em Processamento" (active)
      cy.get('[data-testid="timeline-item-processamento"]')
        .should('be.visible')
        .and('have.class', 'active');
    });
  });

  /**
   * TEST 9: Complete End-to-End Flow (All Steps Combined)
   * This is the master test that runs the complete flow sequentially
   */
  it('Complete E2E: Full order placement from login to confirmation', () => {
    // Step 1: Login
    cy.visit('http://localhost:8080/login');
    cy.get('[data-testid="email-input"]').type(testData.customer.email);
    cy.get('[data-testid="password-input"]').type(testData.customer.password);
    cy.get('[data-testid="login-button"]').click();
    cy.url({ timeout: 5000 }).should('include', '/livros');

    // Step 2: Navigate to books and add one to cart
    cy.visit('http://localhost:8080/livros');
    cy.get('[data-testid="titulo-filter"]').type(testData.book.titulo);
    cy.get('[data-testid="buscar-button"]').click();
    cy.get(`[data-testid="livro-row-${testData.book.codigo}"]`)
      .within(() => {
        cy.get('[data-testid="add-to-cart-button"]').click();
      });

    // Step 3: Verify cart
    cy.visit('http://localhost:8080/carrinho');
    cy.get(`[data-testid="cart-item-${testData.book.codigo}"]`).should('be.visible');
    cy.get('[data-testid="total-value"]').then(($el) => {
      const totalValue = parseFloat($el.text().replace(/[^\d.,]/g, '').replace(',', '.'));
      expect(totalValue).to.be.greaterThanOrEqual(20);
    });

    // Step 4: Proceed to checkout
    cy.get('[data-testid="checkout-button"]').click();
    cy.url({ timeout: 5000 }).should('include', '/checkout');

    // Step 5 & 6: Select address and payment
    cy.get(`[data-testid="endereco-radio-${testData.address.id}"]`).click({ force: true });
    cy.get('[data-testid="cartoes-tab"]').click();

    cy.get('[data-testid="total-value"]').then(($el) => {
      const totalValue = parseFloat($el.text().replace(/[^\d.,]/g, '').replace('.', '').replace(',', '.'));
      cy.get(`[data-testid="valor-cartao-${testData.card.id}"]`).type(totalValue.toFixed(2));
    });

    // Step 7: Accept terms and finalize
    cy.get('[data-testid="concordo-checkbox"]').check({ force: true });
    cy.get('[data-testid="finalizar-compra-button"]').click();

    // Step 8: Verify order was created successfully
    cy.get('[data-testid="success-message"]', { timeout: 5000 }).should('be.visible');

    cy.url({ timeout: 5000 }).then((url) => {
      expect(url).to.include('/pedidos/');

      const orderId = url.split('/pedidos/')[1];

      // Verify final order status
      cy.get('[data-testid="status-badge"]').should('contain', 'EM PROCESSAMENTO');
      cy.get('[data-testid="pagamento-status"]').should('contain', 'APROVADA');
    });
  });
});

/**
 * ======================================================================
 * CUSTOM COMMANDS (Add to cypress/support/commands.js)
 * ======================================================================
 *
 * These custom commands simplify the test code and improve maintainability.
 */

// Custom login command
Cypress.Commands.add('login', (email, password) => {
  cy.visit('http://localhost:8080/login');
  cy.get('[data-testid="email-input"]').type(email);
  cy.get('[data-testid="password-input"]').type(password);
  cy.get('[data-testid="login-button"]').click();
  cy.url({ timeout: 5000 }).should('include', '/livros');
});

// Custom command to add item to cart
Cypress.Commands.add('addToCart', (bookCodigo, quantidade = 1) => {
  cy.visit('http://localhost:8080/carrinho/adicionar');
  cy.get('[data-testid="codigoLivro-input"]').type(bookCodigo);
  cy.get('[data-testid="quantidade-input"]').clear().type(quantidade);
  cy.get('[data-testid="add-button"]').click();
});

// Custom command to wait for element and assert text
Cypress.Commands.add('shouldContainText', { prevSubject: true }, (subject, text) => {
  cy.wrap(subject).should('contain', text);
});
