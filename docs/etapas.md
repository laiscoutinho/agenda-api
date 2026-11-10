# Etapas do projeto

Acompanhamento do que já foi feito e do que falta na Agenda API (Projeto B, Full Cycle, Módulo 4).

**Entrega:** até **13/11** pelo Moodle (commit depois disso não é avaliado). Apresentação opcional em 16/11, ou vídeo até 13/11.

**Atualizado em:** 10/11

**Legenda:** ✅ feito · 🔄 em andamento · ⬜ falta · ➖ não se aplica

## Resumo

Todo o código está feito e em PRs empilhadas, cada uma sobre a anterior. O que falta é revisão, merge e preencher o que só nós duas sabemos.

| Área | Situação |
|---|---|
| Estrutura, Docker e configuração | ✅ feito (PR #1 aberta) |
| CI, lint e gitleaks | ✅ feito (PR #2 aberta) |
| Domínio do compromisso (TDD), refactor e JaCoCo | ✅ feito (PR #3 aberta; falta o force-push) |
| Persistência (Flyway, JPA, repositório) | ✅ feito (PR #4 aberta) |
| Casos de uso e regra de sobreposição | ✅ feito (PR #5 aberta) |
| API web, erros HTTP e teste de integração | ✅ feito (PR #6 aberta) |
| Observabilidade (logs JSON, métrica, SLO) | ✅ feito (PR #7 aberta) |
| Desempenho (k6 e p95 medido) | ✅ feito (PR #8 aberta) |
| Documentação (ADR, OpenAPI, CONTRIBUTORS) | ✅ feito (PR #9 aberta) |
| Revisão, merge e campos `[PREENCHER]` | ⬜ falta |
| Entrega no Moodle | ⬜ falta |

## O que o curso exige no Projeto B

Peso de cada critério da rubrica: M1 35, M2 20, M3 15, M4 30.

| Origem | Entregável | Situação | Onde |
|---|---|---|---|
| M1 | Compose que sobe com 1 comando | ✅ | `docker-compose.yml`, `Dockerfile` (corrigido na #8) |
| M1 | SOLID e 1 pattern justificado (Repository) | ✅ | [ADR 0001](adr/0001-decisao.md), `RepositorioDeCompromissos` |
| M1 | Fluxo principal funcionando de ponta a ponta | ✅ | endpoints `/compromissos` (#6) |
| M2 | Testes unitários do domínio | ✅ | 49 unitários, 45 deles de domínio e casos de uso |
| M2 | 1 teste de integração | ✅ | `FluxoDeCompromissosIntegracaoTest` com Postgres real |
| M2 | Coverage mínimo declarado e respeitado | ✅ | JaCoCo 70%; hoje 98,0% das linhas e 92,1% dos branches |
| M3 | CI verde: lint e testes | ✅ | `.github/workflows/ci.yml`, mais o job `openapi` |
| M3 | `.env.example` e segredos fora do repositório | ✅ | `.env.example`, `.gitignore`, gitleaks |
| M3 | Logs estruturados, 1 métrica e 1 SLO simples | ✅ | logs ECS com `idDaRequisicao`, `http_server_requests_seconds`, SLO de 300 ms no README |
| M4 | README do primeiro dia | ✅ | `README.md` (falta só a parte da Rosana) |
| M4 | 1 ADR | ✅ | `docs/adr/0001-decisao.md` |
| M4 | OpenAPI do endpoint principal | ✅ | `docs/openapi.yaml`, validado com Spectral |
| M4 | 1 smoke test k6 com p95 medido | ✅ | `tests/k6/smoke.js`, `docs/desempenho.md` |
| — | Uso de IA declarado | 🔄 | `docs/ia.md` e `docs/prompts/` (faltam os `[PREENCHER]`) |
| — | `CONTRIBUTORS.md` | 🔄 | raiz do repositório (falta a parte da Rosana) |

## Etapas em detalhe

### 1. Estrutura inicial e Docker: ✅ feito

Branch `chore/estrutura-inicial`, [PR #1](https://github.com/laiscoutinho/agenda-api/pull/1) (aberta).

- ✅ Projeto Spring Boot 4.1.2 com Maven Wrapper, Java 21, na raiz do repositório
- ✅ `Dockerfile` em dois estágios, usuário não-root; `docker-compose.yml` com healthcheck do banco
- ✅ `.env.example` só com placeholders; `.env` no `.gitignore`
- ✅ Prompt 1 registrado
- ⬜ Preencher em `docs/ia.md`: "O que mudei ou rejeitei"
- ⬜ Revisão e merge pela Rosana

### 2. Pipeline de CI: ✅ feito

Branch `ci/pipeline-inicial`, [PR #2](https://github.com/laiscoutinho/agenda-api/pull/2) (aberta).

- ✅ Checkstyle, workflow com `verificacao` e `segredos`, `permissions: contents: read`, CI verde
- ✅ Prompts 2 e 3 registrados
- ⬜ Preencher em `docs/ia.md`: "O que mudei ou rejeitei" dos prompts 2 e 3
- ⬜ Revisão e merge pela Rosana
- ⬜ Proteção de branch na `main`: PR obrigatória, checks `verificacao`, `segredos` e `openapi` obrigatórios, aprovação da outra integrante

### 3. Domínio do compromisso com TDD: ✅ feito

Branch `feat/dominio-compromisso`, [PR #3](https://github.com/laiscoutinho/agenda-api/pull/3) (aberta).

- ✅ Red, Green e Refactor (`5e680aa`: verificação de cancelado extraída e validações agrupadas, sem mudar teste)
- ✅ JaCoCo 0.8.16 com mínimo de 70%
- ✅ Prova da suíte com alterações propositais
- 🔄 Coautoria e refactor feitos localmente; **falta o force-push**, que só a Laís pode rodar:
  `git push --force-with-lease=feat/dominio-compromisso:934db79 origin feat/dominio-compromisso`
- ⬜ Preencher em `docs/ia.md`: "O que mudei ou rejeitei" dos prompts 4 a 6 e a checagem de testes tautológicos
- ⬜ Revisão e merge pela Rosana

### 4. Persistência: ✅ feito

Branch `feat/persistencia-compromisso`, [PR #4](https://github.com/laiscoutinho/agenda-api/pull/4) (aberta).

- ✅ `Compromisso.reconstituir` (com TDD) para recriar o compromisso salvo
- ✅ Migração `V1__cria_tabela_compromisso.sql` com `CHECK`s e *exclusion constraint* contra sobreposição de ativos
- ✅ Interface `RepositorioDeCompromissos` no domínio; entidade JPA separada, mapeador e adaptador
- ✅ Violação da constraint (SQLState `23P01`) traduzida para `ConflitoDeHorarioExcecao`
- ✅ `RepositorioDeCompromissosJpaTest` com Postgres real

### 5. Casos de uso e regra de sobreposição: ✅ feito

Branch `feat/casos-de-uso-compromisso`, [PR #5](https://github.com/laiscoutinho/agenda-api/pull/5) (aberta).

- ✅ Criar, listar (com filtro por dia em UTC), consultar, atualizar e cancelar
- ✅ Sobreposição só com ativos; borda que encosta não conflita; `PUT` ignora o próprio compromisso; cancelado não edita
- ✅ Exceções `ConflitoDeHorarioExcecao`, `CompromissoNaoEncontradoExcecao`, `CompromissoCanceladoNaoPodeSerEditadoExcecao`
- ✅ Testes com repositório em memória e prova por mutantes

### 6. API web e teste de integração: ✅ feito

Branch `feat/api-compromissos`, [PR #6](https://github.com/laiscoutinho/agenda-api/pull/6) (aberta).

- ✅ `POST`, `GET` (lista e por id), `PUT` e `PATCH /{id}/cancelar`
- ✅ DTOs com `@Valid` e `@Size`; datas ISO 8601 em UTC
- ✅ Tratador de erros: 400, 404 e 409, corpo `{ "erro", "mensagem" }`
- ✅ `FluxoDeCompromissosIntegracaoTest` com 13 testes e Postgres real

### 7. Configuração e observabilidade: ✅ feito

Branch `feat/observabilidade`, [PR #7](https://github.com/laiscoutinho/agenda-api/pull/7) (aberta).

- ✅ Logs em JSON (ECS) com `idDaRequisicao` e cabeçalho `X-Request-Id`
- ✅ `http_server_requests_seconds` com histograma e balde de 300 ms em `/actuator/prometheus`
- ✅ SLO no README
- ➖ `@ConfigurationProperties` (decisão 23): não se aplica, porque não há propriedade própria; registrado no `docs/ia.md`

### 8. Documentação: ✅ feito

Branch `docs/adr-openapi`, [PR #9](https://github.com/laiscoutinho/agenda-api/pull/9) (aberta).

- ✅ ADR 0001 em MADR: camadas com o domínio isolado e Repository, com as alternativas descartadas
- ✅ `docs/openapi.yaml` (OpenAPI 3.1) validado com Spectral 6.17.0, também no CI (job `openapi`)
- ✅ `CONTRIBUTORS.md` e README completos, menos a parte da Rosana
- ✅ Prompt 7 registrado em `docs/ia.md` e `docs/prompts/07-etapas-restantes.md`
- ⬜ `docs/ia.md`, seção 4: "Como as decisões foram tomadas", só com o que aconteceu de verdade

### 9. Desempenho: ✅ feito

Branch `perf/smoke-k6`, [PR #8](https://github.com/laiscoutinho/agenda-api/pull/8) (aberta).

- ✅ `tests/k6/smoke.js` contra `GET /compromissos`, com a API no Compose
- ✅ `docs/desempenho.md`: p95 de 8,51 ms (API fria) a 3,09 ms, ambiente e análise
- ✅ SLO de 300 ms mantido e justificado
- ✅ Corrigido o build da imagem Docker, quebrado desde a #2

### 10. Entrega: ⬜ falta

Até 13/11.

- ⬜ Rosana revisar as PRs #1 a #9, na ordem, e mergear (cada base muda para `main` quando a anterior entra)
- ⬜ Todas as PRs mergeadas na `main`, com o CI verde na `main`; trocar no README o link do CI pelo da `main`
- ⬜ Teste do zero: clonar, `cp .env.example .env`, `docker compose up --build`, `curl /actuator/health`
- ⬜ Nenhum `[PREENCHER]`, `A DEFINIR` ou `COLAR_AQUI` sobrando nos documentos
- ⬜ Enviar no Moodle: link do repositório, README, link do CI verde, `docs/desempenho.md` e `CONTRIBUTORS.md`
- ⬜ Opcional: apresentação de 10 minutos (problema e nível, demonstração, uma decisão e a alternativa descartada, números)

## Pontos de atenção

- **Nota individual:** hoje quase todos os commits estão no nome da Laís. A Rosana precisa ter a própria parte (commits, revisões e o que for defender) e registrá-la no `CONTRIBUTORS.md` e no README.
- **Uso de IA:** as etapas 4 a 9 foram feitas pelo agente a partir do prompt 7. Cada uma precisa saber explicar o código das PRs que vai defender; a revisão linha a linha é o momento disso.
- **Campos a preencher:** `[PREENCHER]` no `docs/ia.md` e no `CONTRIBUTORS.md`, e "A DEFINIR" na equipe do README. Só com o que aconteceu de verdade.
- **O que mais derruba a nota, pelo PDF:** escopo inflado sem nada funcionando, CI vermelho, README que não deixa subir a aplicação, segredo commitado, teste sem asserção e número sem medição.
