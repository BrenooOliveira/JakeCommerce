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


