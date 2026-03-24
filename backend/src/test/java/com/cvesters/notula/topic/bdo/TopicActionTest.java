package com.cvesters.notula.topic.bdo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class TopicActionTest {

	@Nested
	class Create {

		@Test
		void success() {
			final var action = new TopicAction.Create("Name");

			assertThat(action.name()).isEqualTo("Name");
		}

		@Test
		void nameNull() {
			assertThatThrownBy(() -> new TopicAction.Create(null))
					.isInstanceOf(NullPointerException.class);
		}

		@ParameterizedTest
		@ValueSource(strings = { "", " " })
		void nameInvalid(final String name) {
			assertThatThrownBy(() -> new TopicAction.Create(name))
					.isInstanceOf(IllegalArgumentException.class);
		}
	}
}
