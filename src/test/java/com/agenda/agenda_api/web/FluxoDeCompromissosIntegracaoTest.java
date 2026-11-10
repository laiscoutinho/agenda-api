package com.agenda.agenda_api.web;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.ResultMatcher;

import com.jayway.jsonpath.JsonPath;

/**
 * Teste de integração do fluxo principal: HTTP, casos de uso, JPA, Flyway e
 * PostgreSQL real (o mesmo do CI), sem mocks.
 */
@SpringBootTest
@AutoConfigureMockMvc
class FluxoDeCompromissosIntegracaoTest {

	@Autowired
	private MockMvc mvc;

	@Autowired
	private JdbcTemplate jdbc;

	@BeforeEach
	void limparAgenda() {
		jdbc.update("delete from compromisso");
	}

	private ResultActions criar(String json) throws Exception {
		return mvc.perform(post("/compromissos").contentType(MediaType.APPLICATION_JSON).content(json));
	}

	private static ResultMatcher mensagem(String esperada) {
		return jsonPath("$.mensagem").value(esperada);
	}

	private String criarERetornarId(String json) throws Exception {
		String corpo = criar(json).andExpect(status().isCreated())
				.andReturn().getResponse().getContentAsString();
		return JsonPath.read(corpo, "$.id");
	}

