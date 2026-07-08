package com.cvesters.notula.organisation.dto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.cvesters.notula.organisation.TestOrganisationUser;

class OrganisationUserInfoDtoTest {

	private static final TestOrganisationUser ORG_USER = TestOrganisationUser.SPORER_EDUARDO_CHRISTIANSEN;

	@Nested
	class Constructor {
		@Test
		void success() {
			final var bdo = ORG_USER.info();

			final var dto = new OrganisationUserInfoDto(bdo);

			assertThat(dto.id()).isEqualTo(ORG_USER.getId());
			assertThat(dto.organisationId())
					.isEqualTo(ORG_USER.getOrganisation().getId());
			assertThat(dto.userId()).isEqualTo(ORG_USER.getUser().getId());
		}

		@Test
		void organisationUserNull() {
			assertThatThrownBy(() -> new OrganisationInfoDto(null))
					.isInstanceOf(NullPointerException.class);
		}
	}
}
