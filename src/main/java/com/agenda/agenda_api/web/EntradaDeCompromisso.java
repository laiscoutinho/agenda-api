package com.agenda.agenda_api.web;

import java.time.Instant;

import com.agenda.agenda_api.aplicacao.DadosDoCompromisso;

import jakarta.validation.constraints.Size;

/**
 * Corpo de entrada do POST e do PUT. Aqui só se valida o formato; as regras
 * de negócio (título obrigatório, fim depois do início) ficam no domínio.
 */
public record EntradaDeCompromisso(
		@Size(max = 200, message = "O título deve ter no máximo 200 caracteres.") String titulo,
		@Size(max = 2000, message = "A descrição deve ter no máximo 2000 caracteres.") String descricao,
		Instant inicio,
		Instant fim) {

	DadosDoCompromisso paraDados() {
		return new DadosDoCompromisso(titulo, descricao, inicio, fim);
	}
}
