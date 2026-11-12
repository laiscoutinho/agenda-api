package com.agenda.agenda_api.dominio;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Porta de persistência dos compromissos. O domínio define o contrato e a
 * infraestrutura o implementa (inversão de dependência).
 */
public interface RepositorioDeCompromissos {

	/**
	 * Grava um compromisso novo ou alterado.
	 *
	 * @throws ConflitoDeHorarioExcecao se o compromisso ativo se sobrepuser a outro ativo
	 */
	void salvar(Compromisso compromisso);

	Optional<Compromisso> buscarPorId(UUID id);

	/** Todos os compromissos, ordenados pelo início. */
	List<Compromisso> listarTodos();

	/** Compromissos de qualquer status que se sobrepõem ao intervalo [inicio, fim), ordenados pelo início. */
	List<Compromisso> listarQueSobrepoem(Instant inicio, Instant fim);

	/** Compromissos ativos que se sobrepõem ao intervalo [inicio, fim). */
	List<Compromisso> listarAtivosQueSobrepoem(Instant inicio, Instant fim);
}
