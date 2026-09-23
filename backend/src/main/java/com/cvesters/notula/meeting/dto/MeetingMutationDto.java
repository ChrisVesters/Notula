package com.cvesters.notula.meeting.dto;

import java.util.Objects;

import com.cvesters.notula.meeting.bdo.MeetingAction;
import com.cvesters.notula.meeting.bdo.MeetingEvent;
import com.fasterxml.jackson.annotation.JsonUnwrapped;

public sealed interface MeetingMutationDto extends MutationDto {

	static MeetingMutationDto of(final MeetingEvent event) {
		Objects.requireNonNull(event);

		return switch (event.action()) {
			case MeetingAction.Create action -> new Add(action);
			case MeetingAction.UpdateName action -> new Rename(action);
			case MeetingAction.UpdateDescription action -> new Describe(action);
			case MeetingAction.Delete _ -> new Remove();
		};
	}

	record Add(String name) implements MeetingMutationDto {

		private Add(final MeetingAction.Create action) {
			Objects.requireNonNull(action);

			final String name = action.getName();

			this(name);
		}
	}

	record Rename(@JsonUnwrapped TextEditDto edit)
			implements MeetingMutationDto {

		private Rename(final MeetingAction.UpdateName action) {
			Objects.requireNonNull(action);

			final var edit = new TextEditDto(action.getPosition(),
					action.getLength(), action.getValue());

			this(edit);
		}
	}

	record Describe(@JsonUnwrapped TextEditDto edit)
			implements MeetingMutationDto {

		private Describe(final MeetingAction.UpdateDescription action) {
			Objects.requireNonNull(action);

			final var edit = new TextEditDto(action.getPosition(),
					action.getLength(), action.getValue());

			this(edit);
		}
	}

	record Remove() implements MeetingMutationDto {
	}
}
