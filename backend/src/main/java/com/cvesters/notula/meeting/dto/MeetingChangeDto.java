package com.cvesters.notula.meeting.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import com.fasterxml.jackson.annotation.JsonUnwrapped;

import com.cvesters.notula.meeting.bdo.MeetingAction;

public sealed interface MeetingChangeDto extends ChangeDto {

	MeetingAction.Update toBdo();

	record Rename(@PositiveOrZero long base,
			@NotNull @Valid @JsonUnwrapped TextEditDto edit)
			implements MeetingChangeDto {
		public MeetingAction.UpdateName toBdo() {
			return new MeetingAction.UpdateName(edit.toBdo());
		}
	}

	record Describe(@PositiveOrZero long base,
			@NotNull @Valid @JsonUnwrapped TextEditDto edit)
			implements MeetingChangeDto {
		public MeetingAction.UpdateDescription toBdo() {
			return new MeetingAction.UpdateDescription(edit.toBdo());
		}
	}
}
