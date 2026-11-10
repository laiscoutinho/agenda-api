# Uso de IA

Documento de transparência sobre o uso de inteligência artificial neste projeto (Agenda API, nível B, Full Cycle, Módulo 4).

> Princípio: a IA sugeriu alternativas e gerou rascunhos. As decisões técnicas foram tomadas pela equipe. Nenhum código entrou no repositório sem ser lido, entendido e verificado por testes. Código que não sabemos explicar não entra.

## 1. Ferramentas e quem as usou

| Pessoa | Ferramenta | Como usou |
|---|---|---|
| Laís Coutinho | [PREENCHER: nome da ferramenta e modelo, ex.: assistente integrado ao editor] | Rascunhos de testes, implementação, configuração e documentação, sempre revisados |
| Rosana Celine | [PREENCHER: ferramenta ou "não usou IA"] | [PREENCHER com o que de fato fez] |

Cada pessoa registra os próprios prompts na seção 5. O campo "Quem usou" identifica a autoria.

## 2. Como trabalhamos com a IA

Ciclo adotado em todo uso:

1. **Contexto:** fornecer à IA as interfaces, as regras e os testes que já existem.
2. **Rascunho:** pedir **uma única mudança** por prompt.
3. **Verificação:** conferir contra os testes, contra o padrão do projeto e contra a nossa compreensão. Se não conseguimos explicar uma linha, ela não entra.

Estrutura de cada prompt: **Papel, Contexto, Tarefa, Restrições, Formato**. Quando a resposta precisava seguir um padrão, usamos exemplos. Pedimos **justificativa das decisões** em vez do raciocínio cru, porque a explicação escrita pelo modelo não prova que o código está correto. A prova vem dos testes e da leitura humana.

## 3. Regras de uso que adotamos

- **Dados:** nunca enviar `.env`, senhas, tokens ou dados pessoais. Usar placeholders.
- **Dependências:** a IA pode inventar métodos, classes e dependências Maven inexistentes. Todo import ou dependência sugerido foi conferido na documentação oficial antes de ser adicionado. Não adicionamos dependências novas sem acordo entre as duas.
- **Licenças:** trechos longos e muito específicos sugeridos pela IA foram buscados na internet antes de serem mantidos.
- **Revisão:** a IA pode comentar uma PR, mas **não aprova**. A aprovação é humana.
- **Testes:** a IA não pode alterar um teste que falha apenas para ele passar. Mudar teste é mudar o contrato e exige decisão nossa.

## 4. Decisões tomadas pela equipe

Estas decisões foram tomadas por nós, não delegadas à IA. A IA foi usada, no máximo, para comparar alternativas.

