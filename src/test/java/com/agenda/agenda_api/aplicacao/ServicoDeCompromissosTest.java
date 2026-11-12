package com.agenda.agenda_api.aplicacao;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.agenda.agenda_api.dominio.Compromisso;
import com.agenda.agenda_api.dominio.CompromissoCanceladoNaoPodeSerEditadoExcecao;
import com.agenda.agenda_api.dominio.CompromissoJaCanceladoExcecao;
import com.agenda.agenda_api.dominio.CompromissoNaoEncontradoExcecao;
import com.agenda.agenda_api.dominio.ConflitoDeHorarioExcecao;
import com.agenda.agenda_api.dominio.StatusCompromisso;

class ServicoDeCompromissosTest {

	private static final UUID ID_INEXISTENTE = UUID.fromString("00000000-0000-0000-0000-000000000001");

	private RepositorioDeCompromissosEmMemoria repositorio;
	private ServicoDeCompromissos servico;

	@BeforeEach
	void prepararServico() {
		repositorio = new RepositorioDeCompromissosEmMemoria();
		servico = new ServicoDeCompromissos(repositorio);
	}

	private static DadosDoCompromisso dados(String titulo, String inicio, String fim) {
		return new DadosDoCompromisso(titulo, null, Instant.parse(inicio), Instant.parse(fim));
	}

	@Test
	void criarGravaUmCompromissoAtivo() {
		// Arrange
		DadosDoCompromisso dados = new DadosDoCompromisso("Dentista", "Levar exames",
				Instant.parse("2026-11-10T14:00:00Z"), Instant.parse("2026-11-10T15:00:00Z"));

		// Act
		Compromisso criado = servico.criar(dados);

		// Assert
		Compromisso gravado = repositorio.buscarPorId(criado.getId()).orElseThrow();
		assertThat(gravado.getTitulo()).isEqualTo("Dentista");
		assertThat(gravado.getStatus()).isEqualTo(StatusCompromisso.ATIVO);
	}

	@Test
	void criarSobrepostoAUmAtivoERejeitado() {
		// Arrange
		servico.criar(dados("Primeiro", "2026-11-10T10:00:00Z", "2026-11-10T11:00:00Z"));

		// Act
		Throwable erro = catchThrowable(
				() -> servico.criar(dados("Segundo", "2026-11-10T10:59:00Z", "2026-11-10T12:00:00Z")));

		// Assert
		assertThat(erro)
				.isInstanceOf(ConflitoDeHorarioExcecao.class)
				.hasMessage("Já existe um compromisso ativo nesse horário.");
		assertThat(repositorio.listarTodos()).hasSize(1);
	}

	@Test
	void criarQueComecaExatamenteQuandoOutroTerminaEAceito() {
		// Arrange
		servico.criar(dados("Primeiro", "2026-11-10T10:00:00Z", "2026-11-10T11:00:00Z"));

		// Act
		servico.criar(dados("Segundo", "2026-11-10T11:00:00Z", "2026-11-10T12:00:00Z"));

		// Assert
		assertThat(repositorio.listarTodos()).hasSize(2);
	}

	@Test
	void criarSobrepostoAUmCanceladoEAceito() {
		// Arrange
		Compromisso cancelado = servico.criar(
				dados("Cancelado", "2026-11-10T10:00:00Z", "2026-11-10T11:00:00Z"));
		servico.cancelar(cancelado.getId());

		// Act
		servico.criar(dados("Novo", "2026-11-10T10:00:00Z", "2026-11-10T11:00:00Z"));

		// Assert
		assertThat(repositorio.listarTodos()).hasSize(2);
	}

	@Test
	void buscarDevolveOCompromissoGravado() {
		// Arrange
		Compromisso criado = servico.criar(dados("Dentista", "2026-11-10T14:00:00Z", "2026-11-10T15:00:00Z"));

		// Act
		Compromisso encontrado = servico.buscar(criado.getId());

		// Assert
		assertThat(encontrado.getTitulo()).isEqualTo("Dentista");
	}

	@Test
	void buscarIdInexistenteERejeitado() {
		// Arrange
		UUID id = ID_INEXISTENTE;

		// Act
		Throwable erro = catchThrowable(() -> servico.buscar(id));

		// Assert
		assertThat(erro)
				.isInstanceOf(CompromissoNaoEncontradoExcecao.class)
				.hasMessage("Compromisso não encontrado.");
	}

	@Test
	void listarDevolveTodosOrdenadosPeloInicio() {
		// Arrange
		servico.criar(dados("Tarde", "2026-11-10T15:00:00Z", "2026-11-10T16:00:00Z"));
		servico.criar(dados("Manhã", "2026-11-10T08:00:00Z", "2026-11-10T09:00:00Z"));

		// Act
		List<Compromisso> compromissos = servico.listar();

		// Assert
		assertThat(compromissos).extracting(Compromisso::getTitulo).containsExactly("Manhã", "Tarde");
	}

