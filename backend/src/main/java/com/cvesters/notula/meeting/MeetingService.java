package com.cvesters.notula.meeting;

import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Service;

import com.cvesters.notula.common.domain.Principal;
import com.cvesters.notula.common.exception.MissingEntityException;
import com.cvesters.notula.meeting.bdo.MeetingDetails;
import com.cvesters.notula.meeting.bdo.MeetingInfo;

@Service
public class MeetingService {

	private final MeetingStorageGateway meetingStorage;

	public MeetingService(final MeetingStorageGateway meetingStorageGateway) {
		this.meetingStorage = meetingStorageGateway;
	}

	public boolean existsById(final Principal principal, final long id) {
		Objects.requireNonNull(principal);

		return meetingStorage.findById(id)
				.filter(meeting -> meeting.getOrganisationId() == principal
						.organisationId())
				.isPresent();
	}

	public List<MeetingInfo> getAll(final Principal principal) {
		Objects.requireNonNull(principal);

		return meetingStorage
				.findAllByOrganisationId(principal.organisationId());
	}

	public MeetingDetails getDetails(final Principal principal, final long id) {
		Objects.requireNonNull(principal);

		final MeetingInfo info = meetingStorage.findById(id)
				.filter(meeting -> meeting.getOrganisationId() == principal
						.organisationId())
				.orElseThrow(MissingEntityException::new);

		return new MeetingDetails(info);
	}

	public MeetingInfo create(final Principal principal,
			final MeetingInfo meeting) {
		Objects.requireNonNull(principal);
		Objects.requireNonNull(meeting);

		return meetingStorage.create(meeting);
	}
}
