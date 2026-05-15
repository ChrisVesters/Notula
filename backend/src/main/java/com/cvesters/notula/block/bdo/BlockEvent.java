package com.cvesters.notula.block.bdo;

import java.util.Objects;

public record BlockEvent(long topicId, long blockId, BlockAction action) {

	public BlockEvent {
		Objects.requireNonNull(action);
	}
}
