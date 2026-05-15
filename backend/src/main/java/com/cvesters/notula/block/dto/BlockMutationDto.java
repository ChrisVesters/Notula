package com.cvesters.notula.block.dto;

import java.util.Objects;

import com.cvesters.notula.block.bdo.BlockAction;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "action")
@JsonSubTypes({
		@Type(value = BlockMutationDto.Create.class, name = "CREATE") })
public sealed interface BlockMutationDto {

	static BlockMutationDto of(final BlockAction action) {
		Objects.requireNonNull(action);

		return switch (action) {
			case BlockAction.Create create -> new Create(create);
		};
	}

	public record Create(String type, int sequenceId) implements BlockMutationDto {

		public Create {
			Objects.requireNonNull(type);
		}

		public Create(final BlockAction.Create create) {
			Objects.requireNonNull(create);

			final String type = BlockTypeDto.toDto(create.getType());
			this(type, create.getSequenceId());
		}
	}
}
