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
