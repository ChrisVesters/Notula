package com.cvesters.notula.organisation;

import static org.assertj.core.api.Assertions.assertThat;

import com.cvesters.notula.organisation.bdo.OrganisationUserAction;
import com.cvesters.notula.test.Matcher;

public final class OrganisationUserActionMatcher {

	private OrganisationUserActionMatcher() {
	}

	public static class Create extends Matcher<OrganisationUserAction.Create> {

		public Create(final OrganisationUserAction.Create expected) {
			super(expected, OrganisationUserAction.Create.class);
		}

		@Override
		public void assertEquals(final OrganisationUserAction.Create actual) {
			assertThat(actual.getEmail()).isEqualTo(expected.getEmail());
			assertThat(actual.getRole()).isEqualTo(expected.getRole());
		}
	}
}
