package com.cvesters.notula.organisation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Collections;
import java.util.List;

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

	@Autowired
	private OrganisationRepository organisationRepository;

	@PersistenceContext
	private EntityManager entityManager;

	@Nested
	class FindAllById {

		@Test
		void single() {
			final List<Long> ids = List.of(TestOrganisation.SPORER.getId());

			final List<OrganisationDao> result = organisationRepository
					.findAllById(ids);

			assertThat(result).hasSize(1).anySatisfy(org -> {
				final var expectedOrg = TestOrganisation.SPORER;
				assertThat(org.getId()).isEqualTo(expectedOrg.getId());
				assertThat(org.getName()).isEqualTo(expectedOrg.getName());
			});
		}

		@Test
		void multiple() {
			final List<Long> ids = List.of(TestOrganisation.SPORER.getId(),
					TestOrganisation.GLOVER.getId());

			final List<OrganisationDao> result = organisationRepository
					.findAllById(ids);

			assertThat(result).hasSize(2).anySatisfy(org -> {
				final var expectedOrg = TestOrganisation.SPORER;
				assertThat(org.getId()).isEqualTo(expectedOrg.getId());
				assertThat(org.getName()).isEqualTo(expectedOrg.getName());
			}).anySatisfy(org -> {
				final var expectedOrg = TestOrganisation.GLOVER;
				assertThat(org.getId()).isEqualTo(expectedOrg.getId());
				assertThat(org.getName()).isEqualTo(expectedOrg.getName());
			});
		}

		@Test
		void notFound() {
			final List<Long> ids = List.of(Long.MAX_VALUE);

			final List<OrganisationDao> result = organisationRepository
					.findAllById(ids);

			assertThat(result).isEmpty();
		}

		@Test
		void someNotFound() {
			final List<Long> ids = List.of(TestOrganisation.SPORER.getId(),
					Long.MAX_VALUE, TestOrganisation.GLOVER.getId());

			final List<OrganisationDao> result = organisationRepository
					.findAllById(ids);

			assertThat(result).hasSize(2).anySatisfy(org -> {
				final var expectedOrg = TestOrganisation.SPORER;
				assertThat(org.getId()).isEqualTo(expectedOrg.getId());
				assertThat(org.getName()).isEqualTo(expectedOrg.getName());
			}).anySatisfy(org -> {
				final var expectedOrg = TestOrganisation.GLOVER;
				assertThat(org.getId()).isEqualTo(expectedOrg.getId());
				assertThat(org.getName()).isEqualTo(expectedOrg.getName());
			});
		}

		@Test
		void idsEmpty() {
			final List<Long> ids = Collections.emptyList();

			final List<OrganisationDao> result = organisationRepository
					.findAllById(ids);

			assertThat(result).isEmpty();
		}

		@Test
		void idsNull() {
			assertThatThrownBy(() -> organisationRepository.findAllById(null))
					.isInstanceOf(InvalidDataAccessApiUsageException.class);
		}
	}

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

			final OrganisationDao found = entityManager
					.find(OrganisationDao.class, saved.getId());
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
