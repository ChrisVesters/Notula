package com.cvesters.notula.meeting.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record TextEditDto(@PositiveOrZero int position,
		@PositiveOrZero int length, @NotNull String value) {
}
