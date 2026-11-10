package com.agenda.agenda_api.infraestrutura.observabilidade;

import java.io.IOException;
import java.util.UUID;
import java.util.regex.Pattern;

import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Dá a cada requisição um id, que vai em todas as linhas de log (campo
 * {@code idDaRequisicao}) e volta no cabeçalho {@code X-Request-Id}.
 * Se o cliente mandar um id válido, ele é reaproveitado; senão, gera um novo.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class FiltroDeIdDaRequisicao extends OncePerRequestFilter {

	public static final String CABECALHO = "X-Request-Id";
	public static final String CHAVE_NO_LOG = "idDaRequisicao";

	// Só letras, números e hífen, até 64 caracteres: evita injetar texto no log.
	private static final Pattern ID_VALIDO = Pattern.compile("[A-Za-z0-9-]{1,64}");

	@Override
	protected void doFilterInternal(HttpServletRequest requisicao, HttpServletResponse resposta,
			FilterChain cadeia) throws ServletException, IOException {
		String id = idRecebidoOuNovo(requisicao.getHeader(CABECALHO));
		MDC.put(CHAVE_NO_LOG, id);
		resposta.setHeader(CABECALHO, id);
		try {
			cadeia.doFilter(requisicao, resposta);
		} finally {
			MDC.remove(CHAVE_NO_LOG);
		}
	}

	private static String idRecebidoOuNovo(String recebido) {
		if (recebido != null && ID_VALIDO.matcher(recebido).matches()) {
			return recebido;
		}
		return UUID.randomUUID().toString();
	}
}
