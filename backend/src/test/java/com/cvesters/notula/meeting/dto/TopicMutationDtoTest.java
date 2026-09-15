package com.cvesters.notula.meeting.dto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.UUID;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.cvesters.notula.common.domain.Minutes;
import com.cvesters.notula.common.domain.Origin;
import com.cvesters.notula.session.TestSession;
import com.cvesters.notula.topic.bdo.TopicAction;
import com.cvesters.notula.topic.bdo.TopicEvent;
import com.cvesters.notula.topic.bdo.TopicInfo;

import tools.jackson.databind.ObjectMapper;

class TopicMutationDtoTest {

	private static final TestSession SESSION = TestSession.EDUARDO_CHRISTIANSEN_SPORER;
	private static final UUID CLIENT_ID = UUID
			.fromString("3f9c1a44-1d2e-4a51-8b0c-2c7e9b1d4a06");
	private static final Origin ORIGIN = new Origin(SESSION.principal(),
			CLIENT_ID);

	private static final long TOPIC_ID = 32L;

	@Nested
	class Of {

		private static MutationDto of(final TopicAction action) {
			final TopicInfo topic = mock();
			when(topic.getId()).thenReturn(TOPIC_ID);

			return TopicMutationDto.of(new TopicEvent(topic, action, ORIGIN));
		}

		@Test
		void create() {
			final var dto = of(new TopicAction.Create(2, "Blockers"));

			final var expected = new TopicMutationDto.Add(TOPIC_ID, 2,
					"Blockers");
			assertThat(dto).isEqualTo(expected);
		}

		@Test
		void move() {
			final var dto = of(new TopicAction.Move(2));

			assertThat(dto).isEqualTo(new TopicMutationDto.Move(TOPIC_ID, 2));
		}

		@Test
		void updateName() {
			final var dto = of(new TopicAction.UpdateName(4, 12, "Updated"));

			final var edit = new TextEditDto(4, 12, "Updated");
			final var expected = new TopicMutationDto.Rename(TOPIC_ID, edit);
			assertThat(dto).isEqualTo(expected);
		}

		@Test
		void updateDescription() {
			final var dto = of(
					new TopicAction.UpdateDescription(4, 12, "Updated"));

			final var edit = new TextEditDto(4, 12, "Updated");
			final var expected = new TopicMutationDto.Describe(TOPIC_ID, edit);
			assertThat(dto).isEqualTo(expected);
		}

		@Test
		void updateDuration() {
			final var dto = of(
					new TopicAction.UpdateDuration(new Minutes(45)));

			final var expected = new TopicMutationDto.Schedule(TOPIC_ID, 45);
			assertThat(dto).isEqualTo(expected);
		}

		@Test
		void updateDurationNull() {
			final var dto = of(new TopicAction.UpdateDuration(null));

			final var expected = new TopicMutationDto.Schedule(TOPIC_ID, null);
			assertThat(dto).isEqualTo(expected);
		}

		@Test
		void delete() {
			final var dto = of(new TopicAction.Delete());

			assertThat(dto).isEqualTo(new TopicMutationDto.Remove(TOPIC_ID));
		}

		@Test
		void eventNull() {
			assertThatThrownBy(() -> TopicMutationDto.of(null))
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
			final String json = write(
					new TopicMutationDto.Add(TOPIC_ID, 2, "Blockers"));

			assertThat(json).isEqualToIgnoringWhitespace("""
					{
						"type": "ADD_TOPIC",
						"topic": 32,
						"sequenceId": 2,
						"name": "Blockers"
					}
					""");
		}

		@Test
		void move() {
			final String json = write(new TopicMutationDto.Move(TOPIC_ID, 2));

			assertThat(json).isEqualToIgnoringWhitespace("""
					{
						"type": "MOVE_TOPIC",
						"topic": 32,
						"sequenceId": 2
					}
					""");
		}

		@Test
		void rename() {
			final String json = write(
					new TopicMutationDto.Rename(TOPIC_ID, EDIT));

			assertThat(json).isEqualToIgnoringWhitespace("""
					{
						"type": "RENAME_TOPIC",
						"topic": 32,
						"position": 4,
						"length": 12,
						"value": "Updated"
					}
					""");
		}

		@Test
		void describe() {
			final String json = write(
					new TopicMutationDto.Describe(TOPIC_ID, EDIT));

			assertThat(json).isEqualToIgnoringWhitespace("""
					{
						"type": "DESCRIBE_TOPIC",
						"topic": 32,
						"position": 4,
						"length": 12,
						"value": "Updated"
					}
					""");
		}

		@Test
		void schedule() {
			final String json = write(
					new TopicMutationDto.Schedule(TOPIC_ID, 45));

			assertThat(json).isEqualToIgnoringWhitespace("""
					{
						"type": "SCHEDULE_TOPIC",
						"topic": 32,
						"minutes": 45
					}
					""");
		}

		@Test
		void scheduleNoMinutes() {
			final String json = write(
					new TopicMutationDto.Schedule(TOPIC_ID, null));

			assertThat(json).isEqualToIgnoringWhitespace("""
					{
						"type": "SCHEDULE_TOPIC",
						"topic": 32,
						"minutes": null
					}
					""");
		}

		@Test
		void remove() {
			final String json = write(new TopicMutationDto.Remove(TOPIC_ID));

			assertThat(json).isEqualToIgnoringWhitespace("""
					{
						"type": "REMOVE_TOPIC",
						"topic": 32
					}
					""");
		}
	}
}
