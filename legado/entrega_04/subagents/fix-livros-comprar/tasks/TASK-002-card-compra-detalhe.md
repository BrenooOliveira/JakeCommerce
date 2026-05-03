# TASK-002: Card de Compra no Detalhe do Livro

## Objetivo
Permitir que clientes autenticados adicionem livros ao carrinho com quantidade variavel na pagina de detalhe.

## Camada Afetada
**View** - Template Thymeleaf

## Arquivo
`src/main/resources/templates/livros/detalhe.html`

## Abordagem

### Posicionamento
Adicionar novo card na sidebar/coluna lateral, antes do card de acoes administrativas. O card de compra deve ter prioridade visual sobre acoes admin.

### Estrutura do Card

**Header:**
- Titulo indicando acao de compra
- Estilo visual destacado (cor de sucesso)

**Body - Estados Condicionais:**

1. **Estado: Disponivel para compra**
   - Condicao: livro ATIVO E estoque > 0
   - Exibir: formulario com seletor de quantidade
   - Seletor limitado a `min(10, estoque)` conforme RN0063
   - Mostrar quantidade disponivel como informacao

2. **Estado: Livro indisponivel**
   - Condicao: livro NAO esta ATIVO
   - Exibir: alerta informando indisponibilidade

3. **Estado: Sem estoque**
   - Condicao: livro ATIVO mas estoque = 0
   - Exibir: alerta informando falta de estoque

### Logica de Exibicao
- Card inteiro visivel apenas para autenticados (`sec:authorize`)
- Estados internos controlados por `th:if` baseado em status e estoque

### Comportamento do Formulario
- POST para `/carrinho/adicionar`
- Campos: codigo do livro (hidden) + quantidade (select)

## Consideracoes de Arquitetura
- Acessar `livro.status()` para verificar StatusLivro.ATIVO
- Acessar `livro.estoque().quantidade()` para verificar disponibilidade
- Usar `T(...)` do Thymeleaf para referenciar enum StatusLivro
- Usar `#numbers.sequence()` para gerar opcoes do seletor

## Regras de Negocio Aplicadas
- RN0063: Maximo 10 unidades por item

## Criterios de Aceite
- Card visivel apenas para autenticados
- Formulario aparece quando livro ativo e com estoque
- Seletor de quantidade respeita limites
- Mensagens de indisponibilidade corretas por estado
- Quantidade disponivel exibida ao usuario
