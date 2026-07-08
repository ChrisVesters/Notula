package com.cvesters.notula.organisation.dto;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.cvesters.notula.organisation.bdo.OrganisationUserAction;
import com.cvesters.notula.user.TestUser;

class OrganisationUserActionDtoTest {

	private static final TestUser USER = TestUser.DAPHNEE_LESCH;

	@Nested
	class Create {

		@Test
		void toBdo() {
			final var dto = new OrganisationUserActionDto.Create(
					USER.getEmail().value());
			final OrganisationUserAction.Create bdo = dto.toBdo();

			assertThat(bdo.getEmail()).isEqualTo(USER.getEmail());
		}
	}
}
