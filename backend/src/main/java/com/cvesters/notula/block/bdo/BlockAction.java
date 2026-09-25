package com.cvesters.notula.block.bdo;

import java.util.Objects;
import java.util.Optional;

import lombok.Getter;

public sealed interface BlockAction {

	@Getter
	final class Create implements BlockAction {

		private final long topicId;
		private final BlockType type;
		private final Long afterId;

		public Create(final long topicId, final BlockType type,
				final Long afterId) {
			Objects.requireNonNull(type);

			this.topicId = topicId;
			this.type = type;
			this.afterId = afterId;
		}

		public Optional<Long> getAfterId() {
			return Optional.ofNullable(afterId);
		}
	}

	@Getter
	final class Move implements BlockAction {

		private final Long afterId;

		public Move(final Long afterId) {
			this.afterId = afterId;
		}

		public Optional<Long> getAfterId() {
			return Optional.ofNullable(afterId);
		}
	}
}
