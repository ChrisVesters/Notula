package com.cvesters.notula.topic.bdo;

import java.util.Objects;

import com.cvesters.notula.common.domain.Origin;

public record TopicEvent(TopicInfo topic, TopicAction action, Origin origin) {

	public TopicEvent {
		Objects.requireNonNull(topic);
		Objects.requireNonNull(action);
		Objects.requireNonNull(origin);
	}
}
