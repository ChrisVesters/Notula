package com.cvesters.notula.topic;

import java.util.Objects;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import com.cvesters.notula.topic.bdo.TopicEvent;
import com.cvesters.notula.topic.dto.TopicEventDto;

@Service
public class TopicPublisher {

	private final SimpMessagingTemplate messagingTemplate;

	public TopicPublisher(final SimpMessagingTemplate messagingTemplate) {
		this.messagingTemplate = messagingTemplate;
	}

	public void publish(final long meetingId, final TopicEvent event) {
		Objects.requireNonNull(event);

		final var eventDto = new TopicEventDto(event);
		messagingTemplate.convertAndSend("/topic/meetings/" + meetingId,
				eventDto);
	}

}
