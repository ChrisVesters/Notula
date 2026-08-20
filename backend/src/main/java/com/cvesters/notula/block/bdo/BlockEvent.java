package com.cvesters.notula.block.bdo;

import java.util.Objects;

public record BlockEvent(BlockInfo block, BlockAction action) {

	public BlockEvent {
		Objects.requireNonNull(block);
		Objects.requireNonNull(action);
	}
}
