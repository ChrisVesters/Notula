package com.cvesters.notula.organisation.dao;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.cvesters.notula.organisation.TestOrganisation;
import com.cvesters.notula.organisation.TestOrganisationUser;
import com.cvesters.notula.user.TestUser;

class OrganisationUserDaoTest {

	private final TestOrganisationUser ORGANISATION_USER = TestOrganisationUser.SPORER_EDUARDO_CHRISTIANSEN;
	private final TestOrganisation ORGANISATION = ORGANISATION_USER
			.getOrganisation();
	private final TestUser USER = ORGANISATION_USER.getUser();

	@Nested
	class Constructor {

		@Test
		void success() {
			final var bdo = ORGANISATION_USER.info();
			final var dao = new OrganisationUserDao(bdo);

			assertThat(dao.getOrganisationId()).isEqualTo(ORGANISATION.getId());
			assertThat(dao.getUserId()).isEqualTo(USER.getId());
		}

		@Test
		void bdoNull() {
			assertThatThrownBy(() -> new OrganisationUserDao(null))
					.isInstanceOf(NullPointerException.class);
		}
	}

	@Nested
	class ToBdo {

		private final OrganisationUserDao dao = new OrganisationUserDao(
				ORGANISATION_USER.info());

		@Test
		void success() throws Exception {
			final var idField = dao.getClass().getDeclaredField("id");
			idField.setAccessible(true);
			idField.set(dao, ORGANISATION_USER.getId());

			final var bdo = dao.toBdo();

			assertThat(bdo.getId()).isEqualTo(ORGANISATION_USER.getId());
			assertThat(bdo.getOrganisationId())
					.isEqualTo(ORGANISATION.getId());
			assertThat(bdo.getUserId()).isEqualTo(USER.getId());
		}

		@Test
		void idNull() {
			assertThatThrownBy(dao::toBdo)
					.isInstanceOf(IllegalStateException.class);
		}
	}
}
