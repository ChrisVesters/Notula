package com.cvesters.notula.block.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import com.cvesters.notula.block.bdo.BlockAction;
import com.cvesters.notula.block.bdo.BlockType;

public final class BlockActionDto {

	private BlockActionDto() {
	}

	public static final class Create {

		private final long topicId;

		@NotNull
		private final BlockTypeDto type;

		@PositiveOrZero
		private final int sequenceId;

		public Create(final long topicId, final BlockTypeDto type,
				final int sequenceId) {
			this.topicId = topicId;
			this.type = type;
			this.sequenceId = sequenceId;
		}

		public BlockAction.Create toBdo() {
			final BlockType blockType = type.toBdo();
			return new BlockAction.Create(topicId, blockType, sequenceId);
		}
	}

	public static final class Delete {

		private final long blockId;

		public Delete(final long blockId) {
			this.blockId = blockId;
		}

		public long getBlockId() {
			return blockId;
		}
	}
}
