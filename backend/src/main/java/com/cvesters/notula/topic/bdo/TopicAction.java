package com.cvesters.notula.topic.bdo;

import java.util.Objects;
import java.util.Optional;

import lombok.Getter;

import com.cvesters.notula.common.domain.Minutes;
import com.cvesters.notula.common.domain.Splice;
import com.cvesters.notula.common.domain.TextUpdate;

public sealed interface TopicAction {

	@Getter
	final class Create implements TopicAction {

		private final Long afterId;
		private final String name;

		public Create(final Long afterId, final String name) {
			Objects.requireNonNull(name);

			this.afterId = afterId;
			this.name = name;
		}

		public Optional<Long> getAfterId() {
			return Optional.ofNullable(afterId);
		}
	}

	sealed interface Update extends TopicAction {

		void apply(final TopicInfo object);
	}

	@Getter
	final class UpdateName extends TextUpdate<TopicInfo>
			implements TopicAction.Update {

		public UpdateName(final Splice edit) {
			super(TopicInfo::getName, TopicInfo::setName, edit);
		}
	}

	@Getter
	final class UpdateDescription extends TextUpdate<TopicInfo>
			implements TopicAction.Update {

		public UpdateDescription(final Splice edit) {
			super(TopicInfo::getDescription, TopicInfo::setDescription, edit);
		}
	}

	@Getter
	final class UpdateDuration implements TopicAction.Update {

		private final Minutes duration;

		public UpdateDuration(final Minutes duration) {
			this.duration = duration;
		}

		@Override
		public void apply(final TopicInfo object) {
			Objects.requireNonNull(object);

			object.setDuration(duration);
		}
	}

	@Getter
	final class Move implements TopicAction {

		private final Long afterId;

		public Move(final Long afterId) {
			this.afterId = afterId;
		}

		public Optional<Long> getAfterId() {
			return Optional.ofNullable(afterId);
		}
	}
}
