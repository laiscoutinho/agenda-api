# Contribuidores

| Nome | GitHub | Parte do sistema |
|---|---|---|
| Laís Coutinho | [@laiscoutinho](https://github.com/laiscoutinho) | Domínio, casos de uso, persistência, API web, observabilidade e desempenho |
| Rosana Celine | [@RosanaCeline](https://github.com/RosanaCeline) | Revisão técnica dos PRs, coimplementação de funcionalidades, CI/CD, testes, persistência, API e documentação |

## Quem fez o quê, por etapa

| Etapa | Branch e PR | Autoria | Coautoria e revisão |
|---|---|---|---|
| Estrutura inicial e Docker | `chore/estrutura-inicial`, [PR #1](https://github.com/laiscoutinho/agenda-api/pull/1) | Laís | Rosana (coautora em `7dd835c`, revisora final); Claude (coautor em `a8c6906`, `7dd835c`, `79455b0`, `7fc3c6b`); revisão: presente nos comentários do PR |
| CI, Checkstyle e gitleaks | `ci/pipeline-inicial`, [PR #2](https://github.com/laiscoutinho/agenda-api/pull/2) | Laís | Rosana (coautora em `eb1ae36`, revisora final); Claude (coautor em `d070ee5`, `f838ef0`, `7c94117`, `aec5ca5`); revisão: presente nos comentários do PR |
| Domínio do compromisso (TDD) e JaCoCo | `feat/dominio-compromisso`, [PR #3](https://github.com/laiscoutinho/agenda-api/pull/3) | Laís | Rosana (coautora em `a9f797f`, `703cf74`, `2396e4b`, revisora final); Claude (coautor em `caee7c2`, `c3a4eae`, `703cf74`, `2396e4b`); revisão: presente nos comentários do PR |
| Persistência (Flyway, JPA) | `feat/persistencia-compromisso`, [PR #4](https://github.com/laiscoutinho/agenda-api/pull/4) | Laís | Rosana (coautora em `3a2a03a`, `e865d52`, `5c94caa`, revisora final); Claude (coautor em `dde68d6`); revisão: presente nos comentários do PR |
| Casos de uso e regra de sobreposição | `feat/casos-de-uso-compromisso`, [PR #5](https://github.com/laiscoutinho/agenda-api/pull/5) | Laís | Rosana (coautora em `7e318a8`, revisora final); Claude (coautor em `ddc3cfa`); revisão: presente nos comentários do PR |
| API web e teste de integração | `feat/api-compromissos`, [PR #6](https://github.com/laiscoutinho/agenda-api/pull/6) | Laís | Rosana (coautora em `b92bcf8`, revisora final); Claude (coautor em `77145de`); revisão: presente nos comentários do PR |
| Observabilidade | `feat/observabilidade`, [PR #7](https://github.com/laiscoutinho/agenda-api/pull/7) | Laís | Rosana (coautora em `5a9a370`, revisora final); revisão: presente nos comentários do PR |
| Desempenho (k6) | `perf/smoke-k6`, [PR #8](https://github.com/laiscoutinho/agenda-api/pull/8) | Laís | Claude (coautor em `d270ecd`, `31b8c31`); revisão: presente nos comentários do PR |
| ADR, OpenAPI e documentação | `docs/adr-openapi`, [PR #9](https://github.com/laiscoutinho/agenda-api/pull/9) | Laís | Rosana (coautora em `ba88c9c`, revisora final); Claude (coautor em todos os commits da etapa); revisão: presente nos comentários do PR |

## Como contribuímos

- Toda mudança entra por PR pequena, com o CI verde, revisada pela outra integrante antes do merge.
- Quem participa de verdade de um commit sem ser a autora entra como `Co-authored-by`: a Rosana nos commits com decisões e implementações centrais, e o Claude (Claude Code) nos commits repetitivos, como configuração do `pom.xml`, testes e ajustes de links, e na documentação.
- O uso de IA em cada etapa está declarado em [docs/ia.md](docs/ia.md), com os prompts em [docs/prompts/](docs/prompts/).
