package com.cvesters.notula.organisation.dto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.cvesters.notula.organisation.TestOrganisation;
import com.cvesters.notula.organisation.TestOrganisationUser;
import com.cvesters.notula.user.TestUser;

class OrganisationUserViewDtoTest {

	private static final TestOrganisationUser ORGANISATION_USER = TestOrganisationUser.SPORER_EDUARDO_CHRISTIANSEN;
	private static final TestOrganisation ORGANISATION = ORGANISATION_USER
			.getOrganisation();
	private static final TestUser USER = ORGANISATION_USER.getUser();

	@Nested
	class Constructor {

		@Test
		void success() {
			final var bdo = ORGANISATION_USER.view();

			final var dto = new OrganisationUserViewDto(bdo);

			assertThat(dto.id()).isEqualTo(ORGANISATION_USER.getId());
			assertThat(dto.organisationId()).isEqualTo(ORGANISATION.getId());
			assertThat(dto.userId()).isEqualTo(USER.getId());
			assertThat(dto.email()).isEqualTo(USER.getEmail().value());
			assertThat(dto.role().role()).isEqualTo("ADMIN");
		}

		@Test
		void organisationNull() {
			assertThatThrownBy(() -> new OrganisationUserViewDto(null))
					.isInstanceOf(NullPointerException.class);
		}
	}
}
