package com.cvesters.notula.textblock;

import java.util.Objects;

import org.springframework.stereotype.Service;

import com.cvesters.notula.block.BlockService;
import com.cvesters.notula.block.bdo.BlockInfo;
import com.cvesters.notula.block.bdo.BlockType;
import com.cvesters.notula.common.domain.Origin;
import com.cvesters.notula.common.exception.InvalidActionException;
import com.cvesters.notula.meeting.MeetingLock;
import com.cvesters.notula.textblock.bdo.TextBlockAction;
import com.cvesters.notula.textblock.bdo.TextBlockInfo;
import com.cvesters.notula.textblock.dao.TextBlockEvent;

@Service
public class TextBlockService {

	private final BlockService blockService;
	private final MeetingLock meetingLock;

	private final TextBlockStorageGateway textBlockStorage;
	private final TextBlockPublisher textBlockPublisher;

	public TextBlockService(final BlockService blockService,
			final MeetingLock meetingLock,
			final TextBlockStorageGateway textBlockStorage,
			final TextBlockPublisher textBlockPublisher) {
		this.blockService = blockService;
		this.meetingLock = meetingLock;
		this.textBlockStorage = textBlockStorage;
		this.textBlockPublisher = textBlockPublisher;
	}

	public TextBlockInfo update(final Origin origin, final long blockId,
			final TextBlockAction.Update action) {
		Objects.requireNonNull(origin);
		Objects.requireNonNull(action);

		final long meetingId = blockService.getMeetingId(origin.principal(),
				blockId);

		return meetingLock.call(meetingId,
				() -> doUpdate(origin, blockId, action));
	}

	private TextBlockInfo doUpdate(final Origin origin, final long blockId,
			final TextBlockAction.Update action) {
		final BlockInfo blockInfo = blockService.getById(origin.principal(),
				blockId);
		if (blockInfo.getType() != BlockType.TEXT) {
			throw new InvalidActionException();
		}

		final TextBlockInfo textBlockInfo = textBlockStorage
				.find(blockInfo.getId())
				.orElseGet(() -> new TextBlockInfo(blockInfo.getId(), ""));
		action.apply(textBlockInfo);
		final TextBlockInfo updated = textBlockStorage
				.update(textBlockInfo);

		final var event = new TextBlockEvent(blockInfo, action, origin);
		textBlockPublisher.publish(event);

		return updated;
	}
}
