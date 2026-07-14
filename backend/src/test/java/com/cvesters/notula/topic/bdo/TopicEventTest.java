package com.cvesters.notula.topic.bdo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class TopicEventTest {

	@Nested
	class Constructor {

		@Test
		void success() {
			final var action = new TopicAction.Create(0, "New");
			final var event = new TopicEvent(1L, action);

			assertThat(event.topicId()).isEqualTo(1L);
			assertThat(event.action()).isEqualTo(action);
		}

		@Test
		void actionNull() {
			assertThatThrownBy(() -> new TopicEvent(1L, null))
					.isInstanceOf(NullPointerException.class);
		}
	}
}
