import re
import logging
import time
from pathlib import Path
from playwright.sync_api import Playwright, sync_playwright, expect

logging.basicConfig(level=logging.INFO, format='%(asctime)s %(levelname)s %(message)s')


def run(playwright: Playwright) -> None:
    logging.info("Test 5: iniciar teste usando cupom de troca")
    shared_file = Path(__file__).resolve().parent / "shared_troca_id.txt"
    if not shared_file.exists():
        raise FileNotFoundError(f"Arquivo de cupom não encontrado: {shared_file}")
    cupom_troca = shared_file.read_text(encoding="utf-8").strip()
    logging.info(f"5: cupom carregado do arquivo compartilhado: {cupom_troca}")

    browser = playwright.chromium.launch(headless=False)
    context = browser.new_context()
    page = context.new_page()
    page.goto("http://localhost:8080/login")
    logging.info("5: aberto /login")
    page.get_by_role("textbox", name=" Email").click()
    page.get_by_role("textbox", name=" Email").fill("breno@teste.com")
    page.get_by_role("textbox", name=" Email").press("Tab")
    page.get_by_role("textbox", name=" Senha").press("CapsLock")
    page.get_by_role("textbox", name=" Senha").fill("B")
    page.get_by_role("textbox", name=" Senha").press("CapsLock")
    page.get_by_role("textbox", name=" Senha").fill("Brenets2009@")
    page.get_by_role("button", name=" Entrar").click()
    logging.info("5: fez login")
    time.sleep(3)
    page.get_by_role("link", name=" Livros").click()
    logging.info("5: navegou para Livros")
    time.sleep(3)
    page.get_by_role("row", name="LIV002 A Hora da Estrela").get_by_role("button").click()
    page.get_by_role("link", name=" Continuar Comprando").click()
    page.get_by_role("row", name="LIV002 A Hora da Estrela").get_by_role("button").click()
    page.get_by_role("link", name=" Continuar Comprando").click()
    page.locator("tr:nth-child(8) > td:nth-child(6) > .d-inline > .btn").click()
    page.get_by_role("button", name=" Prosseguir com Pagamento").click()
    logging.info("5: abriu checkout")
    time.sleep(3)
    page.get_by_text("Casa Principal").first.click()
    page.get_by_role("textbox", name="Digite seu código de cupom").click()
    page.get_by_role("textbox", name="Digite seu código de cupom").fill(cupom_troca)
    logging.info(f"5: cupom aplicado no formulário: {cupom_troca}")
    page.get_by_role("button", name=" Aplicar").click()
    time.sleep(3)
    page.get_by_placeholder("0,00").first.click()
    page.get_by_placeholder("0,00").first.fill("96.3")
    page.locator(".form-check.mt-3").click()
    page.get_by_role("checkbox", name="Concordo com os termos e").check()
    page.get_by_role("button", name=" Finalizar Compra").click()
    logging.info("5: finalizou compra")
    time.sleep(3)
    # ---------------------
    context.close()
    browser.close()
    logging.info("Test 5: finalizado")


with sync_playwright() as playwright:
    run(playwright)
