package com.cvesters.notula.event.dto;

import java.util.Objects;

import com.cvesters.notula.event.bdo.MeetingMutation;
import com.cvesters.notula.meeting.dto.TextEditDto;
import com.fasterxml.jackson.annotation.JsonUnwrapped;

public sealed interface MeetingMutationDto extends MutationDto {

	static MeetingMutationDto of(final MeetingMutation mutation) {
		Objects.requireNonNull(mutation);

		return switch (mutation) {
			case MeetingMutation.Add m -> new Add(m);
			case MeetingMutation.Rename m -> new Rename(m);
			case MeetingMutation.Describe m -> new Describe(m);
			case MeetingMutation.Remove _ -> new Remove();
		};
	}

	@Override
	MeetingMutation toBdo();

	record Add(String name) implements MeetingMutationDto {

		private Add(final MeetingMutation.Add mutation) {
			final String name = mutation.name();

			this(name);
		}

		@Override
		public MeetingMutation toBdo() {
			return new MeetingMutation.Add(name);
		}
	}

	record Rename(@JsonUnwrapped TextEditDto edit)
			implements MeetingMutationDto {

		private Rename(final MeetingMutation.Rename mutation) {
			final var edit = new TextEditDto(mutation.position(),
					mutation.length(), mutation.value());

			this(edit);
		}

		@Override
		public MeetingMutation toBdo() {
			return new MeetingMutation.Rename(edit.position(), edit.length(),
					edit.value());
		}
	}

	record Describe(@JsonUnwrapped TextEditDto edit)
			implements MeetingMutationDto {

		private Describe(final MeetingMutation.Describe mutation) {
			final var edit = new TextEditDto(mutation.position(),
					mutation.length(), mutation.value());

			this(edit);
		}

		@Override
		public MeetingMutation toBdo() {
			return new MeetingMutation.Describe(edit.position(),
					edit.length(), edit.value());
		}
	}

	record Remove() implements MeetingMutationDto {

		@Override
		public MeetingMutation toBdo() {
			return new MeetingMutation.Remove();
		}
	}
}
