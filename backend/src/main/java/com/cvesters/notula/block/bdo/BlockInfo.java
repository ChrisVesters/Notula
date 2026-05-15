package com.cvesters.notula.block.bdo;

import java.util.Objects;

import org.apache.commons.lang3.Validate;

import lombok.Getter;

@Getter
public class BlockInfo {

	private final Long id;
	private final long organisationId;
	private final long topicId;
	private final BlockType type;
	private int sequenceId;

	public BlockInfo(final long organisationId, final long topicId,
			final BlockType type, final int sequenceId) {
		this(null, organisationId, topicId, type, sequenceId);
	}

	public BlockInfo(final Long id, final long organisationId,
			final long topicId, final BlockType type, final int sequenceId) {
		Objects.requireNonNull(type);
		Validate.isTrue(sequenceId >= 0);

		this.id = id;
		this.organisationId = organisationId;
		this.topicId = topicId;
		this.type = type;
		this.sequenceId = sequenceId;
	}

	public long getId() {
		Validate.validState(id != null);

		return id;
	}

	public void moveUp() {
		Validate.validState(sequenceId > 0);

		sequenceId--;
	}

	public void moveDown() {
		Validate.validState(sequenceId < Integer.MAX_VALUE);

		sequenceId++;
	}
}
