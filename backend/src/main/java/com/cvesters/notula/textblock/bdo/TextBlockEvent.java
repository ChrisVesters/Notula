package com.cvesters.notula.textblock.bdo;

import java.util.Objects;

import com.cvesters.notula.block.bdo.BlockInfo;
import com.cvesters.notula.common.domain.Origin;

public record TextBlockEvent(BlockInfo block, TextBlockAction action,
		Origin origin) {

	public TextBlockEvent {
		Objects.requireNonNull(block);
		Objects.requireNonNull(action);
		Objects.requireNonNull(origin);
	}

}
