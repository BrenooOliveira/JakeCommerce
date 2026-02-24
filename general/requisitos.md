---

# 📘 Requisitos Funcionais

---

## 📚 Grupo: Cadastro de Livros

| ID     | Nome                           | Descrição                                                                        |
| ------ | ------------------------------ | -------------------------------------------------------------------------------- |
| RF0011 | Cadastrar livro                | Manter cadastro único de livros no sistema.                                      |
| RF0012 | Inativar livro                 | Permitir inativação manual de livros.                                            |
| RF0013 | Inativar livro automaticamente | Inativar livros sem estoque e sem venda com valor inferior a parâmetro definido. |
| RF0014 | Alterar livro                  | Permitir alteração de dados cadastrais.                                          |
| RF0015 | Consultar livros               | Permitir consulta com filtros combinados ou isolados por qualquer campo.         |
| RF0016 | Ativar livro                   | Permitir reativação de livro inativado.                                          |

---

## 👤 Grupo: Cadastro de Clientes

| ID     | Nome                 | Descrição                                                  |
| ------ | -------------------- | ---------------------------------------------------------- |
| RF0021 | Cadastrar cliente    | Permitir cadastro de clientes.                             |
| RF0022 | Alterar cliente      | Permitir alteração dos dados cadastrais.                   |
| RF0023 | Inativar cliente     | Permitir inativação de clientes.                           |
| RF0024 | Consultar cliente    | Permitir consulta com filtros combinados ou isolados.      |
| RF0025 | Consultar transações | Exibir todas as transações realizadas pelo cliente.        |
| RF0026 | Cadastrar endereços  | Permitir múltiplos endereços identificados por nome curto. |
| RF0027 | Cadastrar cartões    | Permitir múltiplos cartões com um preferencial.            |
| RF0028 | Alterar apenas senha | Permitir alteração de senha sem alterar demais dados.      |

---

## 🛒 Grupo: Gerenciar Vendas Eletrônicas

| ID     | Nome                  | Descrição                                                                 |
| ------ | --------------------- | ------------------------------------------------------------------------- |
| RF0031 | Gerenciar carrinho    | Adicionar, alterar, excluir e visualizar itens no carrinho.               |
| RF0032 | Definir quantidade    | Editar quantidade ao adicionar ou visualizar itens no carrinho.           |
| RF0033 | Realizar compra       | Permitir finalizar compra a partir do carrinho.                           |
| RF0034 | Calcular frete        | Calcular frete com base nos itens e endereço.                             |
| RF0035 | Selecionar endereço   | Selecionar endereço existente ou cadastrar novo.                          |
| RF0036 | Selecionar pagamento  | Usar cartões cadastrados, novos cartões, cupons promocionais ou de troca. |
| RF0037 | Finalizar compra      | Após finalizar, status deve ser **EM PROCESSAMENTO**.                     |
| RF0038 | Despachar produtos    | Alterar status para **EM TRANSPORTE**.                                    |
| RF0039 | Confirmar entrega     | Alterar status para **ENTREGUE**.                                         |
| RF0040 | Solicitar troca       | Permitir solicitação de troca via pedidos.                                |
| RF0041 | Autorizar troca       | Alterar status para **TROCA AUTORIZADA**.                                 |
| RF0042 | Visualizar trocas     | Permitir administrador visualizar pedidos **EM TROCA**.                   |
| RF0043 | Confirmar recebimento | Confirmar recebimento e decidir retorno ao estoque.                       |
| RF0044 | Gerar cupom de troca  | Gerar cupom após confirmação de recebimento.                              |

---

## 📦 Grupo: Controle de Estoque

| ID     | Nome                    | Descrição                                            |
| ------ | ----------------------- | ---------------------------------------------------- |
| RF0051 | Entrada em estoque      | Registrar livro já cadastrado e quantidade.          |
| RF0052 | Calcular valor de venda | Valor = custo + percentual do grupo de precificação. |
| RF0053 | Baixa em estoque        | Dar baixa automática após venda.                     |
| RF0054 | Reentrada em estoque    | Realizar reentrada via troca.                        |

