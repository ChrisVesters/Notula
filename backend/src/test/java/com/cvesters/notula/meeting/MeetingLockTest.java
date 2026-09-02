package com.cvesters.notula.meeting;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.SimpleTransactionStatus;
import org.springframework.transaction.support.TransactionTemplate;

class MeetingLockTest {

	/** A ceiling on waiting for something that is supposed to happen. */
	private static final Duration TIMEOUT = Duration.ofSeconds(5);

	/** A bound on watching for something that is supposed not to. */
	private static final Duration BRIEFLY = Duration.ofMillis(50);

	/** Refuses instead of queueing, so a held lock is answered at once. */
	private static final Duration NEVER_WAITS = Duration.ZERO;

	private static final long MEETING_ID = 1L;
	private static final long OTHER_MEETING_ID = 2L;

	private final MeetingLock meetingLock = new MeetingLock(transactions(),
			TIMEOUT);

	@Nested
	class Call {

		@Test
		void returnsResult() {
			final String result = meetingLock.call(MEETING_ID, () -> "result");

			assertThat(result).isEqualTo("result");
		}

		@Test
		void inTransaction() {
			final PlatformTransactionManager manager = mock();
			final var status = new SimpleTransactionStatus();
			when(manager.getTransaction(any())).thenReturn(status);

			final var lock = new MeetingLock(new TransactionTemplate(manager),
					TIMEOUT);

			lock.run(MEETING_ID, () -> {
				// the action
			});

			final InOrder inOrder = inOrder(manager);
			inOrder.verify(manager).getTransaction(any());
			inOrder.verify(manager).commit(status);
		}

		@Test
		void heldUntilCommitted() {
			final PlatformTransactionManager manager = mock();
			when(manager.getTransaction(any()))
					.thenReturn(new SimpleTransactionStatus());

			// Refuses rather than queueing, so the second action answers at
			// once instead of waiting out the commit it is called from.
			final var lock = new MeetingLock(new TransactionTemplate(manager),
					NEVER_WAITS);

			final var committed = new AtomicBoolean();
			final var enteredWhileCommitting = new AtomicBoolean();

			doAnswer(invocation -> {
				if (committed.compareAndSet(false, true)) {
					enteredWhileCommitting.set(enters(lock));
				}

				return null;
			}).when(manager).commit(any());

			lock.run(MEETING_ID, () -> {
				// the action
			});

			assertThat(committed).isTrue();
			assertThat(enteredWhileCommitting).isFalse();
		}

		@Test
		void releasedAfterFailure() throws Exception {
			assertThatThrownBy(() -> meetingLock.call(MEETING_ID, () -> {
				throw new IllegalStateException("failed");
			})).isInstanceOf(IllegalStateException.class);

			// From another thread, as the lock is reentrant and would let the
			// one that failed straight back in whether it was released or not.
			final var next = CompletableFuture.supplyAsync(
					() -> meetingLock.call(MEETING_ID, () -> "result"));

			assertThat(next.get(TIMEOUT.toSeconds(), TimeUnit.SECONDS))
					.isEqualTo("result");
		}

		private boolean enters(final MeetingLock lock) throws Exception {
			final ExecutorService executor = Executors
					.newSingleThreadExecutor();
			try {
				return executor.submit(() -> {
					try {
						lock.run(MEETING_ID, () -> {
							// the second action
						});

						return true;
					} catch (final IllegalStateException e) {
						return false;
					}
				}).get(TIMEOUT.toSeconds(), TimeUnit.SECONDS);
			} finally {
				executor.shutdownNow();
			}
		}
	}

	@Nested
	class Run {

