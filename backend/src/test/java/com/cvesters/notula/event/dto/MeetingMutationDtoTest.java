package com.cvesters.notula.event.dto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.cvesters.notula.common.domain.Splice;
import com.cvesters.notula.event.bdo.MeetingMutation;
import com.cvesters.notula.meeting.dto.TextEditDto;

import tools.jackson.databind.ObjectMapper;

class MeetingMutationDtoTest {

	@Nested
	class Of {

		@Test
		void add() {
			final var mutation = new MeetingMutation.Add("Kickoff");

			final var dto = MeetingMutationDto.of(mutation);

			assertThat(dto).isEqualTo(new MeetingMutationDto.Add("Kickoff"));
		}

		@Test
		void rename() {
			final var mutation = new MeetingMutation.Rename(
					new Splice(4, 12, "Updated"));

			final var dto = MeetingMutationDto.of(mutation);

			final var edit = new TextEditDto(4, 12, "Updated");
			assertThat(dto).isEqualTo(new MeetingMutationDto.Rename(edit));
		}

		@Test
		void describe() {
			final var mutation = new MeetingMutation.Describe(
					new Splice(4, 12, "Updated"));

			final var dto = MeetingMutationDto.of(mutation);

			final var edit = new TextEditDto(4, 12, "Updated");
			assertThat(dto).isEqualTo(new MeetingMutationDto.Describe(edit));
		}

		@Test
		void remove() {
			final var mutation = new MeetingMutation.Remove();

			final var dto = MeetingMutationDto.of(mutation);

			assertThat(dto).isEqualTo(new MeetingMutationDto.Remove());
		}

		@Test
		void mutationNull() {
			assertThatThrownBy(() -> MeetingMutationDto.of(null))
					.isInstanceOf(NullPointerException.class);
		}
	}

	@Nested
	class ToBdo {

		@Test
		void add() {
			final var dto = new MeetingMutationDto.Add("Kickoff");

			final var expected = new MeetingMutation.Add("Kickoff");
			assertThat(dto.toBdo()).isEqualTo(expected);
		}

		@Test
		void rename() {
			final var edit = new TextEditDto(4, 12, "Updated");
			final var dto = new MeetingMutationDto.Rename(edit);

			final var expected = new MeetingMutation.Rename(
					new Splice(4, 12, "Updated"));
			assertThat(dto.toBdo()).isEqualTo(expected);
		}

		@Test
		void describe() {
			final var edit = new TextEditDto(4, 12, "Updated");
			final var dto = new MeetingMutationDto.Describe(edit);

			final var expected = new MeetingMutation.Describe(
					new Splice(4, 12, "Updated"));
			assertThat(dto.toBdo()).isEqualTo(expected);
		}

		@Test
		void remove() {
			final var dto = new MeetingMutationDto.Remove();

			assertThat(dto.toBdo()).isEqualTo(new MeetingMutation.Remove());
		}
	}

	@Nested
	class Serialise {

		private static final ObjectMapper MAPPER = new ObjectMapper();

		private static final TextEditDto EDIT = new TextEditDto(4, 12,
				"Updated");

		private static String write(final MutationDto mutation) {
			return MAPPER.writeValueAsString(mutation);
		}

		@Test
		void add() {
			final String json = write(new MeetingMutationDto.Add("Kickoff"));

			assertThat(json).isEqualToIgnoringWhitespace("""
					{
						"type": "ADD_MEETING",
						"name": "Kickoff"
					}
					""");
		}

		@Test
		void rename() {
			final String json = write(new MeetingMutationDto.Rename(EDIT));

			assertThat(json).isEqualToIgnoringWhitespace("""
					{
						"type": "RENAME_MEETING",
						"position": 4,
						"length": 12,
						"value": "Updated"
					}
					""");
		}

		@Test
		void describe() {
			final String json = write(new MeetingMutationDto.Describe(EDIT));

			assertThat(json).isEqualToIgnoringWhitespace("""
					{
						"type": "DESCRIBE_MEETING",
						"position": 4,
						"length": 12,
						"value": "Updated"
					}
					""");
		}

		@Test
		void remove() {
			final String json = write(new MeetingMutationDto.Remove());

			assertThat(json).isEqualToIgnoringWhitespace("""
					{
						"type": "REMOVE_MEETING"
					}
					""");
		}
	}
}
