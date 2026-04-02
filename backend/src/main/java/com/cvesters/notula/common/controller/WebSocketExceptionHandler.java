package com.cvesters.notula.common.controller;

import java.util.Optional;

import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.web.bind.annotation.ControllerAdvice;

// TODO: better error handling
// TODO: add information for the client to identify which request went wrong.
@ControllerAdvice
public class WebSocketExceptionHandler {

	@MessageExceptionHandler
	@SendToUser(value = "/queue/errors")
	public String handleException(final Exception ex) {
		return "Error" + Optional.ofNullable(ex.getMessage())
				.map(message -> ": " + message)
				.orElse("");
	}
}
