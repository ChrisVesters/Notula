package com.cvesters.notula.block.dto;

import java.util.Objects;

import com.cvesters.notula.block.bdo.BlockType;

public final class BlockTypeDto {

	private BlockTypeDto() {
	}

	public static BlockType toBdo(final String type) {
		Objects.requireNonNull(type);

		return switch (type) {
			case "TEXT" -> BlockType.TEXT;
			default -> throw new IllegalArgumentException();
		};
	}

	public static String toDto(final BlockType type) {
		Objects.requireNonNull(type);

		return switch (type) {
			case TEXT -> "TEXT";
		};
	}

}
