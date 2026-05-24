package com.cvesters.notula.block.dto;

import java.util.Objects;

import lombok.Getter;

import com.cvesters.notula.block.bdo.BlockAction;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "action")
@JsonSubTypes({ @Type(value = BlockMutationDto.Create.class, name = "CREATE") })
public sealed interface BlockMutationDto {

	static BlockMutationDto of(final BlockAction action) {
		Objects.requireNonNull(action);

		return switch (action) {
			case BlockAction.Create create -> new Create(create);
		};
	}

	@Getter
	final class Create implements BlockMutationDto {

		private final String type;
		private final int sequenceId;

		private Create(final BlockAction.Create create) {
			this.type = BlockTypeDto.toDto(create.getType());
			this.sequenceId = create.getSequenceId();
		}
	}
}
