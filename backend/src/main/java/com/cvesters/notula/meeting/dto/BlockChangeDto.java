package com.cvesters.notula.meeting.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import com.cvesters.notula.block.bdo.BlockAction;
import com.cvesters.notula.block.bdo.BlockType;

public sealed interface BlockChangeDto extends ChangeDto {

	record Add(long topic, @NotNull BlockType blockType,
			@Positive Long afterId) implements BlockChangeDto {
		public BlockAction.Create toBdo() {
			return new BlockAction.Create(topic, blockType, afterId);
		}
	}

	record Move(long block, @Positive Long afterId)
			implements BlockChangeDto {
		public BlockAction.Move toBdo() {
			return new BlockAction.Move(afterId);
		}
	}

	record Remove(long block) implements BlockChangeDto {
	}
}
