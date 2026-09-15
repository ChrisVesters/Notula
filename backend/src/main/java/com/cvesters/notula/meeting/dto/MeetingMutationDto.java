package com.cvesters.notula.meeting.dto;

import java.util.Objects;

import com.cvesters.notula.meeting.bdo.MeetingAction;
import com.cvesters.notula.meeting.bdo.MeetingEvent;
import com.fasterxml.jackson.annotation.JsonUnwrapped;

public sealed interface MeetingMutationDto extends MutationDto {

	static MeetingMutationDto of(final MeetingEvent event) {
		Objects.requireNonNull(event);

		return switch (event.action()) {
			case MeetingAction.Create action -> new Add(action.getName());
			case MeetingAction.UpdateName action -> new Rename(
					new TextEditDto(action.getPosition(), action.getLength(),
							action.getValue()));
			case MeetingAction.UpdateDescription action -> new Describe(
					new TextEditDto(action.getPosition(), action.getLength(),
							action.getValue()));
			case MeetingAction.Delete _ -> new Remove();
		};
	}

	record Add(String name) implements MeetingMutationDto {
	}

	record Rename(@JsonUnwrapped TextEditDto edit)
			implements MeetingMutationDto {
	}

	record Describe(@JsonUnwrapped TextEditDto edit)
			implements MeetingMutationDto {
	}

	record Remove() implements MeetingMutationDto {
	}
}
