package com.cvesters.notula.textblock.dto;

import jakarta.validation.Valid;

import com.cvesters.notula.common.dto.TextUpdateDto;
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
	public abstract static sealed class Update {

		private final long blockId;

		protected Update(final long blockId) {
			this.blockId = blockId;
		}

		public long getBlockId() {
			return blockId;
		}

		public abstract TextBlockAction.Update toBdo();

		public static final class Content extends Update {

			@Valid
			private final TextUpdateDto update;

			public Content(final long blockId, final int position,
					final int length, final String value) {
				super(blockId);

				this.update = new TextUpdateDto(position, length, value);
			}

			public TextBlockAction.Update toBdo() {
				final int position = update.position();
				final int length = update.length();
				final String value = update.value();

				return new TextBlockAction.UpdateContent(position, length,
						value);
			}
		}
	}
}
