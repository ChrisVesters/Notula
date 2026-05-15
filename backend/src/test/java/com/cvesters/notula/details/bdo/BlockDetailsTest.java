package com.cvesters.notula.details.bdo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.cvesters.notula.block.TestBlock;
import com.cvesters.notula.block.bdo.BlockInfo;

class BlockDetailsTest {

	@Nested
	class Constructor {

		@Test
		void success() {
			final TestBlock block = TestBlock.SPORER_PROJECT_BLOCKERS_FIRST;
			final var blockInfo = block.info();

			final var details = new BlockDetails(blockInfo);

			assertThat(details.getId()).isEqualTo(block.getId());
			assertThat(details.getType()).isEqualTo(block.getType());
			assertThat(details.getSequenceId())
					.isEqualTo(block.getSequenceId());
		}

		@Test
		void infoNull() {
			final BlockInfo blockInfo = null;

			assertThatThrownBy(() -> new BlockDetails(blockInfo))
					.isInstanceOf(NullPointerException.class);
		}
	}
}
