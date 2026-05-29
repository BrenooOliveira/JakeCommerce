import re
import logging
import time
from playwright.sync_api import Playwright, sync_playwright, expect

logging.basicConfig(level=logging.INFO, format='%(asctime)s %(levelname)s %(message)s')


def run(playwright: Playwright) -> None:
    logging.info("Test 3: iniciar teste de solicitar troca (cliente)")
    browser = playwright.chromium.launch(headless=False)
    context = browser.new_context()
    page = context.new_page()
    page.goto("http://localhost:8080/pedidos/12")
    logging.info("3: aberto pagina do pedido 12")
    time.sleep(3)
    page.get_by_role("textbox", name=" Email").click()
    page.get_by_role("textbox", name=" Email").fill("breno@teste.com")
    page.get_by_role("textbox", name=" Email").press("Tab")
    page.get_by_role("textbox", name=" Senha").press("CapsLock")
    page.get_by_role("textbox", name=" Senha").fill("B")
    page.get_by_role("textbox", name=" Senha").press("CapsLock")
    page.get_by_role("textbox", name=" Senha").fill("Brenets2009@")
    page.get_by_role("button", name=" Entrar").click()
    time.sleep(3)
    page.get_by_role("link", name=" Solicitar Troca").click()
    page.locator("#item-20").uncheck()
    page.locator("#item-20").check()
    page.locator("#item-20").uncheck()
    page.get_by_role("textbox", name=" Motivo da Troca *").click()
    page.get_by_role("textbox", name=" Motivo da Troca *").fill("meu livro veio errado e não gostei da atitude")
    page.get_by_role("button", name=" Solicitar Troca").click()
    logging.info("3: clicou Solicitar Troca")
    time.sleep(3)
    # ---------------------
    context.close()
    browser.close()
    logging.info("Test 3: finalizado")


with sync_playwright() as playwright:
    run(playwright)
