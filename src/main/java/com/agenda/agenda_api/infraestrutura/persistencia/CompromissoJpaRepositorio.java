package com.agenda.agenda_api.infraestrutura.persistencia;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.agenda.agenda_api.dominio.StatusCompromisso;

interface CompromissoJpaRepositorio extends JpaRepository<CompromissoEntidade, UUID> {

	List<CompromissoEntidade> findAllByOrderByInicioAsc();

	/** Sobreposição de intervalos meio abertos: começa antes do fim do outro e termina depois do início dele. */
	@Query("""
			select c from CompromissoEntidade c
			where c.inicio < :fim and c.fim > :inicio
			order by c.inicio
			""")
	List<CompromissoEntidade> listarQueSobrepoem(@Param("inicio") Instant inicio, @Param("fim") Instant fim);

	@Query("""
			select c from CompromissoEntidade c
			where c.status = :status and c.inicio < :fim and c.fim > :inicio
			order by c.inicio
			""")
	List<CompromissoEntidade> listarPorStatusQueSobrepoem(@Param("status") StatusCompromisso status,
			@Param("inicio") Instant inicio, @Param("fim") Instant fim);
}
