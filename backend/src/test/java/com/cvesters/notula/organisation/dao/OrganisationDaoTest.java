package com.cvesters.notula.organisation.dao;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.cvesters.notula.organisation.TestOrganisation;

class OrganisationDaoTest {

	private final TestOrganisation ORGANISATION = TestOrganisation.SPORER;

	@Nested
	class Constructor {

		@Test
		void success() {
			final var bdo = ORGANISATION.info();
			final var dao = new OrganisationDao(bdo);

			assertThat(dao.getId()).isNull();
			assertThat(dao.getName()).isEqualTo(ORGANISATION.getName());
		}

		@Test
		void bdoNull() {
			assertThatThrownBy(() -> new OrganisationDao(null))
					.isInstanceOf(NullPointerException.class);
		}
	}

	@Nested
	class ToBdo {

		private final OrganisationDao dao = new OrganisationDao(
				ORGANISATION.info());

		@Test
		void success() throws Exception {
			final var idField = dao.getClass().getDeclaredField("id");
			idField.setAccessible(true);
			idField.set(dao, ORGANISATION.getId());

			final var bdo = dao.toBdo();

			assertThat(bdo.getId()).isEqualTo(ORGANISATION.getId());
			assertThat(bdo.getName()).isEqualTo(ORGANISATION.getName());
		}

		@Test
		void idNull() {
			assertThatThrownBy(dao::toBdo)
					.isInstanceOf(IllegalStateException.class);
		}
	}
}