	@Test
	void listarDoDiaTrazSoOsQueOcorremNaqueleDiaEmUtc() {
		// Arrange
		servico.criar(dados("Termina à meia-noite", "2026-11-09T23:00:00Z", "2026-11-10T00:00:00Z"));
		servico.criar(dados("Atravessa a meia-noite", "2026-11-10T23:30:00Z", "2026-11-11T00:30:00Z"));
		servico.criar(dados("No meio do dia", "2026-11-10T12:00:00Z", "2026-11-10T13:00:00Z"));
		servico.criar(dados("Dia seguinte", "2026-11-11T09:00:00Z", "2026-11-11T10:00:00Z"));

		// Act
		List<Compromisso> compromissos = servico.listarDoDia(LocalDate.parse("2026-11-10"));

		// Assert
		assertThat(compromissos).extracting(Compromisso::getTitulo)
				.containsExactly("No meio do dia", "Atravessa a meia-noite");
	}

	@Test
	void atualizarGravaOsNovosDados() {
		// Arrange
		Compromisso criado = servico.criar(dados("Reunião", "2026-11-10T10:00:00Z", "2026-11-10T11:00:00Z"));

		// Act
		servico.atualizar(criado.getId(),
				dados("Reunião longa", "2026-11-10T10:00:00Z", "2026-11-10T12:00:00Z"));

		// Assert
		Compromisso gravado = repositorio.buscarPorId(criado.getId()).orElseThrow();
		assertThat(gravado.getTitulo()).isEqualTo("Reunião longa");
		assertThat(gravado.getFim()).isEqualTo(Instant.parse("2026-11-10T12:00:00Z"));
	}

	@Test
	void atualizarDentroDoProprioHorarioNaoConflitaConsigoMesmo() {
		// Arrange
		Compromisso criado = servico.criar(dados("Reunião", "2026-11-10T10:00:00Z", "2026-11-10T11:00:00Z"));

		// Act
		Throwable erro = catchThrowable(() -> servico.atualizar(criado.getId(),
				dados("Reunião curta", "2026-11-10T10:15:00Z", "2026-11-10T10:45:00Z")));

		// Assert
		assertThat(erro).isNull();
	}

	@Test
	void atualizarParaHorarioDeOutroAtivoERejeitadoENaoAlteraOGravado() {
		// Arrange
		servico.criar(dados("Almoço", "2026-11-10T12:00:00Z", "2026-11-10T13:00:00Z"));
		Compromisso reuniao = servico.criar(dados("Reunião", "2026-11-10T10:00:00Z", "2026-11-10T11:00:00Z"));

		// Act
		Throwable erro = catchThrowable(() -> servico.atualizar(reuniao.getId(),
				dados("Reunião", "2026-11-10T12:30:00Z", "2026-11-10T13:30:00Z")));

		// Assert
		assertThat(erro).isInstanceOf(ConflitoDeHorarioExcecao.class);
		assertThat(repositorio.buscarPorId(reuniao.getId()).orElseThrow().getInicio())
				.isEqualTo(Instant.parse("2026-11-10T10:00:00Z"));
	}

	@Test
	void atualizarCanceladoERejeitado() {
		// Arrange
		Compromisso criado = servico.criar(dados("Reunião", "2026-11-10T10:00:00Z", "2026-11-10T11:00:00Z"));
		servico.cancelar(criado.getId());

		// Act
		Throwable erro = catchThrowable(() -> servico.atualizar(criado.getId(),
				dados("Outro", "2026-11-10T10:00:00Z", "2026-11-10T11:00:00Z")));

		// Assert
		assertThat(erro).isInstanceOf(CompromissoCanceladoNaoPodeSerEditadoExcecao.class);
	}

	@Test
	void atualizarIdInexistenteERejeitado() {
		// Arrange
		DadosDoCompromisso novosDados = dados("Outro", "2026-11-10T10:00:00Z", "2026-11-10T11:00:00Z");

		// Act
		Throwable erro = catchThrowable(() -> servico.atualizar(ID_INEXISTENTE, novosDados));

		// Assert
		assertThat(erro).isInstanceOf(CompromissoNaoEncontradoExcecao.class);
	}

	@Test
	void cancelarGravaOStatusCancelado() {
		// Arrange
		Compromisso criado = servico.criar(dados("Reunião", "2026-11-10T10:00:00Z", "2026-11-10T11:00:00Z"));

		// Act
		servico.cancelar(criado.getId());

		// Assert
		assertThat(repositorio.buscarPorId(criado.getId()).orElseThrow().getStatus())
				.isEqualTo(StatusCompromisso.CANCELADO);
	}

	@Test
	void cancelarDuasVezesERejeitado() {
		// Arrange
		Compromisso criado = servico.criar(dados("Reunião", "2026-11-10T10:00:00Z", "2026-11-10T11:00:00Z"));
		servico.cancelar(criado.getId());

		// Act
		Throwable erro = catchThrowable(() -> servico.cancelar(criado.getId()));

		// Assert
		assertThat(erro).isInstanceOf(CompromissoJaCanceladoExcecao.class);
	}

	@Test
	void cancelarIdInexistenteERejeitado() {
		// Arrange
		UUID id = ID_INEXISTENTE;

		// Act
		Throwable erro = catchThrowable(() -> servico.cancelar(id));

		// Assert
		assertThat(erro).isInstanceOf(CompromissoNaoEncontradoExcecao.class);
	}
}
