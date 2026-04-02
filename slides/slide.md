---
marp: true
theme: default
class: invert
paginate: true
author: Breno Alves de Oliveira
description: Slides do JakeCommerce. Um E-Commerce de Moda desenvolvido para a matéria de Engenharia de Software
header: "JakeCommerce"
footer: "Lab. de Engenharia de Software"
transition: fade-out
---

# JakeBooks
#### Paixão por Livros e Salsichinhas
###### por: Breno de Oliveira

![bg w:500  right:50%](imgs/logo_real.png)

---
# Entrega 01 – 02/03/2026  
## E-Commerce de Livros: JakeBooks

**Tecnologias Utilizadas**

- **Backend:** Java 21 + Spring Boot  
- **Frontend:** Thymeleaf + Bootstrap  
- **Banco de Dados:** PostgreSQL  
- **Integração de IA:** Microserviço em Python
- **IA no Desenvolvimento:** Projects no ChatGPT  

---
# Estimativa
##### Backend
![alt text](image.png)

---
# Estimativa
##### Frontend + IA

![alt text](image-1.png)


---

# Requisitos Funcionais Sugeridos

| ID        | Nome                              | Descrição                                                                                                                         |
| -------   | --------------------------------- | --------------------------------------------------------------                                                                                                                                          |
| RN0063 | Limite de livros                  | Um cliente pode comprar no máximo 10 unidades do mesmo livro por pedido.                                                          |
| RN0064 | Pedido mínimo                     | O pedido deve possuir valor mínimo de R$ 20,00 (sem frete) para poder ser finalizado.                                             | 
|  RN0065 | Cliente inadimplente              | Cliente que possuir 3 pedidos REPROVADOS consecutivos por pagamento terá o carrinho bloqueado temporariamente.                    |

---
# Fluxo Protótipo
> Catálogo

![alt text](image-8.png)

---
# Fluxo Protótipo
> Carrinho

![alt text](image-4.png)

---
# Fluxo Protótipo
> Checkout

![alt text](image-5.png)

---
# Fluxo Protótipo
> Detalhe do Pedido

![alt text](image-6.png)

---

# Entrega 02 – 23/03/2026
## CRUD de Clientes

**Escopo da entrega:**
- Cadastro, consulta, alteração e inativação de clientes
- Gestão de endereços e cartões
- Alteração de senha com validação
---
# Kanban de Tarefas
![alt text](image-9.png)

---

# Requisitos Funcionais Implementados

| RF | Descrição |
|---|---|
| RF0021 | Cadastrar cliente |
| RF0022 | Alterar cliente |
| RF0023 | Inativar cliente |
| RF0024 | Consultar cliente |
| RF0025 | Consultar transações do cliente |
| RF0026 | Cadastrar múltiplos endereços |
| RF0027 | Cadastrar múltiplos cartões (preferencial) |
| RF0028 | Alterar apenas senha |

---

# Regras de Negócio Contempladas

| RN | Descrição |
|---|---|
| RN0023 | Campos obrigatórios do endereço |
| RN0024 | Campos obrigatórios do cartão |
| RN0025 | Bandeira deve estar cadastrada |
| RN0026 | Dados obrigatórios do cliente |
| RN0027 | Cliente possui ranking numérico |

**Segurança:** Senha forte (8+ chars, maiúsc., minúsc., especiais) + BCrypt


---

# Demonstração
> Lista de Clientes


![bg right 95% fit](image-10.png)

---

# Demonstração
> Detalhe do Cliente


![bg right 95% fit](image-11.png)

---

# Demonstração
> Cadastro de Cliente


![bg right 95% fit](image-13.png)


---
# Demonstração
> Cadastro de Endereço do Cliente


![bg right 95% fit](image-17.png)
![alt text](image-14.png)

---

# Demonstração
> Cadastro de Cartão do Cliente

![bg right 95% fit](image-15.png)
![alt text](image-16.png)

---

# Entrega 03 – 30/03/2026
## Documento de Visão de Projeto

**Escopo da entrega:**
- Formalização do DVP do sistema JakeBooks
- Autor: Breno Oliveira | Revisor: Rodrigo Rocha

---
# Kanban de Tarefas
![alt text](image-18.png)