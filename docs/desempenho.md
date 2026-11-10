# Desempenho

Smoke test com k6 do endpoint principal de leitura, `GET /compromissos`, para medir o p95 e conferir o SLO do README.

## Como rodar

Com a API no ar pelo Compose:

```bash
cp .env.example .env   # se ainda não tiver
docker compose up --build -d
docker run --rm --network host -v "$PWD/tests/k6:/scripts:ro" \
  grafana/k6:1.3.0 run -e URL_BASE=http://localhost:8080 /scripts/smoke.js
```

Com o k6 instalado na máquina: `k6 run -e URL_BASE=http://localhost:8080 tests/k6/smoke.js`.

## O que o teste faz

Script: [`tests/k6/smoke.js`](../tests/k6/smoke.js).

- **Preparação (`setup`):** cria 20 compromissos sem sobreposição, de modo que a listagem devolva dados de verdade. Em uma segunda rodada eles já existem e o `POST` responde 409, o que o script trata como esperado.
- **Carga:** 10 usuários virtuais por 30 segundos, cada um chamando `GET /compromissos` com uma pausa de 100 ms entre as chamadas.
- **Checagens:** status 200 e a lista com pelo menos os 20 compromissos preparados.
- **Limites (o k6 falha se algum for violado):** p95 de `GET /compromissos` abaixo de 300 ms (o SLO), menos de 1% de requisições com erro e mais de 99% das checagens passando.

## Resultado medido

Medição de 10/11/2026, na máquina da Laís, com API e banco no Compose e o k6 em contêiner na mesma máquina.

| Item | Valor |
|---|---|
| Máquina | AMD Ryzen 7 5700U (16 threads), 9,6 GiB de RAM, Linux 7.0 |
| Ambiente | Docker 29.9.3, `docker compose up --build`, Postgres 16, Java 21 |
| Ferramenta | k6 1.3.0 (`grafana/k6:1.3.0`) |
| Massa de dados | 20 compromissos |

Foram três rodadas seguidas, com a API recém-subida na primeira:

| Rodada | Requisições | Vazão | Mediana | p90 | **p95** | Máximo | Erros |
|---|---|---|---|---|---|---|---|
| 1 (API fria) | 2.860 | 93,5/s | 3,19 ms | 6,34 ms | **8,51 ms** | 67,15 ms | 0% |
| 2 | 2.882 | 95,5/s | 2,98 ms | 3,83 ms | **4,27 ms** | 10,77 ms | 0% (\*) |
| 3 | 2.910 | 96,6/s | 2,13 ms | 2,78 ms | **3,09 ms** | 10,14 ms | 0% |

Os números são de `http_req_duration{rota:listar}`, só das chamadas de `GET /compromissos`, sem a preparação.

(\*) Na rodada 2 o k6 contou 20 "falhas" em `http_req_failed`: eram os 409 da preparação, porque os compromissos já existiam. As chamadas de `GET` não tiveram erro. Depois disso o script passou a marcar o 409 da preparação como esperado, e a rodada 3 já saiu com ele corrigido.

**Do lado do servidor**, a métrica `http_server_requests_seconds` em `/actuator/prometheus` confirmou o mesmo depois das três rodadas: das 8.652 respostas 200 de `GET /compromissos`, todas as 8.652 ficaram no balde `le="0.3"`, isto é, abaixo de 300 ms.

## Análise

- **O SLO foi cumprido com folga:** o pior p95 (8,51 ms, com a API fria) ficou cerca de 35 vezes abaixo dos 300 ms.
- **A primeira rodada é mais lenta** por causa do aquecimento da JVM (JIT) e do pool de conexões. Os picos de 67 ms e 254 ms (este último no `setup`) aparecem só nela.
- **A vazão ficou em torno de 95 requisições por segundo** porque o próprio script limita: 10 usuários com pausa de 100 ms dão no máximo cerca de 100 por segundo. Este é um smoke test, não um teste de capacidade; não procuramos o limite da API.

## Por que o SLO continua em 300 ms

Decidimos manter a meta em 300 ms em vez de apertá-la para perto do número medido:

- a medição foi numa única máquina, com cliente, API e banco juntos e sem latência de rede;
- a massa tem só 20 compromissos e a listagem não tem paginação (limitação declarada no README), então o tempo cresce com a quantidade de dados;
- 300 ms é um tempo de resposta que quem usa uma agenda não percebe como lento.

A meta deixou de ser provisória: agora ela está medida e cumprida. Se a massa crescer muito, o primeiro passo seria paginar a listagem, e não afrouxar o SLO.

## Limitações da medição

- Cliente e servidor na mesma máquina disputam CPU.
- Só o `GET /compromissos` foi medido; as escritas não entram no SLO.
- Rodadas de 30 segundos mostram a tendência, mas não substituem uma medição longa.
