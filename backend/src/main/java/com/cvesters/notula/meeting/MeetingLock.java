package com.cvesters.notula.meeting;

import java.time.Duration;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import com.cvesters.notula.common.exception.BusyEntityException;

@Component
public class MeetingLock {

	private final TransactionTemplate transactions;
	private final Duration timeout;

	private final Map<Long, Holder> locks = new ConcurrentHashMap<>();

	MeetingLock(final TransactionTemplate transactions,
			@Value("${meeting.lock.timeout}") final Duration timeout) {
		Objects.requireNonNull(timeout);

		this.transactions = transactions;
		this.timeout = timeout;
	}

	public void run(final long meetingId, final Runnable action) {
		call(meetingId, () -> {
			action.run();

			return null;
		});
	}

	public <T> T call(final long meetingId, final Supplier<T> action) {
		Objects.requireNonNull(action);

		final Holder holder = claim(meetingId);

		try {
			lock(holder.lock, meetingId);

			try {
				return transactions.execute(status -> action.get());
			} finally {
				holder.lock.unlock();
			}
		} finally {
			release(meetingId);
		}
	}

	private Holder claim(final long meetingId) {
		return locks.compute(meetingId, (id, holder) -> {
			final Holder claimed = holder == null ? new Holder() : holder;
			claimed.users++;

			return claimed;
		});
	}

	private void release(final long meetingId) {
		locks.compute(meetingId,
				(id, holder) -> --holder.users == 0 ? null : holder);
	}

	private void lock(final ReentrantLock lock, final long meetingId) {
		try {
			if (!lock.tryLock(timeout.toMillis(), TimeUnit.MILLISECONDS)) {
				throw new BusyEntityException(
						"Timed out waiting for meeting " + meetingId);
			}
		} catch (final InterruptedException e) {
			Thread.currentThread().interrupt();

			throw new BusyEntityException(
					"Interrupted waiting for meeting " + meetingId, e);
		}
	}

	private static final class Holder {

		private final ReentrantLock lock = new ReentrantLock();
		private int users;
	}
}
