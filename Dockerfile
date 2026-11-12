# syntax=docker/dockerfile:1

# ---------------------------------------------------------------------------
# Estágio 1: build
# Usa o JDK 21 oficial e o Maven Wrapper do projeto, para que a versão do
# Maven seja a mesma usada localmente e no CI.
# ---------------------------------------------------------------------------
FROM eclipse-temurin:21-jdk AS build

WORKDIR /workspace

# Copia primeiro só o wrapper e o pom.xml: enquanto eles não mudarem, a
# camada com as dependências baixadas é reaproveitada do cache.
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN chmod +x mvnw && ./mvnw -B -q dependency:go-offline

# Agora copia o código. Mudanças aqui não invalidam o cache de dependências.
COPY src/ src/

# Os testes e o Checkstyle não rodam aqui porque o CI já os executa
# (e a pasta config/ do Checkstyle nem é copiada para a imagem).
RUN ./mvnw -B -q -o package -DskipTests -Dcheckstyle.skip \
    && cp target/*.jar /workspace/app.jar

# ---------------------------------------------------------------------------
# Estágio 2: execução
# Só o JRE 21 e o jar: sem JDK, sem Maven, sem código-fonte.
# ---------------------------------------------------------------------------
FROM eclipse-temurin:21-jre AS runtime

# Usuário de sistema sem privilégios e sem shell de login.
RUN groupadd --system app \
    && useradd --system --gid app --no-create-home --shell /usr/sbin/nologin app

WORKDIR /app

COPY --from=build --chown=app:app /workspace/app.jar app.jar

USER app

EXPOSE 8080

# A JVM respeita o limite de memória do contêiner.
ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75"

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
