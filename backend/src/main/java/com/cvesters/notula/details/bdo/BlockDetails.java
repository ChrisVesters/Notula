package com.cvesters.notula.details.bdo;

import java.util.Objects;

import lombok.Getter;

import com.cvesters.notula.block.bdo.BlockInfo;
import com.cvesters.notula.block.bdo.BlockType;

@Getter
public class BlockDetails {

	private final long id;
	private final BlockType type;
	private final int sequenceId;

	public BlockDetails(final BlockInfo info) {
		Objects.requireNonNull(info);
		
		this.id = info.getId();
		this.type = info.getType();
		this.sequenceId = info.getSequenceId();
	}

}
