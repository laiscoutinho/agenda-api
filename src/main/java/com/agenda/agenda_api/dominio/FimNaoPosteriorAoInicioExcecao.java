package com.agenda.agenda_api.dominio;

public class FimNaoPosteriorAoInicioExcecao extends ExcecaoDeDominio {

	public FimNaoPosteriorAoInicioExcecao() {
		super("O fim do compromisso deve ser posterior ao início.");
	}
}
