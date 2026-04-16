package com.cvesters.notula.meeting;

import java.util.Objects;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import com.cvesters.notula.meeting.bdo.MeetingEvent;
import com.cvesters.notula.meeting.dto.MeetingEventDto;

@Service
public class MeetingPublisher {

	private static final String TOPIC = "/topic/meetings/";

	private final SimpMessagingTemplate messagingTemplate;

	public MeetingPublisher(final SimpMessagingTemplate messagingTemplate) {
		this.messagingTemplate = messagingTemplate;
	}

	public void publish(final MeetingEvent event) {
		Objects.requireNonNull(event);

		final var dto = MeetingEventDto.of(event);
		messagingTemplate.convertAndSend(TOPIC + event.meetingId(), dto);
	}

}
