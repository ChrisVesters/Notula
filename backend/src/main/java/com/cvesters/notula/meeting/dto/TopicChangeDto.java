package com.cvesters.notula.meeting.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

import com.fasterxml.jackson.annotation.JsonUnwrapped;

import com.cvesters.notula.common.domain.Minutes;
import com.cvesters.notula.common.domain.Splice;
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

	record Rename(long topic, @PositiveOrZero long base,
			@NotNull @Valid @JsonUnwrapped TextEditDto edit)
			implements TopicChangeDto, TextChangeDto {
		public TopicAction.UpdateName toBdo(final Splice rebased) {
			return new TopicAction.UpdateName(rebased);
		}
	}

	record Describe(long topic, @PositiveOrZero long base,
			@NotNull @Valid @JsonUnwrapped TextEditDto edit)
			implements TopicChangeDto, TextChangeDto {
		public TopicAction.UpdateDescription toBdo(final Splice rebased) {
			return new TopicAction.UpdateDescription(rebased);
		}
	}

	record Schedule(long topic, @PositiveOrZero Integer minutes)
			implements TopicChangeDto {
		public TopicAction.UpdateDuration toBdo() {
			return new TopicAction.UpdateDuration(
					minutes == null ? null : new Minutes(minutes));
		}
	}

	record Remove(long topic) implements TopicChangeDto {
	}
}
