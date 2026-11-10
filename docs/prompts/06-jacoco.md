Papel: você é um engenheiro de build Maven.

Contexto: projeto Java 21 com Spring Boot 4 e Maven. [COLAR O pom.xml]
Decisão da equipe: cobertura mínima de 70% em linhas e em branches, e o
build deve falhar abaixo disso.

Tarefa: configure o jacoco-maven-plugin no pom.xml com prepare-agent, report
e check, ligados à fase verify.

Restrições: regra no nível do pacote inteiro (BUNDLE) com os contadores LINE
e BRANCH, valor mínimo 0.70; não adicione outras dependências; diga a versão
do plugin que usou e onde fica o relatório HTML.

Formato: o trecho do pom.xml em um bloco e uma frase por decisão.
