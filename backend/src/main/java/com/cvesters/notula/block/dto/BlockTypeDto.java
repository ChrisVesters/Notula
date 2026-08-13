package com.cvesters.notula.block.dto;

import java.util.Objects;

import com.cvesters.notula.block.bdo.BlockType;
import com.cvesters.notula.common.exception.InvalidActionException;
import com.fasterxml.jackson.annotation.JsonValue;

public record BlockTypeDto(@JsonValue String type) {

	public BlockTypeDto {
		Objects.requireNonNull(type);
	}

	public BlockTypeDto(final BlockType type) {
		Objects.requireNonNull(type);

		final String value = switch (type) {
			case TEXT -> "TEXT";
		};

		this(value);
	}

	public BlockType toBdo() {
		return switch (type) {
			case "TEXT" -> BlockType.TEXT;
			// TODO: proper validation exception
			default -> throw new InvalidActionException();
		};
	}
}
