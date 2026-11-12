package com.agenda.agenda_api.aplicacao;

import java.time.Instant;

/** Dados informados para criar ou atualizar um compromisso. A descrição é opcional. */
public record DadosDoCompromisso(String titulo, String descricao, Instant inicio, Instant fim) {
}
