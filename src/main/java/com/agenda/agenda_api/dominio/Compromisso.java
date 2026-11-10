package com.agenda.agenda_api.dominio;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public class Compromisso {

	private final UUID id;
	private final String titulo;
	private final String descricao;
	private final Instant inicio;
	private final Instant fim;
	private StatusCompromisso status;

	private Compromisso(String titulo, String descricao, Instant inicio, Instant fim) {
		this.id = UUID.randomUUID();
		this.titulo = titulo;
		this.descricao = descricao;
		this.inicio = inicio;
		this.fim = fim;
		this.status = StatusCompromisso.ATIVO;
	}

	public static Compromisso criar(String titulo, String descricao, Instant inicio, Instant fim) {
		exigirTitulo(titulo);
		exigirHorarios(inicio, fim);
		exigirFimPosteriorAoInicio(inicio, fim);
		return new Compromisso(titulo, descricao, inicio, fim);
	}

	public void cancelar() {
		if (status == StatusCompromisso.CANCELADO) {
			throw new CompromissoJaCanceladoExcecao();
		}
		status = StatusCompromisso.CANCELADO;
	}

	private static void exigirTitulo(String titulo) {
		if (titulo == null || titulo.isBlank()) {
			throw new TituloObrigatorioExcecao();
		}
	}

	private static void exigirHorarios(Instant inicio, Instant fim) {
		if (inicio == null || fim == null) {
			throw new HorarioObrigatorioExcecao();
		}
	}

	private static void exigirFimPosteriorAoInicio(Instant inicio, Instant fim) {
		if (!fim.isAfter(inicio)) {
			throw new FimNaoPosteriorAoInicioExcecao();
		}
	}

	public UUID getId() {
		return id;
	}

	public String getTitulo() {
		return titulo;
	}

	public Optional<String> getDescricao() {
		return Optional.ofNullable(descricao);
	}

	public Instant getInicio() {
		return inicio;
	}

	public Instant getFim() {
		return fim;
	}

	public StatusCompromisso getStatus() {
		return status;
	}
}
