package com.cvesters.notula.common.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import java.lang.reflect.Method;

import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestCookieException;

import com.cvesters.notula.common.exception.BusyEntityException;
import com.cvesters.notula.common.exception.DuplicateEntityException;
import com.cvesters.notula.common.exception.InvalidActionException;
import com.cvesters.notula.common.exception.MissingEntityException;

class HttpExceptionHandlerTest {

	private final HttpExceptionHandler handler = new HttpExceptionHandler();

	@Test
	void missingCookie() throws Exception {
		final var exception = new MissingRequestCookieException("refreshToken",
				parameter());

		final ResponseEntity<Void> response = handler.handle(exception);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
	}

	@Test
	void denied() {
		final var exception = new AuthorizationDeniedException("Denied");

		final ResponseEntity<Void> response = handler.handle(exception);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
	}

	@Test
	void unreadable() {
		final var exception = new HttpMessageNotReadableException("Unreadable",
				mock(HttpInputMessage.class));

		final ResponseEntity<Void> response = handler.handle(exception);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
	}

	@Test
	void duplicateEntity() {
		final ResponseEntity<Void> response = handler
				.handle(new DuplicateEntityException());

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
	}

	@Test
	void missingEntity() {
		final ResponseEntity<Void> response = handler
				.handle(new MissingEntityException());

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
	}

	@Test
	void invalidAction() {
		final ResponseEntity<Void> response = handler
				.handle(new InvalidActionException());

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
	}

	@Test
	void notValid() throws Exception {
		final var exception = new MethodArgumentNotValidException(parameter(),
				new BeanPropertyBindingResult(new Object(), "target"));

		final ResponseEntity<Void> response = handler.handle(exception);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
	}

	@Test
	void busyEntity() {
		final var exception = new BusyEntityException("Timed out");

		final ResponseEntity<Void> response = handler.handle(exception);

		assertThat(response.getStatusCode())
				.isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
	}

	@Test
	void illegalState() {
		final var exception = new IllegalStateException("Unexpected");

		final ResponseEntity<Void> response = handler
				.handleUnexpected(exception);

		assertThat(response.getStatusCode())
				.isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
	}

	@SuppressWarnings("unused")
	private void endpoint(final String cookie) {
	}

	private static MethodParameter parameter() throws Exception {
		final Method method = HttpExceptionHandlerTest.class
				.getDeclaredMethod("endpoint", String.class);

		return new MethodParameter(method, 0);
	}
}
