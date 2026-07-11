package com.cvesters.notula.session;

import static org.assertj.core.api.Assertions.assertThat;

import com.cvesters.notula.test.Matcher;
import com.cvesters.notula.session.bdo.SessionAction;

public final class SessionActionMatcher {

	private SessionActionMatcher() {
	}

	public static class Create extends Matcher<SessionAction.Create> {

		public Create(final SessionAction.Create expected) {
			super(expected, SessionAction.Create.class);
		}

		@Override
		public void assertEquals(final SessionAction.Create actual) {
			assertThat(actual.getEmail()).isEqualTo(expected.getEmail());
			assertThat(actual.getPassword()).isEqualTo(expected.getPassword());
		}
	}
}
