package com.cvesters.notula.organisation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.atIndex;

import java.util.List;

import org.assertj.core.api.Condition;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.test.context.jdbc.Sql;

import com.cvesters.notula.organisation.bdo.OrganisationUserInfo;
import com.cvesters.notula.organisation.bdo.OrganisationUserRole;
import com.cvesters.notula.organisation.bdo.OrganisationUserView;
import com.cvesters.notula.organisation.dao.OrganisationUserDao;
import com.cvesters.notula.test.RepositoryTest;
import com.cvesters.notula.user.TestUser;

@Sql({ "/db/users.sql", "/db/organisations.sql", "/db/organisation_users.sql" })
public class OrganisationUserRepositoryTest extends RepositoryTest {

	private static final TestOrganisationUser ORGANISATION_USER = TestOrganisationUser.SPORER_EDUARDO_CHRISTIANSEN;
	private static final TestOrganisation ORGANISATION = ORGANISATION_USER
			.getOrganisation();
	private static final TestUser USER = ORGANISATION_USER.getUser();

	@Autowired
	private OrganisationUserRepository organisationUserRepository;

	@Nested
	class FindAllByUserId {

		@Test
		void single() {
			final var orgUser = TestOrganisationUser.SPORER_EDUARDO_CHRISTIANSEN;
			final var org = orgUser.getOrganisation();
			final var user = orgUser.getUser();
			final var userId = user.getId();

			final var result = organisationUserRepository
					.findAllByUserId(userId);

			assertThat(result).hasSize(1).anySatisfy(actual -> {
				assertThat(actual.getId()).isEqualTo(orgUser.getId());
				assertThat(actual.getOrganisationId()).isEqualTo(org.getId());
				assertThat(actual.getUserId()).isEqualTo(userId);
				assertThat(actual.getRole()).isEqualTo(orgUser.getRole());
			});
		}

		@Test
		void multiple() {
			final var userId = TestUser.ALISON_DACH.getId();

			final var result = organisationUserRepository
					.findAllByUserId(userId);

			assertThat(result).hasSize(2).anySatisfy(orgUser -> {
				final var expectedOrgUser = TestOrganisationUser.GLOVER_ALISON_DACH;
				final var expectedOrg = expectedOrgUser.getOrganisation();
				final var expectedUser = expectedOrgUser.getUser();

				assertThat(orgUser.getId()).isEqualTo(expectedOrgUser.getId());
				assertThat(orgUser.getOrganisationId())
						.isEqualTo(expectedOrg.getId());
				assertThat(orgUser.getUserId()).isEqualTo(expectedUser.getId());
				assertThat(orgUser.getRole())
						.isEqualTo(expectedOrgUser.getRole());
			}).anySatisfy(orgUser -> {
				final var expectedOrgUser = TestOrganisationUser.HEUL_ALISON_DACH;
				final var expectedOrg = expectedOrgUser.getOrganisation();
				final var expectedUser = expectedOrgUser.getUser();

				assertThat(orgUser.getId()).isEqualTo(expectedOrgUser.getId());
				assertThat(orgUser.getOrganisationId())
						.isEqualTo(expectedOrg.getId());
				assertThat(orgUser.getUserId()).isEqualTo(expectedUser.getId());
				assertThat(orgUser.getRole())
						.isEqualTo(expectedOrgUser.getRole());
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
	class FindAllByOrganisationId {

		@Test
		void single() {
			final var orgId = TestOrganisation.HEUL.getId();
			final var user = TestOrganisationUser.HEUL_ALISON_DACH;

			final var result = organisationUserRepository
					.findAllByOrganisationId(orgId);

			assertThat(result).hasSize(1).is(equalTo(user), atIndex(0));
		}

		@Test
		void multiple() {
			final var org = TestOrganisation.SPORER;
			final var orgId = org.getId();
			final List<TestOrganisationUser> users = TestOrganisationUser
					.ofOrganisation(org);

			final var result = organisationUserRepository
					.findAllByOrganisationId(orgId);

			assertThat(result).hasSize(users.size());

			for (int index = 0; index < users.size(); index++) {
				final TestOrganisationUser expectedAtIndex = users.get(index);
				final OrganisationUserView actualAtIndex = result.get(index);

				assertThat(actualAtIndex).is(equalTo(expectedAtIndex));
			}
		}

		@Test
		void none() {
			final var orgId = Long.MAX_VALUE;

			final var organisationUsers = organisationUserRepository
					.findAllByOrganisationId(orgId);

			assertThat(organisationUsers).isEmpty();
		}
	}

	@Nested
	class FindByUserIdAndOrganisationId {

		@Test
		void found() {
			final var result = organisationUserRepository
					.findByUserIdAndOrganisationId(USER.getId(),
							ORGANISATION.getId());

			assertThat(result).hasValueSatisfying(orgUser -> {
				assertThat(orgUser.getId())
						.isEqualTo(ORGANISATION_USER.getId());
				assertThat(orgUser.getOrganisationId())
						.isEqualTo(ORGANISATION.getId());
				assertThat(orgUser.getUserId()).isEqualTo(USER.getId());
				assertThat(orgUser.getRole())
						.isEqualTo(ORGANISATION_USER.getRole());
			});
		}

		@Test
		void notFound() {
			final var result = organisationUserRepository
					.findByUserIdAndOrganisationId(Long.MAX_VALUE,
							Long.MAX_VALUE);

			assertThat(result).isEmpty();
		}
	}

	@Nested
	class Save {

		@Test
		void success() {
			final var user = TestUser.DAPHNEE_LESCH;
			final var role = OrganisationUserRole.MEMBER;

			final var bdo = new OrganisationUserInfo(ORGANISATION.getId(),
					user.getId(), role);
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
			assertThat(found.getRole()).isEqualTo(role);
		}

		@Test
		void organisationUserNull() {
			assertThatThrownBy(() -> organisationUserRepository.save(null))
					.isInstanceOf(InvalidDataAccessApiUsageException.class);
		}
	}

	private Condition<OrganisationUserView> equalTo(
			final TestOrganisationUser expected) {
		return new Condition<>(actual -> {
			assertThat(actual.getId()).isEqualTo(expected.getId());
			assertThat(actual.getOrganisationId())
					.isEqualTo(expected.getOrganisation().getId());
			assertThat(actual.getUserId())
					.isEqualTo(expected.getUser().getId());
			assertThat(actual.getEmail())
					.isEqualTo(expected.getUser().getEmail().value());
			assertThat(actual.getRole()).isEqualTo(expected.getRole());
			return true;
		}, "equal");
	}

}
