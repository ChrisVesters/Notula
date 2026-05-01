package com.cvesters.notula.topic.dto;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.cvesters.notula.topic.TestTopic;
import com.cvesters.notula.topic.TopicActionMatcher;
import com.cvesters.notula.topic.bdo.TopicAction;

class TopicActionDtoTest {

	private static final TestTopic TOPIC = TestTopic.SPORER_PROJECT_BLOCKERS;

	@Nested
	class Create {

		@Test
		void toBdo() {
			final var dto = new TopicActionDto.Create(TOPIC.getName());
			final TopicAction.Create bdo = dto.toBdo();

			assertThat(bdo.getName()).isEqualTo(TOPIC.getName());
		}
	}

	@Nested
	class UpdateName {

		@Test
		void toBdo() {
			final var dto = new TopicActionDto.Update.Name(5, 2, "Updated");
			final TopicAction.Update bdo = dto.toBdo();

			final var expected = new TopicAction.UpdateName(5, 2, "Updated");
			final var matcher = new TopicActionMatcher.UpdateName(expected);
			assertThat(bdo).is(matcher.equal());
		}
	}
}
