package com.cvesters.notula.meeting.dto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.cvesters.notula.block.bdo.BlockType;

import tools.jackson.databind.DatabindException;
import tools.jackson.databind.ObjectMapper;

class ChangeDtoTest {

	private static final ObjectMapper MAPPER = new ObjectMapper();

	private static final TextEditDto EDIT = new TextEditDto(4, 12, "Updated");

	private static ChangeDto read(final String json) {
		return MAPPER.readValue(json, ChangeDto.class);
	}

	@Nested
	class Deserialise {

		@Test
		void renameMeeting() {
			final ChangeDto change = read("""
					{
						"type": "RENAME_MEETING",
						"position": 4,
						"length": 12,
						"value": "Updated"
					}
					""");

			final var expected = new MeetingChangeDto.Rename(EDIT);
			assertThat(change).isEqualTo(expected);
		}

		@Test
		void describeMeeting() {
			final ChangeDto change = read("""
					{
						"type": "DESCRIBE_MEETING",
						"position": 4,
						"length": 12,
						"value": "Updated"
					}
					""");

			final var expected = new MeetingChangeDto.Describe(EDIT);
			assertThat(change).isEqualTo(expected);
		}

		@Test
		void addTopic() {
			final ChangeDto change = read("""
					{
						"type": "ADD_TOPIC",
						"afterId": 7,
						"name": "Blockers"
					}
					""");

			final var expected = new TopicChangeDto.Add(7L, "Blockers");
			assertThat(change).isEqualTo(expected);
		}

		@Test
		void moveTopic() {
			final ChangeDto change = read("""
					{
						"type": "MOVE_TOPIC",
						"topic": 7,
						"afterId": 7
					}
					""");

			final var expected = new TopicChangeDto.Move(7, 7L);
			assertThat(change).isEqualTo(expected);
		}

		@Test
		void renameTopic() {
			final ChangeDto change = read("""
					{
						"type": "RENAME_TOPIC",
						"topic": 7,
						"position": 4,
						"length": 12,
						"value": "Updated"
					}
					""");

			final var expected = new TopicChangeDto.Rename(7, EDIT);
			assertThat(change).isEqualTo(expected);
		}

		@Test
		void describeTopic() {
			final ChangeDto change = read("""
					{
						"type": "DESCRIBE_TOPIC",
						"topic": 7,
						"position": 4,
						"length": 12,
						"value": "Updated"
					}
					""");

			final var expected = new TopicChangeDto.Describe(7, EDIT);
			assertThat(change).isEqualTo(expected);
		}

		@Test
		void scheduleTopic() {
			final ChangeDto change = read("""
					{
						"type": "SCHEDULE_TOPIC",
						"topic": 7,
						"minutes": 45
					}
					""");

			final var expected = new TopicChangeDto.Schedule(7, 45);
			assertThat(change).isEqualTo(expected);
		}

		@Test
		void scheduleTopicWithoutDuration() {
			final ChangeDto change = read("""
					{
						"type": "SCHEDULE_TOPIC",
						"topic": 7,
						"minutes": null
					}
					""");

			final var expected = new TopicChangeDto.Schedule(7, null);
			assertThat(change).isEqualTo(expected);
		}

		@Test
		void removeTopic() {
			final ChangeDto change = read("""
					{
						"type": "REMOVE_TOPIC",
						"topic": 7
					}
					""");

			final var expected = new TopicChangeDto.Remove(7);
			assertThat(change).isEqualTo(expected);
		}

		@Test
		void addBlock() {
			final ChangeDto change = read("""
					{
						"type": "ADD_BLOCK",
						"topic": 7,
						"blockType": "TEXT",
						"afterId": 7
					}
					""");

			final var expected = new BlockChangeDto.Add(7, BlockType.TEXT, 7L);
			assertThat(change).isEqualTo(expected);
		}

		@Test
		void moveBlock() {
			final ChangeDto change = read("""
					{
						"type": "MOVE_BLOCK",
						"block": 9,
						"afterId": 7
					}
					""");

			final var expected = new BlockChangeDto.Move(9, 7L);
			assertThat(change).isEqualTo(expected);
		}

		@Test
		void removeBlock() {
			final ChangeDto change = read("""
					{
						"type": "REMOVE_BLOCK",
						"block": 9
					}
					""");

			final var expected = new BlockChangeDto.Remove(9);
			assertThat(change).isEqualTo(expected);
		}

		@Test
		void editTextBlock() {
			final ChangeDto change = read("""
					{
						"type": "EDIT_TEXT_BLOCK",
						"block": 9,
						"position": 4,
						"length": 12,
						"value": "Updated"
					}
					""");

			final var expected = new TextBlockChangeDto.Edit(9, EDIT);
			assertThat(change).isEqualTo(expected);
		}

		@Test
		void unknownType() {
			assertThatThrownBy(() -> read("""
					{
						"type": "RENAME_ORGANISATION",
						"name": "Sporer"
					}
					""")).isInstanceOf(DatabindException.class);
		}

		@Test
		void withoutType() {
			assertThatThrownBy(() -> read("""
					{
						"topic": 7
					}
					""")).isInstanceOf(DatabindException.class);
		}
	}
}
