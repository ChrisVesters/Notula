package com.cvesters.notula.textblock;

import java.util.Objects;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import com.cvesters.notula.textblock.dao.TextBlockEvent;
import com.cvesters.notula.textblock.dto.TextBlockEventDto;

@Service
public class TextBlockPublisher {

	private final SimpMessagingTemplate messagingTemplate;

	public TextBlockPublisher(final SimpMessagingTemplate messagingTemplate) {
		this.messagingTemplate = messagingTemplate;
	}

	public void publish(final long meetingId, final TextBlockEvent event) {
		Objects.requireNonNull(event);

		final var eventDto = new TextBlockEventDto(event);
		messagingTemplate.convertAndSend("/topic/meetings/" + meetingId,
				eventDto);
	}
}
