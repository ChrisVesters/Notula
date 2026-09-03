package com.cvesters.notula.block;

import java.util.Objects;

import org.springframework.stereotype.Service;

import com.cvesters.notula.block.bdo.BlockEvent;
import com.cvesters.notula.block.dto.BlockEventDto;
import com.cvesters.notula.common.exception.MissingEntityException;
import com.cvesters.notula.common.messaging.TransactionalPublisher;
import com.cvesters.notula.topic.TopicStorageGateway;
import com.cvesters.notula.topic.bdo.TopicInfo;

@Service
class BlockPublisher {

	private static final String TOPIC = "/topic/meetings/";

	private final TransactionalPublisher publisher;
	private final TopicStorageGateway topicStorage;

	public BlockPublisher(final TransactionalPublisher publisher,
			final TopicStorageGateway topicStorage) {
		this.publisher = publisher;
		this.topicStorage = topicStorage;
	}

	public void publish(final BlockEvent event) {
		Objects.requireNonNull(event);

		final TopicInfo topic = topicStorage.find(event.block().getTopicId())
				.orElseThrow(MissingEntityException::new);

		final var dto = new BlockEventDto(event);
		publisher.send(TOPIC + topic.getMeetingId(), dto);
	}

}
