package com.cvesters.notula.block.bdo;

import java.util.Objects;

import com.cvesters.notula.common.domain.Origin;

public record BlockEvent(BlockInfo block, BlockAction action, Origin origin) {

	public BlockEvent {
		Objects.requireNonNull(block);
		Objects.requireNonNull(action);
		Objects.requireNonNull(origin);
	}
}
