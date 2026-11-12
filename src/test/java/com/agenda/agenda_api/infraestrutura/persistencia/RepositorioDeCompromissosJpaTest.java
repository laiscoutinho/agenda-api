package com.agenda.agenda_api.infraestrutura.persistencia;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;

import com.agenda.agenda_api.dominio.Compromisso;
import com.agenda.agenda_api.dominio.ConflitoDeHorarioExcecao;
import com.agenda.agenda_api.dominio.StatusCompromisso;

/**
 * Testa o adaptador contra o PostgreSQL real (o mesmo do CI), com o esquema
 * criado pelas migrações do Flyway. Cada teste roda numa transação desfeita no fim.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(RepositorioDeCompromissosJpa.class)
class RepositorioDeCompromissosJpaTest {

	@Autowired
	private RepositorioDeCompromissosJpa repositorio;

	@Test
	void compromissoSalvoPodeSerBuscadoPeloId() {
		// Arrange
		Compromisso compromisso = Compromisso.criar("Dentista", "Levar exames",
				Instant.parse("2026-11-10T14:00:00Z"), Instant.parse("2026-11-10T15:00:00Z"));

		// Act
		repositorio.salvar(compromisso);
		Optional<Compromisso> encontrado = repositorio.buscarPorId(compromisso.getId());

		// Assert
		assertThat(encontrado).isPresent();
		assertThat(encontrado.get().getTitulo()).isEqualTo("Dentista");
		assertThat(encontrado.get().getDescricao()).contains("Levar exames");
		assertThat(encontrado.get().getInicio()).isEqualTo(Instant.parse("2026-11-10T14:00:00Z"));
		assertThat(encontrado.get().getFim()).isEqualTo(Instant.parse("2026-11-10T15:00:00Z"));
		assertThat(encontrado.get().getStatus()).isEqualTo(StatusCompromisso.ATIVO);
	}

	@Test
	void buscarIdInexistenteNaoEncontraNada() {
		// Arrange
		UUID idInexistente = UUID.fromString("00000000-0000-0000-0000-000000000001");

		// Act
		Optional<Compromisso> encontrado = repositorio.buscarPorId(idInexistente);

		// Assert
		assertThat(encontrado).isEmpty();
	}

	@Test
	void cancelamentoSalvoFicaGravado() {
		// Arrange
		Compromisso compromisso = salvar("Reunião", "2026-11-10T09:00:00Z", "2026-11-10T10:00:00Z");
		compromisso.cancelar();

		// Act
		repositorio.salvar(compromisso);

		// Assert
		assertThat(repositorio.buscarPorId(compromisso.getId()).orElseThrow().getStatus())
				.isEqualTo(StatusCompromisso.CANCELADO);
	}

	@Test
	void listarTodosOrdenaPeloInicio() {
		// Arrange
		salvar("Tarde", "2026-11-10T15:00:00Z", "2026-11-10T16:00:00Z");
		salvar("Manhã", "2026-11-10T08:00:00Z", "2026-11-10T09:00:00Z");

		// Act
		List<Compromisso> compromissos = repositorio.listarTodos();

		// Assert
		assertThat(compromissos).extracting(Compromisso::getTitulo).containsExactly("Manhã", "Tarde");
	}

	@Test
	void listarQueSobrepoemIgnoraQuemSoEncostaNasBordas() {
		// Arrange
		salvar("Termina no início", "2026-11-10T09:00:00Z", "2026-11-10T10:00:00Z");
		salvar("Dentro", "2026-11-10T10:30:00Z", "2026-11-10T11:00:00Z");
		salvar("Começa no fim", "2026-11-10T12:00:00Z", "2026-11-10T13:00:00Z");

		// Act
		List<Compromisso> compromissos = repositorio.listarQueSobrepoem(
				Instant.parse("2026-11-10T10:00:00Z"), Instant.parse("2026-11-10T12:00:00Z"));

		// Assert
		assertThat(compromissos).extracting(Compromisso::getTitulo).containsExactly("Dentro");
	}

	@Test
	void listarAtivosQueSobrepoemIgnoraCancelados() {
		// Arrange
		Compromisso cancelado = salvar("Cancelado", "2026-11-10T10:00:00Z", "2026-11-10T11:00:00Z");
		cancelado.cancelar();
		repositorio.salvar(cancelado);

		// Act
		List<Compromisso> compromissos = repositorio.listarAtivosQueSobrepoem(
				Instant.parse("2026-11-10T10:00:00Z"), Instant.parse("2026-11-10T11:00:00Z"));

		// Assert
		assertThat(compromissos).isEmpty();
	}

	@Test
	void bancoRejeitaDoisAtivosSobrepostos() {
		// Arrange
		salvar("Primeiro", "2026-11-10T10:00:00Z", "2026-11-10T11:00:00Z");
		Compromisso sobreposto = Compromisso.criar("Segundo", null,
				Instant.parse("2026-11-10T10:30:00Z"), Instant.parse("2026-11-10T11:30:00Z"));

		// Act
		Throwable erro = catchThrowable(() -> repositorio.salvar(sobreposto));

		// Assert
		assertThat(erro)
				.isInstanceOf(ConflitoDeHorarioExcecao.class)
				.hasMessage("Já existe um compromisso ativo nesse horário.");
	}

	@Test
	void bancoAceitaCompromissoQueComecaQuandoOutroTermina() {
		// Arrange
		salvar("Primeiro", "2026-11-10T10:00:00Z", "2026-11-10T11:00:00Z");

		// Act
		salvar("Segundo", "2026-11-10T11:00:00Z", "2026-11-10T12:00:00Z");

		// Assert
		assertThat(repositorio.listarTodos()).hasSize(2);
	}

	private Compromisso salvar(String titulo, String inicio, String fim) {
		Compromisso compromisso = Compromisso.criar(titulo, null, Instant.parse(inicio), Instant.parse(fim));
		repositorio.salvar(compromisso);
		return compromisso;
	}
}
