package com.cvesters.notula.meeting.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import com.fasterxml.jackson.annotation.JsonUnwrapped;

import com.cvesters.notula.common.domain.Splice;
import com.cvesters.notula.meeting.bdo.MeetingAction;

public sealed interface MeetingChangeDto extends ChangeDto {

	record Rename(@PositiveOrZero long base,
			@NotNull @Valid @JsonUnwrapped TextEditDto edit)
			implements MeetingChangeDto, TextChangeDto {
		public MeetingAction.UpdateName toBdo(final Splice rebased) {
			return new MeetingAction.UpdateName(rebased);
		}
	}

	record Describe(@PositiveOrZero long base,
			@NotNull @Valid @JsonUnwrapped TextEditDto edit)
			implements MeetingChangeDto, TextChangeDto {
		public MeetingAction.UpdateDescription toBdo(final Splice rebased) {
			return new MeetingAction.UpdateDescription(rebased);
		}
	}
}
