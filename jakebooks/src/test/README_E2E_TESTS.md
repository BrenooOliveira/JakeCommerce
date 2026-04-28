# E2E Test Suite JakeBooks - Documentação

## 📋 Visão Geral

Script automatizado em Python para testar o fluxo completo de vendas da aplicação JakeBooks (Login → Carrinho → Checkout → Pagamento).

**Local do arquivo**: `jakebooks/src/test/selenium_test_e2e.py`

---

## 🚀 Como Executar

### Pré-requisitos

```bash
# Instalar Selenium
pip install selenium

# (Opcional) Criar virtual environment
python3 -m venv venv
source venv/bin/activate
```

### Executar os testes

```bash
# Do diretório do projeto
cd jakebooks/src/test

# Executar o script
python3 selenium_test_e2e.py

# Ou com permissão de execução
chmod +x selenium_test_e2e.py
./selenium_test_e2e.py
```

### Output

- **Console**: Logs em tempo real com cores de status (✓ sucesso, ✗ erro, ⚠ aviso)
- **Arquivo**: `jakebooks_e2e_test.log` no diretório de execução

---

## 🔍 Melhorias implementadas

### 1. **Logging Estruturado**
   - Logs em arquivo + console simultâneos
   - Timestamps em cada mensagem
   - Níveis: INFO, ERROR, WARNING, DEBUG
   - Fácil rastreamento de falhas

### 2. **Assertions Explícitas**
   - Verifica URLs após cada navegação
   - Verifica visibilidade de elementos críticos
   - Mensagens de erro claras e específicas
   - Suporta wait implícito + explícito

### 3. **Tratamento de Erros**
   - Try-catch em operações críticas
   - Limpeza segura de recursos (teardown)
   - Contagem de testes passou/falhou
   - Exit codes apropriados (0 = sucesso, 1 = falha, 130 = interrupção)

### 4. **Helpers Reutilizáveis**
   - `find_element_by_xpath()` - com espera explícita
   - `click_element()` - clique + log
   - `fill_input()` - preencher + log
   - `assert_page_url()` - verificação de navegação
   - `assert_element_visible()` - verificação de elemento

### 5. **Estrutura Monolítica (Simples)**
   - Um único arquivo sem dependencies externas (além de Selenium)
   - Funções claras e independentes
   - Fácil de ler, debugar e manter
   - Sem Page Object Model (mantém simplicidade)

### 6. **Fluxo Completo Testado**
   - ✅ Login com erro de email
   - ✅ Login com erro de senha
   - ✅ Login bem-sucedido
   - ✅ Adicionar/remover item do carrinho
   - ✅ Adicionar múltiplos itens
   - ✅ Prosseguir para checkout
   - ✅ Selecionar endereço
   - ✅ Validação de valores mínimos de pagamento
   - ✅ Aceitação de termos de uso
   - ✅ Pagamento com valores corretos

---

## 📊 Relatório de Testes

Ao final da execução, exibe:
```
╔══════════════════════════════════════════════════════════╗
║                    RELATÓRIO FINAL                       ║
╠══════════════════════════════════════════════════════════╣
║ Testes Aprovados: 10                                     ║
║ Testes Falhados:  0                                      ║
║ Total:            10                                     ║
╚══════════════════════════════════════════════════════════╝
```

---

## 🐛 Debugging

### Ver logs detalhados
```bash
# Primeiro, edite o arquivo e mude:
# logging.basicConfig(level=logging.INFO, ...)
# para:
# logging.basicConfig(level=logging.DEBUG, ...)

# Depois execute para ver logs de debug
python3 selenium_test_e2e.py
```

### Aumentar timeouts
```python
# No arquivo, procure por:
driver.implicitly_wait(10)  # Aumentar se elementos demoram

# E nos find_element_by_xpath():
find_element_by_xpath(driver, xpath, wait_time=15)  # Aumentar tempo
```

### Verificar elementos HTML
```bash
# Inspecionar a página em runtime:
# 1. Adicione uma pausa antes de um clique:
time.sleep(10)  # Permite inspecionar manualmente

# 2. Use F12 no navegador para ver estrutura HTML
```

---

## 🔧 Customizações Comuns

