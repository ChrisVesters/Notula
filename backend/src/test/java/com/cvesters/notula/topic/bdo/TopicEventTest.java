package com.cvesters.notula.topic.bdo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class TopicEventTest {

	@Nested
	class Constructor {

		@Test
		void success() {
			final TopicInfo topic = mock();
			final var action = new TopicAction.Create(12L, 0, "New");
			final var event = new TopicEvent(topic, action);

			assertThat(event.topic()).isEqualTo(topic);
			assertThat(event.action()).isEqualTo(action);
		}

		@Test
		void topicNull() {
			final var action = new TopicAction.Create(12L, 0, "New");

			assertThatThrownBy(() -> new TopicEvent(null, action))
					.isInstanceOf(NullPointerException.class);
		}

		@Test
		void actionNull() {
			final TopicInfo topic = mock();

			assertThatThrownBy(() -> new TopicEvent(topic, null))
					.isInstanceOf(NullPointerException.class);
		}
	}
}
