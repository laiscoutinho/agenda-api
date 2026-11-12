package com.agenda.agenda_api.infraestrutura.persistencia;

import java.time.Instant;
import java.util.UUID;

import com.agenda.agenda_api.dominio.StatusCompromisso;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/** Representação JPA do compromisso. Fica separada da entidade de domínio. */
@Entity
@Table(name = "compromisso")
public class CompromissoEntidade {

	@Id
	private UUID id;

	@Column(nullable = false, length = 200)
	private String titulo;

	private String descricao;

	@Column(nullable = false)
	private Instant inicio;

	@Column(nullable = false)
	private Instant fim;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private StatusCompromisso status;

	protected CompromissoEntidade() {
		// exigido pelo JPA
	}

	CompromissoEntidade(UUID id, String titulo, String descricao, Instant inicio, Instant fim,
			StatusCompromisso status) {
		this.id = id;
		this.titulo = titulo;
		this.descricao = descricao;
		this.inicio = inicio;
		this.fim = fim;
		this.status = status;
	}

	UUID getId() {
		return id;
	}

	String getTitulo() {
		return titulo;
	}

	String getDescricao() {
		return descricao;
	}

	Instant getInicio() {
		return inicio;
	}

	Instant getFim() {
		return fim;
	}

	StatusCompromisso getStatus() {
		return status;
	}
}
