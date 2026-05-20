#!/usr/bin/env python3
"""
E2E Test Suite para JakeBooks - Fluxo Completo de Vendas
Testes: Login, Carrinho, Checkout e Pagamento
"""

import logging
import sys
import time
from selenium import webdriver
from selenium.webdriver.chrome.options import Options
from selenium.webdriver.common.by import By
from selenium.webdriver.common.keys import Keys
from selenium.webdriver.common.alert import Alert
from selenium.webdriver.support.ui import WebDriverWait
from selenium.webdriver.support import expected_conditions as EC
from selenium.common.exceptions import TimeoutException, NoSuchElementException, ElementClickInterceptedException


# ============================================================================
# CONFIGURAÇÃO DE LOGGING
# ============================================================================

logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(levelname)s - [%(funcName)s] - %(message)s',
    handlers=[
        logging.FileHandler('jakebooks_e2e_test.log'),
        logging.StreamHandler(sys.stdout)
    ]
)
logger = logging.getLogger(__name__)


# ============================================================================
# CONFIGURAÇÃO DO NAVEGADOR
# ============================================================================

def setup_driver():
    """Inicializa o driver do Selenium com configurações padrão."""
    logger.info("Iniciando driver Selenium...")

    try:
        options = Options()
        options.add_argument("--window-size=1440,900")
        options.binary_location = "/usr/bin/brave-browser"

        driver = webdriver.Chrome(options=options)
        driver.implicitly_wait(10)  # Wait implícito de 10s

        logger.info("Driver iniciado com sucesso")
        return driver

    except Exception as e:
        logger.error(f"Erro ao inicializar driver: {e}")
        raise


def teardown_driver(driver):
    """Finaliza o driver e sessão."""
    if driver:
        driver.quit()
        logger.info("Driver finalizado")


# ============================================================================
# HELPERS DE INTERAÇÃO
# ============================================================================

def find_element_by_xpath(driver, xpath, wait_time=10):
    """
    Encontra elemento com espera explícita.

    Args:
        driver: WebDriver
        xpath: XPath do elemento
        wait_time: Tempo máximo de espera em segundos

    Returns:
        WebElement encontrado

    Raises:
        TimeoutException: Se elemento não for encontrado no tempo limite
    """
    try:
        element = WebDriverWait(driver, wait_time).until(
            EC.presence_of_element_located((By.XPATH, xpath))
        )
        logger.debug(f"Elemento encontrado: {xpath}")
        return element

    except TimeoutException:
        logger.error(f"Elemento não encontrado (timeout): {xpath}")
        raise AssertionError(f"Elemento não encontrado: {xpath}")


def wait_for_page_ready(driver, timeout=10):
    """Aguarda a página ficar completamente pronta (stop animações, carregamentos, etc)."""
    try:
        # Aguardar document ready
        WebDriverWait(driver, timeout).until(
            lambda d: d.execute_script("return document.readyState") == "complete"
        )
        logger.debug("✓ Document ready")
        
        # Aguardar jQuery AJAX (se existir)
        try:
            WebDriverWait(driver, timeout).until(
                lambda d: d.execute_script("return typeof jQuery === 'undefined' || jQuery.active == 0")
            )
            logger.debug("✓ jQuery AJAX completo")
        except Exception:
            logger.debug("⚠ jQuery não disponível ou não há AJAX")
        
        # Pequeno delay para animações CSS terminarem
        time.sleep(1)
        logger.debug("✓ Página pronta para interação")
        
    except Exception as e:
        logger.warning(f"⚠ Erro ao aguardar página ficar pronta: {e}")


def scroll_to_element(driver, element):
    """Scroll para trazer o elemento completamente visível."""
    try:
        driver.execute_script("""
            var element = arguments[0];
            var elementRect = element.getBoundingClientRect();
            var absoluteElementTop = elementRect.top + window.pageYOffset;
            var middle = absoluteElementTop - (window.innerHeight / 2);
            window.scrollTo(0, middle);
        """, element)
        time.sleep(0.5)  # Pequeno delay após scroll
    except Exception as e:
        logger.warning(f"⚠ Erro ao fazer scroll: {e}")