| # | Decisão | O que escolhemos | Alternativa descartada | Onde está registrado |
|---|---|---|---|---|
| 1 | Arquitetura | Camadas simples da Arquitetura Limpa: domínio, aplicação, infraestrutura e web. O domínio não importa Spring nem JPA. | Tudo junto nos controladores | [ADR 0001](adr/0001-decisao.md) |
| 2 | Pattern justificado | Repository com injeção por construtor (inversão de dependência). Regras testadas com repositório em memória, sem banco. | Chamar o JPA direto nos controladores | [ADR 0001](adr/0001-decisao.md) |
| 3 | Regras de negócio | Título obrigatório; fim posterior ao início; sem sobreposição de horários; não cancelar duas vezes | N/A | README e testes de domínio |
| 4 | Método de testes | TDD no domínio (Red, Green, Refactor), padrão AAA, muitos testes unitários e 1 de integração com Postgres real | Testar apenas pela API | `src/test` |
| 5 | Casos de borda | Título vazio; fim igual ou anterior ao início; compromisso que termina exatamente quando outro começa (não é conflito); cancelar duas vezes; id inexistente | N/A | testes de domínio |
| 6 | Tratamento de erros | Exceções de domínio nomeadas, sem retornar `null` | Retornar `null` ou exceção genérica | pacote de exceções do domínio |
| 7 | Coverage | JaCoCo com mínimo de 70% em linhas e branches, falhando o build abaixo disso. A suíte também é validada por alteração proposital no código (trocar `<` por `<=`) para confirmar que algum teste falha. | Confiar somente no percentual do relatório | `pom.xml` e CI |
| 8 | CI | GitHub Actions: Java 21 com cache do Maven; Checkstyle, depois testes e JaCoCo; `permissions: contents: read`; Postgres via `services`; gitleaks | Análise estática com Semgrep, fora do escopo do nível B | `.github/workflows/ci.yml` |
| 9 | Processo | PR obrigatória na `main`, revisada pela outra integrante | Push direto na `main` | Proteção de branch |
| 10 | Segurança nos prompts | Nunca colar `.env`, senhas ou tokens | N/A | Seção 3 deste documento |
| 11 | Stack | Java 21, Spring Boot 4 e Maven. Motivo: conhecimento técnico mais aprofundado em Java, o que permite implementar o sistema no prazo e explicar o código. Os exemplos de ferramentas do PDF são de Node, mas são apenas exemplos. | Node.js com TypeScript e Express (exemplos usados no curso) | README |
| 12 | Banco | PostgreSQL, igual no Compose, no CI e nos testes de integração | SQLite | `docker-compose.yml` |
| 13 | Acesso a dados | Spring Data JPA (Hibernate), com entidade de domínio separada da entidade JPA e um mapeador entre elas | `JdbcTemplate` (SQL puro, sem tipagem) | [ADR 0001](adr/0001-decisao.md) |
| 14 | Migrações | Flyway, com migrações SQL versionadas no repositório | `ddl-auto=update` (sem histórico, banco pode divergir entre ambientes) | `src/main/resources/db/migration` |
| 15 | Onde validar | Bean Validation (`@Valid`) nos DTOs de entrada; regras de negócio apenas no domínio | Validar nos controladores | camada web |
| 16 | Datas | ISO 8601 em UTC na API, `timestamptz` no banco | Horário local sem fuso | contrato da API |
| 17 | Identificador | UUID gerado pela aplicação | Inteiro autoincrementado | domínio e migração |
| 18 | Cancelamento | Muda o status para `CANCELADO`; não existe `DELETE`, para preservar o histórico | Exclusão física | contrato da API |
| 19 | Edição (`PUT`) | Revalida a sobreposição ignorando o próprio compromisso; não permite editar um compromisso cancelado | Permitir editar sem revalidar | caso de uso de edição |
| 20 | Erros HTTP | 400 dados inválidos, 404 não encontrado, 409 conflito de horário, corpo JSON padronizado com `erro` e `mensagem` | Responder sempre 400 ou 500 | contrato da API |
| 21 | Escopo e execução | Sem autenticação, sem paginação e sem hospedagem: cada pessoa executa a API localmente com Docker, pois uma API sem autenticação e com banco não deve ficar exposta na internet. Autenticação (JWT) fica como evolução futura, inclusive para reuso da API em outros projetos. | Spring Security com JWT agora | README, seção de limitações |
| 22 | Docker | Build em estágios (Maven no build, JRE 21 na execução), usuário não-root; no Compose a API só sobe com o banco saudável (`healthcheck`) | Imagem única com JDK e Maven | `Dockerfile` e `docker-compose.yml` |
| 23 | Configuração | Propriedades validadas na inicialização (`@ConfigurationProperties` com `@Validated`), falhando cedo; `.env.example` sem valores reais | `@Value` solto pelo código | `.env.example` |
| 24 | Observabilidade | Actuator e Micrometer (Prometheus) com duração HTTP por rota e status; logs JSON com id da requisição | Só logs de texto | README |
| 25 | SLO | Medir primeiro com o k6 e só depois declarar a meta; os 300 ms do README são provisórios | Declarar um número antes de medir | [desempenho](desempenho.md) |
| 26 | Idioma | Todo o código em português: classes, métodos, variáveis, pacotes, rotas, campos JSON e mensagens de erro. Documentação e comentários também em português. Exceções inevitáveis: palavras reservadas do Java, nomes de bibliotecas e anotações do Spring, e as tags de tipo dos commits (`feat`, `fix`, `test`, `docs`, `ci`, `chore`). | Código em inglês com contrato em português, ou tudo em inglês | README |
| 27 | Commits e PRs | Tag de tipo seguida de descrição em português, por exemplo `feat: adiciona validação de sobreposição de horários`. Corpo só quando há coautoria, com o trailer `Co-authored-by`. PRs pequenas, uma mudança por branch. | Commits grandes direto na `main` | histórico do Git |
| 28 | Clean Code | Nomes que revelam a intenção, métodos curtos com uma responsabilidade, sem retornar `null`, princípios SOLID e a Regra do Escoteiro em cada PR | Sem padrão definido de código | revisão de PR |

**Como as decisões foram tomadas:** [PREENCHER COM A VERDADE: ex.: "As decisões acima foram discutidas e confirmadas por Laís e Rosana em DD/MM, por [chamada / mensagem / presencialmente]". Preencha somente se a Rosana realmente participou e confirmou.]

## 5. Registro de prompts

Os prompts são registrados **durante o desenvolvimento**, na mesma PR em que o código gerado com ajuda da IA entra. Só entram prompts que realmente foram usados e que importaram, com o texto como foi enviado.

### Prompt 1: infraestrutura de execução com Docker

- **Data:** 10/11
- **Quem usou:** Laís
- **PR:** https://github.com/laiscoutinho/agenda-api/pull/1
- **Objetivo:** subir a API e o PostgreSQL localmente com Docker, sem segredos no código.
- **Prompt:** (texto como foi enviado; o pom.xml foi colado no lugar do marcador. O prompt diz "Spring Boot 3", mas o projeto gerado usa Spring Boot 4.1.2; a documentação foi corrigida nesta PR.)

