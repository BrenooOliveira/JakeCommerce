Objetivo: Consolidar a estrutura de pacotes eliminando duplicidades e inconsistências.
Contexto: O projeto tem dois pacotes de serviço (service e services), entidades de domínio em domain e enums em model/enums. Isso causa confusão na navegação e manutenção.
Tarefa:
Refatore a estrutura de pacotes para o seguinte padrão:

com.les.jakebooks
├── config/          (sem alteração)
├── controller/      (sem alteração)
├── domain/          (entidades JPA permanecem aqui)
│   └── enums/       ← mover de model/enums para domain/enums
├── dto/             (sem alteração)
├── exception/       (sem alteração)
├── interceptor/     (sem alteração)
├── repository/      (sem alteração)
├── service/         ← unificar service + services aqui
├── util/            (sem alteração)
└── validator/       (sem alteração)

Regras:

- Mover todos os arquivos de services/ para service/
- Mover todos os enums de model/enums/ para domain/enums/
- Atualizar todos os import em todo o projeto
- Deletar os pacotes vazios services/ e model/
- Não alterar lógica de nenhuma classe, apenas imports e declarações de pacote

Validação: O projeto deve compilar sem erros após a refatoração.