		@Test
		void waitsItsTurn() throws Exception {
			final var applied = new CopyOnWriteArrayList<String>();

			final var inside = new CountDownLatch(1);
			final var release = new CountDownLatch(1);

			final ExecutorService executor = Executors.newFixedThreadPool(2);
			try {
				// The first action holds the meeting until the test lets go,
				// so the second is certain to arrive while it is still there.
				executor.execute(() -> meetingLock.run(MEETING_ID, () -> {
					applied.add("first entered");
					awaitQuietly(release, inside);
					applied.add("first left");
				}));

				await(inside);

				final Future<?> second = executor.submit(
						() -> meetingLock.run(MEETING_ID,
								() -> applied.add("second entered")));

				assertThatThrownBy(() -> second.get(BRIEFLY.toMillis(),
						TimeUnit.MILLISECONDS))
						.isInstanceOf(TimeoutException.class);
				assertThat(applied).containsExactly("first entered");

				release.countDown();
				second.get(TIMEOUT.toSeconds(), TimeUnit.SECONDS);
			} finally {
				release.countDown();
				executor.shutdownNow();
			}

			assertThat(applied).containsExactly("first entered", "first left",
					"second entered");
		}

		@Test
		void otherMeetingsUnaffected() throws Exception {
			final var inFirst = new CountDownLatch(1);
			final var inSecond = new CountDownLatch(1);

			final ExecutorService executor = Executors.newFixedThreadPool(2);
			try {
				// Neither can finish unless both are inside their lock at the
				// same time.
				final Future<Boolean> first = executor
						.submit(() -> meetingLock.call(MEETING_ID, () -> {
							inFirst.countDown();

							return awaitQuietly(inSecond);
						}));
				final Future<Boolean> second = executor
						.submit(() -> meetingLock.call(OTHER_MEETING_ID, () -> {
							inSecond.countDown();

							return awaitQuietly(inFirst);
						}));

				assertThat(first.get(TIMEOUT.toSeconds(), TimeUnit.SECONDS))
						.isTrue();
				assertThat(second.get(TIMEOUT.toSeconds(), TimeUnit.SECONDS))
						.isTrue();
			} finally {
				executor.shutdownNow();
			}
		}

		@Test
		void forgottenWhenDone() {
			meetingLock.run(MEETING_ID,
					() -> assertThat(meetingLock.claimed()).isEqualTo(1));
			meetingLock.run(OTHER_MEETING_ID, () -> {
				// the action
			});

			assertThat(meetingLock.claimed()).isZero();
		}

		@Test
		void refusedWhenHeldTooLong() throws Exception {
			final var impatient = new MeetingLock(transactions(), NEVER_WAITS);

			final var held = new CountDownLatch(1);
			final var release = new CountDownLatch(1);

			final ExecutorService executor = Executors
					.newSingleThreadExecutor();
			try {
				executor.execute(() -> impatient.run(MEETING_ID,
						() -> awaitQuietly(release, held)));

				await(held);

				assertThatThrownBy(
						() -> impatient.run(MEETING_ID, () -> { /* never */ }))
						.isInstanceOf(IllegalStateException.class)
						.hasMessageContaining("Timed out");
			} finally {
				release.countDown();
				executor.shutdownNow();
			}
		}
	}

	private static TransactionTemplate transactions() {
		final PlatformTransactionManager manager = mock();
		when(manager.getTransaction(any()))
				.thenReturn(new SimpleTransactionStatus());

		return new TransactionTemplate(manager);
	}

	private static void await(final CountDownLatch latch) {
		assertThat(awaitQuietly(latch)).isTrue();
	}

	private static boolean awaitQuietly(final CountDownLatch latch) {
		try {
			return latch.await(TIMEOUT.toSeconds(), TimeUnit.SECONDS);
		} catch (final InterruptedException e) {
			Thread.currentThread().interrupt();

			return false;
		}
	}

	/**
	 * Waits for a latch, having announced on another that it is in position.
	 * Announcing from inside the action is what makes the test's next step
	 * certain rather than merely likely.
	 */
	private static boolean awaitQuietly(final CountDownLatch latch,
			final CountDownLatch arrived) {
		arrived.countDown();

		return awaitQuietly(latch);
	}
}
