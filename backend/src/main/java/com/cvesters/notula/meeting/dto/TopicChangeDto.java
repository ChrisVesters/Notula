package com.cvesters.notula.meeting.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import com.fasterxml.jackson.annotation.JsonUnwrapped;

import com.cvesters.notula.common.domain.Minutes;
import com.cvesters.notula.topic.bdo.TopicAction;

public sealed interface TopicChangeDto extends ChangeDto {

	record Add(@PositiveOrZero int sequenceId, @NotNull String name)
			implements TopicChangeDto {
		public TopicAction.Create toBdo() {
			return new TopicAction.Create(sequenceId, name);
		}
	}

	record Move(long topic, @PositiveOrZero int sequenceId)
			implements TopicChangeDto {
		public TopicAction.Move toBdo() {
			return new TopicAction.Move(sequenceId);
		}
	}

	sealed interface Update extends TopicChangeDto {

		long topic();

		TopicAction.Update toBdo();
	}

	record Rename(long topic, @NotNull @Valid @JsonUnwrapped TextEditDto edit)
			implements TopicChangeDto.Update {
		public TopicAction.UpdateName toBdo() {
			return new TopicAction.UpdateName(edit.position(), edit.length(),
					edit.value());
		}
	}

	record Describe(long topic, @NotNull @Valid @JsonUnwrapped TextEditDto edit)
			implements TopicChangeDto.Update {
		public TopicAction.UpdateDescription toBdo() {
			return new TopicAction.UpdateDescription(edit.position(),
					edit.length(), edit.value());
		}
	}

	record Schedule(long topic, @PositiveOrZero Integer minutes)
			implements TopicChangeDto.Update {
		public TopicAction.UpdateDuration toBdo() {
			return new TopicAction.UpdateDuration(
					minutes == null ? null : new Minutes(minutes));
		}
	}

	record Remove(long topic) implements TopicChangeDto {
	}
}
