package com.cvesters.notula.topic.bdo;

import java.util.Objects;

public record TopicEvent(TopicInfo topic, TopicAction action) {

	public TopicEvent {
		Objects.requireNonNull(topic);
		Objects.requireNonNull(action);
	}
}
