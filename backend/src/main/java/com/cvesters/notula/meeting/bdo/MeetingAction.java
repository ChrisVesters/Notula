package com.cvesters.notula.meeting.bdo;

import org.apache.commons.lang3.Validate;

public final class MeetingAction {

	private MeetingAction() {
	}

	public static record Create(String name) {

		public Create {
			Validate.notBlank(name);
		}
	}

}
