package com.cvesters.notula.textblock;

import java.util.Objects;

import org.springframework.stereotype.Service;

import com.cvesters.notula.block.BlockService;
import com.cvesters.notula.block.bdo.BlockInfo;
import com.cvesters.notula.block.bdo.BlockType;
import com.cvesters.notula.common.domain.Origin;
import com.cvesters.notula.common.exception.InvalidActionException;
import com.cvesters.notula.meeting.EventPublisher;
import com.cvesters.notula.meeting.bdo.MeetingScope;
import com.cvesters.notula.textblock.bdo.TextBlockAction;
import com.cvesters.notula.textblock.bdo.TextBlockEvent;
import com.cvesters.notula.textblock.bdo.TextBlockInfo;

@Service
public class TextBlockService {

	private final BlockService blockService;

	private final TextBlockStorageGateway textBlockStorage;
	private final EventPublisher eventPublisher;

	public TextBlockService(final BlockService blockService,
			final TextBlockStorageGateway textBlockStorage,
			final EventPublisher eventPublisher) {
		this.blockService = blockService;
		this.textBlockStorage = textBlockStorage;
		this.eventPublisher = eventPublisher;
	}

	public TextBlockInfo update(final Origin origin, final MeetingScope scope,
			final long blockId, final TextBlockAction.Update action) {
		Objects.requireNonNull(origin);
		Objects.requireNonNull(scope);
		Objects.requireNonNull(action);

		final BlockInfo blockInfo = blockService.getById(origin.principal(),
				scope.meetingId(), blockId);
		if (blockInfo.getType() != BlockType.TEXT) {
			throw new InvalidActionException();
		}

		final TextBlockInfo textBlockInfo = textBlockStorage
				.find(blockInfo.getId())
				.orElseGet(() -> new TextBlockInfo(blockInfo.getId(), ""));
		action.apply(textBlockInfo);
		final TextBlockInfo updated = textBlockStorage.update(textBlockInfo);

		final var event = new TextBlockEvent(blockInfo, action, origin);
		eventPublisher.publish(scope, event);

		return updated;
	}
}
