Papel: você é um engenheiro backend que prioriza simplicidade e Clean Code.

Contexto: os testes de CompromissoTest (colados abaixo) estão falhando.
Java 21, todo o código em português. O domínio não importa Spring nem JPA.
[COLAR AQUI O ARQUIVO DE TESTES]

Tarefa: implemente a entidade Compromisso e as exceções de domínio,
escrevendo apenas o necessário para os testes passarem.

Restrições:
- Exceções de domínio herdam de uma classe base, com nomes que revelam a
  regra violada. Não retorne null para sinalizar falha.
- Métodos curtos, com uma responsabilidade e nomes que revelam intenção.
- Não altere os testes. Se achar que um teste está errado, diga qual e por
  quê, sem mexer nele.
- Não adicione dependências.

Formato: um bloco de código por arquivo, e uma lista curta com a justificativa
de cada decisão de validação em uma frase.
