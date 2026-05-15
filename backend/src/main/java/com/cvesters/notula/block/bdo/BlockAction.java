package com.cvesters.notula.block.bdo;

import java.util.Objects;

import org.apache.commons.lang3.Validate;

import lombok.Getter;

public sealed interface BlockAction {

	@Getter
	final class Create implements BlockAction {

		private final BlockType type;
		private final int sequenceId;

		public Create(final BlockType type, final int sequenceId) {
			Objects.requireNonNull(type);
			Validate.isTrue(sequenceId >= 0);

			this.type = type;
			this.sequenceId = sequenceId;
		}
	}
}
