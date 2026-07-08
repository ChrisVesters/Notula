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
			final var action = new OrganisationUserAction.Create(email);

			assertThat(action.getEmail()).isEqualTo(email);
		}

		@Test
		void emailNull() {
			assertThatThrownBy(() -> new OrganisationUserAction.Create(null))
					.isInstanceOf(NullPointerException.class);
		}
	}
}
