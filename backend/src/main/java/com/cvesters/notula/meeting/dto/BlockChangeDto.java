package com.cvesters.notula.meeting.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import com.cvesters.notula.block.bdo.BlockAction;
import com.cvesters.notula.block.bdo.BlockType;

public sealed interface BlockChangeDto extends ChangeDto {

	record Add(long topic, @NotNull BlockType blockType,
			@PositiveOrZero int sequenceId) implements BlockChangeDto {
		public BlockAction.Create toBdo() {
			return new BlockAction.Create(topic, blockType, sequenceId);
		}
	}

	record Move(long block, @PositiveOrZero int sequenceId)
			implements BlockChangeDto {
		public BlockAction.Move toBdo() {
			return new BlockAction.Move(sequenceId);
		}
	}

	record Remove(long block) implements BlockChangeDto {
	}
}
