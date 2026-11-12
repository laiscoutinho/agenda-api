package com.agenda.agenda_api.infraestrutura.configuracao;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.agenda.agenda_api.aplicacao.ServicoDeCompromissos;
import com.agenda.agenda_api.dominio.RepositorioDeCompromissos;

/**
 * Liga os casos de uso ao Spring. A camada de aplicação não conhece o Spring:
 * recebe o repositório pelo construtor e é registrada como bean aqui.
 */
@Configuration
class ConfiguracaoDosCasosDeUso {

	@Bean
	ServicoDeCompromissos servicoDeCompromissos(RepositorioDeCompromissos repositorio) {
		return new ServicoDeCompromissos(repositorio);
	}
}
