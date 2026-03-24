package com.cvesters.notula.topic.bdo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import com.cvesters.notula.topic.TestTopic;

class TopicEventTest {

	@Nested
	class Create {

		private static final TestTopic TOPIC = TestTopic.GLOVER_KICKOFF_2026_LOOKBACK;

		@Test
		void ofInfo() {
			final var info = TOPIC.info();
			final var event = new TopicEvent.Create(info);

			assertThat(event).isEqualTo(TOPIC.createEvent());
		}

		@Test
		void ofInfoNull() {
			assertThatThrownBy(() -> new TopicEvent.Create(null))
					.isInstanceOf(NullPointerException.class);
		}

		@Test
		void nameNull() {
			final long id = TOPIC.getId();
			assertThatThrownBy(() -> new TopicEvent.Create(id, null))
					.isInstanceOf(NullPointerException.class);
		}

		@ParameterizedTest
		@ValueSource(strings = { "", " " })
		void nameInvalid(final String name) {
			final long id = TOPIC.getId();
			assertThatThrownBy(() -> new TopicEvent.Create(id, name))
					.isInstanceOf(IllegalArgumentException.class);
		}
	}
}
