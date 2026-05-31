package com.cvesters.notula.meeting;

import static org.assertj.core.api.Assertions.assertThat;

import com.cvesters.notula.meeting.bdo.MeetingAction;
import com.cvesters.notula.test.Matcher;

public final class MeetingActionMatcher {

	private MeetingActionMatcher() {
	}

	public static class Create extends Matcher<MeetingAction.Create> {

		public Create(final MeetingAction.Create expected) {
			super(expected, MeetingAction.Create.class);
		}

		@Override
		public void assertEquals(final MeetingAction.Create actual) {
			assertThat(actual.getName()).isEqualTo(expected.getName());
		}
	}

	// TODO: Can be made more generic.
	public static class UpdateName extends Matcher<MeetingAction.UpdateName> {

		public UpdateName(final MeetingAction.UpdateName expected) {
			super(expected, MeetingAction.UpdateName.class);
		}

		public void assertEquals(final MeetingAction.UpdateName actual) {
			assertThat(actual.getPosition()).isEqualTo(expected.getPosition());
			assertThat(actual.getLength()).isEqualTo(expected.getLength());
			assertThat(actual.getValue()).isEqualTo(expected.getValue());
		}
	}

	public static class UpdateDescription
			extends Matcher<MeetingAction.UpdateDescription> {

		public UpdateDescription(final MeetingAction.UpdateDescription expected) {
			super(expected, MeetingAction.UpdateDescription.class);
		}

		public void assertEquals(final MeetingAction.UpdateDescription actual) {
			assertThat(actual.getPosition()).isEqualTo(expected.getPosition());
			assertThat(actual.getLength()).isEqualTo(expected.getLength());
			assertThat(actual.getValue()).isEqualTo(expected.getValue());
		}
	}
}
