package com.cvesters.notula.credential;

import static org.assertj.core.api.Assertions.assertThat;

import com.cvesters.notula.credential.bdo.CredentialAction;
import com.cvesters.notula.test.Matcher;

public final class CredentialActionMatcher {

	private CredentialActionMatcher() {
	}

	public static class Create extends Matcher<CredentialAction.Create> {

		public Create(final CredentialAction.Create expected) {
			super(expected, CredentialAction.Create.class);
		}

		@Override
		public void assertEquals(final CredentialAction.Create actual) {
			assertThat(actual.getUserId()).isEqualTo(expected.getUserId());
			assertThat(actual.getPassword()).isEqualTo(expected.getPassword());
		}
	}

	public static class Login extends Matcher<CredentialAction.Login> {

		public Login(final CredentialAction.Login expected) {
			super(expected, CredentialAction.Login.class);
		}

		@Override
		public void assertEquals(final CredentialAction.Login actual) {
			assertThat(actual.getUserId()).isEqualTo(expected.getUserId());
			assertThat(actual.getPassword()).isEqualTo(expected.getPassword());
		}
	}
}
