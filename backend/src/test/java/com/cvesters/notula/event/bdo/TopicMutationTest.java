package com.cvesters.notula.event.bdo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.cvesters.notula.common.domain.Minutes;
import com.cvesters.notula.common.domain.Rank;

class TopicMutationTest {

	private static final long TOPIC_ID = 32L;
	private static final Rank RANK = new Rank("a1");

	@Nested
	class Add {

		@Test
		void success() {
			final var mutation = new TopicMutation.Add(TOPIC_ID, RANK,
					"Blockers");

			assertThat(mutation.topicId()).isEqualTo(TOPIC_ID);
			assertThat(mutation.rank()).isEqualTo(RANK);
			assertThat(mutation.name()).isEqualTo("Blockers");
		}

		@Test
		void rankNull() {
			assertThatThrownBy(
					() -> new TopicMutation.Add(TOPIC_ID, null, "Blockers"))
							.isInstanceOf(NullPointerException.class);
		}

		@Test
		void nameNull() {
			assertThatThrownBy(
					() -> new TopicMutation.Add(TOPIC_ID, RANK, null))
							.isInstanceOf(NullPointerException.class);
		}
	}

	@Nested
	class Move {

		@Test
		void success() {
			final var mutation = new TopicMutation.Move(TOPIC_ID, RANK);

			assertThat(mutation.topicId()).isEqualTo(TOPIC_ID);
			assertThat(mutation.rank()).isEqualTo(RANK);
		}

		@Test
		void rankNull() {
			assertThatThrownBy(() -> new TopicMutation.Move(TOPIC_ID, null))
					.isInstanceOf(NullPointerException.class);
		}
	}

	@Nested
	class Rename {

		@Test
		void success() {
			final var mutation = new TopicMutation.Rename(TOPIC_ID, 4, 12,
					"Updated");

			assertThat(mutation.topicId()).isEqualTo(TOPIC_ID);
			assertThat(mutation.position()).isEqualTo(4);
			assertThat(mutation.length()).isEqualTo(12);
			assertThat(mutation.value()).isEqualTo("Updated");
		}

		@Test
		void start() {
			final var mutation = new TopicMutation.Rename(TOPIC_ID, 0, 12,
					"Updated");

			assertThat(mutation.position()).isEqualTo(0);
		}

		@Test
		void insert() {
			final var mutation = new TopicMutation.Rename(TOPIC_ID, 4, 0,
					"Updated");

			assertThat(mutation.length()).isEqualTo(0);
		}

		@Test
		void positionNegative() {
			assertThatThrownBy(
					() -> new TopicMutation.Rename(TOPIC_ID, -1, 12, "Up"))
							.isInstanceOf(IllegalArgumentException.class);
		}

		@Test
		void lengthNegative() {
			assertThatThrownBy(
					() -> new TopicMutation.Rename(TOPIC_ID, 4, -1, "Up"))
							.isInstanceOf(IllegalArgumentException.class);
		}

		@Test
		void valueNull() {
			assertThatThrownBy(
					() -> new TopicMutation.Rename(TOPIC_ID, 4, 12, null))
							.isInstanceOf(NullPointerException.class);
		}
	}

	@Nested
	class Describe {

		@Test
		void success() {
			final var mutation = new TopicMutation.Describe(TOPIC_ID, 4, 12,
					"Updated");

			assertThat(mutation.topicId()).isEqualTo(TOPIC_ID);
			assertThat(mutation.position()).isEqualTo(4);
			assertThat(mutation.length()).isEqualTo(12);
			assertThat(mutation.value()).isEqualTo("Updated");
		}

		@Test
		void start() {
			final var mutation = new TopicMutation.Describe(TOPIC_ID, 0, 12,
					"Updated");

			assertThat(mutation.position()).isEqualTo(0);
		}

		@Test
		void insert() {
			final var mutation = new TopicMutation.Describe(TOPIC_ID, 4, 0,
					"Updated");

			assertThat(mutation.length()).isEqualTo(0);
		}

		@Test
		void positionNegative() {
			assertThatThrownBy(
					() -> new TopicMutation.Describe(TOPIC_ID, -1, 12, "Up"))
							.isInstanceOf(IllegalArgumentException.class);
		}

		@Test
		void lengthNegative() {
			assertThatThrownBy(
					() -> new TopicMutation.Describe(TOPIC_ID, 4, -1, "Up"))
							.isInstanceOf(IllegalArgumentException.class);
		}

		@Test
		void valueNull() {
			assertThatThrownBy(
					() -> new TopicMutation.Describe(TOPIC_ID, 4, 12, null))
							.isInstanceOf(NullPointerException.class);
		}
	}

	@Nested
	class Schedule {

		@Test
		void success() {
			final var duration = new Minutes(45);

			final var mutation = new TopicMutation.Schedule(TOPIC_ID,
					duration);

			assertThat(mutation.topicId()).isEqualTo(TOPIC_ID);
			assertThat(mutation.duration()).isEqualTo(duration);
		}

		@Test
		void unscheduled() {
			final var mutation = new TopicMutation.Schedule(TOPIC_ID, null);

			assertThat(mutation.topicId()).isEqualTo(TOPIC_ID);
			assertThat(mutation.duration()).isNull();
		}
	}

	@Nested
	class Remove {

		@Test
		void success() {
			final var mutation = new TopicMutation.Remove(TOPIC_ID);

			assertThat(mutation.topicId()).isEqualTo(TOPIC_ID);
		}
	}
}
