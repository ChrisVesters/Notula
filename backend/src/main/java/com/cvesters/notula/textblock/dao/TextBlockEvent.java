package com.cvesters.notula.textblock.dao;

import java.util.Objects;

import com.cvesters.notula.block.bdo.BlockInfo;
import com.cvesters.notula.textblock.bdo.TextBlockAction;

public record TextBlockEvent(BlockInfo block, TextBlockAction action) {

	public TextBlockEvent {
		Objects.requireNonNull(block);
		Objects.requireNonNull(action);
	}

}
