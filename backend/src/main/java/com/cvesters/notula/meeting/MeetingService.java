package com.cvesters.notula.meeting;

import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Service;

import com.cvesters.notula.common.domain.Origin;
import com.cvesters.notula.common.domain.Principal;
import com.cvesters.notula.common.exception.MissingEntityException;
import com.cvesters.notula.event.EventService;
import com.cvesters.notula.event.bdo.EventInfo;
import com.cvesters.notula.event.bdo.MeetingMutation;
import com.cvesters.notula.meeting.bdo.MeetingAction;
import com.cvesters.notula.meeting.bdo.MeetingInfo;
import com.cvesters.notula.meeting.bdo.MeetingScope;

@Service
public class MeetingService {

	private final MeetingLock meetingLock;

	private final EventService eventService;
	private final MeetingStorageGateway meetingStorage;

	public MeetingService(final MeetingLock meetingLock,
			final MeetingStorageGateway meetingStorageGateway,
			final EventService eventService) {
		this.meetingLock = meetingLock;
		this.meetingStorage = meetingStorageGateway;
		this.eventService = eventService;
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

	public MeetingInfo update(final Origin origin, final MeetingScope scope,
			final MeetingAction.Update action) {
		Objects.requireNonNull(origin);
		Objects.requireNonNull(scope);
		Objects.requireNonNull(action);

		final MeetingInfo meetingInfo = getById(origin.principal(),
				scope.meetingId());
		action.apply(meetingInfo);
		final MeetingInfo updated = meetingStorage.update(meetingInfo);

		eventService.publish(new EventInfo(scope, origin, mutation(action)));

		return updated;
	}

	public void delete(final Origin origin, final long id) {
		Objects.requireNonNull(origin);

		meetingLock.run(id, scope -> doDelete(origin, scope));
	}

	private void doDelete(final Origin origin, final MeetingScope scope) {
		final MeetingInfo meetingInfo = getById(origin.principal(),
				scope.meetingId());

		final var mutation = new MeetingMutation.Remove();
		eventService.publish(new EventInfo(scope, origin, mutation));

		meetingStorage.delete(meetingInfo);
	}

	private static MeetingMutation mutation(final MeetingAction.Update action) {
		return switch (action) {
			case MeetingAction.UpdateName update ->
					new MeetingMutation.Rename(update.getPosition(),
							update.getLength(), update.getValue());
			case MeetingAction.UpdateDescription update ->
					new MeetingMutation.Describe(update.getPosition(),
							update.getLength(), update.getValue());
		};
	}
}
