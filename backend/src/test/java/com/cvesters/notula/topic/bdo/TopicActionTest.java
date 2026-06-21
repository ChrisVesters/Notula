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

	@Nested
	class UpdateDescription {

		@ParameterizedTest
		@CsvSource({ "0,0,'Great ','Kickoff','Great Kickoff'",
				"5,0,'Great ','2026 Kickoff','2026 Great Kickoff'",
				"12,0,' Great','2026 Kickoff','2026 Kickoff Great'",
				"0,5,'','2026 Kickoff','Kickoff'",
				"5,8,'','2026 Kickoff Meeting','2026 Meeting'",
				"12,8,'','2026 Kickoff Meeting','2026 Kickoff'",
				"0,4,'2027','2026 Kickoff','2027 Kickoff'",
				"5,7,'Test','2026 Kickoff','2026 Test'",
				"13,7,'Session','2026 Kickoff Meeting','2026 Kickoff Session'",
				"0,20,'X','2026 Kickoff Meeting','X'" })
		void success(final int position, final int length, final String value,
				final String original, final String expected) {
			final var action = new TopicAction.UpdateDescription(position,
					length, value);

			assertThat(action).isNotNull();
			assertThat(action.getPosition()).isEqualTo(position);
			assertThat(action.getLength()).isEqualTo(length);
			assertThat(action.getValue()).isEqualTo(value);

			final TopicInfo topic = mock();
			when(topic.getDescription()).thenReturn(original);

			action.apply(topic);

			verify(topic).setDescription(expected);
		}
	}

	@Nested
	class Delete {

		@Test
		void success() {
			final var action = new TopicAction.Delete();

			assertThat(action).isNotNull();
		}
	}
}
