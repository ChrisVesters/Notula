package com.cvesters.notula.textblock.dto;

import java.util.Objects;

import lombok.Getter;

import com.cvesters.notula.textblock.bdo.TextBlockAction;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "action")
@JsonSubTypes({
		@Type(value = TextBlockMutationDto.UpdateContent.class, name = "UPDATE_CONTENT") })
public sealed interface TextBlockMutationDto {

	static TextBlockMutationDto of(final TextBlockAction action) {
		Objects.requireNonNull(action);

		return switch (action) {
			case TextBlockAction.UpdateContent updateContent -> new UpdateContent(
					updateContent);
		};
	}

	@Getter
	final class UpdateContent implements TextBlockMutationDto {

		private final int position;
		private final int length;
		private final String value;

		private UpdateContent(final TextBlockAction.UpdateContent action) {
			this.position = action.getPosition();
			this.length = action.getLength();
			this.value = action.getValue();
		}
	}
}