### Mudar URL da aplicação
```python
# Procure por:
driver.get("http://localhost:8080/login")

# E mude para:
driver.get("http://seu-dominio.com/login")
```

### Mudar credenciais de teste
```python
# Procure nas funções de login:
fill_input(driver, email_xpath, "breno@teste.com", "Email")
fill_input(driver, password_xpath, "Brenets2009@", "Senha")

# E atualize com suas credenciais
```

### Usar Firefox ao invés de Chrome
```python
# Em setup_driver(), substitua:
from selenium.webdriver.firefox.options import Options
from selenium.webdriver.firefox.service import Service

options = Options()
driver = webdriver.Firefox(options=options)
```

---

## 📝 Comparação: Notebook vs Script

| Aspecto | Notebook (test.ipynb) | Script (selenium_test_e2e.py) |
|---------|----------------------|-------------------------------|
| **Logging** | ❌ Nenhum | ✅ Arquivo + console |
| **Assertions** | ❌ Nenhuma | ✅ Explícitas com messages |
| **Waits** | ⚠️ Apenas `time.sleep()` | ✅ Explícitos + implícitos |
| **Tratamento de Erros** | ❌ Nenhum | ✅ Try-catch + cleanup |
| **Relatório Final** | ❌ Nenhum | ✅ Contagem de testes |
| **Reutilização** | ❌ Código duplicado | ✅ Helpers reutilizáveis |
| **Organização** | ⚠️ Células soltas | ✅ Funções e seções |
| **Exit Codes** | ❌ Nenhum | ✅ 0=sucesso, 1=erro |

---

## 🚦 Exemplo de Output

```
2026-04-27 14:23:01,234 - INFO - [setup_driver] - Iniciando driver Selenium...
2026-04-27 14:23:02,145 - INFO - [setup_driver] - Driver iniciado com sucesso
2026-04-27 14:23:02,156 - INFO - [run_complete_e2e_test] - 
╔══════════════════════════════════════════════════════════╗
║                INICIANDO TESTES E2E JAKEBOOKS            ║
╚══════════════════════════════════════════════════════════╝

2026-04-27 14:23:02,167 - INFO - [test_login_invalid_email] - ============================================================
2026-04-27 14:23:02,178 - INFO - [test_login_invalid_email] - TESTE: Login com Email Inválido
2026-04-27 14:23:03,245 - INFO - [find_element_by_xpath] - Elemento encontrado: /html/body/div[2]/div/main/div[2]/div/div[2]/form/div[1]/input
2026-04-27 14:23:03,256 - INFO - [fill_input] - ✓ Preenchido Email com: email_invalidooo@gmail.com
2026-04-27 14:23:03,678 - INFO - [fill_input] - ✓ Preenchido Senha com: Invalido123@
2026-04-27 14:23:03,689 - INFO - [click_element] - ✓ Clicado: Botão Login
2026-04-27 14:23:05,234 - INFO - [assert_page_url] - ✓ URL verificada: /login está em http://localhost:8080/login
2026-04-27 14:23:05,245 - INFO - [test_login_invalid_email] - ✓ LOGIN REJEITADO - Email inválido conforme esperado

...

╔══════════════════════════════════════════════════════════╗
║                    RELATÓRIO FINAL                       ║
╠══════════════════════════════════════════════════════════╣
║ Testes Aprovados: 10                                     ║
║ Testes Falhados:  0                                      ║
║ Total:            10                                     ║
╚══════════════════════════════════════════════════════════╝

✓ TODOS OS TESTES PASSARAM COM SUCESSO!
```

---

## 📚 Referências

- **Selenium WebDriver**: https://selenium.dev/documentation/webdriver/
- **Expected Conditions**: https://selenium.dev/documentation/webdriver/waits/
- **Python Logging**: https://docs.python.org/3/library/logging.html

---

## ✅ Checklist de Uso

- [ ] Instalar Selenium: `pip install selenium`
- [ ] Verificar credenciais de teste em `test_login_success()`
- [ ] Confirmar URL base: `http://localhost:8080`
- [ ] Executar: `python3 selenium_test_e2e.py`
- [ ] Verificar logs: `jakebooks_e2e_test.log`
- [ ] Revisar relatório final no console