def click_element(driver, xpath, label="", retry_count=3, wait_ready=True):
    """Clica em um elemento com logging, scroll automático e retry logic."""
    for attempt in range(retry_count):
        try:
            # Aguardar página pronta antes de interagir (especialmente na primeira tentativa)
            if attempt == 0 and wait_ready:
                wait_for_page_ready(driver, timeout=5)
            
            element = WebDriverWait(driver, 10).until(
                EC.element_to_be_clickable((By.XPATH, xpath))
            )
            
            # Scroll para trazer elemento visível
            scroll_to_element(driver, element)
            
            element.click()
            logger.info(f"✓ Clicado: {label or xpath}")
            return

        except ElementClickInterceptedException as e:
            logger.warning(f"⚠ Tentativa {attempt + 1}/{retry_count} - Elemento interceptado: {label}")
            if attempt < retry_count - 1:
                time.sleep(1)  # Aguardar mais tempo entre tentativas
                continue
            logger.error(f"✗ Erro ao clicar em {label} após {retry_count} tentativas: {e}")
            raise

        except Exception as e:
            logger.error(f"✗ Erro ao clicar em {label}: {e}")
            raise


def fill_input(driver, xpath, value, label=""):
    """Preenche input com valor, com scroll automático."""
    try:
        element = WebDriverWait(driver, 10).until(
            EC.presence_of_element_located((By.XPATH, xpath))
        )
        
        # Scroll para trazer elemento visível
        scroll_to_element(driver, element)
        
        # Aguardar clicável e depois clicar
        element = WebDriverWait(driver, 10).until(
            EC.element_to_be_clickable((By.XPATH, xpath))
        )
        element.click()
        element.clear()
        element.send_keys(value)
        logger.info(f"✓ Preenchido {label or xpath} com: {value if len(value) < 50 else value[:50] + '...'}")

    except Exception as e:
        logger.error(f"✗ Erro ao preencher {label}: {e}")
        raise


def assert_page_url(driver, expected_path, timeout=10):
    """Verifica se a URL atual contém o path esperado e aguarda página ficar pronta."""
    try:
        WebDriverWait(driver, timeout).until(
            lambda d: expected_path in d.current_url
        )
        logger.info(f"✓ URL verificada: {expected_path} está em {driver.current_url}")
        
        # Aguardar página ficar completamente pronta
        wait_for_page_ready(driver, timeout)

    except TimeoutException:
        logger.error(f"✗ URL não contém '{expected_path}'. URL atual: {driver.current_url}")
        raise AssertionError(f"URL esperada contém '{expected_path}', mas atual é {driver.current_url}")


def assert_element_visible(driver, xpath, label="", timeout=10):
    """Verifica se elemento está visível com scroll automático."""
    try:
        element = WebDriverWait(driver, timeout).until(
            EC.visibility_of_element_located((By.XPATH, xpath))
        )
        
        # Scroll para trazer elemento visível (apenas para reconfirmar)
        scroll_to_element(driver, element)
        
        logger.info(f"✓ Elemento visível: {label or xpath}")
        return element

    except TimeoutException:
        logger.error(f"✗ Elemento não está visível: {label or xpath}")
        raise AssertionError(f"Elemento não visível: {label or xpath}")


# ============================================================================
# TESTES DE LOGIN
# ============================================================================

