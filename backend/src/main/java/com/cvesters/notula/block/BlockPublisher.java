package com.cvesters.notula.block;

import java.util.Objects;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import com.cvesters.notula.block.bdo.BlockEvent;
import com.cvesters.notula.block.dto.BlockEventDto;

@Service
class BlockPublisher {

	private final SimpMessagingTemplate messagingTemplate;

	public BlockPublisher(final SimpMessagingTemplate messagingTemplate) {
		this.messagingTemplate = messagingTemplate;
	}

	public void publish(final long meetingId, final BlockEvent event) {
		Objects.requireNonNull(event);

		final var dto = new BlockEventDto(event);
		messagingTemplate.convertAndSend("/topic/meetings/" + meetingId, dto);
	}

}
