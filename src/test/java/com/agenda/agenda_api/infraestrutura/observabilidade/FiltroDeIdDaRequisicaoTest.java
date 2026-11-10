package com.agenda.agenda_api.infraestrutura.observabilidade;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import jakarta.servlet.http.HttpServlet;

class FiltroDeIdDaRequisicaoTest {

	private final FiltroDeIdDaRequisicao filtro = new FiltroDeIdDaRequisicao();

	/** Executa o filtro e devolve o id que estava no MDC durante a requisição. */
	private String executar(MockHttpServletRequest requisicao, MockHttpServletResponse resposta) throws Exception {
		AtomicReference<String> idDuranteARequisicao = new AtomicReference<>();
		HttpServlet servlet = new HttpServlet() {
			@Override
			protected void service(jakarta.servlet.http.HttpServletRequest req,
					jakarta.servlet.http.HttpServletResponse res) {
				idDuranteARequisicao.set(MDC.get("idDaRequisicao"));
			}
		};
		filtro.doFilter(requisicao, resposta, new MockFilterChain(servlet));
		return idDuranteARequisicao.get();
	}

	@Test
	void reaproveitaOIdQueOClienteMandou() throws Exception {
		// Arrange
		MockHttpServletRequest requisicao = new MockHttpServletRequest();
		requisicao.addHeader("X-Request-Id", "abc-123");
		MockHttpServletResponse resposta = new MockHttpServletResponse();

		// Act
		String idNoLog = executar(requisicao, resposta);

		// Assert
		assertThat(idNoLog).isEqualTo("abc-123");
		assertThat(resposta.getHeader("X-Request-Id")).isEqualTo("abc-123");
	}

	@Test
	void geraUmIdQuandoOClienteNaoManda() throws Exception {
		// Arrange
		MockHttpServletRequest requisicao = new MockHttpServletRequest();
		MockHttpServletResponse resposta = new MockHttpServletResponse();

		// Act
		String idNoLog = executar(requisicao, resposta);

		// Assert
		assertThat(idNoLog).matches("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}");
		assertThat(resposta.getHeader("X-Request-Id")).isEqualTo(idNoLog);
	}

	@Test
	void descartaIdComCaracteresQuePoderiamQuebrarOLog() throws Exception {
		// Arrange
		MockHttpServletRequest requisicao = new MockHttpServletRequest();
		requisicao.addHeader("X-Request-Id", "abc\n{\"nivel\":\"ERROR\"}");
		MockHttpServletResponse resposta = new MockHttpServletResponse();

		// Act
		String idNoLog = executar(requisicao, resposta);

		// Assert
		assertThat(idNoLog).doesNotContain("abc").hasSize(36);
	}

	@Test
	void limpaOIdDoMdcDepoisDaRequisicao() throws Exception {
		// Arrange
		MockHttpServletRequest requisicao = new MockHttpServletRequest();
		MockHttpServletResponse resposta = new MockHttpServletResponse();

		// Act
		executar(requisicao, resposta);

		// Assert
		assertThat(MDC.get("idDaRequisicao")).isNull();
	}
}
