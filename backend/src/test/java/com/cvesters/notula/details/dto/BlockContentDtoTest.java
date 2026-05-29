package com.cvesters.notula.details.dto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.cvesters.notula.block.bdo.BlockType;
import com.cvesters.notula.block.dto.BlockTypeDto;
import com.cvesters.notula.details.bdo.BlockContent;
import com.cvesters.notula.details.dto.BlockContentDto.Text;

class BlockContentDtoTest {

	@Nested
	class Of {

		@Test
		void text() {
			final var content = new BlockContent.Text("content");
			final var dto = BlockContentDto.of(content);

			assertThat(dto.getType())
					.isEqualTo(BlockTypeDto.toDto(BlockType.TEXT));
			assertThat(dto).isInstanceOf(Text.class);

			final var textDto = (Text) dto;
			assertThat(textDto.getContent()).isEqualTo("content");
		}

		@Test
		void contentNull() {
			assertThatThrownBy(() -> BlockContentDto.of(null))
					.isInstanceOf(NullPointerException.class);
		}
	}

}
