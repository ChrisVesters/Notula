package com.cvesters.notula.block.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import com.cvesters.notula.block.bdo.BlockAction;
import com.cvesters.notula.block.bdo.BlockType;

public final class BlockActionDto {

	private BlockActionDto() {
	}

	public static final class Create {

		private final long meetingId;
		private final long topicId;

		@NotNull
		private final BlockTypeDto type;

		@PositiveOrZero
		private final int sequenceId;

		public Create(final long meetingId, final long topicId,
				final BlockTypeDto type, final int sequenceId) {
			this.meetingId = meetingId;
			this.topicId = topicId;
			this.type = type;
			this.sequenceId = sequenceId;
		}

		public long getMeetingId() {
			return meetingId;
		}

		public long getTopicId() {
			return topicId;
		}

		public BlockAction.Create toBdo() {
			final BlockType blockType = type.toBdo();
			return new BlockAction.Create(blockType, sequenceId);
		}
	}

	public static final class Delete {

		private final long meetingId;
		private final long topicId;
		private final long blockId;

		public Delete(final long meetingId, final long topicId,
				final long blockId) {
			this.meetingId = meetingId;
			this.topicId = topicId;
			this.blockId = blockId;
		}

		public long getMeetingId() {
			return meetingId;
		}

		public long getTopicId() {
			return topicId;
		}

		public long getBlockId() {
			return blockId;
		}
	}
}
