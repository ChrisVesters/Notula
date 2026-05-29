package com.cvesters.notula.textblock.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import com.cvesters.notula.textblock.bdo.TextBlockAction;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

public final class TextBlockActionDto {

	private TextBlockActionDto() {
	}

	@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "action")
	@JsonSubTypes({
			@Type(value = Update.Content.class, name = "UPDATE_CONTENT") })
	public sealed interface Update {

		TextBlockAction.Update toBdo();

		public static record Content(@PositiveOrZero int position,
				@PositiveOrZero int length, @NotNull String value)
				implements Update {

			public TextBlockAction.Update toBdo() {
				return new TextBlockAction.UpdateContent(position, length,
						value);
			}
		}
	}
}
