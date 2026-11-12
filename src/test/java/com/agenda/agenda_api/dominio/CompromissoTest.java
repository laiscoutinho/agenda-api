package com.agenda.agenda_api.dominio;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.Test;

class CompromissoTest {

	private static final String TITULO = "Consulta no dentista";
	private static final String DESCRICAO = "Levar os exames";
	private static final Instant INICIO = Instant.parse("2026-11-10T14:00:00Z");
	private static final Instant FIM = Instant.parse("2026-11-10T15:00:00Z");

	@Test
	void criarComDadosValidosGuardaOsValoresInformados() {
		// Arrange
		String titulo = "Consulta no dentista";
		String descricao = "Levar os exames";
		Instant inicio = Instant.parse("2026-11-10T14:00:00Z");
		Instant fim = Instant.parse("2026-11-10T15:00:00Z");

		// Act
		Compromisso compromisso = Compromisso.criar(titulo, descricao, inicio, fim);

		// Assert
		assertThat(compromisso.getTitulo()).isEqualTo("Consulta no dentista");
		assertThat(compromisso.getDescricao()).contains("Levar os exames");
		assertThat(compromisso.getInicio()).isEqualTo(Instant.parse("2026-11-10T14:00:00Z"));
		assertThat(compromisso.getFim()).isEqualTo(Instant.parse("2026-11-10T15:00:00Z"));
	}

	@Test
	void compromissoRecemCriadoFicaAtivo() {
		// Arrange
		// (dados válidos nas constantes da classe)

		// Act
		Compromisso compromisso = Compromisso.criar(TITULO, DESCRICAO, INICIO, FIM);

		// Assert
		assertThat(compromisso.getStatus()).isEqualTo(StatusCompromisso.ATIVO);
	}

	@Test
	void compromissoRecemCriadoRecebeUmIdentificador() {
		// Arrange
		// (dados válidos nas constantes da classe)

		// Act
		Compromisso compromisso = Compromisso.criar(TITULO, DESCRICAO, INICIO, FIM);

		// Assert
		assertThat(compromisso.getId()).isNotNull();
	}

	@Test
	void doisCompromissosCriadosRecebemIdentificadoresDiferentes() {
		// Arrange
		Compromisso primeiro = Compromisso.criar(TITULO, DESCRICAO, INICIO, FIM);

		// Act
		Compromisso segundo = Compromisso.criar(TITULO, DESCRICAO, INICIO, FIM);

		// Assert
		UUID idDoPrimeiro = primeiro.getId();
		assertThat(segundo.getId()).isNotEqualTo(idDoPrimeiro);
	}

	@Test
	void criarSemDescricaoDeixaADescricaoVazia() {
		// Arrange
		String descricaoAusente = null;

		// Act
		Compromisso compromisso = Compromisso.criar(TITULO, descricaoAusente, INICIO, FIM);

		// Assert
		assertThat(compromisso.getDescricao()).isEmpty();
	}

	@Test
	void tituloVazioERejeitado() {
		// Arrange
		String tituloVazio = "";

		// Act
		Throwable erro = catchThrowable(() -> Compromisso.criar(tituloVazio, DESCRICAO, INICIO, FIM));

		// Assert
		assertThat(erro)
				.isInstanceOf(TituloObrigatorioExcecao.class)
				.hasMessage("O título do compromisso é obrigatório.");
	}

	@Test
	void tituloSoComEspacosERejeitado() {
		// Arrange
		String tituloSoComEspacos = "   ";

		// Act
		Throwable erro = catchThrowable(() -> Compromisso.criar(tituloSoComEspacos, DESCRICAO, INICIO, FIM));

		// Assert
		assertThat(erro).isInstanceOf(TituloObrigatorioExcecao.class);
	}

	@Test
	void tituloSoComTabulacaoEQuebraDeLinhaERejeitado() {
		// Arrange
		String tituloSoComCaracteresInvisiveis = "\t\n";

		// Act
		Throwable erro = catchThrowable(
				() -> Compromisso.criar(tituloSoComCaracteresInvisiveis, DESCRICAO, INICIO, FIM));

		// Assert
		assertThat(erro).isInstanceOf(TituloObrigatorioExcecao.class);
	}

