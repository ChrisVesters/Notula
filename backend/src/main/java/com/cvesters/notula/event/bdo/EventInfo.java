package com.cvesters.notula.event.bdo;

import java.util.Objects;
import java.util.UUID;

import org.apache.commons.lang3.Validate;

import lombok.Getter;

import com.cvesters.notula.common.domain.Origin;
import com.cvesters.notula.meeting.bdo.MeetingScope;

@Getter
public class EventInfo {

	private final Long id;
	private final long meetingId;
	private final long revision;
	private final long userId;
	private final UUID clientId;
	private final UUID changeId;
	private final Mutation mutation;

	public EventInfo(final MeetingScope scope, final Origin origin,
			final Mutation mutation) {
		Objects.requireNonNull(scope);
		Objects.requireNonNull(origin);

		this(null, scope.meetingId(), scope.revision(), origin.userId(),
				origin.clientId(), scope.changeId(), mutation);
	}

	public EventInfo(final Long id, final long meetingId, final long revision,
			final long userId, final UUID clientId, final UUID changeId,
			final Mutation mutation) {
		Objects.requireNonNull(mutation);

		this.id = id;
		this.meetingId = meetingId;
		this.revision = revision;
		this.userId = userId;
		this.clientId = clientId;
		this.changeId = changeId;
		this.mutation = mutation;
	}

	public long getId() {
		Validate.validState(id != null);

		return id;
	}
}
