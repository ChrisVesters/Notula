package com.cvesters.notula.meeting;

import java.util.Objects;

import org.springframework.stereotype.Service;

import com.cvesters.notula.block.bdo.BlockEvent;
import com.cvesters.notula.common.domain.Origin;
import com.cvesters.notula.common.dto.OriginDto;
import com.cvesters.notula.common.messaging.TransactionalPublisher;
import com.cvesters.notula.meeting.bdo.MeetingEvent;
import com.cvesters.notula.meeting.bdo.MeetingScope;
import com.cvesters.notula.meeting.dto.BlockMutationDto;
import com.cvesters.notula.meeting.dto.EventDto;
import com.cvesters.notula.meeting.dto.MeetingMutationDto;
import com.cvesters.notula.meeting.dto.MutationDto;
import com.cvesters.notula.meeting.dto.TextBlockMutationDto;
import com.cvesters.notula.meeting.dto.TopicMutationDto;
import com.cvesters.notula.textblock.bdo.TextBlockEvent;
import com.cvesters.notula.topic.bdo.TopicEvent;

@Service
public class EventPublisher {

	private static final String TOPIC = "/topic/meetings/";

	private final TransactionalPublisher publisher;

	public EventPublisher(final TransactionalPublisher publisher) {
		this.publisher = publisher;
	}

	public void publish(final MeetingScope scope, final MeetingEvent event) {
		Objects.requireNonNull(event);

		send(scope, event.origin(), MeetingMutationDto.of(event));
	}

	public void publish(final MeetingScope scope, final TopicEvent event) {
		Objects.requireNonNull(event);

		send(scope, event.origin(), TopicMutationDto.of(event));
	}

	public void publish(final MeetingScope scope, final BlockEvent event) {
		Objects.requireNonNull(event);

		send(scope, event.origin(), BlockMutationDto.of(event));
	}

	public void publish(final MeetingScope scope, final TextBlockEvent event) {
		Objects.requireNonNull(event);

		send(scope, event.origin(), TextBlockMutationDto.of(event));
	}

	private void send(final MeetingScope scope, final Origin origin,
			final MutationDto mutation) {
		final var dto = new EventDto(scope.revision(), new OriginDto(origin),
				mutation);

		publisher.send(TOPIC + scope.meetingId(), dto);
	}
}
