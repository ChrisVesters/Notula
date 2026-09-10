package com.cvesters.notula.config;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;

import org.springframework.stereotype.Component;

@Component
public class SessionOrder {

	private final Map<String, Turn> sessions = new ConcurrentHashMap<>();

	public void acquire(final String sessionId) {
		final Turn turn = sessions.computeIfAbsent(sessionId, id -> new Turn());

		try {
			turn.take();
		} catch (final InterruptedException e) {
			Thread.currentThread().interrupt();

			throw new IllegalStateException(
					"Interrupted waiting for session " + sessionId, e);
		}
	}

	public void release(final String sessionId) {
		final Turn turn = sessions.get(sessionId);
		if (turn == null) {
			return;
		}

		turn.handBack();
	}

	public void forget(final String sessionId) {
		final Turn turn = sessions.remove(sessionId);
		if (turn == null) {
			return;
		}

		turn.handBack();
	}

	private static final class Turn {

		private final Semaphore permit = new Semaphore(1);
		private final AtomicBoolean taken = new AtomicBoolean();

		private void take() throws InterruptedException {
			permit.acquire();
			taken.set(true);
		}

		private void handBack() {
			if (taken.compareAndSet(true, false)) {
				permit.release();
			}
		}
	}
}
