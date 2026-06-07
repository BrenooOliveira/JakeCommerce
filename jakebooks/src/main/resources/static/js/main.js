// =============================================
// SCRIPTS CUSTOMIZADOS - LIVRARIA
// =============================================

document.addEventListener('DOMContentLoaded', function() {
    // Auto-fechar alertas após 5 segundos
    const alerts = document.querySelectorAll('.alert:not(.coupon-alert):not(.alert-persist)');
    alerts.forEach(alert => {
        const bsAlert = new bootstrap.Alert(alert);
        setTimeout(() => {
            bsAlert.close();
        }, 5000);
    });

    // Validação de formulários Bootstrap
    initializeFormValidation();

    // Inicializar tooltips e popovers
    initializeBootstrapComponents();
});

// =============================================
// VALIDAÇÃO DE FORMULÁRIOS
// =============================================

function initializeFormValidation() {
    const forms = document.querySelectorAll('form');
    
    Array.from(forms).forEach(form => {
        form.addEventListener('submit', function(event) {
            if (!form.checkValidity()) {
                event.preventDefault();
                event.stopPropagation();
            }
            form.classList.add('was-validated');
        }, false);
    });
}

// =============================================
// COMPONENTES BOOTSTRAP
// =============================================

function initializeBootstrapComponents() {
    // Tooltips
    const tooltipTriggerList = [].slice.call(
        document.querySelectorAll('[data-bs-toggle="tooltip"]')
    );
    tooltipTriggerList.map(tooltipTriggerEl => 
        new bootstrap.Tooltip(tooltipTriggerEl)
    );

    // Popovers
    const popoverTriggerList = [].slice.call(
        document.querySelectorAll('[data-bs-toggle="popover"]')
    );
    popoverTriggerList.map(popoverTriggerEl => 
        new bootstrap.Popover(popoverTriggerEl)
    );
}

// =============================================
// UTILITÁRIOS
// =============================================

/**
 * Mostra um toast/notificação
 * @param {string} message - Mensagem a exibir
 * @param {string} type - Tipo: success, error, warning, info
 */
function showNotification(message, type = 'info') {
    const alertDiv = document.createElement('div');
    alertDiv.className = `alert alert-${type} alert-dismissible fade show`;
    alertDiv.setAttribute('role', 'alert');
    alertDiv.innerHTML = `
        ${message}
        <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
    `;
    
    const container = document.querySelector('.container-fluid main');
    if (container) {
        container.insertBefore(alertDiv, container.firstChild);
        
        const bsAlert = new bootstrap.Alert(alertDiv);
        setTimeout(() => {
            bsAlert.close();
        }, 5000);
    }
}

/**
 * Confirma uma ação com diálogo nativo
 * @param {string} message - Mensagem de confirmação
 * @returns {boolean} true se confirmado, false caso contrário
 */
function confirmAction(message) {
    return confirm(message || 'Tem certeza que deseja continuar?');
}

/**
 * Desabilita um botão e mostra loadingspinner durante envio de formulário
 * @param {HTMLElement} button - Botão a desabilitar
 */
function disableSubmitButton(button) {
    button.disabled = true;
    const originalText = button.innerHTML;
    button.innerHTML = `
        <span class="spinner-border spinner-border-sm me-2" role="status" aria-hidden="true"></span>
        Enviando...
    `;
    button.dataset.originalText = originalText;
}

/**
 * Reabilita um botão após envio
 * @param {HTMLElement} button - Botão a reabilitar
 */
function enableSubmitButton(button) {
    button.disabled = false;
    button.innerHTML = button.dataset.originalText || 'Enviar';
}

// =============================================
// MÁSCARA DE ENTRADA (OPCIONAL)
// =============================================

/**
 * Aplica máscara de CPF ao campo
 * @param {HTMLElement} field - Campo input
 */
function maskCPF(field) {
    field.addEventListener('input', function(e) {
        let value = e.target.value.replace(/\D/g, '');
        if (value.length > 11) value = value.slice(0, 11);
        
        let formatted = value
            .replace(/(\d{3})(\d)/, '$1.$2')
            .replace(/(\d{3})(\d)/, '$1.$2')
            .replace(/(\d{3})(\d{2})$/, '$1-$2');
        
        e.target.value = formatted;
    });
}

/**
 * Aplica máscara de Telefone ao campo
 * @param {HTMLElement} field - Campo input
 */
function maskPhone(field) {
    field.addEventListener('input', function(e) {
        let value = e.target.value.replace(/\D/g, '');
        if (value.length > 11) value = value.slice(0, 11);
        
        let formatted = value
            .replace(/(\d{2})(\d)/, '($1) $2')
            .replace(/(\d{4})(\d)/, '$1-$2');
        
        e.target.value = formatted;
    });
}

/**
 * Aplica máscara de CEP ao campo
 * @param {HTMLElement} field - Campo input
 */
function maskCEP(field) {
    field.addEventListener('input', function(e) {
        let value = e.target.value.replace(/\D/g, '');
        if (value.length > 8) value = value.slice(0, 8);
        
        let formatted = value.replace(/(\d{5})(\d)/, '$1-$2');
        e.target.value = formatted;
    });
}

// =============================================
// INICIALIZAÇÃO DE MÁSCARAS
// =============================================

document.addEventListener('DOMContentLoaded', function() {
    // Aplicar máscaras a campos específicos
    const cpfFields = document.querySelectorAll('[data-mask="cpf"]');
    cpfFields.forEach(field => maskCPF(field));
    
    const phoneFields = document.querySelectorAll('[data-mask="phone"]');
    phoneFields.forEach(field => maskPhone(field));
    
    const cepFields = document.querySelectorAll('[data-mask="cep"]');
    cepFields.forEach(field => maskCEP(field));
});
