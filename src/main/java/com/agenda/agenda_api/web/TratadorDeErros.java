package com.agenda.agenda_api.web;

import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import com.agenda.agenda_api.dominio.CompromissoCanceladoNaoPodeSerEditadoExcecao;
import com.agenda.agenda_api.dominio.CompromissoJaCanceladoExcecao;
import com.agenda.agenda_api.dominio.CompromissoNaoEncontradoExcecao;
import com.agenda.agenda_api.dominio.ConflitoDeHorarioExcecao;
import com.agenda.agenda_api.dominio.ExcecaoDeDominio;

/**
 * Traduz as exceções em respostas HTTP com corpo padrão {"erro", "mensagem"}.
 * O Spring escolhe o tratador mais específico; as exceções de domínio sem
 * tratador próprio são dados inválidos (400).
 */
@RestControllerAdvice
class TratadorDeErros {

	private static final Logger LOG = LoggerFactory.getLogger(TratadorDeErros.class);

	@ExceptionHandler(CompromissoNaoEncontradoExcecao.class)
	ResponseEntity<ErroResposta> naoEncontrado(CompromissoNaoEncontradoExcecao erro) {
		return responder(HttpStatus.NOT_FOUND, "NAO_ENCONTRADO", erro.getMessage());
	}

	@ExceptionHandler(ConflitoDeHorarioExcecao.class)
	ResponseEntity<ErroResposta> conflitoDeHorario(ConflitoDeHorarioExcecao erro) {
		return responder(HttpStatus.CONFLICT, "CONFLITO_DE_HORARIO", erro.getMessage());
	}

	@ExceptionHandler({CompromissoJaCanceladoExcecao.class, CompromissoCanceladoNaoPodeSerEditadoExcecao.class})
	ResponseEntity<ErroResposta> compromissoCancelado(ExcecaoDeDominio erro) {
		return responder(HttpStatus.CONFLICT, "COMPROMISSO_CANCELADO", erro.getMessage());
	}

	@ExceptionHandler(ExcecaoDeDominio.class)
	ResponseEntity<ErroResposta> regraDeDadosViolada(ExcecaoDeDominio erro) {
		return dadosInvalidos(erro.getMessage());
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	ResponseEntity<ErroResposta> campoInvalido(MethodArgumentNotValidException erro) {
		String mensagens = erro.getBindingResult().getFieldErrors().stream()
				.map(campo -> campo.getDefaultMessage())
				.sorted()
				.collect(Collectors.joining(" "));
		return dadosInvalidos(mensagens);
	}

	@ExceptionHandler(HttpMessageNotReadableException.class)
	ResponseEntity<ErroResposta> corpoIlegivel(HttpMessageNotReadableException erro) {
		return dadosInvalidos(
				"O corpo da requisição não é um JSON válido ou tem datas fora do formato ISO 8601.");
	}

	@ExceptionHandler(MethodArgumentTypeMismatchException.class)
	ResponseEntity<ErroResposta> parametroInvalido(MethodArgumentTypeMismatchException erro) {
		return dadosInvalidos("O valor de '" + erro.getName() + "' é inválido.");
	}

	private static ResponseEntity<ErroResposta> dadosInvalidos(String mensagem) {
		return responder(HttpStatus.BAD_REQUEST, "DADOS_INVALIDOS", mensagem);
	}

	private static ResponseEntity<ErroResposta> responder(HttpStatus status, String erro, String mensagem) {
		// Requisição recusada é comportamento esperado, não falha do sistema: nível WARN.
		LOG.warn("Requisição recusada com {} {}: {}", status.value(), erro, mensagem);
		return ResponseEntity.status(status).body(new ErroResposta(erro, mensagem));
	}
}
