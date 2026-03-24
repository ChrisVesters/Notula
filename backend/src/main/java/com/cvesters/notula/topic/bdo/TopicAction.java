package com.cvesters.notula.topic.bdo;

import org.apache.commons.lang3.Validate;

public final class TopicAction {

	private TopicAction() {
	}

	public final static record Create(String name) {

		public Create {
			Validate.notBlank(name);
		}
	}
}
