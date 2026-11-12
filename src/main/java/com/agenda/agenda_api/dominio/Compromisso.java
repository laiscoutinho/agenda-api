package com.agenda.agenda_api.dominio;

import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public class Compromisso {

	private final UUID id;
	private String titulo;
	private String descricao;
	private Instant inicio;
	private Instant fim;
	private StatusCompromisso status;

	private Compromisso(UUID id, String titulo, String descricao, Instant inicio, Instant fim,
			StatusCompromisso status) {
		this.id = Objects.requireNonNull(id, "id");
		this.titulo = titulo;
		this.descricao = descricao;
		this.inicio = inicio;
		this.fim = fim;
		this.status = Objects.requireNonNull(status, "status");
	}

	public static Compromisso criar(String titulo, String descricao, Instant inicio, Instant fim) {
		validarDados(titulo, inicio, fim);
		return new Compromisso(UUID.randomUUID(), titulo, descricao, inicio, fim, StatusCompromisso.ATIVO);
	}

	/** Recria um compromisso já existente (por exemplo, lido do banco), mantendo id e status. */
	public static Compromisso reconstituir(UUID id, String titulo, String descricao, Instant inicio, Instant fim,
			StatusCompromisso status) {
		validarDados(titulo, inicio, fim);
		return new Compromisso(id, titulo, descricao, inicio, fim, status);
	}

	public void cancelar() {
		if (estaCancelado()) {
			throw new CompromissoJaCanceladoExcecao();
		}
		status = StatusCompromisso.CANCELADO;
	}

	/** Troca os dados do compromisso, com as mesmas validações da criação. */
	public void atualizar(String novoTitulo, String novaDescricao, Instant novoInicio, Instant novoFim) {
		if (estaCancelado()) {
			throw new CompromissoCanceladoNaoPodeSerEditadoExcecao();
		}
		validarDados(novoTitulo, novoInicio, novoFim);
		titulo = novoTitulo;
		descricao = novaDescricao;
		inicio = novoInicio;
		fim = novoFim;
	}

	/**
	 * Indica se o compromisso ocupa parte do intervalo [outroInicio, outroFim).
	 * Encostar nas bordas (um termina quando o outro começa) não é sobreposição.
	 */
	public boolean sobrepoe(Instant outroInicio, Instant outroFim) {
		return inicio.isBefore(outroFim) && outroInicio.isBefore(fim);
	}

	public boolean estaCancelado() {
		return status == StatusCompromisso.CANCELADO;
	}

	private static void validarDados(String titulo, Instant inicio, Instant fim) {
		exigirTitulo(titulo);
		exigirHorarios(inicio, fim);
		exigirFimPosteriorAoInicio(inicio, fim);
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
