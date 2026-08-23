package com.cvesters.notula.block;

import static org.assertj.core.api.Assertions.assertThat;

import com.cvesters.notula.block.bdo.BlockAction;
import com.cvesters.notula.test.Matcher;

public final class BlockActionMatcher {
	
	private BlockActionMatcher() {
	}

	public static class Create extends Matcher<BlockAction.Create> {

		public Create(final BlockAction.Create expected) {
			super(expected, BlockAction.Create.class);
		}

		@Override
		public void assertEquals(final BlockAction.Create actual) {
			assertThat(actual.getTopicId()).isEqualTo(expected.getTopicId());
			assertThat(actual.getType()).isEqualTo(expected.getType());
			assertThat(actual.getSequenceId()).isEqualTo(expected.getSequenceId());
		}
	}

	public static class Move extends Matcher<BlockAction.Move> {

		public Move(final BlockAction.Move expected) {
			super(expected, BlockAction.Move.class);
		}

		@Override
		public void assertEquals(final BlockAction.Move actual) {
			assertThat(actual.getSequenceId())
					.isEqualTo(expected.getSequenceId());
		}
	}
}
