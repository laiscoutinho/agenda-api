package com.agenda.agenda_api.infraestrutura.persistencia;

import java.sql.SQLException;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Repository;

import com.agenda.agenda_api.dominio.Compromisso;
import com.agenda.agenda_api.dominio.ConflitoDeHorarioExcecao;
import com.agenda.agenda_api.dominio.RepositorioDeCompromissos;
import com.agenda.agenda_api.dominio.StatusCompromisso;

/** Implementação da porta de persistência com Spring Data JPA. */
@Repository
class RepositorioDeCompromissosJpa implements RepositorioDeCompromissos {

	/**
	 * SQLState do PostgreSQL para violação de restrição de exclusão. A única
	 * restrição desse tipo na tabela é a de sobreposição de compromissos ativos.
	 */
	private static final String VIOLACAO_DE_EXCLUSAO = "23P01";

	private final CompromissoJpaRepositorio jpa;

	RepositorioDeCompromissosJpa(CompromissoJpaRepositorio jpa) {
		this.jpa = jpa;
	}

	@Override
	public void salvar(Compromisso compromisso) {
		try {
			jpa.saveAndFlush(MapeadorDeCompromisso.paraEntidade(compromisso));
		} catch (DataIntegrityViolationException erro) {
			if (violouRestricaoDeSobreposicao(erro)) {
				throw new ConflitoDeHorarioExcecao();
			}
			throw erro;
		}
	}

	@Override
	public Optional<Compromisso> buscarPorId(UUID id) {
		return jpa.findById(id).map(MapeadorDeCompromisso::paraDominio);
	}

	@Override
	public List<Compromisso> listarTodos() {
		return paraDominio(jpa.findAllByOrderByInicioAsc());
	}

	@Override
	public List<Compromisso> listarQueSobrepoem(Instant inicio, Instant fim) {
		return paraDominio(jpa.listarQueSobrepoem(inicio, fim));
	}

	@Override
	public List<Compromisso> listarAtivosQueSobrepoem(Instant inicio, Instant fim) {
		return paraDominio(jpa.listarPorStatusQueSobrepoem(StatusCompromisso.ATIVO, inicio, fim));
	}

	private static List<Compromisso> paraDominio(List<CompromissoEntidade> entidades) {
		return entidades.stream().map(MapeadorDeCompromisso::paraDominio).toList();
	}

	private static boolean violouRestricaoDeSobreposicao(DataIntegrityViolationException erro) {
		for (Throwable causa = erro; causa != null; causa = causa.getCause()) {
			if (causa instanceof SQLException sql && VIOLACAO_DE_EXCLUSAO.equals(sql.getSQLState())) {
				return true;
			}
		}
		return false;
	}
}
