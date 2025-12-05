package com.cvesters.notula.common.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import org.junit.jupiter.api.Nested;

import com.cvesters.notula.organisation.TestOrganisationUser;
import com.cvesters.notula.user.TestUser;

class PrincipalTest {

	private static final TestOrganisationUser ORGANISATION_USER = TestOrganisationUser.SPORER_EDUARDO_CHRISTIANSEN;
	private static final TestUser USER = ORGANISATION_USER.getUser();

	@Nested
	class Constructor {

		@Test
		void userOnly() {
			final long userId = USER.getId();

			final var principal = new Principal(userId);

			assertThat(principal.userId()).isEqualTo(userId);
		}
	}
	
}
