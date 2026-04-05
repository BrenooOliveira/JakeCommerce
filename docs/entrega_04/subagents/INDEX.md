# Subagents - Entrega 04

Este diretorio contem agentes granulares para corrigir problemas identificados no fluxo de venda.

## Problemas Identificados

| # | Problema | Impacto |
|---|----------|---------|
| 1 | Login nao mantem sessao do cliente | Usuario redirecionado para login em rotas autenticadas |
| 2 | Listagem de livros sem botao de comprar | Impossivel adicionar livro ao carrinho |
| 3 | Detalhe do livro sem opcao de compra | Impossivel adicionar livro ao carrinho |

## Agentes

### 1. fix-session-auth (PRIORIDADE ALTA)

**Caminho:** `./fix-session-auth/AGENT.md`

**Problema:** Apos login, o atributo `codigoClienteAutenticado` nao e populado na sessao HTTP, fazendo com que rotas como `/carrinho` redirecionem para `/login`.

**Solucao:** Criar `CustomAuthenticationSuccessHandler` que popula a sessao apos autenticacao bem-sucedida.

**Arquivos a modificar:**
- Criar: `config/CustomAuthenticationSuccessHandler.java`
- Modificar: `config/SecurityConfig.java`

---

### 2. fix-livros-comprar (DEPENDE DE #1)

**Caminho:** `./fix-livros-comprar/AGENT.md`

**Problema:** Templates de livros nao possuem opcao de adicionar ao carrinho.

**Solucao:** Adicionar botoes e formularios para adicionar livros ao carrinho nos templates de listagem e detalhe.

**Arquivos a modificar:**
- Modificar: `templates/livros/lista.html`
- Modificar: `templates/livros/detalhe.html`

---

## Ordem de Execucao

```
[1] fix-session-auth  ───────────>  [2] fix-livros-comprar
      (obrigatorio)                       (depende de 1)
```

## Como Executar

```bash
# Agente 1: Corrigir sessao
claude-code --agent fix-session-auth

# Agente 2: Adicionar botao comprar (apos agente 1)
claude-code --agent fix-livros-comprar
```

## Validacao Final

Apos executar ambos os agentes:

1. Iniciar a aplicacao
2. Acessar `/livros` como usuario nao autenticado
3. Fazer login com um cliente valido
4. Verificar que botoes de comprar aparecem
5. Adicionar um livro ao carrinho pela listagem
6. Verificar carrinho em `/carrinho`
7. Acessar detalhe de um livro `/livros/{codigo}`
8. Adicionar multiplas unidades ao carrinho
9. Verificar carrinho novamente

## Relacionamento com Agentes do Checkout

Estes subagentes sao pre-requisitos para o fluxo principal de checkout:

```
fix-session-auth ──> fix-livros-comprar ──> checkout-agent
                                                   |
                                      ┌────────────┼────────────┐
                                      v            v            v
                               payment-agent  shipping-agent  ...
```
