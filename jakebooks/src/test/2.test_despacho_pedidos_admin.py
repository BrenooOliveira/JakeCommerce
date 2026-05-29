import re
import logging
import time
from playwright.sync_api import Playwright, sync_playwright, expect

logging.basicConfig(level=logging.INFO, format='%(asctime)s %(levelname)s %(message)s')


def run(playwright: Playwright) -> None:
    logging.info("Test 2: iniciar teste de despacho de pedidos (admin)")
    browser = playwright.chromium.launch(headless=False)
    context = browser.new_context()
    page = context.new_page()
    # Aceita automaticamente qualquer alert/confirm/prompt do JS para evitar timeouts
    page.on("dialog", lambda dialog: dialog.accept())
    page.goto("http://localhost:8080/login")
    logging.info("2: aberto /login")
    page.get_by_role("textbox", name=" Email").click()
    page.get_by_role("textbox", name=" Email").fill("admin@jakebooks.com")
    page.get_by_role("textbox", name=" Email").press("Tab")
    page.get_by_role("textbox", name=" Senha").press("Tab")
    page.get_by_role("textbox", name=" Senha").click()
    page.get_by_role("textbox", name=" Senha").fill("Admin123@")
    # Aguarda a navegação que o login pode disparar para não prosseguir cedo demais
    with page.expect_navigation():
        page.get_by_role("button", name=" Entrar").click()
    logging.info("2: fez login como admin")
    time.sleep(3)
    page.goto("http://localhost:8080/admin/pedidos/12")
    logging.info("2: navegou para pedido 12")
    page.wait_for_load_state("networkidle")
    time.sleep(3)
    despachar_btn = page.get_by_role("button", name=" Despachar Pedido")
    expect(despachar_btn).to_be_visible(timeout=60000)
    expect(despachar_btn).to_be_enabled(timeout=60000)
    despachar_btn.click(timeout=60000)
    logging.info("2: clicou Despachar Pedido")
    time.sleep(3)
    page.goto("http://localhost:8080/admin/pedidos/12")
    page.wait_for_load_state("networkidle")
    confirmar_btn = page.get_by_role("button", name=" Confirmar Entrega")
    expect(confirmar_btn).to_be_visible(timeout=60000)
    expect(confirmar_btn).to_be_enabled(timeout=60000)
    confirmar_btn.click(timeout=60000)
    logging.info("2: clicou Confirmar Entrega")
    time.sleep(3)
    page.goto("http://localhost:8080/admin/pedidos/12")
    page.wait_for_load_state("networkidle")

    # ---------------------
    context.close()
    browser.close()
    logging.info("Test 2: finalizado")


with sync_playwright() as playwright:
    run(playwright)
