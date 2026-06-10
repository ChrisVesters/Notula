package com.cvesters.notula.meeting.bdo;

import java.util.Objects;

import lombok.Getter;

import com.cvesters.notula.common.domain.TextUpdate;

public sealed interface MeetingAction {

	@Getter
	final class Create implements MeetingAction {
		private final String name;

		public Create(final String name) {
			Objects.requireNonNull(name);

			this.name = name;
		}
	}

	sealed interface Update extends MeetingAction {

		void apply(final MeetingInfo object);
	}

	@Getter
	final class UpdateName extends TextUpdate<MeetingInfo>
			implements MeetingAction.Update {

		public UpdateName(final int position, final int length,
				final String value) {
			super(MeetingInfo::getName, MeetingInfo::setName, position, length,
					value);
		}
	}

	@Getter
	final class UpdateDescription extends TextUpdate<MeetingInfo>
			implements MeetingAction.Update {

		public UpdateDescription(final int position, final int length,
				final String value) {
			super(MeetingInfo::getDescription, MeetingInfo::setDescription,
					position, length, value);
		}
	}

	@Getter
	final class Delete implements MeetingAction {
	}

}
