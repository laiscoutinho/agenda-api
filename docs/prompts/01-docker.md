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