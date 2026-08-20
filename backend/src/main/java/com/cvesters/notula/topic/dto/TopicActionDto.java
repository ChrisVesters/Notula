package com.cvesters.notula.topic.dto;

import java.util.Optional;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

import com.cvesters.notula.common.domain.Minutes;
import com.cvesters.notula.common.dto.TextUpdateDto;
import com.cvesters.notula.topic.bdo.TopicAction;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

public final class TopicActionDto {

	private TopicActionDto() {
	}

	public static final class Create {

		private final long meetingId;

		@PositiveOrZero
		private final int sequenceId;

		@NotNull
		private final String name;

		public Create(final long meetingId, final int sequenceId,
				final String name) {
			this.meetingId = meetingId;
			this.sequenceId = sequenceId;
			this.name = name;
		}

		public TopicAction.Create toBdo() {
			return new TopicAction.Create(meetingId, sequenceId, name);
		}
	}

	@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "action")
	@JsonSubTypes({ @Type(value = Update.Name.class, name = "UPDATE_NAME"),
			@Type(value = Update.Description.class, name = "UPDATE_DESCRIPTION"),
			@Type(value = Update.Duration.class, name = "UPDATE_DURATION") })
	public abstract static sealed class Update {

		private final long topicId;

		protected Update(final long topicId) {
			this.topicId = topicId;
		}

		public long getTopicId() {
			return topicId;
		}

		public abstract TopicAction.Update toBdo();

		public static final class Name extends Update {

			@Valid
			private final TextUpdateDto update;

			public Name(final long topicId, final int position,
					final int length, final String value) {
				super(topicId);

				this.update = new TextUpdateDto(position, length, value);
			}

			public TopicAction.Update toBdo() {
				final int position = update.position();
				final int length = update.length();
				final String value = update.value();

				return new TopicAction.UpdateName(position, length, value);
			}
		}

		public static final class Description extends Update {

			@Valid
			private final TextUpdateDto update;

			public Description(final long topicId, final int position,
					final int length, final String value) {
				super(topicId);

				this.update = new TextUpdateDto(position, length, value);
			}

			public TopicAction.Update toBdo() {
				final int position = update.position();
				final int length = update.length();
				final String value = update.value();

				return new TopicAction.UpdateDescription(position, length,
						value);
			}
		}

		public static final class Duration extends Update {

			@Positive
			private final Integer duration;

			public Duration(final long topicId, final Integer duration) {
				super(topicId);

				this.duration = duration;
			}

			public TopicAction.Update toBdo() {
				final Minutes v = Optional.ofNullable(duration)
						.map(Minutes::new)
						.orElse(null);

				return new TopicAction.UpdateDuration(v);
			}
		}

	}

	public static final class Delete {

		private final long topicId;

		public Delete(final long topicId) {
			this.topicId = topicId;
		}

		public long getTopicId() {
			return topicId;
		}
	}
}
