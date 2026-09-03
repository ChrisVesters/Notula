package com.cvesters.notula.topic;

import java.util.Objects;

import org.springframework.stereotype.Service;

import com.cvesters.notula.common.messaging.TransactionalPublisher;
import com.cvesters.notula.topic.bdo.TopicEvent;
import com.cvesters.notula.topic.dto.TopicEventDto;

@Service
public class TopicPublisher {

	private static final String TOPIC = "/topic/meetings/";

	private final TransactionalPublisher publisher;

	public TopicPublisher(final TransactionalPublisher publisher) {
		this.publisher = publisher;
	}

	public void publish(final TopicEvent event) {
		Objects.requireNonNull(event);

		final long meetingId = event.topic().getMeetingId();

		final var eventDto = new TopicEventDto(event);
		publisher.send(TOPIC + meetingId, eventDto);
	}

}