---

## 📊 Grupo: Análise

| ID     | Nome               | Descrição                                                          |
| ------ | ------------------ | ------------------------------------------------------------------ |
| RF0055 | Analisar histórico | Consultar histórico por período comparando produtos ou categorias. |

---

# ⚙️ Requisitos Não Funcionais

---

## 🌐 Grupo: Geral

| ID      | Nome              | Descrição                                                                |
| ------- | ----------------- | ------------------------------------------------------------------------ |
| RNF0011 | Tempo de resposta | Consultas devem responder em até 1 segundo.                              |
| RNF0012 | Log de transação  | Registrar data, hora, usuário e dados alterados em operações de escrita. |

---

## 📚 Grupo: Cadastro de Livros

| ID      | Nome                 | Descrição                                                                       |
| ------- | -------------------- | ------------------------------------------------------------------------------- |
| RNF0021 | Código de livro      | Todo livro deve possuir código único.                                           |
| RNF0013 | Cadastro de domínios | Script de implantação deve inserir registros de domínio (autor, editora, etc.). |

---

## 👤 Grupo: Cadastro de Clientes

| ID      | Nome                   | Descrição                                                     |
| ------- | ---------------------- | ------------------------------------------------------------- |
| RNF0031 | Senha forte            | Mínimo 8 caracteres, maiúsculas, minúsculas e especiais.      |
| RNF0032 | Confirmação de senha   | Senha deve ser digitada duas vezes.                           |
| RNF0033 | Senha criptografada    | Senha deve ser armazenada criptografada.                      |
| RNF0034 | Alteração de endereços | Permitir alterar/adicionar endereços sem editar demais dados. |
| RNF0035 | Código de cliente      | Cliente deve possuir código único.                            |

---

## 🛒 Grupo: Vendas Eletrônicas

| ID      | Nome                        | Descrição                                                           |
| ------- | --------------------------- | ------------------------------------------------------------------- |
| RNF0042 | Itens removidos do carrinho | Exibir itens removidos por expiração de tempo e desabilitar compra. |

---

## 📊 Grupo: Análise

| ID      | Nome              | Descrição                                                  |
| ------- | ----------------- | ---------------------------------------------------------- |
| RNF0043 | Gráfico de linhas | Histórico de vendas deve ser exibido em gráfico de linhas. |

---

## 🤖 Grupo: Recomendação Personalizada

| ID      | Nome          | Descrição                                                                                               |
| ------- | ------------- | ------------------------------------------------------------------------------------------------------- |
| RNF0044 | IA Generativa | Integrar IA generativa para recomendações, chatbot e treinamento contínuo baseado em vendas e feedback. |

---

# 📜 Regras de Negócio

---

## 📚 Grupo: Cadastro de Livros

| ID     | Nome                     | Descrição                                                                                                                      |
| ------ | ------------------------ | ------------------------------------------------------------------------------------------------------------------------------ |
| RN0011 | Dados obrigatórios livro | Autor, categoria, ano, título, editora, edição, ISBN, nº páginas, sinopse, dimensões, grupo de precificação, código de barras. |
| RN0012 | Associação categorias    | Livro pode ter múltiplas categorias.                                                                                           |
| RN0013 | Definir valor venda      | Baseado na margem do grupo de precificação.                                                                                    |
| RN0014 | Validar margem           | Alteração abaixo da margem exige autorização de gerente.                                                                       |
| RN0015 | Motivo inativação manual | Deve ter justificativa e categoria.                                                                                            |
| RN0016 | Inativação automática    | Categoria deve ser FORA DE MERCADO.                                                                                            |
| RN0017 | Motivo ativação          | Deve registrar justificativa e categoria.                                                                                      |

---

## 👤 Grupo: Cadastro de Clientes

