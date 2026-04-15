# TASK-003: Card de Incentivo ao Login

## Objetivo
Incentivar usuarios nao autenticados a fazer login/cadastro para realizar compras.

## Camada Afetada
**View** - Template Thymeleaf

## Arquivo
`src/main/resources/templates/livros/detalhe.html`

## Abordagem

### Posicionamento
Mesmo local do card de compra (TASK-002). Os dois cards sao mutuamente exclusivos:
- Autenticado: ve card de compra
- Nao autenticado: ve card de incentivo

### Estrutura do Card

**Header:**
- Mesmo titulo do card de compra para consistencia visual

**Body:**
- Mensagem explicativa convidando ao login
- Botao principal para pagina de login
- Link secundario para cadastro de novos clientes

### Logica de Exibicao
- Condicao inversa do card de compra: `!isAuthenticated()`
- Nao depende de status ou estoque do livro

### Rotas Utilizadas
- Login: `/login` (verificar rota existente)
- Cadastro: `/clientes/novo` (verificar rota existente)

## Consideracoes de Arquitetura
- Nenhuma alteracao em backend
- Apenas adicao de HTML condicional
- Manter consistencia visual com card de compra

## Criterios de Aceite
- Card visivel apenas para NAO autenticados
- Link de login funciona
- Link de cadastro funciona
- Card de compra (TASK-002) fica oculto quando este aparece
