package com.cvesters.notula.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class SessionOrderTest {

	private static final String SESSION_ID = "session";
	private static final String OTHER_SESSION_ID = "other";

	private final SessionOrder order = new SessionOrder();

	private CountDownLatch acquireElsewhere(final String sessionId)
			throws InterruptedException {
		final var started = new CountDownLatch(1);
		final var acquired = new CountDownLatch(1);

		final var thread = new Thread(() -> {
			started.countDown();
			order.acquire(sessionId);
			acquired.countDown();
		});
		thread.setDaemon(true);
		thread.start();

		assertThat(started.await(1, TimeUnit.SECONDS)).isTrue();

		return acquired;
	}

	@Nested
	class Acquire {

		@Test
		void multipleActions() throws Exception {
			order.acquire(SESSION_ID);

			final CountDownLatch acquired = acquireElsewhere(SESSION_ID);

			assertThat(acquired.await(100, TimeUnit.MILLISECONDS)).isFalse();

			order.release(SESSION_ID);

			assertThat(acquired.await(1, TimeUnit.SECONDS)).isTrue();
		}

		@Test
		void interrupted() {
			Thread.currentThread().interrupt();
			try {
				assertThatThrownBy(() -> order.acquire(SESSION_ID))
						.isInstanceOf(IllegalStateException.class)
						.hasMessageContaining(SESSION_ID);

				assertThat(Thread.currentThread().isInterrupted()).isTrue();
			} finally {
				Thread.interrupted();
			}
		}

		@Test
		void multipleSessions() throws Exception {
			order.acquire(SESSION_ID);

			final CountDownLatch acquired = acquireElsewhere(OTHER_SESSION_ID);

			assertThat(acquired.await(1, TimeUnit.SECONDS)).isTrue();
		}
	}

	@Nested
	class Release {

		@Test
		void twice() throws Exception {
			order.acquire(SESSION_ID);

			order.release(SESSION_ID);
			order.release(SESSION_ID);

			order.acquire(SESSION_ID);
			final CountDownLatch acquired = acquireElsewhere(SESSION_ID);

			assertThat(acquired.await(100, TimeUnit.MILLISECONDS)).isFalse();
		}

		@Test
		void unknownSession() throws Exception {
			order.release(SESSION_ID);

			order.acquire(SESSION_ID);
			final CountDownLatch acquired = acquireElsewhere(SESSION_ID);

			assertThat(acquired.await(100, TimeUnit.MILLISECONDS)).isFalse();
		}
	}

	@Nested
	class Forget {

		@Test
		void success() throws Exception {
			order.acquire(SESSION_ID);

			order.forget(SESSION_ID);

			final CountDownLatch acquired = acquireElsewhere(SESSION_ID);

			assertThat(acquired.await(1, TimeUnit.SECONDS)).isTrue();
		}

		@Test
		void pending() throws Exception {
			order.acquire(SESSION_ID);

			final CountDownLatch acquired = acquireElsewhere(SESSION_ID);

			assertThat(acquired.await(100, TimeUnit.MILLISECONDS)).isFalse();

			order.forget(SESSION_ID);

			assertThat(acquired.await(1, TimeUnit.SECONDS)).isTrue();
		}

		@Test
		void unknownSession() throws Exception {
			order.forget(SESSION_ID);

			order.acquire(SESSION_ID);
			final CountDownLatch acquired = acquireElsewhere(SESSION_ID);

			assertThat(acquired.await(100, TimeUnit.MILLISECONDS)).isFalse();
		}
	}
}
