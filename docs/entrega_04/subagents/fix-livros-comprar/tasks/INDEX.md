# Tasks - fix-livros-comprar

## Resumo
Implementar funcionalidade de compra nas paginas de listagem e detalhe de livros.

## Arquitetura

### Camadas Envolvidas
- **View**: Templates Thymeleaf (unica camada modificada)
- **Controller**: Reutiliza `CarrinhoController` existente
- **Service**: Nenhuma modificacao

### Fluxo MVC
```
[Template] --POST--> [CarrinhoController] ---> [CarrinhoService] ---> [Repository]
     ^                      |
     |                      |
     +---- redirect --------+
```

## Tasks

| Task | Arquivo | Escopo |
|------|---------|--------|
| TASK-001 | `livros/lista.html` | Botao de compra rapida (1 un.) |
| TASK-002 | `livros/detalhe.html` | Card de compra com quantidade |
| TASK-003 | `livros/detalhe.html` | Card de incentivo ao login |

## Ordem de Execucao

1. **TASK-001** - Independente, pode ser feita primeiro
2. **TASK-002 + TASK-003** - Fazer juntas no mesmo arquivo

## Pre-Requisitos

### Verificar no Template
- Namespace Spring Security: `xmlns:sec="http://www.thymeleaf.org/extras/spring-security"`
- Se nao existir, adicionar na tag `<html>`

### Verificar Rotas Existentes
- `/carrinho/adicionar` - endpoint de adicao ao carrinho
- `/login` - pagina de login
- `/clientes/novo` - cadastro de clientes

### Verificar Modelo
- Acesso a `livro.status()` retorna StatusLivro
- Acesso a `livro.estoque().quantidade()` retorna quantidade disponivel

## Regras de Negocio

| Regra | Descricao | Onde Aplicar |
|-------|-----------|--------------|
| RN0063 | Max 10 unidades por item | TASK-002 (seletor de quantidade) |

## Dependencias Externas
- `fix-session-auth` deve estar concluido para o fluxo funcionar

## Criterios de Conclusao

- [ ] Botao na listagem funciona para autenticados
- [ ] Card de compra no detalhe funciona com validacoes
- [ ] Card de login aparece para visitantes
- [ ] Aplicacao compila sem erros
- [ ] Fluxo end-to-end testado
