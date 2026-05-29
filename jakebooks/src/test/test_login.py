import re
from playwright.sync_api import Playwright, sync_playwright, expect
import time


def run(playwright: Playwright) -> None:
    browser = playwright.chromium.launch(headless=False)
    context = browser.new_context()
    page = context.new_page()
    page.goto("http://localhost:8080/")
    page.get_by_role("link", name=" Login").click()
    time.sleep(3)
    page.get_by_role("textbox", name=" Email").click()
    page.get_by_role("textbox", name=" Email").click()
    page.get_by_role("textbox", name=" Email").fill("email_invalido")
    page.get_by_role("textbox", name=" Senha").click()
    page.get_by_role("textbox", name=" Senha").fill("senha")
    page.get_by_role("textbox", name=" Senha").click()
    page.get_by_role("textbox", name=" Senha").fill("senhainvalida")
    page.get_by_role("button", name=" Entrar").click()
    time.sleep(3)
    page.get_by_role("textbox", name=" Email").fill("email_invalido@invalido")
    page.get_by_role("button", name=" Entrar").click()
    time.sleep(3)
    page.get_by_role("textbox", name=" Senha").click()
    page.get_by_role("textbox", name=" Senha").press("CapsLock")
    page.get_by_role("textbox", name=" Senha").fill("B")
    page.get_by_role("textbox", name=" Senha").press("CapsLock")
    page.get_by_role("textbox", name=" Senha").fill("Brenets2009@")
    page.get_by_role("button", name=" Entrar").click()

    # ---------------------
    context.close()
    browser.close()


with sync_playwright() as playwright:
    run(playwright)
