Papel: você é um engenheiro de QA sênior, rigoroso com casos de borda.

Contexto: API de agenda em Java 21 e Spring Boot 4, testes com JUnit 5 e
AssertJ. Todo o código em português. A entidade Compromisso ainda NÃO existe.
Campos: id (UUID), titulo, descricao (opcional), inicio e fim (Instant),
status (enum StatusCompromisso com ATIVO e CANCELADO). Regras: título
obrigatório; fim deve ser posterior ao início; um compromisso já cancelado
não pode ser cancelado de novo. O domínio não importa Spring nem JPA. Pacote
base: com.agenda.agenda_api.dominio.

Tarefa: escreva apenas os testes unitários da entidade Compromisso, antes de
qualquer implementação (os testes devem falhar). Proponha a API pública
mínima (criar e cancelar) e use-a nos testes. Identifique também os casos de
borda que eu não listei.

Restrições:
- Padrão Arrange, Act, Assert, com os três blocos visíveis.
- O nome de cada teste descreve o comportamento esperado, não a implementação.
- Valores esperados escritos como literais conferidos à mão, nunca calculados
  com a mesma lógica da implementação.
- Não crie a implementação, não adicione dependências, não use mocks.
- Casos obrigatórios: título vazio e só com espaços; fim igual ao início; fim
  anterior ao início; cancelar duas vezes.

Formato: um único bloco de código para CompromissoTest.java. Depois, uma
tabela curta com: caso de teste e o risco que ele evita.
