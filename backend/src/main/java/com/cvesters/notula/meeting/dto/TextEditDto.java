package com.cvesters.notula.meeting.dto;

import java.util.Objects;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import com.cvesters.notula.common.domain.Splice;

public record TextEditDto(@PositiveOrZero int position,
		@PositiveOrZero int length, @NotNull String value) {

	public TextEditDto(final Splice edit) {
		Objects.requireNonNull(edit);

		this(edit.position(), edit.length(), edit.value());
	}

	public Splice toBdo() {
		return new Splice(position, length, value);
	}
}
