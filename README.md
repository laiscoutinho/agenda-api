**Nível: B (simples)**

# Agenda API

Projeto final do curso **Full Cycle**, Módulo 4. API REST de domínio único para gerenciar uma agenda de compromissos, com testes, CI, observabilidade básica e documentação.

> Objetivo do projeto: mostrar engenharia, não tamanho. O fluxo principal funciona de ponta a ponta e está coberto por testes, CI e medição de desempenho.

## Equipe

| Nome | GitHub | Parte do sistema |
|---|---|---|
| Laís Coutinho | [@laiscoutinho](https://github.com/laiscoutinho) | Domínio, casos de uso, persistência, API web, observabilidade e desempenho |
| Rosana Celine | [@RosanaCeline](https://github.com/RosanaCeline) | A DEFINIR (conforme CONTRIBUTORS.md) |

Detalhes de quem fez o quê em [CONTRIBUTORS.md](CONTRIBUTORS.md).

## O que a API faz

Permite criar e gerenciar compromissos de uma agenda.

**Fluxo principal:** criar compromisso → listar/consultar → atualizar → cancelar.

### Endpoints

| Método | Rota | Descrição |
|---|---|---|
| POST | `/compromissos` | Cria um compromisso |
| GET | `/compromissos` | Lista compromissos, ordenados pelo início (filtro opcional `?data=AAAA-MM-DD`, em UTC) |
| GET | `/compromissos/{id}` | Consulta um compromisso |
| PUT | `/compromissos/{id}` | Atualiza um compromisso |
| PATCH | `/compromissos/{id}/cancelar` | Cancela um compromisso |
| GET | `/actuator/health` | Verifica se a API está no ar |
| GET | `/actuator/prometheus` | Métricas no formato Prometheus |

A descrição completa está em [docs/openapi.yaml](docs/openapi.yaml). Datas e horas vão em ISO 8601, em UTC.

Exemplo:

```bash
curl -i -X POST http://localhost:8080/compromissos \
  -H 'Content-Type: application/json' \
  -d '{"titulo": "Dentista", "inicio": "2026-11-10T14:00:00Z", "fim": "2026-11-10T15:00:00Z"}'
```

### Regras de negócio

- O título é obrigatório.
- O horário de fim deve ser posterior ao de início.
- Não pode haver dois compromissos ativos com horários sobrepostos. Um compromisso que termina exatamente quando outro começa não conflita.
- Um compromisso já cancelado não pode ser cancelado de novo nem editado.
- Não existe exclusão: cancelar muda o status para `CANCELADO`.

### Respostas de erro

| Código | `erro` | Quando |
|---|---|---|
| 400 | `DADOS_INVALIDOS` | Título vazio, fim antes do início, horário ausente, JSON ou data mal formados, id que não é UUID |
| 404 | `NAO_ENCONTRADO` | Compromisso não encontrado |
| 409 | `CONFLITO_DE_HORARIO` | Sobreposição com outro compromisso ativo |
| 409 | `COMPROMISSO_CANCELADO` | Cancelar de novo ou editar um compromisso cancelado |

O corpo do erro é JSON com os campos `erro` (código estável) e `mensagem` (texto em português), por exemplo:

```json
{ "erro": "CONFLITO_DE_HORARIO", "mensagem": "Já existe um compromisso ativo nesse horário." }
```

## Tecnologias

| Área | Tecnologia |
|---|---|
| Linguagem e build | Java 21 e Maven |
| Framework | Spring Boot 4 |
| Banco de dados | PostgreSQL |
| Acesso a dados | Spring Data JPA (Hibernate) |
| Migrações | Flyway |
| Validação | Bean Validation |
| Containers | Docker e Docker Compose |
| Testes | JUnit 6, AssertJ e Spring Boot Test |
| Coverage | JaCoCo |
| Lint | Checkstyle |
| CI | GitHub Actions |
| Segredos | variáveis de ambiente e gitleaks |
| Logs e métricas | logs JSON, Actuator e Micrometer (Prometheus) |
| Documentação da API | OpenAPI (validada com Spectral) |
| Desempenho | k6 |
| Decisões | ADR (template MADR) |

## Como subir

Pré-requisitos: Docker e Docker Compose.

```bash
git clone https://github.com/laiscoutinho/agenda-api.git
cd agenda-api
cp .env.example .env
docker compose up --build
```

A API fica disponível em `http://localhost:8080`. Teste com:

```bash
curl http://localhost:8080/actuator/health
```

## Como testar

Pré-requisitos: Java 21 e Docker.

Lint, sem precisar de banco:

```bash
./mvnw checkstyle:check
```

Testes: os de integração precisam de um PostgreSQL. Suba um descartável e aponte as variáveis para ele:

```bash
docker run -d --rm --name agenda-db-teste -e POSTGRES_DB=agenda_teste \
  -e POSTGRES_USER=agenda -e POSTGRES_PASSWORD=agenda_ci -p 5432:5432 postgres:16
export SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/agenda_teste
export SPRING_DATASOURCE_USERNAME=agenda SPRING_DATASOURCE_PASSWORD=agenda_ci
./mvnw verify
```

- **Unitários (49):** regras do domínio e casos de uso (45 testes, com repositório em memória e sem Spring: `CompromissoTest`, `EdicaoDeCompromissoTest`, `ServicoDeCompromissosTest` e outros) e o filtro do id da requisição (4).
- **Integração com PostgreSQL real:** o fluxo principal de ponta a ponta pela HTTP (`FluxoDeCompromissosIntegracaoTest`: criar → listar → consultar → atualizar → cancelar, mais os erros 400, 404 e 409 e o caso de borda), o repositório JPA com a constraint de sobreposição (`RepositorioDeCompromissosJpaTest`) e a métrica em `/actuator/prometheus`.
- **Cobertura:** o `verify` falha se linhas ou branches ficarem abaixo do mínimo declarado de **70%** (configurado no `pom.xml` e verificado no CI). Hoje: 98,0% das linhas e 92,1% dos branches, em 73 testes. Relatório em `target/site/jacoco/index.html`.

## Variáveis de ambiente

Copie `.env.example` para `.env` e troque os placeholders. O `docker-compose.yml` lê `POSTGRES_DB`, `POSTGRES_USER` e `POSTGRES_PASSWORD` e monta a partir delas as variáveis `SPRING_DATASOURCE_*` que o Spring lê; se alguma faltar, o Compose para com erro. Segredos nunca são commitados; o arquivo `.env` está no `.gitignore` e o CI roda o gitleaks.

## CI

Workflow em `.github/workflows/ci.yml`, disparado em toda PR e em push na `main`, com três jobs:

- `verificacao`: Java 21 com cache do Maven, Checkstyle e depois `./mvnw verify` (testes e cobertura mínima com JaCoCo), com PostgreSQL 16 em `services`.
- `segredos`: gitleaks em todo o histórico do repositório.
- `openapi`: valida `docs/openapi.yaml` com o Spectral (regras em `.spectral.yaml`).

Link da última execução verde: [CI da PR #3](https://github.com/laiscoutinho/agenda-api/actions/runs/42848429074) (trocar pelo da `main` depois do merge).

## Observabilidade

- **Logs estruturados** em JSON (formato ECS) no console. Cada linha de uma requisição traz o campo `idDaRequisicao`, que também volta no cabeçalho `X-Request-Id`; se o cliente mandar um id válido, ele é reaproveitado. Requisições recusadas (400, 404, 409) geram um log `WARN` com o código e a mensagem.
- **Métrica:** `http_server_requests_seconds`, a duração das requisições HTTP por rota, método e status, com histograma, em `/actuator/prometheus`.
- **SLO simples:** 95% das requisições de `GET /compromissos` respondem em menos de 300 ms. Medido com k6: p95 entre 3 e 9 ms ([docs/desempenho.md](docs/desempenho.md)). A métrica tem um balde exato em 300 ms (`le="0.3"`), então o SLO pode ser conferido direto em `/actuator/prometheus`.

## Desempenho

Smoke test com k6 em `tests/k6/smoke.js` (10 usuários por 30 s em `GET /compromissos`), que falha se o p95 passar de 300 ms. O p95 medido foi de 8,51 ms com a API fria e de 3,09 ms aquecida; o ambiente, os números e a análise estão em [docs/desempenho.md](docs/desempenho.md).

## Limitações conhecidas

- **Sem autenticação.** Por isso a API deve ser executada **apenas localmente**, com Docker, e não deve ser exposta na internet.
- Sem paginação e sem hospedagem.
- **Evolução futura:** autenticação com JWT, para permitir o uso da API em outros projetos.

## Convenções

- Todo o código (classes, métodos, variáveis, rotas e campos JSON) e toda a documentação estão em português. Ficam em inglês apenas palavras reservadas do Java, nomes de bibliotecas e anotações do Spring, e as tags dos commits.
- Commits com tag de tipo e descrição em português, por exemplo `feat: adiciona validação de sobreposição de horários`.
- Princípios de Clean Code e SOLID, com PRs pequenas revisadas pela outra integrante.

## Documentação

- [ADR 0001 - camadas com o domínio isolado e o padrão Repository](docs/adr/0001-decisao.md)
- [OpenAPI](docs/openapi.yaml)
- [Desempenho](docs/desempenho.md)
- [Uso de IA](docs/ia.md)
- [Contribuidores](CONTRIBUTORS.md)

## Estrutura do repositório

```
.
├─ docker-compose.yml
├─ Dockerfile
├─ pom.xml
├─ README.md
├─ CONTRIBUTORS.md
├─ .env.example
├─ .spectral.yaml
├─ config/checkstyle.xml
├─ docs/
│  ├─ adr/0001-decisao.md
│  ├─ openapi.yaml
│  ├─ desempenho.md
│  ├─ etapas.md
│  ├─ ia.md
│  └─ prompts/
├─ src/
│  ├─ main/java/.../dominio          entidade, regras, exceções e a interface do repositório
│  ├─ main/java/.../aplicacao        casos de uso (Java puro)
│  ├─ main/java/.../infraestrutura   JPA, configuração e observabilidade
│  ├─ main/java/.../web              controlador, DTOs e tratamento de erros
│  ├─ main/resources/db/migration    migrações do Flyway
│  └─ test/java
├─ tests/k6/smoke.js
└─ .github/workflows/ci.yml
```

## Uso de IA

Ferramentas de IA foram usadas como apoio. Os prompts relevantes estão declarados em [docs/ia.md](docs/ia.md). As decisões técnicas são da equipe.