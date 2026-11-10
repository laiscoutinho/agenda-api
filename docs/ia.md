# Uso de IA

Documento de transparência sobre o uso de inteligência artificial neste projeto (Agenda API, nível B, Full Cycle, Módulo 4).

> Princípio: a IA sugeriu alternativas e gerou rascunhos. As decisões técnicas foram tomadas pela equipe. Nenhum código entrou no repositório sem ser lido, entendido e verificado por testes. Código que não sabemos explicar não entra.

## 1. Ferramentas e quem as usou

| Pessoa | Ferramenta | Como usou |
|---|---|---|
| Laís Coutinho | Claude Opus 5.5 | Inicialização do projeto , configuração da pipiline de CI |
| Rosana Celine | [PREENCHER: nome da ferramenta e modelo, ex.: assistente integrado ao editor] | Rascunhos de testes, implementação, configuração e documentação, sempre revisados |

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

### Prompt 2: workflow do GitHub Actions

- **Data:** 10/11
- **Quem usou:** Laís
- **PR:** https://github.com/laiscoutinho/agenda-api/pull/2
- **Objetivo:** rodar lint, testes e detecção de segredos em toda PR e em push na `main`.
- **Prompt:** (texto como foi enviado. O prompt diz "Spring Boot 3"; o projeto usa Spring Boot 4.1.2, e o pom.xml colado mostrava a versão certa.)

```text
Papel: você é um engenheiro DevOps sênior, rigoroso com segurança de pipeline.

Contexto: projeto Java 21, Spring Boot 3, Maven com Maven Wrapper, PostgreSQL
16. Já existem Dockerfile e docker-compose. O pom.xml está abaixo. O teste
contextLoads gerado pelo Spring precisa de um banco. Decisões da equipe:
Postgres via `services` do GitHub Actions; permissões mínimas; ordem dos
passos do mais barato ao mais caro; segredos nunca no código. Comentários e
nomes em português. [COLAR O pom.xml]

Tarefa: crie .github/workflows/ci.yml com um job de verificação e um job de
detecção de segredos com o gitleaks.

Restrições:
- Gatilhos: pull_request e push na branch main.
- `permissions: contents: read` definido no workflow.
- Java 21 com cache do Maven.
- Ordem no job de verificação: Checkstyle, depois `./mvnw verify`.
- Postgres 16 em `services`, com health-cmd (pg_isready); a conexão do teste
  vem de variáveis de ambiente. A senha do banco de teste pode ficar no YAML
  porque o banco só existe durante o job.
- Dê aos jobs nomes estáveis (serão checks obrigatórios da main).
- Não invente versões: use apenas actions e tags que existem e diga quais
  usou. Não adicione dependências ao pom.xml.

Formato: um bloco de código com o ci.yml, e uma lista curta com a
justificativa de cada decisão em uma frase.
```

- **O que a IA devolveu:** `ci.yml` com os jobs `verificacao` (Postgres 16 em `services`, Checkstyle e depois `./mvnw verify`) e `segredos` (gitleaks em todo o histórico). Actions: `actions/checkout@v7`, `actions/setup-java@v6` e `gitleaks/gitleaks-action@v3`.
- **O que mudei ou rejeitei, e por quê:** [PREENCHER]
- **Verificação feita:** tags `v7`, `v6` e `v3` conferidas nos repositórios oficiais; o gitleaks-action não exige licença para conta pessoal (README da action); comentários do gitleaks na PR desligados para manter `contents: read`; `./mvnw verify` rodado localmente contra um Postgres 16 com as mesmas variáveis do CI; gitleaks local sem vazamentos; CI verde na PR: [PREENCHER com o link da execução].
- **Onde ficou:** `.github/workflows/ci.yml`

### Prompt 3: Checkstyle

- **Data:** 10/11
- **Quem usou:** Laís
- **PR:** https://github.com/laiscoutinho/agenda-api/pull/2
- **Objetivo:** lint enxuto que falhe o build em violação.
- **Prompt:** (texto como foi enviado)

