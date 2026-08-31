package com.cvesters.notula.topic.bdo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

import java.util.UUID;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.cvesters.notula.common.domain.Origin;
import com.cvesters.notula.session.TestSession;

class TopicEventTest {

	private static final TestSession SESSION = TestSession.EDUARDO_CHRISTIANSEN_SPORER;
	private static final UUID CLIENT_ID = UUID
			.fromString("3f9c1a44-1d2e-4a51-8b0c-2c7e9b1d4a02");
	private static final Origin ORIGIN = new Origin(SESSION.principal(),
			CLIENT_ID);

	@Nested
	class Constructor {

		@Test
		void success() {
			final TopicInfo topic = mock();
			final var action = new TopicAction.Create(12L, 0, "New");
			final var event = new TopicEvent(topic, action, ORIGIN);

			assertThat(event.topic()).isEqualTo(topic);
			assertThat(event.action()).isEqualTo(action);
			assertThat(event.origin()).isEqualTo(ORIGIN);
		}

		@Test
		void topicNull() {
			final var action = new TopicAction.Create(12L, 0, "New");

			assertThatThrownBy(() -> new TopicEvent(null, action, ORIGIN))
					.isInstanceOf(NullPointerException.class);
		}

		@Test
		void actionNull() {
			final TopicInfo topic = mock();

			assertThatThrownBy(() -> new TopicEvent(topic, null, ORIGIN))
					.isInstanceOf(NullPointerException.class);
		}

		@Test
		void originNull() {
			final TopicInfo topic = mock();
			final var action = new TopicAction.Create(12L, 0, "New");

			assertThatThrownBy(() -> new TopicEvent(topic, action, null))
					.isInstanceOf(NullPointerException.class);
		}
	}
}
