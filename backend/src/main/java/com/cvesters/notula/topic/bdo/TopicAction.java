package com.cvesters.notula.topic.bdo;

import org.apache.commons.lang3.Validate;

public final class TopicAction {

	public static record Create(String name) {

		public Create {
			Validate.notBlank(name);
		}
	}
}
