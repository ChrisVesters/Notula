package com.cvesters.notula.meeting.dto;

import jakarta.validation.constraints.NotNull;

import com.cvesters.notula.meeting.bdo.MeetingAction;

public final class MeetingActionDto {

	private MeetingActionDto() {
	}

	public record Create(@NotNull String name) {

		public MeetingAction.Create toBdo() {
			return new MeetingAction.Create(name);
		}
	}
}
