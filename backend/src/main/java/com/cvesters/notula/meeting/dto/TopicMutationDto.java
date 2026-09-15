package com.cvesters.notula.meeting.dto;

import java.util.Objects;

import com.cvesters.notula.common.domain.Minutes;
import com.cvesters.notula.topic.bdo.TopicAction;
import com.cvesters.notula.topic.bdo.TopicEvent;
import com.fasterxml.jackson.annotation.JsonUnwrapped;

public sealed interface TopicMutationDto extends MutationDto {

	static TopicMutationDto of(final TopicEvent event) {
		Objects.requireNonNull(event);

		final long topic = event.topic().getId();

		return switch (event.action()) {
			case TopicAction.Create action -> new Add(topic,
					action.getSequenceId(), action.getName());
			case TopicAction.Move action -> new Move(topic,
					action.getSequenceId());
			case TopicAction.UpdateName action -> new Rename(topic,
					new TextEditDto(action.getPosition(), action.getLength(),
							action.getValue()));
			case TopicAction.UpdateDescription action -> new Describe(topic,
					new TextEditDto(action.getPosition(), action.getLength(),
							action.getValue()));
			case TopicAction.UpdateDuration action -> new Schedule(topic,
					minutes(action.getDuration()));
			case TopicAction.Delete _ -> new Remove(topic);
		};
	}

	private static Integer minutes(final Minutes duration) {
		return duration == null ? null : duration.value();
	}

	record Add(long topic, int sequenceId, String name)
			implements TopicMutationDto {
	}

	record Move(long topic, int sequenceId) implements TopicMutationDto {
	}

	record Rename(long topic, @JsonUnwrapped TextEditDto edit)
			implements TopicMutationDto {
	}

	record Describe(long topic, @JsonUnwrapped TextEditDto edit)
			implements TopicMutationDto {
	}

	record Schedule(long topic, Integer minutes)
			implements TopicMutationDto {
	}

	record Remove(long topic) implements TopicMutationDto {
	}
}
