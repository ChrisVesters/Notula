package com.cvesters.notula.common.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestCookieException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import com.cvesters.notula.common.exception.DuplicateEntityException;
import com.cvesters.notula.common.exception.MissingEntityException;

// TODO: make sure all exceptions are handled properly
@ControllerAdvice
public class HttpExceptionHandler { // extends ResponseEntityExceptionHandler {

	@ExceptionHandler(MissingRequestCookieException.class)
	public ResponseEntity<Void> handle(final MissingRequestCookieException e) {
		return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
	}

	@ExceptionHandler(AuthorizationDeniedException.class)
	public ResponseEntity<Void> handle(final AuthorizationDeniedException e) {
		return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
	}

	@ExceptionHandler(HttpMessageNotReadableException.class)
	public ResponseEntity<Void> handle(
			final HttpMessageNotReadableException e) {
		return ResponseEntity.badRequest().build();
	}

	@ExceptionHandler(DuplicateEntityException.class)
	public ResponseEntity<Void> handle(final DuplicateEntityException e) {
		return ResponseEntity.badRequest().build();
	}

	@ExceptionHandler(MissingEntityException.class)
	public ResponseEntity<Void> handle(final MissingEntityException e) {
		return ResponseEntity.badRequest().build();
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<Void> handle(
			final MethodArgumentNotValidException e) {
		return ResponseEntity.badRequest().build();
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<Void> handleUnexpected(final Exception e) {
		return ResponseEntity.internalServerError().build();
	}
}