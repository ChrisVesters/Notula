package com.cvesters.notula.event.dto;

import java.util.Objects;

import com.cvesters.notula.common.domain.Minutes;
import com.cvesters.notula.common.domain.Rank;
import com.cvesters.notula.event.bdo.TopicMutation;
import com.cvesters.notula.meeting.dto.TextEditDto;
import com.fasterxml.jackson.annotation.JsonUnwrapped;

public sealed interface TopicMutationDto extends MutationDto {

	static TopicMutationDto of(final TopicMutation mutation) {
		Objects.requireNonNull(mutation);

		return switch (mutation) {
			case TopicMutation.Add m -> new Add(m);
			case TopicMutation.Move m -> new Move(m);
			case TopicMutation.Rename m -> new Rename(m);
			case TopicMutation.Describe m -> new Describe(m);
			case TopicMutation.Schedule m -> new Schedule(m);
			case TopicMutation.Remove m -> new Remove(m);
		};
	}

	@Override
	TopicMutation toBdo();

	record Add(long topic, String rank, String name)
			implements TopicMutationDto {

		private Add(final TopicMutation.Add mutation) {
			final long topicId = mutation.topicId();
			final String rank = mutation.rank().value();
			final String name = mutation.name();

			this(topicId, rank, name);
		}

		@Override
		public TopicMutation toBdo() {
			return new TopicMutation.Add(topic, new Rank(rank), name);
		}
	}

	record Move(long topic, String rank) implements TopicMutationDto {

		private Move(final TopicMutation.Move mutation) {
			final long topicId = mutation.topicId();
			final String rank = mutation.rank().value();

			this(topicId, rank);
		}

		@Override
		public TopicMutation toBdo() {
			return new TopicMutation.Move(topic, new Rank(rank));
		}
	}

	record Rename(long topic, @JsonUnwrapped TextEditDto edit)
			implements TopicMutationDto {

		private Rename(final TopicMutation.Rename mutation) {
			final long topicId = mutation.topicId();
			final var edit = new TextEditDto(mutation.position(),
					mutation.length(), mutation.value());

			this(topicId, edit);
		}

		@Override
		public TopicMutation toBdo() {
			return new TopicMutation.Rename(topic, edit.position(),
					edit.length(), edit.value());
		}
	}

	record Describe(long topic, @JsonUnwrapped TextEditDto edit)
			implements TopicMutationDto {

		private Describe(final TopicMutation.Describe mutation) {
			final long topicId = mutation.topicId();
			final var edit = new TextEditDto(mutation.position(),
					mutation.length(), mutation.value());

			this(topicId, edit);
		}

		@Override
		public TopicMutation toBdo() {
			return new TopicMutation.Describe(topic, edit.position(),
					edit.length(), edit.value());
		}
	}

	record Schedule(long topic, Integer minutes) implements TopicMutationDto {

		private Schedule(final TopicMutation.Schedule mutation) {
			final long topicId = mutation.topicId();
			final Minutes duration = mutation.duration();
			final Integer minutes = duration == null ? null : duration.value();

			this(topicId, minutes);
		}

		@Override
		public TopicMutation toBdo() {
			final Minutes duration = minutes == null ? null
					: new Minutes(minutes);

			return new TopicMutation.Schedule(topic, duration);
		}
	}

	record Remove(long topic) implements TopicMutationDto {

		private Remove(final TopicMutation.Remove mutation) {
			final long topicId = mutation.topicId();

			this(topicId);
		}

		@Override
		public TopicMutation toBdo() {
			return new TopicMutation.Remove(topic);
		}
	}
}
