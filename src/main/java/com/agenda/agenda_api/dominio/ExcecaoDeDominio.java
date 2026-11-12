package com.agenda.agenda_api.dominio;

/** Base das exceções que sinalizam a violação de uma regra de negócio. */
public abstract class ExcecaoDeDominio extends RuntimeException {

	protected ExcecaoDeDominio(String mensagem) {
		super(mensagem);
	}
}