```text
Papel: você é um engenheiro DevOps/backend sênior, cuidadoso com segurança
e com builds reproduzíveis.

Contexto: projeto Java 21 com Spring Boot 3, Maven (com Maven Wrapper) e
PostgreSQL 16, gerado no start.spring.io com as dependências Web, Validation,
Data JPA, driver do PostgreSQL, Flyway, Actuator e Prometheus. O pom.xml
está abaixo. A API ainda não tem regra de negócio, só a estrutura. Decisões
da equipe: tudo roda localmente com Docker; sem autenticação; build em
estágios; usuário não-root; a API só sobe com o banco saudável; segredos
nunca no código. Todos os comentários e a documentação em português.
[COLAR AQUI O pom.xml]

Tarefa: crie a infraestrutura de execução: Dockerfile, docker-compose.yml,
.env.example e src/main/resources/application.properties.

Restrições:
- Dockerfile em dois estágios: build com Maven (aproveitando o cache de
  dependências, copiando o pom.xml antes do código) e execução com uma
  imagem JRE 21 oficial. Rodar como usuário não-root. O build da imagem não
  executa os testes, porque o CI já os executa.
- docker-compose.yml com os serviços "db" (PostgreSQL 16 com healthcheck
  usando pg_isready e volume para os dados) e "api" (depends_on com
  condition: service_healthy, porta 8080). Credenciais vindas do arquivo
  .env, nunca escritas no compose.
- .env.example apenas com placeholders, sem nenhum valor real. Use os nomes
  de variável exigidos pela imagem do PostgreSQL e pelo Spring.
- application.properties lê a conexão do banco só de variáveis de ambiente,
  sem senha padrão. Flyway habilitado e Hibernate com ddl-auto=none: o
  esquema virá só das migrações. Exponha no Actuator apenas health e
  prometheus.
- Não adicione dependências nem altere o pom.xml. Não invente versões de
  imagem: use apenas tags que existem nos repositórios oficiais e diga quais
  você usou.
- Não use ddl-auto=update.

Formato: um bloco de código por arquivo, na ordem Dockerfile,
docker-compose.yml, .env.example, application.properties. Depois, uma lista
curta com a justificativa de cada decisão em uma frase, e os comandos para
subir e testar com curl no /actuator/health.
```

- **O que a IA devolveu:** Dockerfile em dois estágios, docker-compose.yml com `db` e `api`, `.env.example` com placeholders e `application.properties` lendo a conexão de variáveis de ambiente.
- **O que mudei ou rejeitei, e por quê:** [PREENCHER]
- **Verificação feita:** leitura linha a linha; tags `postgres:16`, `eclipse-temurin:21-jdk` e `eclipse-temurin:21-jre` conferidas no Docker Hub [CONFIRMAR]; `docker compose up --build` com o banco saudável e `curl http://localhost:8080/actuator/health` respondendo `UP`.
- **Onde ficou:** `Dockerfile`, `docker-compose.yml`, `.env.example`, `.dockerignore` e `src/main/resources/application.properties`, commit 3ab1fae.

Modelo de entrada:

### Prompt N: título curto

- **Data:** DD/MM
- **Quem usou:** Laís / Rosana
- **PR:** link da PR em que o resultado entrou
- **Objetivo:** o que se queria resolver
- **Prompt:**

```text
Papel:
Contexto:
Tarefa:
Restrições:
Formato:
```

- **O que a IA devolveu:** resumo em 1 ou 2 linhas
- **O que mudei ou rejeitei, e por quê:**
- **Verificação feita:** testes, leitura, conferência na documentação oficial
- **Onde ficou:** arquivo e commit

## 6. Como validamos o que a IA produziu

- Toda saída foi lida linha a linha antes do commit.
- Imports e dependências conferidos na documentação oficial.
- Testes rodados localmente e no CI.
- Suíte validada por alteração proposital no código: [PREENCHER com o que foi feito e o resultado].
- Checagem de testes tautológicos: [PREENCHER].
- Checklist do revisor humano em cada PR: o autor explica o código linha a linha? A complexidade é adequada? Segue os padrões do projeto? Algum trecho parece copiado de outro lugar?

## 7. Limitações e riscos reconhecidos

- A IA pode produzir código convincente porém incorreto, inventar métodos ou dependências, e gerar testes que passam sempre.
- A IA não executa o código que escreve. A verificação é nossa.
- A responsabilidade técnica pelo que está no repositório é da equipe.

## 8. Referências cruzadas

- Decisões e alternativas descartadas: [ADR 0001](adr/0001-decisao.md)
- Quem fez o quê: [CONTRIBUTORS.md](../CONTRIBUTORS.md)