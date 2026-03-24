package com.cvesters.notula.topic.bdo;

import java.util.Objects;

import org.apache.commons.lang3.Validate;

public sealed interface TopicEvent {

	public final static record Create(long id, String name)
			implements TopicEvent {

		public Create {
			Validate.notBlank(name);
		}

		public Create(final TopicInfo topic) {
			Objects.requireNonNull(topic);

			this(topic.getId(), topic.getName());
		}
	}
}
