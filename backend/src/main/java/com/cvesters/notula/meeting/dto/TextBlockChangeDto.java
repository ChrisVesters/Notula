package com.cvesters.notula.meeting.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import com.fasterxml.jackson.annotation.JsonUnwrapped;

import com.cvesters.notula.common.domain.Splice;
import com.cvesters.notula.textblock.bdo.TextBlockAction;

public sealed interface TextBlockChangeDto extends ChangeDto {

	record Edit(long block, @PositiveOrZero long base,
			@NotNull @Valid @JsonUnwrapped TextEditDto edit)
			implements TextBlockChangeDto, TextChangeDto {
		public TextBlockAction.UpdateContent toBdo(final Splice rebased) {
			return new TextBlockAction.UpdateContent(rebased);
		}
	}
}
