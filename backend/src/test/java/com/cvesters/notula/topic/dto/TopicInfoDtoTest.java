package com.cvesters.notula.topic.dto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.cvesters.notula.topic.TestTopic;
import com.cvesters.notula.topic.bdo.TopicInfo;

class TopicInfoDtoTest {

	private static final TestTopic TOPIC = TestTopic.GLOVER_KICKOFF_2026_LOOKBACK;

	@Nested
	class Constructor {

		@Test
		void success() {
			final TopicInfo bdo = TOPIC.info();

			final var dto = new TopicInfoDto(bdo);

			assertThat(dto.id()).isEqualTo(TOPIC.getId());
			assertThat(dto.name()).isEqualTo(TOPIC.getName());
		}

		@Test
		void topicNull() {
			assertThatThrownBy(() -> new TopicInfoDto(null))
					.isInstanceOf(NullPointerException.class);
		}
	}

}
