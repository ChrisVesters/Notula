package com.cvesters.notula.block;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Service;

import com.cvesters.notula.block.bdo.BlockAction;
import com.cvesters.notula.block.bdo.BlockEvent;
import com.cvesters.notula.block.bdo.BlockInfo;
import com.cvesters.notula.common.domain.Origin;
import com.cvesters.notula.common.domain.Principal;
import com.cvesters.notula.common.exception.MissingEntityException;
import com.cvesters.notula.meeting.MeetingLock;
import com.cvesters.notula.topic.TopicService;
import com.cvesters.notula.topic.bdo.TopicInfo;

@Service
public class BlockService {

	private final TopicService topicService;
	private final MeetingLock meetingLock;

	private final BlockStorageGateway blockStorage;
	private final BlockPublisher blockPublisher;

	public BlockService(final TopicService topicService,
			final MeetingLock meetingLock,
			final BlockStorageGateway blockStorage,
			final BlockPublisher blockPublisher) {
		this.topicService = topicService;
		this.meetingLock = meetingLock;
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

	public long getMeetingId(final Principal principal, final long blockId) {
		final BlockInfo block = getById(principal, blockId);

		return topicService.getMeetingId(principal, block.getTopicId());
	}

	public BlockInfo create(final Origin origin,
			final BlockAction.Create action) {
		Objects.requireNonNull(origin);
		Objects.requireNonNull(action);

		final long meetingId = topicService.getMeetingId(origin.principal(),
				action.getTopicId());

		return meetingLock.call(meetingId,
				() -> doCreate(origin, action));
	}

	private BlockInfo doCreate(final Origin origin,
			final BlockAction.Create action) {
		final TopicInfo topic = topicService.getById(origin.principal(),
				action.getTopicId());

		final List<BlockInfo> existingBlocks = blockStorage
				.findAllByTopicId(topic.getId());
		if (action.getSequenceId() > existingBlocks.size()) {
			throw new IllegalArgumentException();
		}

		final var events = new ArrayList<BlockEvent>();
		for (final BlockInfo b : existingBlocks) {
			if (b.getSequenceId() < action.getSequenceId()) {
				continue;
			}

			final int updatedSequenceId = b.getSequenceId() + 1;
			final var move = new BlockAction.Move(updatedSequenceId);
			move.apply(b);
			final BlockInfo updatedBlock = blockStorage.update(b);
			events.add(new BlockEvent(updatedBlock, move, origin));
		}

		final var block = new BlockInfo(topic.getOrganisationId(),
				topic.getId(), action.getType(), action.getSequenceId());
		final BlockInfo created = blockStorage.create(block);
		events.add(new BlockEvent(created, action, origin));

		events.forEach(blockPublisher::publish);

		return created;
	}

	public BlockInfo move(final Origin origin, final long blockId,
			final BlockAction.Move action) {
		Objects.requireNonNull(origin);
		Objects.requireNonNull(action);

		final long meetingId = getMeetingId(origin.principal(), blockId);

		return meetingLock.call(meetingId,
				() -> doMove(origin, blockId, action));
	}

	private BlockInfo doMove(final Origin origin, final long blockId,
			final BlockAction.Move action) {
		final BlockInfo block = getById(origin.principal(), blockId);
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
		final BlockInfo updated = blockStorage.update(block);
		events.add(new BlockEvent(updated, action, origin));

		for (final BlockInfo b : toUpdateBlocks) {
			final int updatedSequenceId = b.getSequenceId() - direction;
			final var move = new BlockAction.Move(updatedSequenceId);
			move.apply(b);
			final BlockInfo updatedBlock = blockStorage.update(b);
			events.add(new BlockEvent(updatedBlock, move, origin));
		}

		events.forEach(blockPublisher::publish);

		return block;
	}

	public void delete(final Origin origin, final long blockId) {
		Objects.requireNonNull(origin);

		final long meetingId = getMeetingId(origin.principal(), blockId);

		meetingLock.run(meetingId,
				() -> doDelete(origin, blockId));
	}

	private void doDelete(final Origin origin, final long blockId) {
		final BlockInfo blockInfo = getById(origin.principal(), blockId);
		blockStorage.delete(blockInfo);

		final var events = new ArrayList<BlockEvent>();
		events.add(
				new BlockEvent(blockInfo, new BlockAction.Delete(), origin));

		final List<BlockInfo> existingBlocks = blockStorage
				.findAllByTopicId(blockInfo.getTopicId());
		for (final BlockInfo b : existingBlocks) {
			if (b.getSequenceId() <= blockInfo.getSequenceId()) {
				continue;
			}

			final int updatedSequenceId = b.getSequenceId() - 1;
			final var move = new BlockAction.Move(updatedSequenceId);
			move.apply(b);
			final BlockInfo updatedBlock = blockStorage.update(b);
			events.add(new BlockEvent(updatedBlock, move, origin));
		}

		events.forEach(blockPublisher::publish);
	}
}
