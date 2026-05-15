package com.cvesters.notula.details;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Service;

import com.cvesters.notula.block.BlockStorageGateway;
import com.cvesters.notula.common.domain.Principal;
import com.cvesters.notula.common.exception.MissingEntityException;
import com.cvesters.notula.details.bdo.BlockDetails;
import com.cvesters.notula.details.bdo.MeetingDetails;
import com.cvesters.notula.details.bdo.TopicDetails;
import com.cvesters.notula.meeting.MeetingStorageGateway;
import com.cvesters.notula.meeting.bdo.MeetingInfo;
import com.cvesters.notula.topic.TopicStorageGateway;
import com.cvesters.notula.topic.bdo.TopicInfo;

@Service
public class DetailsService {

	// TODO: use service?
	private final MeetingStorageGateway meetingStorage;
	private final TopicStorageGateway topicStorage;
	private final BlockStorageGateway blockStorage;

	public DetailsService(final MeetingStorageGateway meetingStorage,
			final TopicStorageGateway topicStorage,
			final BlockStorageGateway blockStorage) {
		this.meetingStorage = meetingStorage;
		this.topicStorage = topicStorage;
		this.blockStorage = blockStorage;
	}

	public MeetingDetails get(final Principal principal, final long id) {
		Objects.requireNonNull(principal);

		final long organisationId = principal.organisationId();

		final MeetingInfo info = meetingStorage
				.findByOrganisationIdAndId(organisationId, id)
				.orElseThrow(MissingEntityException::new);

		final var topicDetails = new ArrayList<TopicDetails>();
		for (final TopicInfo topic : topicStorage.findAllByMeetingId(id)) {
			final List<BlockDetails> blockDetails = blockStorage
					.findAllByTopicId(topic.getId())
					.stream()
					.map(BlockDetails::new)
					.toList();

			topicDetails.add(new TopicDetails(topic, blockDetails));
		}

		return new MeetingDetails(info, topicDetails);
	}
}
