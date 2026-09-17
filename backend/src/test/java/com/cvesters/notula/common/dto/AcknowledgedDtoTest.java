package com.cvesters.notula.common.dto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.UUID;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.cvesters.notula.common.domain.ChangeId;

import tools.jackson.databind.ObjectMapper;

class AcknowledgedDtoTest {

	private static final UUID ID = UUID
			.fromString("7c6f0d54-2f70-4a1e-9f5a-1d4c8b2e0a11");

	private static final ChangeId CHANGE = new ChangeId(ID);

	private static final long REVISION = 12L;

	@Nested
	class Constructor {

		@Test
		void success() {
			final var acknowledged = new AcknowledgedDto(ID, REVISION);

			assertThat(acknowledged.id()).isEqualTo(ID);
			assertThat(acknowledged.revision()).isEqualTo(REVISION);
		}

		@Test
		void withoutId() {
			final var acknowledged = new AcknowledgedDto(null, REVISION);

			assertThat(acknowledged.id()).isNull();
			assertThat(acknowledged.revision()).isEqualTo(REVISION);
		}
	}

	@Nested
	class Of {

		@Test
		void success() {
			final var acknowledged = AcknowledgedDto.of(CHANGE, REVISION);

			assertThat(acknowledged.id()).isEqualTo(ID);
			assertThat(acknowledged.revision()).isEqualTo(REVISION);
		}

		@Test
		void unnamed() {
			final var acknowledged = AcknowledgedDto.of(ChangeId.NONE,
					REVISION);

			assertThat(acknowledged.id()).isNull();
			assertThat(acknowledged.revision()).isEqualTo(REVISION);
		}

		@Test
		void changeIdNull() {
			assertThatThrownBy(() -> AcknowledgedDto.of(null, REVISION))
					.isInstanceOf(NullPointerException.class);
		}
	}

	@Nested
	class Serialise {

		private static final ObjectMapper MAPPER = new ObjectMapper();

		@Test
		void success() {
			final var acknowledged = new AcknowledgedDto(ID, REVISION);

			final String json = MAPPER.writeValueAsString(acknowledged);

			assertThat(json).isEqualToIgnoringWhitespace("""
					{
						"id": "7c6f0d54-2f70-4a1e-9f5a-1d4c8b2e0a11",
						"revision": 12
					}
					""");
		}

		@Test
		void withoutId() {
			final var acknowledged = new AcknowledgedDto(null, REVISION);

			final String json = MAPPER.writeValueAsString(acknowledged);

			assertThat(json).isEqualToIgnoringWhitespace("""
					{
						"id": null,
						"revision": 12
					}
					""");
		}
	}
}
