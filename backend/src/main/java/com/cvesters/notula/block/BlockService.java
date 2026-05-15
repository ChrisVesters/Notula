package com.cvesters.notula.block;

import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Service;

import com.cvesters.notula.block.bdo.BlockAction;
import com.cvesters.notula.block.bdo.BlockEvent;
import com.cvesters.notula.block.bdo.BlockInfo;
import com.cvesters.notula.common.domain.Principal;
import com.cvesters.notula.topic.TopicService;
import com.cvesters.notula.topic.bdo.TopicInfo;

@Service
public class BlockService {

	private final TopicService topicService;

	private final BlockStorageGateway blockStorage;
	private final BlockPublisher blockPublisher;

	public BlockService(final TopicService topicService,
			final BlockStorageGateway blockStorage,
			final BlockPublisher blockPublisher) {
		this.topicService = topicService;
		this.blockStorage = blockStorage;
		this.blockPublisher = blockPublisher;
	}

	public BlockInfo create(final Principal principal, final long meetingId,
			final long topicId, final BlockAction.Create action) {
		Objects.requireNonNull(principal);
		Objects.requireNonNull(action);

		final TopicInfo topic = topicService.getById(principal, meetingId,
				topicId);

		final List<BlockInfo> existingBlocks = blockStorage
				.findAllByTopicId(topic.getId());
		// TODO: move logic into action?
		if (action.getSequenceId() > existingBlocks.size()) {
			throw new IllegalArgumentException();
		}

		final List<BlockInfo> toUpdateBlocks = existingBlocks.stream()
				.filter(b -> b.getSequenceId() >= action.getSequenceId())
				.toList();
		toUpdateBlocks.forEach(BlockInfo::moveDown);
		blockStorage.updateAll(toUpdateBlocks);

		final var block = new BlockInfo(topic.getOrganisationId(),
				topic.getId(), action.getType(), action.getSequenceId());

		final BlockInfo created = blockStorage.create(block);

		final var event = new BlockEvent(topic.getId(), created.getId(),
				action);
		blockPublisher.publish(meetingId, event);

		return created;
	}
}
