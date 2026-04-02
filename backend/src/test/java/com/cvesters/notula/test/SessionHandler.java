package com.cvesters.notula.test;

import java.util.concurrent.CompletableFuture;

import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;

public class SessionHandler extends StompSessionHandlerAdapter {

	private final CompletableFuture<Throwable> error = new CompletableFuture<>();

	@Override
	public void handleTransportError(final StompSession session,
			Throwable exception) {
		error.complete(exception);
	}

	@Override
	public void handleException(final StompSession session,
			final StompCommand command, final StompHeaders headers,
			final byte[] payload, final Throwable exception) {
		error.complete(exception);
	}

	public CompletableFuture<Throwable> getError() {
		return error;
	}
}