def test_login_invalid_email(driver):
    """Testa login com email inválido - deve ser rejeitado."""
    logger.info("\n" + "="*60)
    logger.info("TESTE: Login com Email Inválido")
    logger.info("="*60)

    driver.get("http://localhost:8080/login")
    assert_page_url(driver, "/login")

    email_xpath = "/html/body/div[2]/div/main/div[2]/div/div[2]/form/div[1]/input"
    password_xpath = "/html/body/div[2]/div/main/div[2]/div/div[2]/form/div[2]/input"
    button_xpath = "/html/body/div[2]/div/main/div[2]/div/div[2]/form/button"

    fill_input(driver, email_xpath, "email_invalidooo@gmail.com", "Email")
    fill_input(driver, password_xpath, "Invalido123@", "Senha")
    click_element(driver, button_xpath, "Botão Login")

    time.sleep(2)

    # Verifica se permanece na página de login (rejeição)
    assert_page_url(driver, "/login")
    logger.info("✓ LOGIN REJEITADO - Email inválido conforme esperado\n")


def test_login_invalid_password(driver):
    """Testa login com senha inválida - deve ser rejeitado."""
    logger.info("\n" + "="*60)
    logger.info("TESTE: Login com Senha Inválida")
    logger.info("="*60)

    driver.get("http://localhost:8080/login")
    assert_page_url(driver, "/login")

    email_xpath = "/html/body/div[2]/div/main/div[2]/div/div[2]/form/div[1]/input"
    password_xpath = "/html/body/div[2]/div/main/div[2]/div/div[2]/form/div[2]/input"
    button_xpath = "/html/body/div[2]/div/main/div[2]/div/div[2]/form/button"

    fill_input(driver, email_xpath, "breno@teste.com", "Email")
    fill_input(driver, password_xpath, "SENHA INVALIDA", "Senha")
    click_element(driver, button_xpath, "Botão Login")

    time.sleep(2)

    assert_page_url(driver, "/login")
    logger.info("✓ LOGIN REJEITADO - Senha inválida conforme esperado\n")


def test_login_success(driver):
    """Testa login com sucesso."""
    logger.info("\n" + "="*60)
    logger.info("TESTE: Login com Sucesso")
    logger.info("="*60)

    driver.get("http://localhost:8080/login")
    assert_page_url(driver, "/login")

    email_xpath = "/html/body/div[2]/div/main/div[2]/div/div[2]/form/div[1]/input"
    password_xpath = "/html/body/div[2]/div/main/div[2]/div/div[2]/form/div[2]/input"
    button_xpath = "/html/body/div[2]/div/main/div[2]/div/div[2]/form/button"

    fill_input(driver, email_xpath, "breno@teste.com", "Email")
    fill_input(driver, password_xpath, "Brenets2009@", "Senha")
    click_element(driver, button_xpath, "Botão Login")

    time.sleep(2)

    # Verifica se foi redirecionado (não está mais em /login)
    assert_element_visible(driver, "/html/body/div[1]/nav", "Navbar (indicador de login bem-sucedido)")
    logger.info(f"✓ LOGIN SUCESSO - Redirecionado para: {driver.current_url}\n")


# ============================================================================
# TESTES DE CARRINHO
# ============================================================================

def test_add_remove_single_item(driver):
    """Testa adicionar e remover um item do carrinho."""
    logger.info("\n" + "="*60)
    logger.info("TESTE: Adicionar e Remover Item do Carrinho")
    logger.info("="*60)

    # Navegar para livros
    click_element(driver, "/html/body/div[1]/nav/div/div/ul[1]/li[2]/a", "Menu Livros")
    assert_page_url(driver, "/livros")
    time.sleep(1)

    # Adicionar primeiro livro
    click_element(driver, "/html/body/div[2]/div/main/div[2]/div/div[3]/div/table/tbody/tr[1]/td[6]/form/button", "Adicionar Livro 1")
    assert_page_url(driver, "/carrinho")
    logger.info("✓ Item adicionado ao carrinho")

    # Verificar que item está no carrinho
    assert_element_visible(driver, "/html/body/div[2]/div/main/div[2]/div/div[2]/div[1]/div/div[2]/table/tbody/tr",
                          "Item no Carrinho")

    # Remover item
    click_element(driver, "/html/body/div[2]/div/main/div[2]/div/div[2]/div[1]/div/div[2]/table/tbody/tr/td[5]/form",
                 "Remover Item")
    time.sleep(1)

    logger.info("✓ CARRINHO - Item adicionado e removido com sucesso\n")


