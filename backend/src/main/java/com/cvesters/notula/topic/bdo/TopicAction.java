package com.cvesters.notula.topic.bdo;

import java.util.Objects;

import org.apache.commons.lang3.Validate;

import lombok.Getter;

import com.cvesters.notula.common.domain.TextUpdate;

public sealed interface TopicAction {

	@Getter
	final class Create implements TopicAction {

		private final int sequenceId;
		private final String name;

		public Create(final int sequenceId, final String name) {
			Validate.isTrue(sequenceId >= 0);
			Objects.requireNonNull(name);

			this.sequenceId = sequenceId;
			this.name = name;
		}
	}

	sealed interface Update extends TopicAction {

		void apply(final TopicInfo object);
	}

	@Getter
	final class UpdateName extends TextUpdate<TopicInfo>
			implements TopicAction.Update {

		public UpdateName(final int position, final int length,
				final String value) {
			super(TopicInfo::getName, TopicInfo::setName, position, length,
					value);
		}
	}

	@Getter
	final class UpdateDescription extends TextUpdate<TopicInfo>
			implements TopicAction.Update {

		public UpdateDescription(final int position, final int length,
				final String value) {
			super(TopicInfo::getDescription, TopicInfo::setDescription,
					position, length, value);
		}
	}

	@Getter
	final class Delete implements TopicAction {
	}
}
