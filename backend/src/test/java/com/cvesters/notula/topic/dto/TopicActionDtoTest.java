package com.cvesters.notula.topic.dto;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.cvesters.notula.common.domain.Minutes;
import com.cvesters.notula.meeting.TestMeeting;
import com.cvesters.notula.topic.TestTopic;
import com.cvesters.notula.topic.TopicActionMatcher;
import com.cvesters.notula.topic.bdo.TopicAction;

class TopicActionDtoTest {

	private static final TestTopic TOPIC = TestTopic.SPORER_PROJECT_BLOCKERS;
	private static final TestMeeting MEETING = TOPIC.getMeeting();

	@Nested
	class Create {

		@Test
		void toBdo() {
			final var dto = new TopicActionDto.Create(MEETING.getId(),
					TOPIC.getSequenceId(), TOPIC.getName());
			final TopicAction.Create bdo = dto.toBdo();

			assertThat(bdo.getMeetingId()).isEqualTo(MEETING.getId());
			assertThat(bdo.getSequenceId()).isEqualTo(TOPIC.getSequenceId());
			assertThat(bdo.getName()).isEqualTo(TOPIC.getName());
		}
	}

	@Nested
	class UpdateName {

		@Test
		void toBdo() {
			final var dto = new TopicActionDto.Update.Name(TOPIC.getId(), 5, 2,
					"Updated");
			final TopicAction.Update bdo = dto.toBdo();

			final var expected = new TopicAction.UpdateName(5, 2, "Updated");
			final var matcher = new TopicActionMatcher.UpdateName(expected);
			assertThat(bdo).is(matcher.equal());
		}
	}

	@Nested
	class UpdateDescription {

		@Test
		void toBdo() {
			final var dto = new TopicActionDto.Update.Description(
					TOPIC.getId(), 5, 2, "Updated");
			final TopicAction.Update bdo = dto.toBdo();

			final var expected = new TopicAction.UpdateDescription(5, 2,
					"Updated");
			final var matcher = new TopicActionMatcher.UpdateDescription(
					expected);
			assertThat(bdo).is(matcher.equal());
		}
	}

	@Nested
	class UpdateDuration {

		@Test
		void toBdo() {
			final var dto = new TopicActionDto.Update.Duration(TOPIC.getId(),
					45);
			final TopicAction.Update bdo = dto.toBdo();

			final var duration = new Minutes(45);
			final var expected = new TopicAction.UpdateDuration(duration);
			final var matcher = new TopicActionMatcher.UpdateDuration(expected);
			assertThat(bdo).is(matcher.equal());
		}

		@Test
		void toBdoWithoutDuration() {
			final var dto = new TopicActionDto.Update.Duration(TOPIC.getId(),
					null);
			final TopicAction.Update bdo = dto.toBdo();

			final var expected = new TopicAction.UpdateDuration(null);
			final var matcher = new TopicActionMatcher.UpdateDuration(expected);
			assertThat(bdo).is(matcher.equal());
		}
	}
}