```text
Papel: você é um engenheiro Java que cuida de padronização de código.

Contexto: projeto Maven Java 21. Todo o código será em português. Queremos um
lint enxuto, que não gere centenas de avisos num projeto novo. [COLAR O pom.xml]

Tarefa: configure o maven-checkstyle-plugin no pom.xml e crie
config/checkstyle.xml com regras enxutas: convenção de nomes, imports não
usados, blocos sem chaves, tamanho máximo de método e de linha.

Restrições: o build falha em violação; não use as regras sun_checks nem
google_checks completas; não adicione outras dependências; diga a versão do
plugin que usou.

Formato: o trecho do pom.xml e o checkstyle.xml, cada um em um bloco, e uma
frase por regra explicando o risco que ela evita.
```

- **O que a IA devolveu:** `maven-checkstyle-plugin` 3.6.0 ligado à fase `validate` e `config/checkstyle.xml` com nomes, imports não usados, chaves obrigatórias, método até 40 linhas e linha até 120 caracteres.
- **O que mudei ou rejeitei, e por quê:** [PREENCHER] Ponto de atenção: o plugin 3.6.0 traz o Checkstyle 9.3, que não interpreta toda a sintaxe do Java 21; por isso o plugin declara a dependência `com.puppycrawl.tools:checkstyle:14.3.0`. É uma dependência do plugin, não do projeto, mas foge da restrição "não adicione outras dependências" e precisa de acordo entre nós duas.
- **Verificação feita:** `./mvnw checkstyle:check` passa no código atual; uma classe com import não usado, `if` sem chaves e método `Metodo_Ruim` fez o build falhar com as três violações.
- **Onde ficou:** `pom.xml` e `config/checkstyle.xml`

### Prompt 4: testes de domínio do compromisso (Red)

- **Data:** 10/11
- **Quem usou:** Laís
- **PR:** [PREENCHER com o link da PR]
- **Objetivo:** escrever os testes da entidade `Compromisso` antes da implementação (TDD, etapa Red).
- **Prompt:** texto como foi enviado em [docs/prompts/04-dominio-red.md](prompts/04-dominio-red.md). O pacote base foi preenchido com `com.agenda.agenda_api.dominio` e o texto diz Spring Boot 4, a versão do projeto.
- **O que a IA devolveu:** `CompromissoTest` com 18 testes em AAA. API proposta: `Compromisso.criar(titulo, descricao, inicio, fim)`, `cancelar()`, getters e `getDescricao()` como `Optional<String>`; exceções `TituloObrigatorioExcecao`, `HorarioObrigatorioExcecao`, `FimNaoPosteriorAoInicioExcecao` e `CompromissoJaCanceladoExcecao`, todas filhas de `ExcecaoDeDominio`. Casos de borda além dos pedidos: título nulo, título só com tabulação e quebra de linha, início ou fim nulos, descrição ausente, fim 1 nanossegundo depois do início (fronteira do `<` contra `<=`), ids diferentes em dois compromissos e status mantido como `CANCELADO` após o segundo cancelamento rejeitado.
- **O que mudei ou rejeitei, e por quê:** [PREENCHER]
- **Verificação feita:** os testes não compilavam, e todos os erros eram `cannot find symbol` das classes do domínio que ainda não existiam (nenhum erro de digitação ou de import); valores esperados conferidos como literais; Checkstyle limpo.
- **Onde ficou:** `src/test/java/com/agenda/agenda_api/dominio/CompromissoTest.java`, commit dc4646f.

### Prompt 5: entidade Compromisso e exceções de domínio (Green)

