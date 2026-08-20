package com.cvesters.notula.textblock;

import java.util.Objects;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import com.cvesters.notula.common.exception.MissingEntityException;
import com.cvesters.notula.textblock.dao.TextBlockEvent;
import com.cvesters.notula.textblock.dto.TextBlockEventDto;
import com.cvesters.notula.topic.TopicStorageGateway;
import com.cvesters.notula.topic.bdo.TopicInfo;

@Service
public class TextBlockPublisher {

	private static final String TOPIC = "/topic/meetings/";

	private final SimpMessagingTemplate messagingTemplate;
	private final TopicStorageGateway topicStorage;

	public TextBlockPublisher(final SimpMessagingTemplate messagingTemplate,
			final TopicStorageGateway topicStorage) {
		this.messagingTemplate = messagingTemplate;
		this.topicStorage = topicStorage;
	}

	public void publish(final TextBlockEvent event) {
		Objects.requireNonNull(event);

		final TopicInfo topic = topicStorage.find(event.block().getTopicId())
				.orElseThrow(MissingEntityException::new);

		final var eventDto = new TextBlockEventDto(event);
		messagingTemplate.convertAndSend(TOPIC + topic.getMeetingId(),
				eventDto);
	}
}
