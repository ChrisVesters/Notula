package com.cvesters.notula.common.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record TextUpdateDto(@PositiveOrZero int position,
		@PositiveOrZero int length, @NotNull String value) {

}
