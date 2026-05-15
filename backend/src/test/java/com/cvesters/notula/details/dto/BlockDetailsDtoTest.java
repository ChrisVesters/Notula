package com.cvesters.notula.details.dto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.cvesters.notula.block.TestBlock;
import com.cvesters.notula.block.bdo.BlockInfo;
import com.cvesters.notula.block.dto.BlockTypeDto;
import com.cvesters.notula.details.bdo.BlockDetails;

class BlockDetailsDtoTest {

	@Nested
	class Constructor {

		@Test
		void success() {
			final BlockInfo info = TestBlock.SPORER_PROJECT_BLOCKERS_FIRST
					.info();
			final BlockDetails details = new BlockDetails(info);

			final var dto = new BlockDetailsDto(details);

			assertThat(dto.getId()).isEqualTo(details.getId());
			assertThat(dto.getType())
					.isEqualTo(BlockTypeDto.toDto(details.getType()));
			assertThat(dto.getSequenceId()).isEqualTo(details.getSequenceId());
		}

		@Test
		void detailsNull() {
			final BlockDetails details = null;

			assertThatThrownBy(() -> new BlockDetailsDto(details))
					.isInstanceOf(NullPointerException.class);
		}
	}
}
