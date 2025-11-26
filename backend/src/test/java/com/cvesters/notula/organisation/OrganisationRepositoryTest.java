package com.cvesters.notula.organisation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.test.context.jdbc.Sql;

import com.cvesters.notula.TestContainerConfig;
import com.cvesters.notula.organisation.bdo.OrganisationInfo;
import com.cvesters.notula.organisation.dao.OrganisationDao;

@Sql({ "/db/organisations.sql" })
@DataJpaTest
@Import(TestContainerConfig.class)
@AutoConfigureTestDatabase(replace = Replace.NONE)
class OrganisationRepositoryTest {

	private static final TestOrganisation ORGANISATION = TestOrganisation.SPORER;

	@Autowired
	private OrganisationRepository organisationRepository;

	@PersistenceContext
	private EntityManager entityManager;

	@Nested
	class Save {

		@Test
		void success() {
			final String name = "test";
			final var bdo = new OrganisationInfo(name);
			final var dao = new OrganisationDao(bdo);
			final OrganisationDao saved = organisationRepository.save(dao);

			assertThat(saved.getId()).isNotNull();
			assertThat(saved.getName()).isEqualTo(name);

			final OrganisationDao found = entityManager.find(OrganisationDao.class,
					saved.getId());
			assertThat(found).isNotNull();
			assertThat(found.getId()).isEqualTo(saved.getId());
			assertThat(found.getName()).isEqualTo(name);
		}

		@Test
		void organisationNull() {
			assertThatThrownBy(() -> organisationRepository.save(null))
					.isInstanceOf(InvalidDataAccessApiUsageException.class);
		}
	}
}
