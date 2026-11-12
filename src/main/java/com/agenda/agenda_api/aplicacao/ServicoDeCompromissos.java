package com.agenda.agenda_api.aplicacao;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

import com.agenda.agenda_api.dominio.Compromisso;
import com.agenda.agenda_api.dominio.CompromissoNaoEncontradoExcecao;
import com.agenda.agenda_api.dominio.ConflitoDeHorarioExcecao;
import com.agenda.agenda_api.dominio.RepositorioDeCompromissos;

/**
 * Casos de uso da agenda. Coordena o domínio e o repositório; as regras de
 * cada compromisso ficam na entidade, e a regra entre compromissos
 * (sobreposição) fica aqui, porque precisa consultar os outros.
 */
public class ServicoDeCompromissos {

	private final RepositorioDeCompromissos repositorio;

	public ServicoDeCompromissos(RepositorioDeCompromissos repositorio) {
		this.repositorio = repositorio;
	}

	public Compromisso criar(DadosDoCompromisso dados) {
		Compromisso compromisso = Compromisso.criar(
				dados.titulo(), dados.descricao(), dados.inicio(), dados.fim());
		exigirHorarioLivre(compromisso);
		repositorio.salvar(compromisso);
		return compromisso;
	}

	public List<Compromisso> listar() {
		return repositorio.listarTodos();
	}

	/** Compromissos que ocupam alguma parte do dia informado, em UTC. */
	public List<Compromisso> listarDoDia(LocalDate dia) {
		Instant inicioDoDia = dia.atStartOfDay(ZoneOffset.UTC).toInstant();
		Instant inicioDoDiaSeguinte = dia.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant();
		return repositorio.listarQueSobrepoem(inicioDoDia, inicioDoDiaSeguinte);
	}

	public Compromisso buscar(UUID id) {
		return repositorio.buscarPorId(id).orElseThrow(CompromissoNaoEncontradoExcecao::new);
	}

	public Compromisso atualizar(UUID id, DadosDoCompromisso dados) {
		Compromisso compromisso = buscar(id);
		compromisso.atualizar(dados.titulo(), dados.descricao(), dados.inicio(), dados.fim());
		exigirHorarioLivre(compromisso);
		repositorio.salvar(compromisso);
		return compromisso;
	}

	public Compromisso cancelar(UUID id) {
		Compromisso compromisso = buscar(id);
		compromisso.cancelar();
		repositorio.salvar(compromisso);
		return compromisso;
	}

	/** Rejeita o compromisso se outro ativo, que não seja ele mesmo, ocupar o mesmo horário. */
	private void exigirHorarioLivre(Compromisso compromisso) {
		List<Compromisso> ativosNoHorario =
				repositorio.listarAtivosQueSobrepoem(compromisso.getInicio(), compromisso.getFim());
		boolean temConflito = ativosNoHorario.stream()
				.anyMatch(outro -> !outro.getId().equals(compromisso.getId()));
		if (temConflito) {
			throw new ConflitoDeHorarioExcecao();
		}
	}
}
