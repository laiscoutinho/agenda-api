package com.agenda.agenda_api.infraestrutura.observabilidade;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.micrometer.metrics.test.autoconfigure.AutoConfigureMetrics;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Confere que a métrica de duração HTTP sai em /actuator/prometheus por rota e
 * status, com o balde de 300 ms do SLO, e que a resposta traz o X-Request-Id.
 */
@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureMetrics
class MetricasIntegracaoTest {

	@Autowired
	private MockMvc mvc;

	@Test
	void expoeADuracaoDasRequisicoesComOBaldeDoSlo() throws Exception {
		// Arrange
		mvc.perform(get("/compromissos")).andExpect(status().isOk());

		// Act e Assert
		mvc.perform(get("/actuator/prometheus"))
				.andExpect(status().isOk())
				.andExpect(content().string(containsString("http_server_requests_seconds_bucket{")))
				.andExpect(content().string(containsString("uri=\"/compromissos\"")))
				.andExpect(content().string(containsString("le=\"0.3\"")));
	}

	@Test
	void respostaTrazOIdDaRequisicao() throws Exception {
		// Act e Assert
		mvc.perform(get("/compromissos").header("X-Request-Id", "teste-42"))
				.andExpect(header().string("X-Request-Id", "teste-42"));
	}
}
