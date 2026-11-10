package com.agenda.agenda_api.web;

import java.time.Instant;
import java.util.UUID;

import com.agenda.agenda_api.dominio.Compromisso;
import com.agenda.agenda_api.dominio.StatusCompromisso;

/** Corpo de saída de um compromisso. Datas em ISO 8601, em UTC. */
public record CompromissoResposta(
		UUID id,
		String titulo,
		String descricao,
		Instant inicio,
		Instant fim,
		StatusCompromisso status) {

	static CompromissoResposta de(Compromisso compromisso) {
		return new CompromissoResposta(
				compromisso.getId(),
				compromisso.getTitulo(),
				compromisso.getDescricao().orElse(null),
				compromisso.getInicio(),
				compromisso.getFim(),
				compromisso.getStatus());
	}
}
