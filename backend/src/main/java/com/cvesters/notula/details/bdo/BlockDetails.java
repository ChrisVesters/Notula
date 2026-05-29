package com.cvesters.notula.details.bdo;

import java.util.Objects;

import lombok.Getter;

import com.cvesters.notula.block.bdo.BlockInfo;

@Getter
public class BlockDetails {

	private final long id;
	private final int sequenceId;
	private final BlockContent content;

	public BlockDetails(final BlockInfo info, final BlockContent content) {
		Objects.requireNonNull(info);
		Objects.requireNonNull(content);

		this.id = info.getId();
		this.sequenceId = info.getSequenceId();
		this.content = content;
	}
}