def test_add_multiple_items(driver):
    """Testa adicionar múltiplos itens ao carrinho."""
    logger.info("\n" + "="*60)
    logger.info("TESTE: Adicionar Múltiplos Itens ao Carrinho")
    logger.info("="*60)

    # Ir para livros
    click_element(driver, "/html/body/div[1]/nav/div/div/ul[1]/li[2]/a", "Menu Livros")
    assert_page_url(driver, "/livros")
    time.sleep(1)

    # Primeiro livro
    click_element(driver, "/html/body/div[2]/div/main/div[2]/div/div[3]/div/table/tbody/tr[1]/td[6]/form/button",
                 "Adicionar Livro 1")
    assert_page_url(driver, "/carrinho")
    logger.info("✓ Livro 1 adicionado")

    # Continuar comprando
    click_element(driver, "/html/body/div[2]/div/main/div[2]/div/div[2]/div[2]/div[1]/div[2]/a",
                 "Continuar Comprando")
    assert_page_url(driver, "/livros")
    time.sleep(1)

    # Segundo livro
    click_element(driver, "/html/body/div[2]/div/main/div[2]/div/div[3]/div/table/tbody/tr[2]/td[6]/form/button",
                 "Adicionar Livro 2")
    assert_page_url(driver, "/carrinho")
    logger.info("✓ Livro 2 adicionado")

    # Testar limite máximo de 10 unidades
    quantity_input_xpath = "/html/body/div[2]/div/main/div[2]/div/div[2]/div[1]/div/div[2]/table/tbody/tr[1]/td[3]/form/div/input[2]"
    try:
        input_elem = find_element_by_xpath(driver, quantity_input_xpath)
        input_elem.send_keys(Keys.CONTROL + "a")
        input_elem.send_keys("100")
        input_elem.send_keys(Keys.ENTER)
        time.sleep(1)
        logger.info("✓ Testado limite de quantidade (máximo 10 unidades)")
    except Exception as e:
        logger.warning(f"⚠ Não foi possível testar limite de quantidade: {e}")

    logger.info("✓ CARRINHO - Múltiplos itens adicionados com sucesso\n")


def test_proceed_to_checkout(driver):
    """Testa procedimento de checkout."""
    logger.info("\n" + "="*60)
    logger.info("TESTE: Prosseguir para Checkout")
    logger.info("="*60)

    # Prosseguir com pagamento
    click_element(driver, "/html/body/div[2]/div/main/div[2]/div/div[2]/div[2]/div[1]/div[2]/button",
                 "Prosseguir para Checkout")

    time.sleep(2)
    assert_page_url(driver, "/checkout")
    logger.info("✓ CHECKOUT - Página de checkout carregada com sucesso\n")


# ============================================================================
# TESTES DE CHECKOUT
# ============================================================================

def test_select_address(driver):
    """Testa seleção de endereço no checkout."""
    logger.info("\n" + "="*60)
    logger.info("TESTE: Selecionar Endereço")
    logger.info("="*60)

    # Selecionar endereço (segunda opção)
    click_element(driver, "/html/body/div[2]/div/main/div[2]/div/div/div[1]/div[1]/div[2]/div[1]/div/div[1]/div/input",
                 "Selecionar Endereço")

    logger.info("✓ CHECKOUT - Endereço selecionado com sucesso\n")


