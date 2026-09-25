package com.cvesters.notula.event.dto;

import java.util.Objects;

import com.cvesters.notula.event.bdo.TextBlockMutation;
import com.cvesters.notula.meeting.dto.TextEditDto;
import com.fasterxml.jackson.annotation.JsonUnwrapped;

public sealed interface TextBlockMutationDto extends MutationDto {

	static TextBlockMutationDto of(final TextBlockMutation mutation) {
		Objects.requireNonNull(mutation);

		return switch (mutation) {
			case TextBlockMutation.Edit m -> new Edit(m);
		};
	}

	@Override
	TextBlockMutation toBdo();

	record Edit(long block, @JsonUnwrapped TextEditDto edit)
			implements TextBlockMutationDto {

		private Edit(final TextBlockMutation.Edit mutation) {
			final long blockId = mutation.blockId();
			final var edit = new TextEditDto(mutation.position(),
					mutation.length(), mutation.value());

			this(blockId, edit);
		}

		@Override
		public TextBlockMutation toBdo() {
			return new TextBlockMutation.Edit(block, edit.position(),
					edit.length(), edit.value());
		}
	}
}
