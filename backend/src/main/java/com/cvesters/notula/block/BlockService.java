package com.cvesters.notula.block;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Service;

import com.cvesters.notula.block.bdo.BlockAction;
import com.cvesters.notula.block.bdo.BlockEvent;
import com.cvesters.notula.block.bdo.BlockInfo;
import com.cvesters.notula.common.domain.Principal;
import com.cvesters.notula.common.exception.MissingEntityException;
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

	public BlockInfo getById(final Principal principal, final long blockId) {
		Objects.requireNonNull(principal);

		final long organisationId = principal.organisationId();

		return blockStorage.find(blockId)
				.filter(b -> b.getOrganisationId() == organisationId)
				.orElseThrow(MissingEntityException::new);
	}

	public BlockInfo create(final Principal principal,
			final BlockAction.Create action) {
		Objects.requireNonNull(principal);
		Objects.requireNonNull(action);

		final TopicInfo topic = topicService.getById(principal,
				action.getTopicId());

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

		final var event = new BlockEvent(created, action);
		blockPublisher.publish(event);

		return created;
	}

	public BlockInfo move(final Principal principal, final long blockId,
			final BlockAction.Move action) {
		Objects.requireNonNull(principal);
		Objects.requireNonNull(action);

		final BlockInfo block = getById(principal, blockId);
		final int from = block.getSequenceId();
		final int to = action.getSequenceId();
		final int direction = Integer.signum(to - from);
		if (direction == 0) {
			return block;
		}

		final List<BlockInfo> existingBlocks = blockStorage
				.findAllByTopicId(block.getTopicId());
		if (to >= existingBlocks.size()) {
			throw new IllegalArgumentException();
		}

		final int min = Math.min(from + direction, to);
		final int max = Math.max(from + direction, to);
		final List<BlockInfo> toUpdateBlocks = existingBlocks.stream()
				.filter(b -> b.getSequenceId() >= min)
				.filter(b -> b.getSequenceId() <= max)
				.toList();

		final var events = new ArrayList<BlockEvent>();

		action.apply(block);
		events.add(new BlockEvent(block, action));

		for (final BlockInfo b : toUpdateBlocks) {
			final int updatedSequenceId = b.getSequenceId() - direction;
			final var move = new BlockAction.Move(updatedSequenceId);
			move.apply(b);
			events.add(new BlockEvent(b, move));
		}

		blockStorage.updateAll(events.stream().map(BlockEvent::block).toList());
		events.forEach(blockPublisher::publish);

		return block;
	}

	public void delete(final Principal principal, final long blockId) {
		Objects.requireNonNull(principal);

		final BlockInfo blockInfo = getById(principal, blockId);
		blockStorage.delete(blockInfo);

		final List<BlockInfo> existingBlocks = blockStorage
				.findAllByTopicId(blockInfo.getTopicId());
		// TODO: move logic into action?
		final List<BlockInfo> toUpdateTopics = existingBlocks.stream()
				.filter(t -> t.getSequenceId() > blockInfo.getSequenceId())
				.toList();
		toUpdateTopics.forEach(BlockInfo::moveUp);
		blockStorage.updateAll(toUpdateTopics);
		// TODO: publish move action/event!!

		final var event = new BlockEvent(blockInfo, new BlockAction.Delete());
		blockPublisher.publish(event);
	}
}
