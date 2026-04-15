# TASK-001: Botao Adicionar ao Carrinho na Listagem

## Objetivo
Permitir que clientes autenticados adicionem livros ao carrinho diretamente da listagem.

## Camada Afetada
**View** - Template Thymeleaf

## Arquivo
`src/main/resources/templates/livros/lista.html`

## Abordagem

### Localizacao no Template
Identificar a secao de acoes dentro do loop de iteracao dos livros (provavelmente onde estao os botoes "Ver", "Editar", etc).

### Logica de Exibicao
- Condicao de autenticacao via Spring Security (`sec:authorize`)
- Condicao de status do livro via Thymeleaf (`th:if`)
- Exibir apenas para livros com status ATIVO

### Comportamento
- Formulario POST para endpoint existente `/carrinho/adicionar`
- Quantidade fixa: 1 unidade (simplicidade na listagem)
- Campos hidden: codigo do livro e quantidade

### Componentes Visuais
- Botao pequeno (btn-sm) com icone de carrinho
- Classe de sucesso (verde) para indicar acao positiva
- Tooltip explicativo

## Consideracoes de Arquitetura
- Nenhuma alteracao no Controller ou Service necessaria
- Reutiliza endpoint existente do CarrinhoController
- Segue padrao ja estabelecido nos outros botoes de acao

## Criterios de Aceite
- Usuario nao autenticado: botao invisivel
- Usuario autenticado + livro ATIVO: botao visivel
- Usuario autenticado + livro INATIVO: botao invisivel
- Acao redireciona corretamente apos adicionar
