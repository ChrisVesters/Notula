package com.cvesters.notula.meeting.dto;

import java.util.Objects;

import com.cvesters.notula.block.bdo.BlockInfo;
import com.cvesters.notula.textblock.bdo.TextBlockAction;
import com.cvesters.notula.textblock.bdo.TextBlockEvent;
import com.fasterxml.jackson.annotation.JsonUnwrapped;

public sealed interface TextBlockMutationDto extends MutationDto {

	static TextBlockMutationDto of(final TextBlockEvent event) {
		Objects.requireNonNull(event);

		final BlockInfo block = event.block();

		return switch (event.action()) {
			case TextBlockAction.UpdateContent action -> new Edit(block,
					action);
		};
	}

	record Edit(long block, @JsonUnwrapped TextEditDto edit)
			implements TextBlockMutationDto {

		private Edit(final BlockInfo block,
				final TextBlockAction.UpdateContent action) {
			Objects.requireNonNull(block);
			Objects.requireNonNull(action);

			final long blockId = block.getId();
			final var edit = new TextEditDto(action.getPosition(),
					action.getLength(), action.getValue());

			this(blockId, edit);
		}
	}
}
