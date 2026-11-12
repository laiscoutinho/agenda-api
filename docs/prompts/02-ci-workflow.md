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
