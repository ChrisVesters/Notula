package com.cvesters.notula.meeting;

import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Service;

import com.cvesters.notula.common.domain.Principal;
import com.cvesters.notula.common.exception.MissingEntityException;
import com.cvesters.notula.meeting.bdo.MeetingDetails;
import com.cvesters.notula.meeting.bdo.MeetingInfo;
import com.cvesters.notula.topic.TopicStorageGateway;
import com.cvesters.notula.topic.bdo.TopicInfo;

@Service
public class MeetingDetailsService {

	private final MeetingStorageGateway meetingStorage;
	private final TopicStorageGateway topicStorage;

	public MeetingDetailsService(final MeetingStorageGateway meetingStorage,
			final TopicStorageGateway topicStorage) {
		this.meetingStorage = meetingStorage;
		this.topicStorage = topicStorage;
	}

	public MeetingDetails get(final Principal principal, final long id) {
		Objects.requireNonNull(principal);

		final long organisationId = principal.organisationId();

		final MeetingInfo info = meetingStorage
				.findByOrganisationIdAndId(organisationId, id)
				.orElseThrow(MissingEntityException::new);
		final List<TopicInfo> topics = topicStorage.findAllByMeetingId(id);

		return new MeetingDetails(info, topics);
	}
}
