Objetivo: Remover dependência de isAdmin nos templates e padronizar badges de status.
Contexto: Após AGENT_02, os templates ainda podem ter resquícios de isAdmin e comparações T(...) com enums que causam ClassCastException.
Tarefa:
1. Substituir todas as ocorrências de T(com.les.jakebooks.model.enums.*) nos templates pela comparação via .name():
html<!-- Antes -->
th:classappend="${status == T(com.les.jakebooks.model.enums.StatusPedido).ENTREGUE}"

<!-- Depois -->
th:classappend="${status.name() == 'ENTREGUE'}"
2. Substituir th:if="${isAdmin}" por sec:authorize:
html<div sec:authorize="hasRole('ADMIN')">...</div>
3. Adicionar namespace do Thymeleaf Security nos templates que usarem sec:authorize:
htmlxmlns:sec="http://www.thymeleaf.org/extras/spring-security"
4. Verificar consistência entre nomes de campos dos DTOs e o que os templates chamam — qualquer Method X() cannot be found deve ser corrigido alinhando o template ao nome real do campo no record.
Regras:

Não alterar lógica de nenhum template, apenas sintaxe de expressões
Manter todos os fluxos visuais funcionando

Validação: Nenhuma página lança TemplateInputException. Badges de status aparecem corretamente em todas as listagens.