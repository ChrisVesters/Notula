package com.cvesters.notula.meeting;

import java.util.Objects;

import org.springframework.stereotype.Service;

import com.cvesters.notula.common.messaging.TransactionalPublisher;
import com.cvesters.notula.meeting.bdo.MeetingEvent;
import com.cvesters.notula.meeting.dto.MeetingEventDto;

@Service
public class MeetingPublisher {

	private static final String TOPIC = "/topic/meetings/";

	private final TransactionalPublisher publisher;

	public MeetingPublisher(final TransactionalPublisher publisher) {
		this.publisher = publisher;
	}

	public void publish(final MeetingEvent event) {
		Objects.requireNonNull(event);

		final var dto = new MeetingEventDto(event);
		publisher.send(TOPIC + event.meetingId(), dto);
	}

}
