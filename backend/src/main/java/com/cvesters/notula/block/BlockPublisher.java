package com.cvesters.notula.block;

import java.util.Objects;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import com.cvesters.notula.block.bdo.BlockEvent;
import com.cvesters.notula.block.dto.BlockEventDto;
import com.cvesters.notula.common.exception.MissingEntityException;
import com.cvesters.notula.topic.TopicStorageGateway;
import com.cvesters.notula.topic.bdo.TopicInfo;

@Service
class BlockPublisher {

	private static final String TOPIC = "/topic/meetings/";

	private final SimpMessagingTemplate messagingTemplate;
	private final TopicStorageGateway topicStorage;

	public BlockPublisher(final SimpMessagingTemplate messagingTemplate,
			final TopicStorageGateway topicStorage) {
		this.messagingTemplate = messagingTemplate;
		this.topicStorage = topicStorage;
	}

	public void publish(final BlockEvent event) {
		Objects.requireNonNull(event);

		final TopicInfo topic = topicStorage.find(event.block().getTopicId())
				.orElseThrow(MissingEntityException::new);

		final var dto = new BlockEventDto(event);
		messagingTemplate.convertAndSend(TOPIC + topic.getMeetingId(), dto);
	}

}
