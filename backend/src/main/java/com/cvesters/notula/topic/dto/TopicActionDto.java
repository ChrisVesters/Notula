package com.cvesters.notula.topic.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;

import com.cvesters.notula.topic.bdo.TopicAction;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

public final class TopicActionDto {

	private TopicActionDto() {
	}

	public record Create(@NotBlank String name) {

		public TopicAction.Create toBdo() {
			return new TopicAction.Create(name);
		}
	}
	
	@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "action")
	@JsonSubTypes({ @Type(value = Update.Name.class, name = "UPDATE_NAME") })
	public sealed interface Update {

		TopicAction.Update toBdo();

		public static record Name(@PositiveOrZero int position,
				@PositiveOrZero int length, String value)
				implements Update {

			public TopicAction.Update toBdo() {
				return new TopicAction.UpdateName(position, length, value);
			}
		}

	}

}
