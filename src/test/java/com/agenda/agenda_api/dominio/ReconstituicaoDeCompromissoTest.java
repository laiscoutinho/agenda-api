package com.agenda.agenda_api.dominio;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.Test;

class ReconstituicaoDeCompromissoTest {

	private static final UUID ID = UUID.fromString("0b6f3c1e-8a4d-4c2b-9f1a-2d3e4f5a6b7c");
	private static final Instant INICIO = Instant.parse("2026-11-10T14:00:00Z");
	private static final Instant FIM = Instant.parse("2026-11-10T15:00:00Z");

	@Test
	void reconstituirMantemOIdentificadorSalvo() {
		// Arrange
		UUID idSalvo = UUID.fromString("0b6f3c1e-8a4d-4c2b-9f1a-2d3e4f5a6b7c");

		// Act
		Compromisso compromisso = Compromisso.reconstituir(
				idSalvo, "Reunião", null, INICIO, FIM, StatusCompromisso.ATIVO);

		// Assert
		assertThat(compromisso.getId()).isEqualTo(UUID.fromString("0b6f3c1e-8a4d-4c2b-9f1a-2d3e4f5a6b7c"));
		assertThat(compromisso.getTitulo()).isEqualTo("Reunião");
		assertThat(compromisso.getDescricao()).isEmpty();
	}

	@Test
	void reconstituirMantemOStatusSalvo() {
		// Arrange
		StatusCompromisso statusSalvo = StatusCompromisso.CANCELADO;

		// Act
		Compromisso compromisso = Compromisso.reconstituir(ID, "Reunião", "Pauta", INICIO, FIM, statusSalvo);

		// Assert
		assertThat(compromisso.getStatus()).isEqualTo(StatusCompromisso.CANCELADO);
	}

	@Test
	void compromissoReconstituidoJaCanceladoNaoPodeSerCanceladoDeNovo() {
		// Arrange
		Compromisso compromisso = Compromisso.reconstituir(
				ID, "Reunião", "Pauta", INICIO, FIM, StatusCompromisso.CANCELADO);

		// Act
		Throwable erro = catchThrowable(compromisso::cancelar);

		// Assert
		assertThat(erro).isInstanceOf(CompromissoJaCanceladoExcecao.class);
	}
}
