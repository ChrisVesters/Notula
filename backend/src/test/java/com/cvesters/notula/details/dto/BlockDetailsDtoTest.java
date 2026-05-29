package com.cvesters.notula.details.dto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.cvesters.notula.block.TestBlock;
import com.cvesters.notula.block.bdo.BlockInfo;
import com.cvesters.notula.details.bdo.BlockContent;
import com.cvesters.notula.details.bdo.BlockDetails;

class BlockDetailsDtoTest {

	@Nested
	class Constructor {

		@Test
		void success() {
			final BlockInfo info = TestBlock.SPORER_PROJECT_BLOCKERS_FIRST
					.info();
			final var content = new BlockContent.Text("Text");
			final BlockDetails details = new BlockDetails(info, content);

			final var dto = new BlockDetailsDto(details);

			assertThat(dto.getId()).isEqualTo(details.getId());
			assertThat(dto.getSequenceId()).isEqualTo(details.getSequenceId());
			assertThat(dto.getContent())
					.isInstanceOf(BlockContentDto.Text.class);
		}

		@Test
		void detailsNull() {
			final BlockDetails details = null;

			assertThatThrownBy(() -> new BlockDetailsDto(details))
					.isInstanceOf(NullPointerException.class);
		}
	}
}
