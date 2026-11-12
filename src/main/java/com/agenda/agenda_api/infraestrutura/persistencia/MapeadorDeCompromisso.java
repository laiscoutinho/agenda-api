package com.agenda.agenda_api.infraestrutura.persistencia;

import com.agenda.agenda_api.dominio.Compromisso;

/** Converte entre a entidade de domínio e a entidade JPA. */
final class MapeadorDeCompromisso {

	private MapeadorDeCompromisso() {
	}

	static CompromissoEntidade paraEntidade(Compromisso compromisso) {
		return new CompromissoEntidade(
				compromisso.getId(),
				compromisso.getTitulo(),
				compromisso.getDescricao().orElse(null),
				compromisso.getInicio(),
				compromisso.getFim(),
				compromisso.getStatus());
	}

	static Compromisso paraDominio(CompromissoEntidade entidade) {
		return Compromisso.reconstituir(
				entidade.getId(),
				entidade.getTitulo(),
				entidade.getDescricao(),
				entidade.getInicio(),
				entidade.getFim(),
				entidade.getStatus());
	}
}
