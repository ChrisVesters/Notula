package com.cvesters.notula.common.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.cvesters.notula.organisation.TestOrganisation;
import com.cvesters.notula.organisation.TestOrganisationUser;
import com.cvesters.notula.user.TestUser;

class PrincipalTest {

	private static final TestOrganisationUser ORGANISATION_USER = TestOrganisationUser.SPORER_EDUARDO_CHRISTIANSEN;
	private static final TestUser USER = ORGANISATION_USER.getUser();
	private static final TestOrganisation ORGANISATION = ORGANISATION_USER
			.getOrganisation();

	@Nested
	class Constructor {

		@Test
		void userOnly() {
			final var principal = new Principal(USER.getId());

			assertThat(principal.userId()).isEqualTo(USER.getId());
			assertThatThrownBy(principal::organisationId)
					.isInstanceOf(IllegalStateException.class);
		}

		@Test
		void withOrganisation() {
			final var principal = new Principal(USER.getId(),
					ORGANISATION.getId());

			assertThat(principal.userId()).isEqualTo(USER.getId());
			assertThat(principal.organisationId())
					.isEqualTo(ORGANISATION.getId());
		}
	}

}
