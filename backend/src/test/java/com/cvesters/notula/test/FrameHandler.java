package com.cvesters.notula.test;

import java.lang.reflect.Type;
import java.util.concurrent.CompletableFuture;

import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;

public class FrameHandler<T> implements StompFrameHandler {

	private final Class<T> clazz;
	private final CompletableFuture<T> response = new CompletableFuture<>();

	public FrameHandler(final Class<T> clazz) {
		this.clazz = clazz;
	}

	@Override
	public Type getPayloadType(final StompHeaders headers) {
		return clazz;
	}

	@Override
	public void handleFrame(final StompHeaders headers, final Object payload) {
		response.complete(clazz.cast(payload));
	}

	public CompletableFuture<T> getResponse() throws Exception {
		return response;
	}
}
