// Smoke test de desempenho do endpoint principal de leitura (GET /compromissos).
// Como rodar e o resultado medido: docs/desempenho.md.
//
// Uso: k6 run -e URL_BASE=http://localhost:8080 tests/k6/smoke.js
import http from 'k6/http';
import { check, sleep } from 'k6';

const URL_BASE = __ENV.URL_BASE || 'http://localhost:8080';
const QUANTIDADE_DE_COMPROMISSOS = 20;

export const options = {
	// Carga leve e constante: o objetivo é medir o p95 sem estressar a máquina.
	vus: 10,
	duration: '30s',
	thresholds: {
		// SLO do README: 95% das requisições de GET /compromissos abaixo de 300 ms.
		'http_req_duration{rota:listar}': ['p(95)<300'],
		http_req_failed: ['rate<0.01'],
		checks: ['rate>0.99'],
	},
};

// Cria uma massa pequena de compromissos, um por hora, sem sobreposição.
// Rodar de novo responde 409 para os que já existem, o que é esperado.
export function setup() {
	const parametros = {
		headers: { 'Content-Type': 'application/json' },
		// 409 aqui não é falha: o compromisso já existia de uma rodada anterior.
		responseCallback: http.expectedStatuses(201, 409),
	};
	for (let i = 0; i < QUANTIDADE_DE_COMPROMISSOS; i++) {
		const inicio = new Date(Date.UTC(2030, 0, 1, i, 0, 0));
		const fim = new Date(Date.UTC(2030, 0, 1, i, 30, 0));
		const corpo = JSON.stringify({
			titulo: `Compromisso de carga ${i + 1}`,
			inicio: inicio.toISOString(),
			fim: fim.toISOString(),
		});
		const resposta = http.post(`${URL_BASE}/compromissos`, corpo, parametros);
		if (resposta.status !== 201 && resposta.status !== 409) {
			throw new Error(`Falha ao preparar a massa: ${resposta.status} ${resposta.body}`);
		}
	}
}

export default function () {
	const resposta = http.get(`${URL_BASE}/compromissos`, { tags: { rota: 'listar' } });
	check(resposta, {
		'status 200': (r) => r.status === 200,
		'lista com a massa preparada': (r) => r.json().length >= QUANTIDADE_DE_COMPROMISSOS,
	});
	sleep(0.1);
}
