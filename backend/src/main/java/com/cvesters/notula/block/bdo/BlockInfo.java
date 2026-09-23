package com.cvesters.notula.block.bdo;

import java.util.Objects;

import org.apache.commons.lang3.Validate;

import lombok.Getter;

import com.cvesters.notula.common.domain.Rank;

@Getter
public class BlockInfo {

	private final Long id;
	private final long organisationId;
	private final long topicId;
	private final BlockType type;
	private Rank rank;

	public BlockInfo(final long organisationId, final long topicId,
			final BlockType type, final Rank rank) {
		this(null, organisationId, topicId, type, rank);
	}

	public BlockInfo(final Long id, final long organisationId,
			final long topicId, final BlockType type, final Rank rank) {
		Objects.requireNonNull(type);
		Objects.requireNonNull(rank);

		this.id = id;
		this.organisationId = organisationId;
		this.topicId = topicId;
		this.type = type;
		this.rank = rank;
	}

	public long getId() {
		Validate.validState(id != null);

		return id;
	}

	public void setRank(final Rank rank) {
		Objects.requireNonNull(rank);

		this.rank = rank;
	}
}
