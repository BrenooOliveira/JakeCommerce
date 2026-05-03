# Tasks - fix-session-auth

## Visao Geral

Este diretorio contem as tasks para corrigir o problema de sessao apos autenticacao.

## Problema

Apos login, o atributo `codigoClienteAutenticado` nao e populado na sessao HTTP, fazendo com que rotas protegidas redirecionem para `/login`.

## Tasks

| # | Task | Descricao | Status |
|---|------|-----------|--------|
| 1 | [TASK-01-criar-handler.md](./TASK-01-criar-handler.md) | Criar CustomAuthenticationSuccessHandler | CONCLUIDA |
| 2 | [TASK-02-integrar-security.md](./TASK-02-integrar-security.md) | Integrar handler no SecurityConfig | CONCLUIDA |
| 3 | [TASK-03-validar.md](./TASK-03-validar.md) | Validar implementacao | CONCLUIDA |

## Ordem de Execucao

```
TASK-01 ──> TASK-02 ──> TASK-03
 (criar)    (integrar)  (validar)
```

## Arquivos Afetados

| Arquivo | Acao |
|---------|------|
| `config/CustomAuthenticationSuccessHandler.java` | CRIAR |
| `config/SecurityConfig.java` | MODIFICAR |

## Comando para Executar

```bash
# Navegar para o diretorio do projeto
cd jakebooks

# Compilar apos modificacoes
./mvnw compile

# Executar aplicacao para testes
./mvnw spring-boot:run
```

## Criterios de Sucesso

- [ ] CustomAuthenticationSuccessHandler criado e funcional
- [ ] SecurityConfig usando o success handler
- [ ] Sessao populada com `codigoClienteAutenticado` apos login
- [ ] Navegacao para `/carrinho` funciona apos login
- [ ] Logout limpa sessao corretamente
