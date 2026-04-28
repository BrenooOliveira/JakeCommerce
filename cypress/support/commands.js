/**
 * ======================================================================
 * CYPRESS CUSTOM COMMANDS
 * ======================================================================
 *
 * Reusable command definitions for common test operations.
 * These commands can be used across all test files with cy.command_name()
 */

/**
 * Custom login command
 * Usage: cy.login('email@example.com', 'password123')
 */
Cypress.Commands.add('login', (email, password) => {
  cy.visit('http://localhost:8080/login', { timeout: 10000 });

  // Wait for login form to be visible
  cy.get('[data-testid="login-form"]', { timeout: 5000 }).should('be.visible');

  // Fill credentials
  cy.get('[data-testid="email-input"]')
    .should('be.visible')
    .type(email, { delay: 50 });

  cy.get('[data-testid="password-input"]')
    .should('be.visible')
    .type(password, { delay: 50 });

  // Submit form
  cy.get('[data-testid="login-button"]')
    .should('be.visible')
    .click();

  // Verify redirect to books page
  cy.url({ timeout: 10000 }).should('include', '/livros');

  // Wait for books page to load
  cy.get('[data-testid="livros-header"]', { timeout: 5000 }).should('be.visible');
});

/**
 * Custom command to add a book to cart
 * Usage: cy.addBookToCart('LIV001', 1)
 */
Cypress.Commands.add('addBookToCart', (codigoLivro, quantidade = 1) => {
  // Navigate to books page if not already there
  cy.visit('http://localhost:8080/livros');

  // Find and click the add to cart button for the specific book
  cy.get(`[data-testid="livro-row-${codigoLivro}"]`)
    .should('be.visible')
    .within(() => {
      cy.get('[data-testid="add-to-cart-button"]')
        .should('be.visible')
        .click();
    });

  // Wait for success message
  cy.get('[data-testid="success-message"]', { timeout: 5000 })
    .should('be.visible')
    .and('contain', 'adicionado ao carrinho');
});

/**
 * Custom command to navigate to cart and verify items
 * Usage: cy.goToCart()
 */
Cypress.Commands.add('goToCart', () => {
  cy.visit('http://localhost:8080/carrinho', { timeout: 10000 });

  // Wait for cart page to load
  cy.get('[data-testid="carrinho-header"]', { timeout: 5000 }).should('be.visible');

  // Ensure cart content is visible
  cy.get('[data-testid="cart-items-table"]', { timeout: 3000 }).should('exist');
});

/**
 * Custom command to proceed to checkout
 * Usage: cy.proceedToCheckout()
 */
Cypress.Commands.add('proceedToCheckout', () => {
  // Verify we're on cart page
  cy.url().should('include', '/carrinho');

  // Click checkout button
  cy.get('[data-testid="checkout-button"]')
    .should('be.visible')
    .and('not.be.disabled')
    .click();

  // Wait for checkout page
  cy.url({ timeout: 10000 }).should('include', '/checkout');
  cy.get('[data-testid="checkout-header"]', { timeout: 5000 }).should('be.visible');
});

/**
 * Custom command to select delivery address
 * Usage: cy.selectAddress(1)
 */
Cypress.Commands.add('selectAddress', (addressId) => {
  cy.get(`[data-testid="endereco-radio-${addressId}"]`)
    .should('be.visible')
    .click({ force: true });

  // Verify selection
  cy.get(`[data-testid="endereco-radio-${addressId}"]`)
    .should('be.checked');
});

/**
 * Custom command to select payment card
 * Usage: cy.selectCard(1, 99.90)
 */
Cypress.Commands.add('selectCard', (cardId, valor) => {
  // Switch to cards tab
  cy.get('[data-testid="cartoes-tab"]')
    .should('be.visible')
    .click();

  // Wait for tab content
  cy.get('#cartoes', { timeout: 3000 }).should('be.visible');

  // Enter payment amount
  cy.get(`[data-testid="valor-cartao-${cardId}"]`)
    .should('be.visible')
    .clear()
    .type(valor.toFixed(2), { delay: 30 });
});

/**
 * Custom command to finalize order
 * Usage: cy.finalizeOrder()
 */
Cypress.Commands.add('finalizeOrder', () => {
  // Accept terms and conditions
  cy.get('[data-testid="concordo-checkbox"]')
    .should('be.visible')
    .check({ force: true });

  // Click finalize button
  cy.get('[data-testid="finalizar-compra-button"]')
    .should('be.visible')
    .and('not.be.disabled')
    .click();

  // Wait for success message
  cy.get('[data-testid="success-message"]', { timeout: 7000 })
    .should('be.visible')
    .and('contain', 'sucesso');
});

/**
 * Custom command to get order ID from URL
 * Usage: cy.getOrderId().then(id => { ... })
 */
Cypress.Commands.add('getOrderId', () => {
  return cy.url().then((url) => {
    const match = url.match(/\/pedidos\/(\d+)/);
    if (!match) {
      throw new Error('Order ID not found in URL: ' + url);
    }
    return match[1];
  });
});

/**
 * Custom command to verify order details
 * Usage: cy.verifyOrderDetails({ status: 'EM_PROCESSAMENTO', paymentStatus: 'APROVADA' })
 */
Cypress.Commands.add('verifyOrderDetails', (expectedValues) => {
  // Verify order status
  if (expectedValues.status) {
    cy.get('[data-testid="status-badge"]')
      .should('be.visible')
      .and('contain', expectedValues.status.replace('_', ' '));
  }

  // Verify payment status
  if (expectedValues.paymentStatus) {
    cy.get('[data-testid="pagamento-status"]')
      .should('be.visible')
      .and('contain', expectedValues.paymentStatus);
  }

  // Verify customer name
  if (expectedValues.customerName) {
    cy.get('[data-testid="cliente-nome"]')
      .should('be.visible')
      .and('contain', expectedValues.customerName);
  }

  // Verify at least one item exists
  if (expectedValues.hasItems) {
    cy.get('[data-testid="item-livro"]')
      .should('have.length.greaterThan', 0);
  }
});

/**
 * Custom command to wait for order redirect
 * Usage: cy.waitForOrderConfirmation()
 */
Cypress.Commands.add('waitForOrderConfirmation', () => {
  cy.url({ timeout: 10000 }).should('include', '/pedidos/');
  cy.get('[data-testid="pedido-header"]', { timeout: 5000 }).should('be.visible');
});
