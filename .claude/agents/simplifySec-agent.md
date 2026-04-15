Objetivo: Tornar o controle de acesso mais simples e centralizado.
Contexto: O sistema tem dois perfis: ADMIN (gerencia livros, estoque, trocas, pedidos de todos) e CLIENTE (compra, gerencia próprio perfil). Atualmente a verificação de papel está espalhada em templates com isAdmin injetado via GlobalModelAttributeAdvice e comparações com T(...) no Thymeleaf.
Tarefa:
1. Simplificar o SecurityConfig para usar apenas dois roles limpos:
java// Público
"/", "/livros", "/livros/{codigo}", "/login", "/clientes/novo", "/clientes"

// ROLE_CLIENTE
"/carrinho/**", "/pedidos/**", "/trocas/solicitar", "/clientes/perfil", "/clientes/alterar/**"

// ROLE_ADMIN  
"/admin/**", "/estoque/**", "/analise/**", "/trocas/**", "/clientes/**", "/livros/novo", "/livros/editar/**"
2. Remover o isAdmin do GlobalModelAttributeAdvice — os templates devem usar sec:authorize do Thymeleaf Security:
html<!-- Substituir th:if="${isAdmin}" por: -->
<div sec:authorize="hasRole('ADMIN')">...</div>
3. Garantir que o CustomUserDetailsService carregue o role correto baseado em um campo perfil ou role no Cliente — se não existir, criar campo role: String com valor padrão "ROLE_CLIENTE" e valor "ROLE_ADMIN" para admins.
Regras:

Não alterar a entidade Cliente além de adicionar o campo role se necessário
Manter BCrypt força 12
Manter usernameParameter("email") e passwordParameter("senha")
Não quebrar nenhum fluxo de autenticação existente

Validação: Login como admin redireciona para /admin, login como cliente redireciona para /. Acesso a /estoque sem ser admin retorna 403.