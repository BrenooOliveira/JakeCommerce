# 📚 JakeBooks - E-Commerce para Venda de Livros

**Versão**: 1.0.0-SNAPSHOT  
**Data**: Março de 2026  
**Status**: ✅ Pronto para Desenvolvimento

---

## 📖 Visão Geral do Sistema

JakeBooks é um **sistema de e-commerce completo** para venda de livros desenvolvido como trabalho acadêmico na disciplina de **Engenharia de Software (LES)**.

### Características Principais

- ✅ **Catálogo de Livros** com múltiplas categorias, autores e editoras
- ✅ **Carrinho de Compras** com validação de estoque em tempo real
- ✅ **Processamento de Pedidos** com múltiplos pagamentos (cartão + cupom)
- ✅ **Controle de Estoque** com entrada/saída automática
- ✅ **Gerenciamento de Clientes** com múltiplos endereços e cartões
- ✅ **Sistema de Trocas** de produtos com geração de cupons
- ✅ **Painel Administrativo** para gestão completa
- ✅ **Análise de Vendas** com gráficos e comparações
- ✅ **Segurança** com Spring Security e criptografia BCrypt

---

## 🔧 Pré-Requisitos

Antes de iniciar, certifique-se de ter instalado:

### Obrigatório
| Componente | Versão | Instalação |
|-----------|--------|-----------|
| **Java (JDK)** | 21+ | [Download](https://jdk.java.net/21/) |
| **PostgreSQL** | 12+ | [Download](https://www.postgresql.org/download/) |
| **Maven** | 3.8+ | [Download](https://maven.apache.org/download.cgi) |

### Recomendado
- IDE: [IntelliJ IDEA](https://www.jetbrains.com/idea/) ou [VSCode](https://code.visualstudio.com/)
- Git: para controle de versão
- Postman: para testar APIs (se necessário)

---

## 🗄️ Configuração do Banco de Dados

### 1. Criar Banco de Dados

```sql
-- Conectar como superusuário postgres
psql -U postgres

-- Criar banco de dados
CREATE DATABASE jakebooks ENCODING 'UTF8';

-- Verificar criação
\l
```

### 2. Configurar Credenciais (`.env` ou variáveis de ambiente)

```bash
# Linux/Mac
export DB_HOST=localhost
export DB_PORT=5432
export DB_NAME=jakebooks
export DB_USER=postgres
export DB_PASSWORD=postgres

# Windows (Command Prompt)
set DB_HOST=localhost
set DB_PORT=5432
set DB_NAME=jakebooks
set DB_USER=postgres
set DB_PASSWORD=postgres

# Windows (PowerShell)
$env:DB_HOST="localhost"
$env:DB_PORT="5432"
$env:DB_NAME="jakebooks"
$env:DB_USER="postgres"
$env:DB_PASSWORD="postgres"
```

### 3. Verificar Conexão

```bash
psql -U postgres -h localhost -d jakebooks
```

Se conectar com sucesso, você verá o prompt `jakebooks=#`

### 4. Schema e Dados Iniciais

O schema é criado automaticamente na primeira execução via `schema.sql` e `data.sql`.

---

## 🚀 Como Executar

### Via Maven (Recomendado)

```bash
# 1. Clonar repositório
git clone https://github.com/seu-usuario/jakebooks.git
cd jakebooks

# 2. Instalar dependências
./mvnw clean install

# 3. Executar aplicação
./mvnw spring-boot:run

# Ou em Windows
mvnw.cmd spring-boot:run
```

### Via IDE (IntelliJ IDEA)

1. Abrir projeto em File → Open
2. Aguardar indexação de dependências
3. Executar: `JakebooksApplication` (Run → Run 'JakebooksApplication')

### Via Linha de Comando (Jar)

```bash
# Build
./mvnw clean package

# Executar
java -jar target/jakebooks-0.0.1-SNAPSHOT.jar
```

---

## ✅ Verificar Execução

Após iniciar, acesse:

```
http://localhost:8080
```

Você verá a página inicial com:
- ✅ Lista de livros disponíveis
- ✅ Opções de login
- ✅ Cadastro de cliente

**Logs esperados**:
```
[INFO] Tomcat started on port(s): 8080 (http)
[INFO] Started JakebooksApplication in X seconds
```

---

## 📁 Estrutura de Pacotes

```
src/main/java/com/les/jakebooks/
├── JakebooksApplication.java          # Classe principal
├── config/                             # Configurações da aplicação
│   ├── SecurityConfig.java             # Spring Security
│   ├── CustomUserDetailsService.java   # Autenticação customizada
│   └── WebMvcConfig.java               # Configuração MVC
├── controller/                         # Controllers (apresentação)
│   ├── LivroController.java
│   ├── ClienteController.java
│   ├── CarrinhoController.java
│   ├── PedidoController.java
│   ├── EstoqueController.java
│   ├── TrocaController.java
│   └── AnaliseController.java
├── service/                            # Business logic (obsoleto - usar services)
├── services/                           # Services (regras de negócio)
│   ├── LivroService.java
│   ├── ClienteService.java
│   ├── CarrinhoService.java
│   ├── PedidoService.java
│   ├── EstoqueService.java
│   ├── TrocaService.java
│   └── AnaliseService.java
├── repository/                         # Data access layer
│   ├── LivroRepository.java
│   ├── ClienteRepository.java
│   ├── CarrinhoRepository.java
│   ├── PedidoRepository.java
│   └── ... (outros repositories)
├── domain/                             # Entidades JPA
│   ├── Livro.java
│   ├── Cliente.java
│   ├── Carrinho.java
│   ├── Pedido.java
│   ├── Estoque.java
│   ├── Pagamento.java
│   ├── Troca.java
│   └── ... (outras entidades)
├── dto/                                # Data Transfer Objects
│   ├── LivroDetalheDTO.java
│   ├── ClienteDetalheDTO.java
│   ├── PedidoConfirmadoDTO.java
│   └── ... (outros DTOs)
├── exception/                          # Exceções customizadas
│   ├── NegocioException.java
│   ├── ValidacaoNegocioException.java
│   ├── EstoqueInsuficienteException.java
│   └── ... (outras exceções)
├── validator/                          # Validadores de regras
│   ├── PagamentoValidator.java
│   └── ClienteValidator.java
├── util/                               # Utilitários
│   └── CriptografiaUtil.java
├── interceptor/                        # Interceptadores HTTP
│   └── LoggingInterceptor.java
├── model/                              # Enums e modelos
│   └── enums/
│       ├── StatusLivro.java
│       ├── StatusCliente.java
│       ├── StatusPedido.java
│       └── ... (outros enums)
└── exception/
    └── GlobalExceptionHandler.java     # Manipulador global de exceções
```

### Resources

```
src/main/resources/
├── application.properties              # Configurações da aplicação
├── schema.sql                          # Criação do schema
├── data.sql                            # Dados iniciais
├── templates/                          # Templates Thymeleaf
│   ├── index.html
│   ├── livros/
│   │   ├── lista.html
│   │   ├── detalhe.html
│   │   └── form.html
│   ├── clientes/
│   │   ├── lista.html
│   │   ├── detalhe.html
│   │   ├── form-cadastro.html
│   │   ├── form-edicao.html
│   │   ├── form-endereco.html
│   │   └── form-cartao.html
│   ├── carrinho/
│   │   ├── view.html
│   │   └── checkout.html
│   ├── pedidos/
│   │   ├── lista.html
│   │   └── detalhe.html
│   ├── estoque/
│   │   ├── lista.html
│   │   └── form-entrada.html
│   ├── trocas/
│   │   ├── lista.html
│   │   ├── detalhe.html
│   │   └── solicitar.html
│   ├── analise/
│   │   └── dashboard.html
│   ├── login/
│   │   └── form.html
│   ├── error/
│   │   ├── 403.html
│   │   ├── 404.html
│   │   └── 500.html
│   └── fragments/
│       ├── layout.html                 # Layout base
│       ├── navbar.html
│       ├── sidebar.html
│       ├── footer.html
│       ├── messages.html
│       ├── pagination.html
│       └── components.html
└── static/
    ├── css/
    │   └── style.css
    └── js/
        └── main.js
```

---

## 🔐 Credenciais de Acesso Padrão

### Cliente de Teste

```
Email:    cliente@teste.com
Senha:    ClienteTeste@123
Código:   CLT0001
Perfil:   CLIENTE
```

### Administrador

```
Email:    admin@jakebooks.com
Senha:    Admin@123456
Código:   ADM0001
Perfil:   ADMIN
```

### Para Acessar

1. Ir para: `http://localhost:8080/login`
2. Usar credenciais acima
3. Click em "Login"

### Padrão de Senhas

- **Mínimo 8 caracteres**
- **Maiúsculas + Minúsculas + Números + Caracteres especiais (@$!%*?&)**
- **Exemplo válido**: `ClienteTeste@123`, `MinhaSenh@456`, `Livros2026!`

---

## 🗺️ Mapa de URLs Principais

### 📖 Público (sem login)

| URL | Método | Descrição |
|-----|--------|-----------|
| `/` | GET | Página inicial |
| `/livros` | GET | Listar livros com filtros |
| `/livros/{codigo}` | GET | Detalhe do livro |
| `/login` | GET | Formulário de login |
| `/clientes/novo` | GET | Cadastro de cliente |

### 🛒 Cliente Autenticado

| URL | Método | Descrição |
|-----|--------|-----------|
| `/carrinho` | GET | Visualizar carrinho |
| `/carrinho/adicionar` | POST | Adicionar item |
| `/carrinho/remover/{id}` | POST | Remover item |
| `/carrinho/checkout` | GET | Checkout |
| `/carrinho/finalizar` | POST | Finalizar compra |
| `/pedidos` | GET | Meus pedidos |
| `/pedidos/{id}` | GET | Detalhe do pedido |
| `/trocas/solicitar/{pedidoId}` | GET | Solicitar troca |
| `/clientes/{codigo}` | GET | Perfil do cliente |
| `/clientes/{codigo}/editar` | GET | Editar cliente |
| `/clientes/{codigo}/endereco` | GET | Gerenciar endereços |
| `/clientes/{codigo}/cartao` | GET | Gerenciar cartões |
| `/logout` | GET | Sair |

### 🔧 Administrativo (requer ROLE_ADMIN)

| URL | Método | Descrição |
|-----|--------|-----------|
| `/livros` | POST | Criar livro |
| `/livros/{codigo}/editar` | GET | Editar livro |
| `/livros/{codigo}` | POST | Atualizar livro |
| `/livros/{codigo}/inativar` | POST | Inativar livro |
| `/clientes` | GET | Listar clientes |
| `/clientes/{codigo}` | GET | Detalhe do cliente |
| `/estoque` | GET | Listar estoque |
| `/estoque/entrada` | GET | Formulário entrada |
| `/estoque/entrada` | POST | Registrar entrada |
| `/pedidos` | GET | Listar pedidos |
| `/pedidos/{id}` | GET | Detalhe do pedido |
| `/pedidos/{id}/despachar` | POST | Marcar como EM_TRANSPORTE |
| `/pedidos/{id}/entregar` | POST | Marcar como ENTREGUE |
| `/trocas` | GET | Listar trocas |
| `/trocas/{id}` | GET | Detalhe da troca |
| `/trocas/{id}/autorizar` | POST | Autorizar troca |
| `/trocas/{id}/receber` | POST | Confirmar recebimento |
| `/analise` | GET | Dashboard de análises |

---

## 📊 Variáveis de Ambiente

Configure as seguintes variáveis para customizar a aplicação:

```properties
# Banco de Dados
DB_HOST=localhost          # Host do PostgreSQL
DB_PORT=5432              # Porta do PostgreSQL
DB_NAME=jakebooks         # Nome do banco
DB_USER=postgres          # Usuário PostgreSQL
DB_PASSWORD=postgres      # Senha PostgreSQL

# Servidor
SERVER_PORT=8080          # Porta da aplicação (padrão: 8080)
SERVER_TIMEOUT=30m        # Timeout da sessão

# Logging
LOG_LEVEL=INFO            # Nível de log (DEBUG, INFO, WARN, ERROR)
```

Elas já estão no `application.properties` com valores padrão.

---

## 🐛 Resolução de Problemas

### Erro: "Connection refused" para PostgreSQL

```
Solução:
1. Verificar se PostgreSQL está rodando
   - Linux: sudo systemctl status postgresql
   - Mac: brew services list
   - Windows: Services > PostgreSQL

2. Verificar variáveis de ambiente
   - Confirmar DB_HOST, DB_PORT, DB_USER, DB_PASSWORD

3. Testar conexão
   psql -U postgres -h localhost -d jakebooks
```

### Erro: "Cannot find JDK 21"

```
Solução:
1. Instalar JDK 21
   https://jdk.java.net/21/

2. Configurar variável JAVA_HOME
   - Linux/Mac: export JAVA_HOME=/path/to/jdk21
   - Windows: set JAVA_HOME=C:\path\to\jdk21

3. Verificar
   java -version
```

### Erro: "Maven not found"

```
Solução:
1. Instalar Maven
   https://maven.apache.org/download.cgi

2. Configurar PATH
   - Adicionar bin do Maven ao PATH

3. Verificar
   mvn -v
```

### Erro de Compilação: "Unresolved dependencies"

```
Solução:
./mvnw clean install -U
```

---

## 📝 Logs da Aplicação

Os logs são exibidos no console e salvos em:
- **Console**: saída padrão ao executar
- **Arquivo**: `logs/jakebooks.log` (configurável)

Exemplo de log com sucesso:
```
[2026-03-09 10:30:45.123] INFO  com.les.jakebooks.JakebooksApplication : Started JakebooksApplication in 5.234 seconds
[2026-03-09 10:30:45.234] INFO  org.springframework.boot.actuate.endpoint.web.EndpointMediaTypes : Registered endpoints with 15 handlers
```

---

## 🚦 Status da Aplicação

### Build
```bash
./mvnw clean compile
```

### Testes
```bash
./mvnw test
```

### Cobertura
```bash
./mvnw clean verify
```

---

## 📚 Documentação Adicional

Consulte os seguintes arquivos para mais detalhes:

- **[AGENTS.md](./AGENTS.md)** - Especificação técnica completa
- **[BUSINESS-RULES-GUIDE.md](./BUSINESS-RULES-GUIDE.md)** - Guia de regras de negócio
- **[FRONTEND-GUIDE.md](./FRONTEND-GUIDE.md)** - Guia de desenvolvimento frontend
- **[QUICK-REFERENCE.md](./QUICK-REFERENCE.md)** - Referência rápida

---

## 📞 Suporte

Para dúvidas ou problemas:

1. Consulte a documentação em `/docs`
2. Abra uma issue no repositório
3. Entre em contato pelo email do projeto

---

## 📄 Licença

Este projeto é desenvolvido como trabalho acadêmico para a disciplina de **Engenharia de Software (LES)**.

---

## ✅ Checklist de Início Rápido

- [ ] Java 21 instalado e configurado
- [ ] PostgreSQL instalado e rodando
- [ ] Banco de dados `jakebooks` criado
- [ ] Variáveis de ambiente configuradas
- [ ] Maven instalado
- [ ] Repositório clonado
- [ ] `./mvnw clean install` executado com sucesso
- [ ] `./mvnw spring-boot:run` iniciado
- [ ] `http://localhost:8080` acessível
- [ ] Login com admin@jakebooks.com / Admin@123456 funcionando

---

**Desenvolvido com ❤️ para LES - 2026**
