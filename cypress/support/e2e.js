import './commands';

// Set up global configurations for all tests
beforeEach(() => {
  // Increase timeout for slower operations
  cy.config('defaultCommandTimeout', 10000);
  cy.config('requestTimeout', 10000);
});

// Global error handling
Cypress.on('uncaught:exception', (err, runnable) => {
  // Ignore specific errors that don't affect test
  if (
    err.message.includes('fetch') ||
    err.message.includes('Network request failed')
  ) {
    return false;
  }
});
