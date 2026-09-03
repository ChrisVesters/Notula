package com.cvesters.notula.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class SessionOrderTest {

	private static final String SESSION_ID = "session";
	private static final Duration TIMEOUT = Duration.ofSeconds(5);

	private final SessionOrder order = new SessionOrder(TIMEOUT);

	private CountDownLatch acquireElsewhere() throws InterruptedException {
		final var started = new CountDownLatch(1);
		final var acquired = new CountDownLatch(1);

		final Thread thread = new Thread(() -> {
			started.countDown();
			order.acquire(SESSION_ID);
			acquired.countDown();
		});
		thread.start();

		assertThat(started.await(1, TimeUnit.SECONDS)).isTrue();

		return acquired;
	}

	@Nested
	class Acquire {

		@Test
		void firstAction() {
			order.acquire(SESSION_ID);

			assertThat(order.tracked()).isEqualTo(1);
		}

		@Test
		void multipleActions() throws Exception {
			order.acquire(SESSION_ID);

			final CountDownLatch acquired = acquireElsewhere();

			assertThat(acquired.await(100, TimeUnit.MILLISECONDS)).isFalse();

			order.release(SESSION_ID);

			assertThat(acquired.await(1, TimeUnit.SECONDS)).isTrue();
		}

		@Test
		void multipleSessions() {
			order.acquire(SESSION_ID);

			order.acquire("other");

			assertThat(order.tracked()).isEqualTo(2);
		}
	}

	@Nested
	class Release {

		@Test
		void multipleAquires() throws Exception {
			final var impatient = new SessionOrder(Duration.ofMillis(50));

			impatient.acquire(SESSION_ID);
			impatient.acquire(SESSION_ID);
			impatient.release(SESSION_ID);

			final var acquired = new CountDownLatch(1);
			final Thread thread = new Thread(() -> {
				impatient.acquire(SESSION_ID);
				acquired.countDown();
			});
			thread.start();

			assertThat(acquired.await(1, TimeUnit.SECONDS)).isTrue();
		}

		@Test
		void unknownSession() {
			order.release("unknown");

			assertThat(order.tracked()).isZero();
		}
	}

	@Nested
	class Forget {

		@Test
		void success() {
			order.acquire(SESSION_ID);
			assertThat(order.tracked()).isEqualTo(1);

			order.forget(SESSION_ID);

			assertThat(order.tracked()).isZero();
		}

		@Test
		void pending() throws Exception {
			order.acquire(SESSION_ID);

			final CountDownLatch acquired = acquireElsewhere();

			assertThat(acquired.await(100, TimeUnit.MILLISECONDS)).isFalse();

			order.forget(SESSION_ID);

			assertThat(acquired.await(1, TimeUnit.SECONDS)).isTrue();
		}

		@Test
		void unknownSession() {
			order.forget("unknown");

			assertThat(order.tracked()).isZero();
		}
	}
}
