package com.cvesters.notula.meeting;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.cvesters.notula.common.domain.Principal;
import com.cvesters.notula.common.exception.MissingEntityException;
import com.cvesters.notula.meeting.bdo.MeetingAction;
import com.cvesters.notula.meeting.bdo.MeetingInfo;

@Service
public class MeetingService {

	private final MeetingStorageGateway meetingStorage;

	public MeetingService(final MeetingStorageGateway meetingStorageGateway) {
		this.meetingStorage = meetingStorageGateway;
	}

	public boolean existsById(final Principal principal, final long id) {
		Objects.requireNonNull(principal);

		return findById(principal, id).isPresent();
	}

	public MeetingInfo getById(final Principal principal, final long id) {
		Objects.requireNonNull(principal);

		return findById(principal, id)
				.orElseThrow(MissingEntityException::new);
	}

	private Optional<MeetingInfo> findById(final Principal principal,
			final long id) {
		Objects.requireNonNull(principal);

		return meetingStorage
				.findByOrganisationIdAndId(principal.organisationId(), id);
	}

	public List<MeetingInfo> getAll(final Principal principal) {
		Objects.requireNonNull(principal);

		return meetingStorage
				.findAllByOrganisationId(principal.organisationId());
	}

	public MeetingInfo create(final Principal principal,
			final MeetingAction.Create meeting) {
		Objects.requireNonNull(principal);
		Objects.requireNonNull(meeting);

		final var meetingInfo = new MeetingInfo(principal.organisationId(),
				meeting.name());

		return meetingStorage.create(meetingInfo);
	}
}
