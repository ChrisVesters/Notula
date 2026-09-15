package com.cvesters.notula.meeting.dto;

import java.util.Objects;

import com.cvesters.notula.textblock.bdo.TextBlockAction;
import com.cvesters.notula.textblock.bdo.TextBlockEvent;
import com.fasterxml.jackson.annotation.JsonUnwrapped;

public sealed interface TextBlockMutationDto extends MutationDto {

	static TextBlockMutationDto of(final TextBlockEvent event) {
		Objects.requireNonNull(event);

		final long block = event.block().getId();

		return switch (event.action()) {
			case TextBlockAction.UpdateContent action -> new Edit(block,
					new TextEditDto(action.getPosition(), action.getLength(),
							action.getValue()));
		};
	}

	record Edit(long block, @JsonUnwrapped TextEditDto edit)
			implements TextBlockMutationDto {
	}
}
