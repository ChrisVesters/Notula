package com.cvesters.notula.block.bdo;

import java.util.Objects;

import org.apache.commons.lang3.Validate;

import lombok.Getter;

public sealed interface BlockAction {

	@Getter
	final class Create implements BlockAction {

		private final long topicId;
		private final BlockType type;
		private final int sequenceId;

		public Create(final long topicId, final BlockType type,
				final int sequenceId) {
			Objects.requireNonNull(type);
			Validate.isTrue(sequenceId >= 0);

			this.topicId = topicId;
			this.type = type;
			this.sequenceId = sequenceId;
		}
	}

	sealed interface Update extends BlockAction {

		void apply(final BlockInfo block);
	}

	@Getter
	final class Move implements BlockAction.Update {

		private final int sequenceId;

		public Move(final int sequenceId) {
			Validate.isTrue(sequenceId >= 0);

			this.sequenceId = sequenceId;
		}

		@Override
		public void apply(final BlockInfo block) {
			Objects.requireNonNull(block);

			block.setSequenceId(sequenceId);
		}
	}

	@Getter
	final class Delete implements BlockAction {
	}
}
