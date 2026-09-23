package com.cvesters.notula.meeting.dto;

import java.util.Objects;

import com.cvesters.notula.block.bdo.BlockAction;
import com.cvesters.notula.block.bdo.BlockEvent;
import com.cvesters.notula.block.bdo.BlockInfo;
import com.cvesters.notula.block.dto.BlockTypeDto;

public sealed interface BlockMutationDto extends MutationDto {

	static BlockMutationDto of(final BlockEvent event) {
		Objects.requireNonNull(event);

		final BlockInfo block = event.block();

		return switch (event.action()) {
			case BlockAction.Create action -> new Add(block, action);
			case BlockAction.Move action -> new Move(block, action);
			case BlockAction.Delete _ -> new Remove(block);
		};
	}

	record Add(long block, long topic, BlockTypeDto blockType, String rank)
			implements BlockMutationDto {

		private Add(final BlockInfo block, final BlockAction.Create action) {
			Objects.requireNonNull(block);
			Objects.requireNonNull(action);

			final long blockId = block.getId();
			final long topicId = action.getTopicId();
			final var type = new BlockTypeDto(action.getType());
			final String rank = block.getRank().value();

			this(blockId, topicId, type, rank);
		}
	}

	record Move(long block, String rank) implements BlockMutationDto {

		private Move(final BlockInfo block, final BlockAction.Move action) {
			Objects.requireNonNull(block);
			Objects.requireNonNull(action);

			final long blockId = block.getId();
			final String rank = block.getRank().value();

			this(blockId, rank);
		}
	}

	record Remove(long block) implements BlockMutationDto {

		private Remove(final BlockInfo block) {
			Objects.requireNonNull(block);

			final long blockId = block.getId();

			this(blockId);
		}
	}
}