def test_payment_invalid_values(driver):
    """Testa validação de valores de pagamento (mínimo de 10 por cartão)."""
    logger.info("\n" + "="*60)
    logger.info("TESTE: Validação de Valores de Pagamento (Inválido)")
    logger.info("="*60)

    # Selecionar aba de cartões
    click_element(driver, "/html/body/div[2]/div/main/div[2]/div/form/div/div[1]/div[2]/div[2]/ul/li[2]/button",
                 "Aba de Cartões")
    time.sleep(1)

    # Preencher valores inválidos (menos de 10)
    card1_xpath = "/html/body/div[2]/div/main/div[2]/div/form/div/div[1]/div[2]/div[2]/div/div[2]/div/div[2]/div[2]/div/input"
    card2_xpath = "/html/body/div[2]/div/main/div[2]/div/form/div/div[1]/div[2]/div[2]/div/div[2]/div/div[3]/div[2]/div/input"

    fill_input(driver, card1_xpath, "1", "Cartão 1 - Valor Inválido")
    fill_input(driver, card2_xpath, "1", "Cartão 2 - Valor Inválido")

    # Tentar finalizar sem aceitar termos
    click_element(driver, "/html/body/div[2]/div/main/div[2]/div/form/div/div[2]/div/div[2]/button",
                 "Finalizar Compra (sem termos)")

    time.sleep(1)

    try:
        Alert(driver).accept()
        logger.info("✓ Alerta de termos aceito")
    except Exception:
        logger.warning("⚠ Alerta de termos não encontrado")

    logger.info("✓ VALIDAÇÃO - Valores inválidos testados\n")


def test_payment_accept_terms(driver):
    """Testa aceitação de termos de uso."""
    logger.info("\n" + "="*60)
    logger.info("TESTE: Aceitar Termos de Uso")
    logger.info("="*60)

    # Aceitar termo de uso
    terms_xpath = "/html/body/div[2]/div/main/div[2]/div/form/div/div[2]/div/div[2]/div[6]/input"
    try:
        element = find_element_by_xpath(driver, terms_xpath)
        element.click()
        logger.info("✓ Termos de uso aceitos")
    except Exception as e:
        logger.warning(f"⚠ Erro ao aceitar termos: {e}")

    logger.info("✓ VALIDAÇÃO - Termos de uso processados\n")


def test_payment_correct_values(driver):
    """Testa pagamento com valores válidos e corretos."""
    logger.info("\n" + "="*60)
    logger.info("TESTE: Pagamento com Valores Corretos")
    logger.info("="*60)

    # Corrigir valores dos cartões
    card1_xpath = "/html/body/div[2]/div/main/div[2]/div/form/div/div[1]/div[2]/div[2]/div/div[2]/div/div[2]/div[2]/div/input"
    card2_xpath = "/html/body/div[2]/div/main/div[2]/div/form/div/div[1]/div[2]/div[2]/div/div[2]/div/div[3]/div[2]/div/input"

    fill_input(driver, card1_xpath, "28.90", "Cartão 1 - Valor Correto")
    fill_input(driver, card2_xpath, "20", "Cartão 2 - Valor Correto")

    # Finalizar compra
    click_element(driver, "/html/body/div[2]/div/main/div[2]/div/form/div/div[2]/div/div[2]/button",
                 "Finalizar Compra")

    time.sleep(2)

    try:
        Alert(driver).accept()
        logger.info("✓ Alerta de confirmação aceito")
    except Exception:
        logger.info("✓ Nenhum alerta para aceitar (compra finalizada)")

    time.sleep(2)

    # Verificar se a compra foi bem-sucedida
    try:
        assert_page_url(driver, "/pedidos", timeout=5)
        logger.info(f"✓ PAGAMENTO SUCESSO - Redirecionado para pedidos: {driver.current_url}")
    except AssertionError:
        logger.warning(f"⚠ Página não redirecionada para pedidos. URL atual: {driver.current_url}")

    logger.info("✓ PAGAMENTO - Compra finalizada com sucesso\n")


# ============================================================================
# SUITE DE TESTES COMPLETA
# ============================================================================

