package com.cvesters.notula.block.dao;

import java.util.Objects;

import com.cvesters.notula.block.bdo.BlockType;

public final class BlockTypeDao {

	private BlockTypeDao() {}

	public static BlockType toBdo(final int type) {
		return switch (type) {
			case 0 -> BlockType.TEXT;
			default -> throw new IllegalArgumentException();
		};
	}

	public static int toDao(final BlockType type) {
		Objects.requireNonNull(type);

		return switch (type) {
			case TEXT -> 0;
		};
	}
}
