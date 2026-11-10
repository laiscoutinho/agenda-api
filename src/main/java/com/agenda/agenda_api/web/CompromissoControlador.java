package com.agenda.agenda_api.web;

import java.net.URI;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.agenda.agenda_api.aplicacao.ServicoDeCompromissos;
import com.agenda.agenda_api.dominio.Compromisso;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/compromissos")
class CompromissoControlador {

	private final ServicoDeCompromissos servico;

	CompromissoControlador(ServicoDeCompromissos servico) {
		this.servico = servico;
	}

	@PostMapping
	ResponseEntity<CompromissoResposta> criar(@Valid @RequestBody EntradaDeCompromisso entrada) {
		Compromisso criado = servico.criar(entrada.paraDados());
		URI local = URI.create("/compromissos/" + criado.getId());
		return ResponseEntity.created(local).body(CompromissoResposta.de(criado));
	}

	/** Lista todos, ou só os que ocorrem no dia informado (UTC), no formato AAAA-MM-DD. */
	@GetMapping
	List<CompromissoResposta> listar(@RequestParam(required = false) LocalDate data) {
		List<Compromisso> compromissos = data == null ? servico.listar() : servico.listarDoDia(data);
		return compromissos.stream().map(CompromissoResposta::de).toList();
	}

	@GetMapping("/{id}")
	CompromissoResposta buscar(@PathVariable UUID id) {
		return CompromissoResposta.de(servico.buscar(id));
	}

	@PutMapping("/{id}")
	CompromissoResposta atualizar(@PathVariable UUID id, @Valid @RequestBody EntradaDeCompromisso entrada) {
		return CompromissoResposta.de(servico.atualizar(id, entrada.paraDados()));
	}

	@PatchMapping("/{id}/cancelar")
	CompromissoResposta cancelar(@PathVariable UUID id) {
		return CompromissoResposta.de(servico.cancelar(id));
	}
}
