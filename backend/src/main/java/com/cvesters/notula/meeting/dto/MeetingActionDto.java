package com.cvesters.notula.meeting.dto;

import jakarta.validation.constraints.NotBlank;

import com.cvesters.notula.meeting.bdo.MeetingAction;

public final class MeetingActionDto {

	private MeetingActionDto() {
	}

	public static record Create(@NotBlank String name) {

		public MeetingAction.Create toBdo() {
			return new MeetingAction.Create(name);
		}
	}
}
