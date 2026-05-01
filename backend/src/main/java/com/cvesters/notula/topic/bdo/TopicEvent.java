package com.cvesters.notula.topic.bdo;

import java.util.Objects;

public record TopicEvent(long topicId, TopicAction action) {

	public TopicEvent {
		Objects.requireNonNull(action);
	}
}
