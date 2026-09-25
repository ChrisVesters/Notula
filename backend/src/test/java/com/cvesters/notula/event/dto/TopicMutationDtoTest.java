package com.cvesters.notula.event.dto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.cvesters.notula.common.domain.Minutes;
import com.cvesters.notula.common.domain.Rank;
import com.cvesters.notula.common.domain.Splice;
import com.cvesters.notula.event.bdo.TopicMutation;
import com.cvesters.notula.meeting.dto.TextEditDto;

import tools.jackson.databind.ObjectMapper;

class TopicMutationDtoTest {

	private static final long TOPIC_ID = 32L;

	private static final String RANK = "a1";

	@Nested
	class Of {

		@Test
		void add() {
			final var mutation = new TopicMutation.Add(TOPIC_ID,
					new Rank(RANK), "Blockers");

			final var dto = TopicMutationDto.of(mutation);

			final var expected = new TopicMutationDto.Add(TOPIC_ID, RANK,
					"Blockers");
			assertThat(dto).isEqualTo(expected);
		}

		@Test
		void move() {
			final var mutation = new TopicMutation.Move(TOPIC_ID,
					new Rank(RANK));

			final var dto = TopicMutationDto.of(mutation);

			assertThat(dto)
					.isEqualTo(new TopicMutationDto.Move(TOPIC_ID, RANK));
		}

		@Test
		void rename() {
			final var mutation = new TopicMutation.Rename(TOPIC_ID,
					new Splice(4, 12, "Updated"));

			final var dto = TopicMutationDto.of(mutation);

			final var edit = new TextEditDto(4, 12, "Updated");
			final var expected = new TopicMutationDto.Rename(TOPIC_ID, edit);
			assertThat(dto).isEqualTo(expected);
		}

		@Test
		void describe() {
			final var mutation = new TopicMutation.Describe(TOPIC_ID,
					new Splice(4, 12, "Updated"));

			final var dto = TopicMutationDto.of(mutation);

			final var edit = new TextEditDto(4, 12, "Updated");
			final var expected = new TopicMutationDto.Describe(TOPIC_ID, edit);
			assertThat(dto).isEqualTo(expected);
		}

		@Test
		void schedule() {
			final var mutation = new TopicMutation.Schedule(TOPIC_ID,
					new Minutes(45));

			final var dto = TopicMutationDto.of(mutation);

			final var expected = new TopicMutationDto.Schedule(TOPIC_ID, 45);
			assertThat(dto).isEqualTo(expected);
		}

		@Test
		void unscheduled() {
			final var mutation = new TopicMutation.Schedule(TOPIC_ID, null);

			final var dto = TopicMutationDto.of(mutation);

			final var expected = new TopicMutationDto.Schedule(TOPIC_ID, null);
			assertThat(dto).isEqualTo(expected);
		}

		@Test
		void remove() {
			final var mutation = new TopicMutation.Remove(TOPIC_ID);

			final var dto = TopicMutationDto.of(mutation);

			assertThat(dto).isEqualTo(new TopicMutationDto.Remove(TOPIC_ID));
		}

		@Test
		void mutationNull() {
			assertThatThrownBy(() -> TopicMutationDto.of(null))
					.isInstanceOf(NullPointerException.class);
		}
	}

	@Nested
	class ToBdo {

		@Test
		void add() {
			final var dto = new TopicMutationDto.Add(TOPIC_ID, RANK,
					"Blockers");

			final var expected = new TopicMutation.Add(TOPIC_ID,
					new Rank(RANK), "Blockers");
			assertThat(dto.toBdo()).isEqualTo(expected);
		}

		@Test
		void move() {
			final var dto = new TopicMutationDto.Move(TOPIC_ID, RANK);

			final var expected = new TopicMutation.Move(TOPIC_ID,
					new Rank(RANK));
			assertThat(dto.toBdo()).isEqualTo(expected);
		}

		@Test
		void rename() {
			final var edit = new TextEditDto(4, 12, "Updated");
			final var dto = new TopicMutationDto.Rename(TOPIC_ID, edit);

			final var expected = new TopicMutation.Rename(TOPIC_ID,
					new Splice(4, 12, "Updated"));
			assertThat(dto.toBdo()).isEqualTo(expected);
		}

		@Test
		void describe() {
			final var edit = new TextEditDto(4, 12, "Updated");
			final var dto = new TopicMutationDto.Describe(TOPIC_ID, edit);

			final var expected = new TopicMutation.Describe(TOPIC_ID,
					new Splice(4, 12, "Updated"));
			assertThat(dto.toBdo()).isEqualTo(expected);
		}

		@Test
		void schedule() {
			final var dto = new TopicMutationDto.Schedule(TOPIC_ID, 45);

			final var expected = new TopicMutation.Schedule(TOPIC_ID,
					new Minutes(45));
			assertThat(dto.toBdo()).isEqualTo(expected);
		}

		@Test
		void unscheduled() {
			final var dto = new TopicMutationDto.Schedule(TOPIC_ID, null);

			final var expected = new TopicMutation.Schedule(TOPIC_ID, null);
			assertThat(dto.toBdo()).isEqualTo(expected);
		}

		@Test
		void remove() {
			final var dto = new TopicMutationDto.Remove(TOPIC_ID);

			final var expected = new TopicMutation.Remove(TOPIC_ID);
			assertThat(dto.toBdo()).isEqualTo(expected);
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
					new TopicMutationDto.Add(TOPIC_ID, RANK, "Blockers"));

			assertThat(json).isEqualToIgnoringWhitespace("""
					{
						"type": "ADD_TOPIC",
						"topic": 32,
						"rank": "a1",
						"name": "Blockers"
					}
					""");
		}

		@Test
		void move() {
			final String json = write(
					new TopicMutationDto.Move(TOPIC_ID, RANK));

			assertThat(json).isEqualToIgnoringWhitespace("""
					{
						"type": "MOVE_TOPIC",
						"topic": 32,
						"rank": "a1"
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
