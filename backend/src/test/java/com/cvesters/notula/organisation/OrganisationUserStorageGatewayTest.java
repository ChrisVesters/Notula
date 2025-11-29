package com.cvesters.notula.organisation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.cvesters.notula.organisation.bdo.OrganisationUserInfo;
import com.cvesters.notula.organisation.dao.OrganisationUserDao;
import com.cvesters.notula.user.TestUser;

class OrganisationUserStorageGatewayTest {

	private static final TestOrganisationUser ORGANISATION_USER = TestOrganisationUser.SPORER_EDUARDO_CHRISTIANSEN;
	private static final TestOrganisation ORGANISATION = ORGANISATION_USER
			.getOrganisation();
	private static final TestUser USER = ORGANISATION_USER.getUser();

	private final OrganisationUserRepository organisationUserRepository = mock();

	private final OrganisationUserStorageGateway gateway = new OrganisationUserStorageGateway(
			organisationUserRepository);

	@Nested
	class Create {

		@Test
		void success() {
			final OrganisationUserDao created = mock();
			final OrganisationUserInfo bdo = mock();
			when(created.toBdo()).thenReturn(bdo);

			when(organisationUserRepository.save(argThat(dao -> {
				assertThat(dao.getId()).isNull();
				assertThat(dao.getOrganisationId())
						.isEqualTo(ORGANISATION.getId());
				assertThat(dao.getUserId()).isEqualTo(USER.getId());
				return true;
			}))).thenReturn(created);

			final OrganisationUserInfo organisationUserInfo = gateway
					.create(ORGANISATION_USER.info());
			
			assertThat(organisationUserInfo).isEqualTo(bdo);
		}

		@Test
		void organisationUserNull() {
			assertThatThrownBy(() -> gateway.create(null))
					.isInstanceOf(NullPointerException.class);
		}
	}
}
