package com.cvesters.notula.organisation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.test.context.jdbc.Sql;

import com.cvesters.notula.organisation.bdo.OrganisationUserInfo;
import com.cvesters.notula.organisation.dao.OrganisationUserDao;
import com.cvesters.notula.test.RepositoryTest;
import com.cvesters.notula.user.TestUser;

@Sql({ "/db/users.sql", "/db/organisations.sql", "/db/organisation_users.sql" })
public class OrganisationUserRepositoryTest extends RepositoryTest {

	private static final TestOrganisationUser ORGANISATION_USER = TestOrganisationUser.SPORER_EDUARDO_CHRISTIANSEN;
	private static final TestOrganisation ORGANISATION = ORGANISATION_USER
			.getOrganisation();

	@Autowired
	private OrganisationUserRepository organisationUserRepository;

	@Nested
	class FindAllByUserId {

		@Test
		void single() {
			final var userId = TestUser.EDUARDO_CHRISTIANSEN.getId();

			final var result = organisationUserRepository
					.findAllByUserId(userId);

			assertThat(result).hasSize(1).anySatisfy(orgUser -> {
				assertThat(orgUser.getId()).isEqualTo(
						TestOrganisationUser.SPORER_EDUARDO_CHRISTIANSEN
								.getId());
				assertThat(orgUser.getOrganisationId())
						.isEqualTo(TestOrganisation.SPORER.getId());
				assertThat(orgUser.getUserId()).isEqualTo(userId);
			});
		}

		@Test
		void multiple() {
			final var userId = TestUser.ALISON_DACH.getId();

			final var result = organisationUserRepository
					.findAllByUserId(userId);

			assertThat(result).hasSize(2).anySatisfy(orgUser -> {
				assertThat(orgUser.getId()).isEqualTo(
						TestOrganisationUser.GLOVER_ALISON_DACH.getId());
				assertThat(orgUser.getOrganisationId())
						.isEqualTo(TestOrganisation.GLOVER.getId());
				assertThat(orgUser.getUserId()).isEqualTo(userId);
			}).anySatisfy(orgUser -> {
				assertThat(orgUser.getId()).isEqualTo(
						TestOrganisationUser.HEUL_ALISON_DACH.getId());
				assertThat(orgUser.getOrganisationId())
						.isEqualTo(TestOrganisation.HEUL.getId());
				assertThat(orgUser.getUserId()).isEqualTo(userId);
			});
		}

		@Test
		void none() {
			final var userId = Long.MAX_VALUE;

			final var organisationUsers = organisationUserRepository
					.findAllByUserId(userId);

			assertThat(organisationUsers).isEmpty();
		}
	}

	@Nested
	class Save {

		@Test
		void success() {
			final var user = TestUser.DAPHNEE_LESCH;
			final var bdo = new OrganisationUserInfo(ORGANISATION.getId(),
					user.getId());
			final var dao = new OrganisationUserDao(bdo);

			final OrganisationUserDao saved = organisationUserRepository
					.save(dao);

			assertThat(saved.getOrganisationId())
					.isEqualTo(ORGANISATION.getId());
			assertThat(saved.getUserId()).isEqualTo(user.getId());

			final OrganisationUserDao found = entityManager
					.find(OrganisationUserDao.class, saved.getId());
			assertThat(found).isNotNull();
			assertThat(found.getId()).isEqualTo(saved.getId());
			assertThat(found.getOrganisationId())
					.isEqualTo(ORGANISATION.getId());
			assertThat(found.getUserId()).isEqualTo(user.getId());
		}

		@Test
		void organisationUserNull() {
			assertThatThrownBy(() -> organisationUserRepository.save(null))
					.isInstanceOf(InvalidDataAccessApiUsageException.class);
		}
	}

}
