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
	class Save {

		@Test
		void success() {
			final TestUser USER = TestUser.JUDY_HARBER;
			final var bdo = new OrganisationUserInfo(ORGANISATION.getId(),
					USER.getId());
			final var dao = new OrganisationUserDao(bdo);

			final OrganisationUserDao saved = organisationUserRepository
					.save(dao);

			assertThat(saved.getOrganisationId())
					.isEqualTo(ORGANISATION.getId());
			assertThat(saved.getUserId()).isEqualTo(USER.getId());

			final OrganisationUserDao found = entityManager
					.find(OrganisationUserDao.class, saved.getId());
			assertThat(found).isNotNull();
			assertThat(found.getId()).isEqualTo(saved.getId());
			assertThat(found.getOrganisationId())
					.isEqualTo(ORGANISATION.getId());
			assertThat(found.getUserId()).isEqualTo(USER.getId());
		}

		@Test
		void organisationUserNull() {
			assertThatThrownBy(() -> organisationUserRepository.save(null))
					.isInstanceOf(InvalidDataAccessApiUsageException.class);
		}
	}

}