	@Test
	void tituloAusenteERejeitado() {
		// Arrange
		String tituloAusente = null;

		// Act
		Throwable erro = catchThrowable(() -> Compromisso.criar(tituloAusente, DESCRICAO, INICIO, FIM));

		// Assert
		assertThat(erro).isInstanceOf(TituloObrigatorioExcecao.class);
	}

	@Test
	void fimIgualAoInicioERejeitado() {
		// Arrange
		Instant inicio = Instant.parse("2026-11-10T14:00:00Z");
		Instant fim = Instant.parse("2026-11-10T14:00:00Z");

		// Act
		Throwable erro = catchThrowable(() -> Compromisso.criar(TITULO, DESCRICAO, inicio, fim));

		// Assert
		assertThat(erro)
				.isInstanceOf(FimNaoPosteriorAoInicioExcecao.class)
				.hasMessage("O fim do compromisso deve ser posterior ao início.");
	}

	@Test
	void fimAnteriorAoInicioERejeitado() {
		// Arrange
		Instant inicio = Instant.parse("2026-11-10T14:00:00Z");
		Instant fim = Instant.parse("2026-11-10T13:00:00Z");

		// Act
		Throwable erro = catchThrowable(() -> Compromisso.criar(TITULO, DESCRICAO, inicio, fim));

		// Assert
		assertThat(erro).isInstanceOf(FimNaoPosteriorAoInicioExcecao.class);
	}

	@Test
	void fimUmNanossegundoDepoisDoInicioEAceito() {
		// Arrange
		Instant inicio = Instant.parse("2026-11-10T14:00:00Z");
		Instant fim = Instant.parse("2026-11-10T14:00:00.000000001Z");

		// Act
		Compromisso compromisso = Compromisso.criar(TITULO, DESCRICAO, inicio, fim);

		// Assert
		assertThat(compromisso.getFim()).isEqualTo(Instant.parse("2026-11-10T14:00:00.000000001Z"));
	}

	@Test
	void inicioAusenteERejeitado() {
		// Arrange
		Instant inicioAusente = null;

		// Act
		Throwable erro = catchThrowable(() -> Compromisso.criar(TITULO, DESCRICAO, inicioAusente, FIM));

		// Assert
		assertThat(erro)
				.isInstanceOf(HorarioObrigatorioExcecao.class)
				.hasMessage("O início e o fim do compromisso são obrigatórios.");
	}

	@Test
	void fimAusenteERejeitado() {
		// Arrange
		Instant fimAusente = null;

		// Act
		Throwable erro = catchThrowable(() -> Compromisso.criar(TITULO, DESCRICAO, INICIO, fimAusente));

		// Assert
		assertThat(erro).isInstanceOf(HorarioObrigatorioExcecao.class);
	}

	@Test
	void todasAsExcecoesDeValidacaoSaoExcecoesDeDominio() {
		// Arrange
		String tituloVazio = "";

		// Act
		Throwable erro = catchThrowable(() -> Compromisso.criar(tituloVazio, DESCRICAO, INICIO, FIM));

		// Assert
		assertThat(erro).isInstanceOf(ExcecaoDeDominio.class);
	}

	@Test
	void cancelarCompromissoAtivoMudaOStatusParaCancelado() {
		// Arrange
		Compromisso compromisso = Compromisso.criar(TITULO, DESCRICAO, INICIO, FIM);

		// Act
		compromisso.cancelar();

		// Assert
		assertThat(compromisso.getStatus()).isEqualTo(StatusCompromisso.CANCELADO);
	}

	@Test
	void cancelarDuasVezesERejeitado() {
		// Arrange
		Compromisso compromisso = Compromisso.criar(TITULO, DESCRICAO, INICIO, FIM);
		compromisso.cancelar();

		// Act
		Throwable erro = catchThrowable(compromisso::cancelar);

		// Assert
		assertThat(erro)
				.isInstanceOf(CompromissoJaCanceladoExcecao.class)
				.isInstanceOf(ExcecaoDeDominio.class)
				.hasMessage("O compromisso já está cancelado.");
	}

	@Test
	void segundoCancelamentoRejeitadoMantemOCompromissoCancelado() {
		// Arrange
		Compromisso compromisso = Compromisso.criar(TITULO, DESCRICAO, INICIO, FIM);
		compromisso.cancelar();

		// Act
		catchThrowable(compromisso::cancelar);

		// Assert
		assertThat(compromisso.getStatus()).isEqualTo(StatusCompromisso.CANCELADO);
	}
}