| ID     | Nome                       | Descrição                                                                          |
| ------ | -------------------------- | ---------------------------------------------------------------------------------- |
| RN0021 | Endereço cobrança          | Obrigatório ao menos um.                                                           |
| RN0022 | Endereço entrega           | Obrigatório ao menos um.                                                           |
| RN0023 | Composição endereço        | Tipo residência, logradouro, nº, bairro, CEP, cidade, estado, país (obrigatórios). |
| RN0024 | Composição cartão          | Nº cartão, nome impresso, bandeira, código segurança.                              |
| RN0025 | Bandeiras permitidas       | Deve estar cadastrada no sistema.                                                  |
| RN0026 | Dados obrigatórios cliente | Gênero, nome, nascimento, CPF, telefone, e-mail, senha, endereço residencial.      |
| RN0027 | Ranking cliente            | Cliente recebe ranking numérico por perfil de compra.                              |
| RN0028 | Baixa estoque              | Apenas após aprovação do pagamento.                                                |

---

## 🛒 Grupo: Vendas Eletrônicas

| ID        | Nome                              | Descrição                                                                                                                         |
| -------   | --------------------------------- | --------------------------------------------------------------                                                                    |
| RN0031    | Validar estoque carrinho          | Não permitir itens indisponíveis ou acima do estoque.                                                                             |
| RN0032    | Validar estoque compra            | Atualizar/remover itens se estoque mudar antes da finalização.                                                                    |
| RN0033    | Cupom promocional                 | Apenas um por compra.                                                                                                             |
| RN0034    | Múltiplos cartões                 | Permitido, mínimo R$ 10 por cartão.                                                                                               |
| RN0035    | Cupom + cartão                    | Usar valor máximo dos cupons antes do cartão.                                                                                     |
| RN0036    | Gerar cupom excedente             | Gerar cupom se pagamento com cupons exceder valor.                                                                                |
| RN0037    | Validar pagamento                 | Validar cupons e aprovação da operadora.                                                                                          |
| RN0038    | Alterar status pagamento          | APROVADA ou REPROVADA.                                                                                                            |
| RN0039    | Status transporte                 | Alterar para EM TRANSPORTE.                                                                                                       |
| RN0040    | Status entrega                    | Alterar para ENTREGUE.                                                                                                            |
| RN0041    | Gerar pedido troca                | Status EM TROCA.                                                                                                                  |
| RN0042    | Status após troca                 | Alterar para TROCADO.                                                                                                             |
| RN0043    | Validação troca                   | Apenas pedidos ENTREGUES podem solicitar troca.                                                                                   |
| RN0044    | Bloqueio carrinho                 | Bloqueio temporário com notificação 5 min antes de expirar.                                                                       |
| RNF0045   | Remover item desbloqueado         | Itens desbloqueados devem ser removidos do carrinho.                                                                              |
| RNF0046   | Notificação troca                 | Sistema deve notificar cliente quando troca for autorizada.                                                                       |
| 🟢 RN0063 | Limite de livros                  | Um cliente pode comprar no máximo 10 unidades do mesmo livro por pedido.                                                          |
| 🟢 RN0064 | Pedido mínimo                     | O pedido deve possuir valor mínimo de R$ 20,00 (sem frete) para poder ser finalizado.                                             | 
| 🟢 RN0065 | Cliente inadimplente              | Cliente que possuir 3 pedidos REPROVADOS consecutivos por pagamento terá o carrinho bloqueado temporariamente.                    |


---

## 📦 Grupo: Controle de Estoque

| ID      | Nome                  | Descrição                                                   |
| ------- | --------------------- | ----------------------------------------------------------- |
| RN0051  | Validar dados entrada | Produto, quantidade, custo, fornecedor e data obrigatórios. |
| RN005x  | Diferentes custos     | Considerar maior custo para definir valor de venda.         |
| RN0061  | Quantidade            | Não permitir entrada com quantidade zero.                   |
| RN0062  | Valor de custo        | Todo item deve possuir valor de custo.                      |
| RNF0064 | Data entrada          | Não permitir registro sem data de entrada.                  |

---

