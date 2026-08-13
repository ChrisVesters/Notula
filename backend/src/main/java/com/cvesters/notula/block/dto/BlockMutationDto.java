package com.cvesters.notula.block.dto;

import java.util.Objects;

import lombok.Getter;

import com.cvesters.notula.block.bdo.BlockAction;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "action")
@JsonSubTypes({ @Type(value = BlockMutationDto.Create.class, name = "CREATE"),
		@Type(value = BlockMutationDto.Delete.class, name = "DELETE") })
public sealed interface BlockMutationDto {

	static BlockMutationDto of(final BlockAction action) {
		Objects.requireNonNull(action);

		return switch (action) {
			case BlockAction.Create create -> new Create(create);
			case BlockAction.Delete _ -> new Delete();
		};
	}

	@Getter
	final class Create implements BlockMutationDto {

		private final BlockTypeDto type;
		private final int sequenceId;

		private Create(final BlockAction.Create create) {
			this.type = new BlockTypeDto(create.getType());
			this.sequenceId = create.getSequenceId();
		}
	}

	@Getter
	final class Delete implements BlockMutationDto {

		private Delete() {
		}
	}
}
