package com.cvesters.notula.config;

import java.time.Duration;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

// TODO: cleanup
@Slf4j
@Component
public class SessionOrder {

	private final Duration timeout;

	private final Map<String, Semaphore> sessions = new ConcurrentHashMap<>();

	SessionOrder(@Value("${websocket.order.timeout}") final Duration timeout) {
		Objects.requireNonNull(timeout);

		this.timeout = timeout;
	}

	public void acquire(final String sessionId) {
		final Semaphore turn = sessions.computeIfAbsent(sessionId,
				id -> new Semaphore(1));

		try {
			if (!turn.tryAcquire(timeout.toMillis(), TimeUnit.MILLISECONDS)) {
				log.warn("Session {} timed out waiting for its previous action",
						sessionId);
			}
		} catch (final InterruptedException e) {
			Thread.currentThread().interrupt();

			throw new IllegalStateException(
					"Interrupted waiting for session " + sessionId, e);
		}
	}

	public void release(final String sessionId) {
		final Semaphore turn = sessions.get(sessionId);
		if (turn == null) {
			return;
		}

		turn.release();
	}

	public void forget(final String sessionId) {
		final Semaphore turn = sessions.remove(sessionId);
		if (turn == null) {
			return;
		}

		turn.release();
	}

	int tracked() {
		return sessions.size();
	}
}
