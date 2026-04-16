package com.cvesters.notula.meeting.bdo;

import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Function;

import org.apache.commons.lang3.Validate;

import lombok.Getter;

public abstract sealed interface MeetingAction {

	@Getter
	public static final class Create implements MeetingAction {
		private final String name;

		public Create(final String name) {
			Validate.notBlank(name);

			this.name = name;
		}
	}

	// TODO: Very likely to be extracted and to be shared with topic and other entities.
	public abstract static sealed class Update implements MeetingAction {

		private final Function<MeetingInfo, String> getter;
		private final BiConsumer<MeetingInfo, String> setter;

		protected Update(final Function<MeetingInfo, String> getter,
				final BiConsumer<MeetingInfo, String> setter) {
			Objects.requireNonNull(getter);
			Objects.requireNonNull(setter);

			this.getter = getter;
			this.setter = setter;
		}

		public void apply(final MeetingInfo meeting) {
			Objects.requireNonNull(meeting);

			final String current = getter.apply(meeting);
			final String updated = execute(current);
			setter.accept(meeting, updated);
		}

		protected abstract String execute(final String input);
	}

	// Should our action also be like this
	// TODO: speciliazed updates would be helpfull, but maybe common logic?
	@Getter
	public static final class UpdateName extends Update {

		private final int position;
		private final int length;
		private final String value;

		public UpdateName(final int offset, final int length,
				final String value) {
			Objects.requireNonNull(value);
			Validate.isTrue(offset >= 0);
			Validate.isTrue(length >= 0);

			super(MeetingInfo::getName, MeetingInfo::setName);

			this.position = offset;
			this.length = length;
			this.value = value;
		}

		@Override
		protected String execute(final String input) {
			Objects.requireNonNull(input);
			Validate.isTrue(input.length() >= position + length);

			final String prefix = input.substring(0, position);
			final String suffix = input.substring(position + length);

			return prefix + value + suffix;
		}
	}

}
