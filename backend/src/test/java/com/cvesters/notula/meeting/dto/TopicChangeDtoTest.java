package com.cvesters.notula.meeting.dto;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.cvesters.notula.common.domain.Minutes;
import com.cvesters.notula.topic.TestTopic;
import com.cvesters.notula.topic.TopicActionMatcher;
import com.cvesters.notula.topic.bdo.TopicAction;

class TopicChangeDtoTest {

	private static final TestTopic TOPIC = TestTopic.SPORER_PROJECT_BLOCKERS;

	private static final TextEditDto EDIT = new TextEditDto(4, 12, "Updated");

	@Nested
	class Add {

		@Test
		void toBdo() {
			final var dto = new TopicChangeDto.Add(TOPIC.getSequenceId(),
					TOPIC.getName());

			final TopicAction.Create bdo = dto.toBdo();

			final var expected = new TopicAction.Create(TOPIC.getSequenceId(),
					TOPIC.getName());
			final var matcher = new TopicActionMatcher.Create(expected);
			assertThat(bdo).is(matcher.equal());
		}
	}

	@Nested
	class Move {

		@Test
		void toBdo() {
			final var dto = new TopicChangeDto.Move(TOPIC.getId(), 2);

			final TopicAction.Move bdo = dto.toBdo();

			final var matcher = new TopicActionMatcher.Move(
					new TopicAction.Move(2));
			assertThat(bdo).is(matcher.equal());
		}
	}

	@Nested
	class Rename {

		@Test
		void toBdo() {
			final var dto = new TopicChangeDto.Rename(TOPIC.getId(), EDIT);

			final TopicAction.UpdateName bdo = dto.toBdo();

			final var expected = new TopicAction.UpdateName(4, 12, "Updated");
			final var matcher = new TopicActionMatcher.UpdateName(expected);
			assertThat(bdo).is(matcher.equal());
		}
	}

	@Nested
	class Describe {

		@Test
		void toBdo() {
			final var dto = new TopicChangeDto.Describe(TOPIC.getId(), EDIT);

			final TopicAction.UpdateDescription bdo = dto.toBdo();

			final var expected = new TopicAction.UpdateDescription(4, 12,
					"Updated");
			final var matcher = new TopicActionMatcher.UpdateDescription(
					expected);
			assertThat(bdo).is(matcher.equal());
		}
	}

	@Nested
	class Schedule {

		@Test
		void toBdo() {
			final var dto = new TopicChangeDto.Schedule(TOPIC.getId(), 45);

			final TopicAction.UpdateDuration bdo = dto.toBdo();

			assertThat(bdo.getDuration()).isEqualTo(new Minutes(45));
		}

		@Test
		void withoutDuration() {
			final var dto = new TopicChangeDto.Schedule(TOPIC.getId(), null);

			final TopicAction.UpdateDuration bdo = dto.toBdo();

			assertThat(bdo.getDuration()).isNull();
		}
	}
}
