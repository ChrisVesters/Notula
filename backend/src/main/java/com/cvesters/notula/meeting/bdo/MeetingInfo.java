package com.cvesters.notula.meeting.bdo;

import org.apache.commons.lang3.Validate;

import lombok.Getter;

@Getter
public class MeetingInfo {

	private final Long id;
	private final String name;

	public MeetingInfo(final String name) {
		this(null, name);
	}

	public MeetingInfo(final Long id, final String name) {
		Validate.notBlank(name);

		this.id = id;
		this.name = name;
	}

	// TODO: Optional for getId, or just throw exception if null?
	// TODO: make other object consistent with this!
	// public Optional<Long> getId() {
	// 	return Optional.ofNullable(id);
	// }
}
