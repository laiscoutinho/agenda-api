---
status: aceita
date: 2026-11-10
decision-makers: Laís Coutinho (proposta), Rosana Celine (revisão da PR)
---

# 0001 - Camadas com o domínio isolado e o padrão Repository

## Contexto e problema

A Agenda API tem uma regra central: **não pode haver dois compromissos ativos com horários sobrepostos**. Além dela, há as regras de título obrigatório, fim depois do início e não editar nem cancelar de novo um compromisso cancelado.

Precisávamos decidir onde essas regras moram e como o código chega ao banco. A rubrica pede SOLID e um pattern justificado, testes unitários do domínio e um teste de integração, e o projeto tem três dias de prazo. Por isso a solução não podia ser cerimoniosa demais.

Pergunta: **como organizar o código para que as regras de negócio sejam testáveis sem banco e sem Spring, sem duplicar trabalho à toa?**

## Fatores da decisão

- Testar as regras do domínio em milissegundos, sem subir contexto Spring nem banco.
- Deixar claro onde cada regra mora, para a revisão das PRs entre nós duas.
- Garantir a regra de sobreposição mesmo com duas requisições ao mesmo tempo.
- Manter o tamanho adequado a um projeto de nível B: sem abstrações que não pagam o custo.

## Opções consideradas

1. **Camadas com o domínio isolado e o padrão Repository**: a interface `RepositorioDeCompromissos` fica no domínio e a implementação JPA fica na infraestrutura.
2. **Serviço usando o Spring Data direto**: a própria classe `Compromisso` anotada com `@Entity`, e o serviço chamando um `JpaRepository`.
3. **Regras no banco**: constraints e lógica no PostgreSQL, com a aplicação só repassando os dados.

## Decisão

Escolhemos a **opção 1**.

```
web  →  aplicacao  →  dominio  ←  infraestrutura
                      (interface         (implementação JPA,
                       RepositorioDe...)  Flyway, configuração)
```

- `dominio`: `Compromisso`, `StatusCompromisso`, as exceções de domínio e a interface `RepositorioDeCompromissos`. Não importa nada do Spring nem do Jakarta Persistence, o que conferimos com `grep` a cada etapa.
- `aplicacao`: `ServicoDeCompromissos`, os casos de uso. É Java puro; recebe o repositório pelo construtor.
- `infraestrutura`: `CompromissoEntidade` (JPA), o mapeador e `RepositorioDeCompromissosJpa`, que implementa a interface do domínio. Também a configuração que cria o serviço como bean.
- `web`: controlador, DTOs e o tratador que traduz exceções de domínio em 400, 404 e 409.

**Princípios SOLID envolvidos:**

- **Inversão de dependência (D):** o caso de uso depende da interface do domínio, não do JPA. Quem aponta para quem é a infraestrutura, para o domínio.
- **Responsabilidade única (S):** a entidade JPA só descreve a tabela; a entidade de domínio só guarda as regras; o mapeador só converte.

**A regra de sobreposição fica em dois lugares, de propósito:**

1. No caso de uso, que consulta os compromissos ativos que se sobrepõem e lança `ConflitoDeHorarioExcecao`. É o que dá a mensagem clara e é testado com o repositório em memória.
2. No banco, com uma *exclusion constraint* do PostgreSQL (`EXCLUDE USING gist (tstzrange(inicio, fim, '[)') WITH &&) WHERE (status = 'ATIVO')`). Ela cobre a corrida entre duas requisições simultâneas, que passariam juntas pela checagem do caso de uso. O adaptador JPA reconhece a violação pelo SQLState `23P01` e a traduz para a mesma `ConflitoDeHorarioExcecao`, então a camada web não fica sabendo que veio do banco.

O intervalo `'[)'` (semiaberto) é o mesmo critério de `Compromisso.sobrepoe`: terminar às 11:00 não conflita com começar às 11:00.

## Consequências

**Boas:**

- O domínio e os casos de uso têm 45 testes unitários que rodam sem banco, com `RepositorioDeCompromissosEmMemoria`.
- Trocar o JPA por JDBC puro, por exemplo, mexeria só em `infraestrutura`.
- A regra de sobreposição é garantida também sob concorrência, sem lock explícito na aplicação.

**Ruins (aceitas):**

- Há duas classes por conceito (`Compromisso` e `CompromissoEntidade`) e um mapeador entre elas. Para um único agregado, é código a mais.
- Para recriar um compromisso salvo foi preciso o método `Compromisso.reconstituir`, que existe só por causa da persistência.
- A *exclusion constraint* usa um recurso específico do PostgreSQL. Trocar de banco exigiria outra solução para a concorrência.
- O repositório em memória dos testes precisa reproduzir o critério de sobreposição do banco. Se os dois divergirem, os testes unitários mentem; por isso o teste de integração (`RepositorioDeCompromissosJpaTest` e `FluxoDeCompromissosIntegracaoTest`) roda contra o PostgreSQL de verdade, inclusive no caso de borda.

## Prós e contras das opções

### Opção 1: camadas com o domínio isolado e Repository (escolhida)

- Bom: testes do domínio rápidos e sem infraestrutura.
- Bom: cada camada tem uma razão para mudar, o que facilita a revisão.
- Ruim: entidade JPA e mapeador a mais.

### Opção 2: serviço usando o Spring Data direto (descartada)

- Bom: menos código; seria o caminho mais rápido para o prazo.
- Ruim: o domínio passaria a depender do JPA (anotações na classe, construtor sem argumentos e o id controlado pelo Hibernate), o que enfraquece as invariantes de `Compromisso`.
- Ruim: testar a regra de sobreposição exigiria banco ou mocks do `JpaRepository`, que testam o mock e não a regra.
- **Por que descartamos:** a regra de sobreposição é o coração do projeto e queríamos testá-la de forma isolada e completa, com casos de borda, sem depender do banco.

### Opção 3: regras no banco (descartada como única defesa)

- Bom: a constraint garante a sobreposição mesmo sob concorrência.
- Ruim: as mensagens de erro ficariam genéricas, e as regras só seriam testáveis com o banco.
- **Por que descartamos:** usamos a constraint como segunda linha de defesa, não como o lugar da regra.
