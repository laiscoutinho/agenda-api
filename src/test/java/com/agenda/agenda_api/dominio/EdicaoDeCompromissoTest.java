package com.agenda.agenda_api.dominio;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import java.time.Instant;

import org.junit.jupiter.api.Test;

class EdicaoDeCompromissoTest {

	private static Compromisso compromissoDasDezAsOnze() {
		return Compromisso.criar("Reunião", "Pauta antiga",
				Instant.parse("2026-11-10T10:00:00Z"), Instant.parse("2026-11-10T11:00:00Z"));
	}

	@Test
	void atualizarTrocaTodosOsDados() {
		// Arrange
		Compromisso compromisso = compromissoDasDezAsOnze();

		// Act
		compromisso.atualizar("Reunião de equipe", "Pauta nova",
				Instant.parse("2026-11-11T14:00:00Z"), Instant.parse("2026-11-11T15:30:00Z"));

		// Assert
		assertThat(compromisso.getTitulo()).isEqualTo("Reunião de equipe");
		assertThat(compromisso.getDescricao()).contains("Pauta nova");
		assertThat(compromisso.getInicio()).isEqualTo(Instant.parse("2026-11-11T14:00:00Z"));
		assertThat(compromisso.getFim()).isEqualTo(Instant.parse("2026-11-11T15:30:00Z"));
	}

	@Test
	void atualizarComTituloEmBrancoERejeitadoEMantemOsDadosAntigos() {
		// Arrange
		Compromisso compromisso = compromissoDasDezAsOnze();

		// Act
		Throwable erro = catchThrowable(() -> compromisso.atualizar("  ", "Pauta nova",
				Instant.parse("2026-11-11T14:00:00Z"), Instant.parse("2026-11-11T15:00:00Z")));

		// Assert
		assertThat(erro).isInstanceOf(TituloObrigatorioExcecao.class);
		assertThat(compromisso.getTitulo()).isEqualTo("Reunião");
		assertThat(compromisso.getInicio()).isEqualTo(Instant.parse("2026-11-10T10:00:00Z"));
	}

	@Test
	void atualizarComFimIgualAoInicioERejeitado() {
		// Arrange
		Compromisso compromisso = compromissoDasDezAsOnze();

		// Act
		Throwable erro = catchThrowable(() -> compromisso.atualizar("Reunião", null,
				Instant.parse("2026-11-11T14:00:00Z"), Instant.parse("2026-11-11T14:00:00Z")));

		// Assert
		assertThat(erro).isInstanceOf(FimNaoPosteriorAoInicioExcecao.class);
	}

	@Test
	void compromissoCanceladoNaoPodeSerEditado() {
		// Arrange
		Compromisso compromisso = compromissoDasDezAsOnze();
		compromisso.cancelar();

		// Act
		Throwable erro = catchThrowable(() -> compromisso.atualizar("Outro título", null,
				Instant.parse("2026-11-11T14:00:00Z"), Instant.parse("2026-11-11T15:00:00Z")));

		// Assert
		assertThat(erro)
				.isInstanceOf(CompromissoCanceladoNaoPodeSerEditadoExcecao.class)
				.isInstanceOf(ExcecaoDeDominio.class)
				.hasMessage("Um compromisso cancelado não pode ser editado.");
		assertThat(compromisso.getTitulo()).isEqualTo("Reunião");
	}

	@Test
	void intervaloQueCruzaOCompromissoSobrepoe() {
		// Arrange
		Compromisso compromisso = compromissoDasDezAsOnze();

		// Act
		boolean sobrepoe = compromisso.sobrepoe(
				Instant.parse("2026-11-10T10:30:00Z"), Instant.parse("2026-11-10T11:30:00Z"));

		// Assert
		assertThat(sobrepoe).isTrue();
	}

	@Test
	void intervaloQueContemOCompromissoSobrepoe() {
		// Arrange
		Compromisso compromisso = compromissoDasDezAsOnze();

		// Act
		boolean sobrepoe = compromisso.sobrepoe(
				Instant.parse("2026-11-10T09:00:00Z"), Instant.parse("2026-11-10T12:00:00Z"));

		// Assert
		assertThat(sobrepoe).isTrue();
	}

	@Test
	void intervaloQueTerminaQuandoOCompromissoComecaNaoSobrepoe() {
		// Arrange
		Compromisso compromisso = compromissoDasDezAsOnze();

		// Act
		boolean sobrepoe = compromisso.sobrepoe(
				Instant.parse("2026-11-10T09:00:00Z"), Instant.parse("2026-11-10T10:00:00Z"));

		// Assert
		assertThat(sobrepoe).isFalse();
	}

	@Test
	void intervaloQueComecaQuandoOCompromissoTerminaNaoSobrepoe() {
		// Arrange
		Compromisso compromisso = compromissoDasDezAsOnze();

		// Act
		boolean sobrepoe = compromisso.sobrepoe(
				Instant.parse("2026-11-10T11:00:00Z"), Instant.parse("2026-11-10T12:00:00Z"));

		// Assert
		assertThat(sobrepoe).isFalse();
	}
}
