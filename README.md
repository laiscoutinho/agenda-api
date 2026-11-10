**Nível: B (simples)**

# Agenda API

Projeto final do curso **Full Cycle**, Módulo 4. API REST de domínio único para gerenciar uma agenda de compromissos, com testes, CI, observabilidade básica e documentação.

> Objetivo do projeto: mostrar engenharia, não tamanho. O fluxo principal funciona de ponta a ponta e está coberto por testes, CI e medição de desempenho.

## Equipe

| Nome | GitHub | Parte do sistema |
|---|---|---|
| Laís Coutinho | [@laiscoutinho](https://github.com/laiscoutinho) | A DEFINIR (conforme CONTRIBUTORS.md) |
| Rosana Celine | [@rosanaceline](https://github.com/rosanaceline) | A DEFINIR (conforme CONTRIBUTORS.md) |

Detalhes de quem fez o quê em [CONTRIBUTORS.md](CONTRIBUTORS.md).

## O que a API faz

Permite criar e gerenciar compromissos de uma agenda.

**Fluxo principal:** criar compromisso → listar/consultar → atualizar → cancelar.

### Endpoints

| Método | Rota | Descrição |
|---|---|---|
| POST | `/compromissos` | Cria um compromisso |
| GET | `/compromissos` | Lista compromissos (filtro opcional por data) |
| GET | `/compromissos/{id}` | Consulta um compromisso |
| PUT | `/compromissos/{id}` | Atualiza um compromisso |
| PATCH | `/compromissos/{id}/cancelar` | Cancela um compromisso |
| GET | `/actuator/health` | Verifica se a API está no ar |
| GET | `/actuator/prometheus` | Métricas no formato Prometheus |

A descrição completa está em [docs/openapi.yaml](docs/openapi.yaml).

### Regras de negócio

- O título é obrigatório.
- O horário de fim deve ser posterior ao de início.
- Não pode haver dois compromissos ativos com horários sobrepostos. Um compromisso que termina exatamente quando outro começa não conflita.
- Um compromisso já cancelado não pode ser cancelado de novo nem editado.
- Não existe exclusão: cancelar muda o status para `CANCELADO`.

### Respostas de erro

| Código | Quando |
|---|---|
| 400 | Dados inválidos |
| 404 | Compromisso não encontrado |
| 409 | Conflito de horário |

O corpo do erro é JSON com os campos `erro` e `mensagem`.

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

Pré-requisito: Java 21.

```bash
./mvnw checkstyle:check
./mvnw verify
```

- Testes unitários cobrem as regras do domínio, com repositório em memória.
- Há 1 teste de integração cobrindo o fluxo principal com PostgreSQL real.
- O `verify` falha se a cobertura de linhas ou de branches ficar abaixo do mínimo declarado: **70%** (valor a confirmar; configurado no `pom.xml` e verificado no CI).

## Variáveis de ambiente

Copie `.env.example` para `.env` e troque os placeholders. O `docker-compose.yml` lê `POSTGRES_DB`, `POSTGRES_USER` e `POSTGRES_PASSWORD` e monta a partir delas as variáveis `SPRING_DATASOURCE_*` que o Spring lê; se alguma faltar, o Compose para com erro. Segredos nunca são commitados; o arquivo `.env` está no `.gitignore` e o CI roda o gitleaks.

## CI

Workflow em `.github/workflows/ci.yml`, disparado em toda PR e em push na `main`, com dois jobs:

- `verificacao`: Java 21 com cache do Maven, Checkstyle e depois `./mvnw verify`, com PostgreSQL 16 em `services`.
- `segredos`: gitleaks em todo o histórico do repositório.

A verificação de cobertura com JaCoCo entra junto com os testes do domínio.

Link da última execução verde: COLAR_AQUI

## Observabilidade

- **Logs estruturados** em JSON, com id da requisição.
- **Métrica:** duração das requisições HTTP por rota e status, em `/actuator/prometheus`.
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

- [ADR 0001 - decisão técnica](docs/adr/0001-decisao.md)
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
├─ docs/
│  ├─ adr/0001-decisao.md
│  ├─ openapi.yaml
│  ├─ desempenho.md
│  └─ ia.md
├─ src/
│  ├─ main/java/.../dominio
│  ├─ main/java/.../aplicacao
│  ├─ main/java/.../infraestrutura
│  ├─ main/java/.../web
│  ├─ main/resources/db/migration
│  └─ test/java
├─ tests/k6/smoke.js
└─ .github/workflows/ci.yml
```

## Uso de IA

Ferramentas de IA foram usadas como apoio. Os prompts relevantes estão declarados em [docs/ia.md](docs/ia.md). As decisões técnicas são da equipe.