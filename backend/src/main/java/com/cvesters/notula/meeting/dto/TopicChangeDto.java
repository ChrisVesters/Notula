package com.cvesters.notula.meeting.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

import com.fasterxml.jackson.annotation.JsonUnwrapped;

import com.cvesters.notula.common.domain.Minutes;
import com.cvesters.notula.topic.bdo.TopicAction;

public sealed interface TopicChangeDto extends ChangeDto {

	record Add(@Positive Long afterId, @NotNull String name)
			implements TopicChangeDto {
		public TopicAction.Create toBdo() {
			return new TopicAction.Create(afterId, name);
		}
	}

	record Move(long topic, @Positive Long afterId)
			implements TopicChangeDto {
		public TopicAction.Move toBdo() {
			return new TopicAction.Move(afterId);
		}
	}

	sealed interface Update extends TopicChangeDto {

		long topic();

		TopicAction.Update toBdo();
	}

	record Rename(long topic, @NotNull @Valid @JsonUnwrapped TextEditDto edit)
			implements TopicChangeDto.Update {
		public TopicAction.UpdateName toBdo() {
			return new TopicAction.UpdateName(edit.toBdo());
		}
	}

	record Describe(long topic, @NotNull @Valid @JsonUnwrapped TextEditDto edit)
			implements TopicChangeDto.Update {
		public TopicAction.UpdateDescription toBdo() {
			return new TopicAction.UpdateDescription(edit.toBdo());
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
