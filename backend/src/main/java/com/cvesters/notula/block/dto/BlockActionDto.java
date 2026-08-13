package com.cvesters.notula.block.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import com.cvesters.notula.block.bdo.BlockAction;
import com.cvesters.notula.block.bdo.BlockType;

public final class BlockActionDto {

	private BlockActionDto() {
	}

	public record Create(@NotNull BlockTypeDto type, @PositiveOrZero int sequenceId) {

		public BlockAction.Create toBdo() {
			final BlockType blockType = type.toBdo();
			return new BlockAction.Create(blockType, sequenceId);
		}
	}
}
