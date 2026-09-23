package com.cvesters.notula.meeting.dto;

import java.util.Objects;

import com.cvesters.notula.common.domain.Minutes;
import com.cvesters.notula.topic.bdo.TopicAction;
import com.cvesters.notula.topic.bdo.TopicEvent;
import com.cvesters.notula.topic.bdo.TopicInfo;
import com.fasterxml.jackson.annotation.JsonUnwrapped;

public sealed interface TopicMutationDto extends MutationDto {

	static TopicMutationDto of(final TopicEvent event) {
		Objects.requireNonNull(event);

		final TopicInfo topic = event.topic();

		return switch (event.action()) {
			case TopicAction.Create action -> new Add(topic, action);
			case TopicAction.Move action -> new Move(topic, action);
			case TopicAction.UpdateName action -> new Rename(topic, action);
			case TopicAction.UpdateDescription action -> new Describe(topic,
					action);
			case TopicAction.UpdateDuration action -> new Schedule(topic,
					action);
			case TopicAction.Delete _ -> new Remove(topic);
		};
	}

	record Add(long topic, String rank, String name)
			implements TopicMutationDto {

		private Add(final TopicInfo topic, final TopicAction.Create action) {
			Objects.requireNonNull(topic);
			Objects.requireNonNull(action);

			final long topicId = topic.getId();
			final String rank = topic.getRank().value();
			final String name = action.getName();

			this(topicId, rank, name);
		}
	}

	record Move(long topic, String rank) implements TopicMutationDto {

		private Move(final TopicInfo topic, final TopicAction.Move action) {
			Objects.requireNonNull(topic);
			Objects.requireNonNull(action);

			final long topicId = topic.getId();
			final String rank = topic.getRank().value();

			this(topicId, rank);
		}
	}

	record Rename(long topic, @JsonUnwrapped TextEditDto edit)
			implements TopicMutationDto {

		private Rename(final TopicInfo topic,
				final TopicAction.UpdateName action) {
			Objects.requireNonNull(topic);
			Objects.requireNonNull(action);

			final long topicId = topic.getId();
			final var edit = new TextEditDto(action.getPosition(),
					action.getLength(), action.getValue());

			this(topicId, edit);
		}
	}

	record Describe(long topic, @JsonUnwrapped TextEditDto edit)
			implements TopicMutationDto {

		private Describe(final TopicInfo topic,
				final TopicAction.UpdateDescription action) {
			Objects.requireNonNull(topic);
			Objects.requireNonNull(action);

			final long topicId = topic.getId();
			final var edit = new TextEditDto(action.getPosition(),
					action.getLength(), action.getValue());

			this(topicId, edit);
		}
	}

	record Schedule(long topic, Integer minutes) implements TopicMutationDto {

		private Schedule(final TopicInfo topic,
				final TopicAction.UpdateDuration action) {
			Objects.requireNonNull(topic);
			Objects.requireNonNull(action);

			final long topicId = topic.getId();
			final Minutes duration = action.getDuration();
			final Integer minutes = duration == null ? null : duration.value();

			this(topicId, minutes);
		}
	}

	record Remove(long topic) implements TopicMutationDto {

		private Remove(final TopicInfo topic) {
			Objects.requireNonNull(topic);

			final long topicId = topic.getId();

			this(topicId);
		}
	}
}
