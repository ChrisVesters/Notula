package com.cvesters.notula.textblock;

import java.util.Objects;

import org.springframework.stereotype.Service;

import com.cvesters.notula.block.BlockService;
import com.cvesters.notula.block.bdo.BlockInfo;
import com.cvesters.notula.block.bdo.BlockType;
import com.cvesters.notula.common.domain.Principal;
import com.cvesters.notula.common.exception.InvalidActionException;
import com.cvesters.notula.textblock.bdo.TextBlockAction;
import com.cvesters.notula.textblock.bdo.TextBlockInfo;
import com.cvesters.notula.textblock.dao.TextBlockEvent;

@Service
public class TextBlockService {

	private final BlockService blockService;

	private final TextBlockStorageGateway textBlockStorage;
	private final TextBlockPublisher textBlockPublisher;

	public TextBlockService(final BlockService blockService,
			final TextBlockStorageGateway textBlockStorage,
			final TextBlockPublisher textBlockPublisher) {
		this.blockService = blockService;
		this.textBlockStorage = textBlockStorage;
		this.textBlockPublisher = textBlockPublisher;
	}

	public TextBlockInfo update(final Principal principal, final long meetingId,
			final long topicId, final long blockId,
			final TextBlockAction.Update action) {
		Objects.requireNonNull(action);

		final BlockInfo blockInfo = blockService.getById(principal, meetingId,
				topicId, blockId);
		if (blockInfo.getType() != BlockType.TEXT) {
			throw new InvalidActionException();
		}

		final TextBlockInfo textBlockInfo = textBlockStorage
				.find(blockInfo.getId())
				.orElseGet(() -> new TextBlockInfo(blockInfo.getId(), ""));
		action.apply(textBlockInfo);
		final TextBlockInfo updated = textBlockStorage.update(textBlockInfo);

		final var event = new TextBlockEvent(topicId, blockId, action);
		textBlockPublisher.publish(meetingId, event);

		return updated;
	}
}
