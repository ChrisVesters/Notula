package com.cvesters.notula.meeting;

import java.time.Duration;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import com.cvesters.notula.common.exception.BusyEntityException;
import com.cvesters.notula.common.exception.MissingEntityException;
import com.cvesters.notula.meeting.bdo.MeetingInfo;
import com.cvesters.notula.meeting.bdo.MeetingScope;

@Component
public class MeetingLock {

	private final TransactionTemplate transactions;
	private final MeetingStorageGateway meetingStorageGateway;
	private final Duration timeout;

	private final Map<Long, Holder> locks = new ConcurrentHashMap<>();

	MeetingLock(final TransactionTemplate transactions,
			final MeetingStorageGateway meetingStorageGateway,
			@Value("${meeting.lock.timeout}") final Duration timeout) {
		Objects.requireNonNull(timeout);

		this.transactions = transactions;
		this.meetingStorageGateway = meetingStorageGateway;
		this.timeout = timeout;
	}

	public void run(final long meetingId,
			final Consumer<MeetingScope> action) {
		call(meetingId, scope -> {
			action.accept(scope);

			return null;
		});
	}

	public <T> T call(final long meetingId,
			final Function<MeetingScope, T> action) {
		Objects.requireNonNull(action);

		return locked(meetingId, () -> action.apply(bump(meetingId)));
	}

	public <T> T hold(final long meetingId, final Supplier<T> action) {
		Objects.requireNonNull(action);

		return locked(meetingId, action);
	}

	private MeetingScope bump(final long meetingId) {
		final MeetingInfo meeting = meetingStorageGateway.find(meetingId)
				.orElseThrow(MissingEntityException::new);
		meeting.bumpRevision();
		final MeetingInfo bumped = meetingStorageGateway.update(meeting);

		return new MeetingScope(meetingId, bumped.getRevision());
	}

	private <T> T locked(final long meetingId, final Supplier<T> action) {
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
