package com.cvesters.notula.block;

import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Service;

import com.cvesters.notula.block.bdo.BlockAction;
import com.cvesters.notula.block.bdo.BlockInfo;
import com.cvesters.notula.common.domain.Origin;
import com.cvesters.notula.common.domain.Principal;
import com.cvesters.notula.common.domain.Rank;
import com.cvesters.notula.common.exception.MissingEntityException;
import com.cvesters.notula.event.EventService;
import com.cvesters.notula.event.bdo.BlockMutation;
import com.cvesters.notula.event.bdo.EventInfo;
import com.cvesters.notula.meeting.bdo.MeetingScope;
import com.cvesters.notula.topic.TopicService;
import com.cvesters.notula.topic.bdo.TopicInfo;

@Service
public class BlockService {

	private final TopicService topicService;

	private final BlockStorageGateway blockStorage;
	private final EventService eventService;

	public BlockService(final TopicService topicService,
			final BlockStorageGateway blockStorage,
			final EventService eventService) {
		this.topicService = topicService;
		this.blockStorage = blockStorage;
		this.eventService = eventService;
	}

	public BlockInfo getById(final Principal principal, final long blockId) {
		Objects.requireNonNull(principal);

		final long organisationId = principal.organisationId();

		return blockStorage.find(blockId)
				.filter(b -> b.getOrganisationId() == organisationId)
				.orElseThrow(MissingEntityException::new);
	}

	public BlockInfo getById(final Principal principal, final long meetingId,
			final long blockId) {
		final BlockInfo block = getById(principal, blockId);
		topicService.getById(principal, meetingId, block.getTopicId());

		return block;
	}

	public BlockInfo create(final Origin origin, final MeetingScope scope,
			final BlockAction.Create action) {
		Objects.requireNonNull(origin);
		Objects.requireNonNull(scope);
		Objects.requireNonNull(action);

		final TopicInfo topic = topicService.getById(origin.principal(),
				scope.meetingId(), action.getTopicId());

		final List<BlockInfo> siblings = blockStorage
				.findAllByTopicId(topic.getId());

		final BlockInfo after = action.getAfterId()
				.map(id -> find(siblings, id))
				.orElse(null);

		final Rank rank = Rank.after(after, siblings, BlockInfo::getRank);

		final var block = new BlockInfo(topic.getOrganisationId(),
				topic.getId(), action.getType(), rank);
		final BlockInfo created = blockStorage.create(block);

		final var mutation = new BlockMutation.Add(created.getId(),
				created.getTopicId(), created.getType(), created.getRank());
		eventService.publish(new EventInfo(scope, origin, mutation));

		return created;
	}

	public BlockInfo move(final Origin origin, final MeetingScope scope,
			final long blockId, final BlockAction.Move action) {
		Objects.requireNonNull(origin);
		Objects.requireNonNull(scope);
		Objects.requireNonNull(action);

		final BlockInfo block = getById(origin.principal(), scope.meetingId(),
				blockId);

		final List<BlockInfo> siblings = blockStorage
				.findAllByTopicId(block.getTopicId())
				.stream()
				.filter(b -> b.getId() != blockId)
				.toList();

		final BlockInfo after = action.getAfterId()
				.map(id -> find(siblings, id))
				.orElse(null);

		final Rank rank = Rank.after(after, siblings, BlockInfo::getRank);
		block.setRank(rank);

		final BlockInfo updated = blockStorage.update(block);

		final var mutation = new BlockMutation.Move(updated.getId(),
				updated.getRank());
		eventService.publish(new EventInfo(scope, origin, mutation));

		return updated;
	}

	public void delete(final Origin origin, final MeetingScope scope,
			final long blockId) {
		Objects.requireNonNull(origin);
		Objects.requireNonNull(scope);

		final BlockInfo block = getById(origin.principal(), scope.meetingId(),
				blockId);
		blockStorage.delete(block);

		final var mutation = new BlockMutation.Remove(block.getId());
		eventService.publish(new EventInfo(scope, origin, mutation));
	}

	private static BlockInfo find(final List<BlockInfo> elements,
			final long blockId) {
		return elements.stream()
				.filter(b -> b.getId() == blockId)
				.findFirst()
				.orElseThrow(MissingEntityException::new);
	}
}