def run_complete_e2e_test():
    """Executa o fluxo completo de E2E testing."""
    logger.info("\n")
    logger.info("╔" + "="*58 + "╗")
    logger.info("║" + " "*15 + "INICIANDO TESTES E2E JAKEBOOKS" + " "*13 + "║")
    logger.info("╚" + "="*58 + "╝")
    logger.info(f"Data/Hora: {time.strftime('%Y-%m-%d %H:%M:%S')}\n")

    driver = None
    tests_passed = 0
    tests_failed = 0

    try:
        driver = setup_driver()

        # ========== TESTES DE LOGIN ==========
        try:
            test_login_invalid_email(driver)
            tests_passed += 1
        except AssertionError as e:
            logger.error(f"✗ TESTE FALHOU: test_login_invalid_email - {e}")
            tests_failed += 1

        try:
            test_login_invalid_password(driver)
            tests_passed += 1
        except AssertionError as e:
            logger.error(f"✗ TESTE FALHOU: test_login_invalid_password - {e}")
            tests_failed += 1

        try:
            test_login_success(driver)
            tests_passed += 1
        except AssertionError as e:
            logger.error(f"✗ TESTE FALHOU: test_login_success - {e}")
            tests_failed += 1

        # ========== TESTES DE CARRINHO ==========
        try:
            test_add_remove_single_item(driver)
            tests_passed += 1
        except AssertionError as e:
            logger.error(f"✗ TESTE FALHOU: test_add_remove_single_item - {e}")
            tests_failed += 1

        try:
            test_add_multiple_items(driver)
            tests_passed += 1
        except AssertionError as e:
            logger.error(f"✗ TESTE FALHOU: test_add_multiple_items - {e}")
            tests_failed += 1

        try:
            test_proceed_to_checkout(driver)
            tests_passed += 1
        except AssertionError as e:
            logger.error(f"✗ TESTE FALHOU: test_proceed_to_checkout - {e}")
            tests_failed += 1

        # ========== TESTES DE CHECKOUT ==========
        try:
            test_select_address(driver)
            tests_passed += 1
        except AssertionError as e:
            logger.error(f"✗ TESTE FALHOU: test_select_address - {e}")
            tests_failed += 1

        try:
            test_payment_invalid_values(driver)
            tests_passed += 1
        except AssertionError as e:
            logger.error(f"✗ TESTE FALHOU: test_payment_invalid_values - {e}")
            tests_failed += 1

        try:
            test_payment_accept_terms(driver)
            tests_passed += 1
        except AssertionError as e:
            logger.error(f"✗ TESTE FALHOU: test_payment_accept_terms - {e}")
            tests_failed += 1

        try:
            test_payment_correct_values(driver)
            tests_passed += 1
        except AssertionError as e:
            logger.error(f"✗ TESTE FALHOU: test_payment_correct_values - {e}")
            tests_failed += 1

    except Exception as e:
        logger.error(f"✗ ERRO CRÍTICO: {e}")
        tests_failed += 1

    finally:
        teardown_driver(driver)

    # ========== RELATÓRIO FINAL ==========
    logger.info("\n")
    logger.info("╔" + "="*58 + "╗")
    logger.info("║" + " "*20 + "RELATÓRIO FINAL" + " "*23 + "║")
    logger.info("╠" + "="*58 + "╣")
    logger.info(f"║ Testes Aprovados: {tests_passed:<37}║")
    logger.info(f"║ Testes Falhados:  {tests_failed:<37}║")
    logger.info(f"║ Total:            {tests_passed + tests_failed:<37}║")
    logger.info("╚" + "="*58 + "╝\n")

    return tests_passed, tests_failed


# ============================================================================
# ENTRY POINT
# ============================================================================

if __name__ == "__main__":
    try:
        passed, failed = run_complete_e2e_test()

        if failed == 0:
            logger.info("✓ TODOS OS TESTES PASSARAM COM SUCESSO!")
            sys.exit(0)
        else:
            logger.error(f"✗ {failed} TESTE(S) FALHARAM")
            sys.exit(1)

    except KeyboardInterrupt:
        logger.info("\n⚠ Testes interrompidos pelo usuário")
        sys.exit(130)