- **Data:** 10/11
- **Quem usou:** Laís
- **PR:** [PREENCHER com o link da PR]
- **Objetivo:** implementar só o necessário para os testes do prompt 4 passarem.
- **Prompt:** texto como foi enviado em [docs/prompts/05-dominio-green.md](prompts/05-dominio-green.md), com o `CompromissoTest.java` colado no lugar do marcador.
- **O que a IA devolveu:** `Compromisso` com construtor privado e fábrica `criar`, que valida título (`null` ou em branco), horários nulos e fim posterior ao início com `isAfter`; `cancelar()` rejeita o segundo cancelamento. `StatusCompromisso`, a classe abstrata `ExcecaoDeDominio` (filha de `RuntimeException`) e as quatro exceções com a mensagem fixa. Nenhum teste foi apontado como errado e nenhum teste foi alterado.
- **O que mudei ou rejeitei, e por quê:** [PREENCHER]
- **Verificação feita:** 18 testes verdes; `grep -rE "springframework|jakarta"` no pacote `dominio` sem resultados; Checkstyle limpo; alteração proposital do código (ver seção 6).
- **Onde ficou:** `src/main/java/com/agenda/agenda_api/dominio/`, commit cfd16dd.

### Prompt 6: cobertura mínima com JaCoCo

- **Data:** 10/11
- **Quem usou:** Laís
- **PR:** [PREENCHER com o link da PR]
- **Objetivo:** falhar o build com cobertura abaixo de 70% em linhas ou em branches.
- **Prompt:** texto como foi enviado em [docs/prompts/06-jacoco.md](prompts/06-jacoco.md), com o pom.xml colado no lugar do marcador.
- **O que a IA devolveu:** `jacoco-maven-plugin` 0.8.16 (a versão mais recente no Maven Central, de 26/10/2026) com `prepare-agent`, `report` e `check` na fase `verify`; regra `BUNDLE` com `LINE` e `BRANCH` em `COVEREDRATIO` mínimo de 0.70. Relatório em `target/site/jacoco/index.html`.
- **O que mudei ou rejeitei, e por quê:** [PREENCHER]
- **Verificação feita:** `./mvnw verify` com Postgres 16 local (mesmas variáveis do CI) passou com 95,7% de linhas (45/47) e 100% de branches (12/12); as 2 linhas sem cobertura são do `main` de `AgendaApiApplication`. Com o mínimo trocado para 0.99 de propósito, o build falhou em `cobertura-minima` ("Coverage checks have not been met"), e o valor voltou para 0.70.
- **Onde ficou:** `pom.xml`, commit 3eb1725.

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
- Suíte validada por alteração proposital no código (`Compromisso`, 10/11), uma alteração por vez, revertida em seguida:
  - fim igual ao início passou a ser aceito (`!fim.isAfter(inicio)` trocado por `fim.isBefore(inicio)`, o equivalente a trocar `<` por `<=`): 1 teste falhou, `fimIgualAoInicioERejeitado`;
  - validação do título apagada: 5 testes falharam (título vazio, só espaços, só tabulação e quebra de linha, nulo, e a herança de `ExcecaoDeDominio`);
  - checagem de "já cancelado" apagada do `cancelar()`: 1 teste falhou, `cancelarDuasVezesERejeitado`.
  Com o código original, os 18 testes voltaram a passar.
- Checagem de testes tautológicos: em `CompromissoTest`, todo valor esperado é literal (textos, `Instant.parse("...")` e constantes do enum), e nenhum é calculado pela lógica da entidade. As mensagens das exceções estão escritas por extenso no teste, em vez de lidas da própria classe. A única comparação que não usa literal é a de ids diferentes, que compara dois valores gerados, sem prever o valor. [PREENCHER: confirme que você conferiu e se rejeitou algum teste]
- Checklist do revisor humano em cada PR: o autor explica o código linha a linha? A complexidade é adequada? Segue os padrões do projeto? Algum trecho parece copiado de outro lugar?

## 7. Limitações e riscos reconhecidos

- A IA pode produzir código convincente porém incorreto, inventar métodos ou dependências, e gerar testes que passam sempre.
- A IA não executa o código que escreve. A verificação é nossa.
- A responsabilidade técnica pelo que está no repositório é da equipe.

## 8. Referências cruzadas

- Decisões e alternativas descartadas: [ADR 0001](adr/0001-decisao.md)
- Quem fez o quê: [CONTRIBUTORS.md](../CONTRIBUTORS.md)