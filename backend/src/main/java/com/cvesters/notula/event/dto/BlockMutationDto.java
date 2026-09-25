package com.cvesters.notula.event.dto;

import java.util.Objects;

import com.cvesters.notula.block.dto.BlockTypeDto;
import com.cvesters.notula.common.domain.Rank;
import com.cvesters.notula.event.bdo.BlockMutation;

public sealed interface BlockMutationDto extends MutationDto {

	static BlockMutationDto of(final BlockMutation mutation) {
		Objects.requireNonNull(mutation);

		return switch (mutation) {
			case BlockMutation.Add m -> new Add(m);
			case BlockMutation.Move m -> new Move(m);
			case BlockMutation.Remove m -> new Remove(m);
		};
	}

	@Override
	BlockMutation toBdo();

	record Add(long block, long topic, BlockTypeDto blockType, String rank)
			implements BlockMutationDto {

		private Add(final BlockMutation.Add mutation) {
			final long blockId = mutation.blockId();
			final long topicId = mutation.topicId();
			final var type = new BlockTypeDto(mutation.type());
			final String rank = mutation.rank().value();

			this(blockId, topicId, type, rank);
		}

		@Override
		public BlockMutation toBdo() {
			return new BlockMutation.Add(block, topic, blockType.toBdo(),
					new Rank(rank));
		}
	}

	record Move(long block, String rank) implements BlockMutationDto {

		private Move(final BlockMutation.Move mutation) {
			final long blockId = mutation.blockId();
			final String rank = mutation.rank().value();

			this(blockId, rank);
		}

		@Override
		public BlockMutation toBdo() {
			return new BlockMutation.Move(block, new Rank(rank));
		}
	}

	record Remove(long block) implements BlockMutationDto {

		private Remove(final BlockMutation.Remove mutation) {
			final long blockId = mutation.blockId();

			this(blockId);
		}

		@Override
		public BlockMutation toBdo() {
			return new BlockMutation.Remove(block);
		}
	}
}
