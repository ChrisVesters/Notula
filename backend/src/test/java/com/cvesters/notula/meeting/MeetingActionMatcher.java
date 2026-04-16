package com.cvesters.notula.meeting;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.argThat;

import com.cvesters.notula.meeting.bdo.MeetingAction;

public class MeetingActionMatcher {

	public static MeetingAction.Create matches(
			final MeetingAction.Create expected) {
		return argThat(actual -> isEqualTo(actual, expected));
	}

	public static boolean isEqualTo(final MeetingAction actual,
			final MeetingAction.Create expected) {
		final var create = isInstanceOf(actual, MeetingAction.Create.class);
		assertThat(create.getName()).isEqualTo(expected.getName());
		return true;
	}

	public static MeetingAction.UpdateName matches(
			final MeetingAction.UpdateName expected) {
		return argThat(actual -> isEqualTo(actual, expected));
	}

	public static boolean isEqualTo(final MeetingAction actual,
			final MeetingAction.UpdateName expected) {
		final var update = isInstanceOf(actual, MeetingAction.UpdateName.class);
		assertThat(update.getPosition()).isEqualTo(expected.getPosition());
		assertThat(update.getLength()).isEqualTo(expected.getLength());
		assertThat(update.getValue()).isEqualTo(expected.getValue());
		return true;
	}

	private static <T extends MeetingAction> T isInstanceOf(
			final MeetingAction actual, final Class<T> expected) {
		assertThat(actual).isExactlyInstanceOf(expected);
		return expected.cast(actual);
	}
}
