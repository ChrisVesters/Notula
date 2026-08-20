package com.cvesters.notula.topic;

import java.util.Objects;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import com.cvesters.notula.topic.bdo.TopicEvent;
import com.cvesters.notula.topic.dto.TopicEventDto;

@Service
public class TopicPublisher {

	private static final String TOPIC = "/topic/meetings/";

	private final SimpMessagingTemplate messagingTemplate;

	public TopicPublisher(final SimpMessagingTemplate messagingTemplate) {
		this.messagingTemplate = messagingTemplate;
	}

	public void publish(final TopicEvent event) {
		Objects.requireNonNull(event);

		final long meetingId = event.topic().getMeetingId();

		final var eventDto = new TopicEventDto(event);
		messagingTemplate.convertAndSend(TOPIC + meetingId, eventDto);
	}

}
