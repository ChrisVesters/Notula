package com.cvesters.notula.organisation.dto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.cvesters.notula.common.exception.InvalidActionException;
import com.cvesters.notula.organisation.bdo.OrganisationUserRole;

class OrganisationUserRoleDtoTest {

	@Nested
	class Constructor {

		@Test
		void admin() {
			final var dto = new OrganisationUserRoleDto(
					OrganisationUserRole.ADMIN);

			assertThat(dto.role()).isEqualTo("ADMIN");
		}

		@Test
		void member() {
			final var dto = new OrganisationUserRoleDto(
					OrganisationUserRole.MEMBER);

			assertThat(dto.role()).isEqualTo("MEMBER");
		}

		@Test
		void roleNull() {
			final OrganisationUserRole role = null;
			assertThatThrownBy(() -> new OrganisationUserRoleDto(role))
					.isInstanceOf(NullPointerException.class);
		}

		@Test
		void valueNull() {
			final String value = null;

			assertThatThrownBy(() -> new OrganisationUserRoleDto(value))
					.isInstanceOf(NullPointerException.class);
		}

		@Test
		void valueInvalid() {
			final var roleDto = new OrganisationUserRoleDto("invalid");

			assertThat(roleDto.role()).isEqualTo("invalid");
		}
	}

	@Nested
	class ToBdo {

		@Test
		void enumConstructed() {
			final var dto = new OrganisationUserRoleDto(
					OrganisationUserRole.ADMIN);

			assertThat(dto.toBdo()).isEqualTo(OrganisationUserRole.ADMIN);
		}

		@Test
		void admin() {
			final var dto = new OrganisationUserRoleDto("ADMIN");

			assertThat(dto.toBdo()).isEqualTo(OrganisationUserRole.ADMIN);
		}

		@Test
		void member() {
			final var dto = new OrganisationUserRoleDto("MEMBER");

			assertThat(dto.toBdo()).isEqualTo(OrganisationUserRole.MEMBER);
		}

		@Test
		void roleInvalid() {
			final var dto = new OrganisationUserRoleDto("invalid");

			assertThatThrownBy(dto::toBdo)
					.isInstanceOf(InvalidActionException.class);
		}
	}
}
