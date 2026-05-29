package com.cvesters.notula.textblock.dao;

import java.util.Objects;

import com.cvesters.notula.textblock.bdo.TextBlockAction;

public record TextBlockEvent(long topicId, long blockId, TextBlockAction action) {

	public TextBlockEvent {
		Objects.requireNonNull(action);
	}

}
