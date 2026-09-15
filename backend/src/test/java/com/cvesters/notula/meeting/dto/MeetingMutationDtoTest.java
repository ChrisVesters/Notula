package com.cvesters.notula.meeting.dto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.UUID;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.cvesters.notula.common.domain.Origin;
import com.cvesters.notula.meeting.bdo.MeetingAction;
import com.cvesters.notula.meeting.bdo.MeetingEvent;
import com.cvesters.notula.session.TestSession;

import tools.jackson.databind.ObjectMapper;

class MeetingMutationDtoTest {

	private static final TestSession SESSION = TestSession.EDUARDO_CHRISTIANSEN_SPORER;
	private static final UUID CLIENT_ID = UUID
			.fromString("3f9c1a44-1d2e-4a51-8b0c-2c7e9b1d4a06");
	private static final Origin ORIGIN = new Origin(SESSION.principal(),
			CLIENT_ID);

	@Nested
	class Of {

		private static MutationDto of(final MeetingAction action) {
			return MeetingMutationDto
					.of(new MeetingEvent(action, ORIGIN));
		}

		@Test
		void create() {
			final var dto = of(new MeetingAction.Create("Kickoff"));

			final var expected = new MeetingMutationDto.Add("Kickoff");
			assertThat(dto).isEqualTo(expected);
		}

		@Test
		void updateName() {
			final var dto = of(new MeetingAction.UpdateName(4, 12, "Updated"));

			final var edit = new TextEditDto(4, 12, "Updated");
			assertThat(dto).isEqualTo(new MeetingMutationDto.Rename(edit));
		}

		@Test
		void updateDescription() {
			final var dto = of(
					new MeetingAction.UpdateDescription(4, 12, "Updated"));

			final var edit = new TextEditDto(4, 12, "Updated");
			assertThat(dto).isEqualTo(new MeetingMutationDto.Describe(edit));
		}

		@Test
		void delete() {
			final var dto = of(new MeetingAction.Delete());

			assertThat(dto).isEqualTo(new MeetingMutationDto.Remove());
		}

		@Test
		void eventNull() {
			assertThatThrownBy(() -> MeetingMutationDto.of(null))
					.isInstanceOf(NullPointerException.class);
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