	@Test
	void fluxoPrincipalCriaListaConsultaAtualizaECancela() throws Exception {
		// Arrange
		String novo = """
				{"titulo": "Dentista", "descricao": "Levar exames",
				 "inicio": "2026-11-10T14:00:00Z", "fim": "2026-11-10T15:00:00Z"}
				""";
		String alterado = """
				{"titulo": "Dentista (retorno)",
				 "inicio": "2026-11-10T16:00:00Z", "fim": "2026-11-10T17:00:00Z"}
				""";

		// Act e Assert: criar
		String corpo = criar(novo)
				.andExpect(status().isCreated())
				.andExpect(header().string("Location", startsWith("/compromissos/")))
				.andExpect(jsonPath("$.titulo").value("Dentista"))
				.andExpect(jsonPath("$.descricao").value("Levar exames"))
				.andExpect(jsonPath("$.inicio").value("2026-11-10T14:00:00Z"))
				.andExpect(jsonPath("$.fim").value("2026-11-10T15:00:00Z"))
				.andExpect(jsonPath("$.status").value("ATIVO"))
				.andReturn().getResponse().getContentAsString();
		String id = JsonPath.read(corpo, "$.id");

		// Act e Assert: listar e consultar
		mvc.perform(get("/compromissos"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(1)))
				.andExpect(jsonPath("$[0].id").value(id));
		mvc.perform(get("/compromissos/" + id))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.titulo").value("Dentista"));

		// Act e Assert: atualizar
		mvc.perform(put("/compromissos/" + id).contentType(MediaType.APPLICATION_JSON).content(alterado))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.titulo").value("Dentista (retorno)"))
				.andExpect(jsonPath("$.descricao").doesNotExist())
				.andExpect(jsonPath("$.inicio").value("2026-11-10T16:00:00Z"));

		// Act e Assert: cancelar, e o cancelamento fica gravado
		mvc.perform(patch("/compromissos/" + id + "/cancelar"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("CANCELADO"));
		mvc.perform(get("/compromissos/" + id))
				.andExpect(jsonPath("$.status").value("CANCELADO"));
	}

	@Test
	void listarComFiltroDeDataTrazSoOsDoDia() throws Exception {
		// Arrange
		criar("""
				{"titulo": "Hoje", "inicio": "2026-11-10T14:00:00Z", "fim": "2026-11-10T15:00:00Z"}
				""");
		criar("""
				{"titulo": "Amanhã", "inicio": "2026-11-11T14:00:00Z", "fim": "2026-11-11T15:00:00Z"}
				""");

		// Act
		ResultActions resposta = mvc.perform(get("/compromissos").param("data", "2026-11-11"));

		// Assert
		resposta.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(1)))
				.andExpect(jsonPath("$[0].titulo").value("Amanhã"));
	}

	@Test
	void criarComTituloEmBrancoResponde400() throws Exception {
		// Arrange
		String json = """
				{"titulo": "   ", "inicio": "2026-11-10T14:00:00Z", "fim": "2026-11-10T15:00:00Z"}
				""";

		// Act
		ResultActions resposta = criar(json);

		// Assert
		resposta.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.erro").value("DADOS_INVALIDOS"))
				.andExpect(mensagem("O título do compromisso é obrigatório."));
	}

	@Test
	void criarComFimAntesDoInicioResponde400() throws Exception {
		// Arrange
		String json = """
				{"titulo": "Reunião", "inicio": "2026-11-10T15:00:00Z", "fim": "2026-11-10T14:00:00Z"}
				""";

		// Act
		ResultActions resposta = criar(json);

		// Assert
		resposta.andExpect(status().isBadRequest())
				.andExpect(mensagem("O fim do compromisso deve ser posterior ao início."));
	}

	@Test
	void criarSemHorariosResponde400() throws Exception {
		// Arrange
		String json = """
				{"titulo": "Reunião"}
				""";

		// Act
		ResultActions resposta = criar(json);

		// Assert
		resposta.andExpect(status().isBadRequest())
				.andExpect(mensagem("O início e o fim do compromisso são obrigatórios."));
	}

	@Test
	void criarComTituloAcimaDoLimiteResponde400() throws Exception {
		// Arrange
		String tituloCom201Caracteres = "a".repeat(201);
		String json = "{\"titulo\": \"" + tituloCom201Caracteres + "\","
				+ " \"inicio\": \"2026-11-10T14:00:00Z\", \"fim\": \"2026-11-10T15:00:00Z\"}";

		// Act
		ResultActions resposta = criar(json);

		// Assert
		resposta.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.erro").value("DADOS_INVALIDOS"))
				.andExpect(mensagem("O título deve ter no máximo 200 caracteres."));
	}

	@Test
	void criarComDataForaDoFormatoResponde400() throws Exception {
		// Arrange
		String json = """
				{"titulo": "Reunião", "inicio": "10/11/2026 14:00", "fim": "2026-11-10T15:00:00Z"}
				""";

		// Act
		ResultActions resposta = criar(json);

		// Assert
		resposta.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.erro").value("DADOS_INVALIDOS"));
	}

	@Test
	void consultarIdInexistenteResponde404() throws Exception {
		// Arrange
		String idInexistente = "00000000-0000-0000-0000-000000000001";

		// Act
		ResultActions resposta = mvc.perform(get("/compromissos/" + idInexistente));

		// Assert
		resposta.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.erro").value("NAO_ENCONTRADO"))
				.andExpect(mensagem("Compromisso não encontrado."));
	}

	@Test
	void consultarComIdMalFormadoResponde400() throws Exception {
		// Arrange
		String idMalFormado = "nao-e-um-uuid";

		// Act
		ResultActions resposta = mvc.perform(get("/compromissos/" + idMalFormado));

		// Assert
		resposta.andExpect(status().isBadRequest())
				.andExpect(mensagem("O valor de 'id' é inválido."));
	}

	@Test
	void criarSobrepostoAOutroAtivoResponde409() throws Exception {
		// Arrange
		criar("""
				{"titulo": "Primeiro", "inicio": "2026-11-10T10:00:00Z", "fim": "2026-11-10T11:00:00Z"}
				""");

		// Act
		ResultActions resposta = criar("""
				{"titulo": "Segundo", "inicio": "2026-11-10T10:30:00Z", "fim": "2026-11-10T11:30:00Z"}
				""");

		// Assert
		resposta.andExpect(status().isConflict())
				.andExpect(jsonPath("$.erro").value("CONFLITO_DE_HORARIO"))
				.andExpect(mensagem("Já existe um compromisso ativo nesse horário."));
	}

	@Test
	void criarQueComecaQuandoOutroTerminaResponde201() throws Exception {
		// Arrange
		criar("""
				{"titulo": "Primeiro", "inicio": "2026-11-10T10:00:00Z", "fim": "2026-11-10T11:00:00Z"}
				""");

		// Act
		ResultActions resposta = criar("""
				{"titulo": "Segundo", "inicio": "2026-11-10T11:00:00Z", "fim": "2026-11-10T12:00:00Z"}
				""");

		// Assert
		resposta.andExpect(status().isCreated());
	}

	@Test
	void cancelarDuasVezesResponde409() throws Exception {
		// Arrange
		String id = criarERetornarId("""
				{"titulo": "Reunião", "inicio": "2026-11-10T10:00:00Z", "fim": "2026-11-10T11:00:00Z"}
				""");
		mvc.perform(patch("/compromissos/" + id + "/cancelar"));

		// Act
		ResultActions resposta = mvc.perform(patch("/compromissos/" + id + "/cancelar"));

		// Assert
		resposta.andExpect(status().isConflict())
				.andExpect(jsonPath("$.erro").value("COMPROMISSO_CANCELADO"))
				.andExpect(mensagem("O compromisso já está cancelado."));
	}

	@Test
	void editarCanceladoResponde409() throws Exception {
		// Arrange
		String id = criarERetornarId("""
				{"titulo": "Reunião", "inicio": "2026-11-10T10:00:00Z", "fim": "2026-11-10T11:00:00Z"}
				""");
		mvc.perform(patch("/compromissos/" + id + "/cancelar"));

		// Act
		ResultActions resposta = mvc.perform(put("/compromissos/" + id).contentType(MediaType.APPLICATION_JSON)
				.content("""
				{"titulo": "Outro", "inicio": "2026-11-10T10:00:00Z", "fim": "2026-11-10T11:00:00Z"}
				"""));

		// Assert
		resposta.andExpect(status().isConflict())
				.andExpect(mensagem("Um compromisso cancelado não pode ser editado."));
	}
}
