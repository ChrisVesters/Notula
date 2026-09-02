package com.cvesters.notula.meeting;

import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Service;

import com.cvesters.notula.common.domain.Origin;
import com.cvesters.notula.common.domain.Principal;
import com.cvesters.notula.common.exception.MissingEntityException;
import com.cvesters.notula.meeting.bdo.MeetingAction;
import com.cvesters.notula.meeting.bdo.MeetingEvent;
import com.cvesters.notula.meeting.bdo.MeetingInfo;

@Service
public class MeetingService {

	private final MeetingLock meetingLock;

	private final MeetingPublisher meetingPublisher;
	private final MeetingStorageGateway meetingStorage;

	public MeetingService(final MeetingLock meetingLock,
			final MeetingStorageGateway meetingStorageGateway,
			final MeetingPublisher meetingPublisher) {
		this.meetingLock = meetingLock;
		this.meetingStorage = meetingStorageGateway;
		this.meetingPublisher = meetingPublisher;
	}

	public MeetingInfo getById(final Principal principal, final long id) {
		Objects.requireNonNull(principal);

		final long organisationId = principal.organisationId();

		return meetingStorage.find(id)
				.filter(m -> m.getOrganisationId() == organisationId)
				.orElseThrow(MissingEntityException::new);
	}

	public List<MeetingInfo> getAll(final Principal principal) {
		Objects.requireNonNull(principal);

		return meetingStorage
				.findAllByOrganisationId(principal.organisationId());
	}

	public MeetingInfo create(final Origin origin,
			final MeetingAction.Create meeting) {
		Objects.requireNonNull(origin);
		Objects.requireNonNull(meeting);

		final var meetingInfo = new MeetingInfo(
				origin.principal().organisationId(), meeting.getName());

		return meetingStorage.create(meetingInfo);
	}

	public MeetingInfo update(final Origin origin, final long id,
			final MeetingAction.Update action) {
		Objects.requireNonNull(origin);
		Objects.requireNonNull(action);

		return meetingLock.call(id,
				() -> doUpdate(origin, id, action));
	}

	private MeetingInfo doUpdate(final Origin origin, final long id,
			final MeetingAction.Update action) {
		final MeetingInfo meetingInfo = getById(origin.principal(), id);
		action.apply(meetingInfo);
		final MeetingInfo updated = meetingStorage.update(meetingInfo);

		final var event = new MeetingEvent(id, action, origin);
		meetingPublisher.publish(event);

		return updated;
	}

	public void delete(final Origin origin, final long id) {
		Objects.requireNonNull(origin);

		meetingLock.run(id,
				() -> doDelete(origin, id));
	}

	private void doDelete(final Origin origin, final long id) {
		final MeetingInfo meetingInfo = getById(origin.principal(), id);

		meetingStorage.delete(meetingInfo);

		final var action = new MeetingAction.Delete();
		final var event = new MeetingEvent(id, action, origin);
		meetingPublisher.publish(event);
	}
}
