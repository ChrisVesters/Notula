package com.cvesters.notula.topic.bdo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

class TopicActionTest {

	@Nested
	class Create {

		@Test
		void success() {
			final var action = new TopicAction.Create("Name");

			assertThat(action.getName()).isEqualTo("Name");
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

	@Nested
	class UpdateName {

		@ParameterizedTest
		@CsvSource({ "0,4,'Updated',Test,Updated", "1,4,2026,M2025,M2026",
				"2,2,tr,Rexxospective,Retrospective" })
		void success(final int position, final int length, final String value,
				final String original, final String expected) {
			final var action = new TopicAction.UpdateName(position, length,
					value);

			assertThat(action).isNotNull();
			assertThat(action.getPosition()).isEqualTo(position);
			assertThat(action.getLength()).isEqualTo(length);
			assertThat(action.getValue()).isEqualTo(value);

			final TopicInfo topic = mock();
			when(topic.getName()).thenReturn(original);

			action.apply(topic);

			verify(topic).setName(expected);
		}

	}
}
