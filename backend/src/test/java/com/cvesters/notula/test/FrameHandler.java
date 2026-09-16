package com.cvesters.notula.test;

import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;

public class FrameHandler implements StompFrameHandler {

	private final CompletableFuture<String> response = new CompletableFuture<>();
	private final BlockingQueue<String> frames = new LinkedBlockingQueue<>();

	@Override
	public Type getPayloadType(final StompHeaders headers) {
		return byte[].class;
	}

	@Override
	public void handleFrame(final StompHeaders headers, final Object payload) {
		final var data = new String((byte[]) payload, StandardCharsets.UTF_8);

		response.complete(data);
		frames.add(data);
	}

	public CompletableFuture<String> getResponse() {
		return response;
	}

	// Returns what arrived rather than failing, so a caller asserting on the
	// size reports how many frames it got instead of a timeout.
	public List<String> await(final int count, final Duration timeout)
			throws InterruptedException {
		final long deadline = System.nanoTime() + timeout.toNanos();
		final var received = new ArrayList<String>(count);

		while (received.size() < count) {
			final long remaining = deadline - System.nanoTime();
			final String frame = frames.poll(remaining, TimeUnit.NANOSECONDS);
			if (frame == null) {
				break;
			}

			received.add(frame);
		}

		return received;
	}
}
