import re
from playwright.sync_api import Playwright, sync_playwright, expect
import logging
import time
from pathlib import Path

logging.basicConfig(level=logging.INFO, format='%(asctime)s %(levelname)s %(message)s')


def run(playwright: Playwright) -> None:
    logging.info("Test 4: iniciar teste autorizar troca e despacho (admin)")
    browser = playwright.chromium.launch(headless=False)
    context = browser.new_context()
    page = context.new_page()
    page.on("dialog", lambda dialog: dialog.accept())
    page.goto("http://localhost:8080/login")
    logging.info("4: aberto /login")
    page.get_by_role("textbox", name=" Email").click()
    page.get_by_role("textbox", name=" Email").fill("admin@jakebooks.com")
    logging.info("4: email preenchido: admin@jakebooks.com")
    page.get_by_role("textbox", name=" Email").press("Tab")
    page.get_by_role("textbox", name=" Senha").press("CapsLock")
    page.get_by_role("textbox", name=" Senha").fill("A")
    page.get_by_role("textbox", name=" Senha").press("CapsLock")
    page.get_by_role("textbox", name=" Senha").fill("Admin123@")
    logging.info("4: senha preenchida")
    page.get_by_role("button", name=" Entrar").click()
    logging.info("4: clicou Entrar")
    time.sleep(2)
    page.goto("http://localhost:8080/admin/trocas")
    logging.info("4: navegou para /admin/trocas")
    time.sleep(2)
    page.get_by_role("link", name=" Detalhes").first.click()
    logging.info("4: abriu Detalhes da troca")
    logging.info("4: prestes a clicar Autorizar Troca")
    page.get_by_role("button", name=" Autorizar Troca").click()
    logging.info("4: clicou Autorizar Troca")
    time.sleep(3)
    logging.info("4: prestes a clicar Confirmar Recebimento")
    page.get_by_role("button", name=" Confirmar Recebimento").click()
    logging.info("4: clicou Confirmar Recebimento")
    # Captura o identificador da troca (ex: "TROCA-1234") e salva para uso por outros testes
    troca_locator = page.get_by_text("TROCA-").first
    troca_text = troca_locator.inner_text()
    # salva em arquivo compartilhado entre testes
    shared_file = Path(__file__).resolve().parent / "shared_troca_id.txt"
    shared_file.write_text(troca_text)
    troca_locator.click()
    logging.info(f"4: troca capturada e salva: {troca_text}")

    # ---------------------
    context.close()
    browser.close()


with sync_playwright() as playwright:
    run(playwright)

