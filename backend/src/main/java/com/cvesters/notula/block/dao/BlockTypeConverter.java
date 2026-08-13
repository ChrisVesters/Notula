package com.cvesters.notula.block.dao;

import java.util.Objects;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import com.cvesters.notula.block.bdo.BlockType;

@Converter(autoApply = true)
public class BlockTypeConverter
		implements AttributeConverter<BlockType, Short> {

	@Override
	public Short convertToDatabaseColumn(final BlockType type) {
		Objects.requireNonNull(type);

		return switch (type) {
			case TEXT -> 0;
		};
	}

	@Override
	public BlockType convertToEntityAttribute(final Short value) {
		Objects.requireNonNull(value);

		return switch (value) {
			case 0 -> BlockType.TEXT;
			default -> throw new IllegalArgumentException();
		};
	}

}
