package com.cvesters.notula.meeting.dto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.cvesters.notula.common.domain.Splice;

class TextEditDtoTest {

	@Nested
	class Constructor {

		@Test
		void success() {
			final var edit = new Splice(4, 12, "Updated");

			final var dto = new TextEditDto(edit);

			assertThat(dto.position()).isEqualTo(4);
			assertThat(dto.length()).isEqualTo(12);
			assertThat(dto.value()).isEqualTo("Updated");
		}

		@Test
		void editNull() {
			assertThatThrownBy(() -> new TextEditDto(null))
					.isInstanceOf(NullPointerException.class);
		}
	}

	@Nested
	class ToBdo {

		@Test
		void success() {
			final var dto = new TextEditDto(4, 12, "Updated");

			final Splice edit = dto.toBdo();

			assertThat(edit).isEqualTo(new Splice(4, 12, "Updated"));
		}
	}
}
