package com.cvesters.notula.organisation.bdo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.cvesters.notula.common.domain.Email;

class OrganisationUserActionTest {

	@Nested
	class Create {

		@Test
		void success() {
			final var email = new Email("user@test");
			final var role = OrganisationUserRole.ADMIN;
			final var action = new OrganisationUserAction.Create(email, role);

			assertThat(action.getEmail()).isEqualTo(email);
			assertThat(action.getRole()).isEqualTo(role);
		}

		@Test
		void emailNull() {
			final Email email = null;
			final var role = OrganisationUserRole.ADMIN;

			assertThatThrownBy(
					() -> new OrganisationUserAction.Create(email, role))
							.isInstanceOf(NullPointerException.class);
		}

		@Test
		void roleNull() {
			final var email = new Email("user@test");
			final OrganisationUserRole role = null;

			assertThatThrownBy(
					() -> new OrganisationUserAction.Create(email, role))
							.isInstanceOf(NullPointerException.class);
		}
	}
}
