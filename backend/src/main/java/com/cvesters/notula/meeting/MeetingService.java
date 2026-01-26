package com.cvesters.notula.meeting;

import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Service;

import com.cvesters.notula.common.domain.Principal;
import com.cvesters.notula.meeting.bdo.MeetingInfo;

@Service
public class MeetingService {

	private final MeetingStorageGateway meetingStorageGateway;

	public MeetingService(final MeetingStorageGateway meetingStorageGateway) {
		this.meetingStorageGateway = meetingStorageGateway;
	}

	public List<MeetingInfo> getAll(final Principal principal) {
		Objects.requireNonNull(principal);

		return meetingStorageGateway
				.findAllByOrganisationId(principal.organisationId());
	}
}
