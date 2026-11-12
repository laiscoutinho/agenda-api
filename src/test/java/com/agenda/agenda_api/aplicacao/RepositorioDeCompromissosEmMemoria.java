package com.agenda.agenda_api.aplicacao;

import java.time.Instant;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import com.agenda.agenda_api.dominio.Compromisso;
import com.agenda.agenda_api.dominio.RepositorioDeCompromissos;

/**
 * Repositório em memória para os testes dos casos de uso, sem banco.
 * Guarda cópias, como um banco faria: alterar um objeto depois de salvo não
 * muda o que está gravado.
 */
class RepositorioDeCompromissosEmMemoria implements RepositorioDeCompromissos {

	private final Map<UUID, Compromisso> gravados = new LinkedHashMap<>();

	@Override
	public void salvar(Compromisso compromisso) {
		gravados.put(compromisso.getId(), copiar(compromisso));
	}

	@Override
	public Optional<Compromisso> buscarPorId(UUID id) {
		return Optional.ofNullable(gravados.get(id)).map(RepositorioDeCompromissosEmMemoria::copiar);
	}

	@Override
	public List<Compromisso> listarTodos() {
		return gravados.values().stream()
				.sorted(Comparator.comparing(Compromisso::getInicio))
				.map(RepositorioDeCompromissosEmMemoria::copiar)
				.toList();
	}

	@Override
	public List<Compromisso> listarQueSobrepoem(Instant inicio, Instant fim) {
		return listarTodos().stream().filter(c -> c.sobrepoe(inicio, fim)).toList();
	}

	@Override
	public List<Compromisso> listarAtivosQueSobrepoem(Instant inicio, Instant fim) {
		return listarQueSobrepoem(inicio, fim).stream().filter(c -> !c.estaCancelado()).toList();
	}

	private static Compromisso copiar(Compromisso original) {
		return Compromisso.reconstituir(original.getId(), original.getTitulo(),
				original.getDescricao().orElse(null), original.getInicio(), original.getFim(),
				original.getStatus());
	}
}
