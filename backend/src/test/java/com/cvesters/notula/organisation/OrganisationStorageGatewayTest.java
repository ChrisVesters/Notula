package com.cvesters.notula.organisation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.cvesters.notula.organisation.bdo.OrganisationInfo;
import com.cvesters.notula.organisation.dao.OrganisationDao;

class OrganisationStorageGatewayTest {

	private static final TestOrganisation ORGANISATION = TestOrganisation.SPORER;

	private final OrganisationRepository organisationRepository = mock();

	private final OrganisationStorageGateway gateway = new OrganisationStorageGateway(
			organisationRepository);

	@Nested
	class Create {

		@Test
		void success() {
			final OrganisationDao created = mock();
			final OrganisationInfo bdo = mock();
			when(created.toBdo()).thenReturn(bdo);

			when(organisationRepository.save(argThat(dao -> {
				assertThat(dao.getId()).isNull();
				assertThat(dao.getName()).isEqualTo(ORGANISATION.getName());
				return true;
			}))).thenReturn(created);

			final OrganisationInfo organisationInfo = gateway
					.create(ORGANISATION.info());

			assertThat(organisationInfo).isEqualTo(bdo);
		}

		@Test
		void organisationNull() {
			assertThatThrownBy(() -> gateway.create(null))
					.isInstanceOf(NullPointerException.class);
		}
	}
}
