package com.agenda.agenda_api.dominio;

public class ConflitoDeHorarioExcecao extends ExcecaoDeDominio {

	public ConflitoDeHorarioExcecao() {
		super("Já existe um compromisso ativo nesse horário.");
	}
}
