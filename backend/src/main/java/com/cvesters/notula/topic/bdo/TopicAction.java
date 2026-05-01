package com.cvesters.notula.topic.bdo;

import java.util.Objects;

import lombok.Getter;

import com.cvesters.notula.common.domain.TextUpdate;

public sealed interface TopicAction {

	@Getter
	final class Create implements TopicAction {

		private final String name;

		public Create(final String name) {
			Objects.requireNonNull(name);

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
}
