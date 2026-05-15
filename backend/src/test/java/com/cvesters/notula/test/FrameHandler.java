package com.cvesters.notula.test;

import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;

import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;

public class FrameHandler implements StompFrameHandler {

	private final CompletableFuture<String> response = new CompletableFuture<>();

	@Override
	public Type getPayloadType(final StompHeaders headers) {
		return byte[].class;
	}

	@Override
	public void handleFrame(final StompHeaders headers, final Object payload) {
		final var data = new String((byte[]) payload, StandardCharsets.UTF_8);
		response.complete(data);
	}

	public CompletableFuture<String> getResponse() {
		return response;
	}
}
