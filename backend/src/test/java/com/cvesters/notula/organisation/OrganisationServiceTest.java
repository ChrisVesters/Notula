package com.cvesters.notula.organisation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.cvesters.notula.organisation.bdo.OrganisationInfo;

class OrganisationServiceTest {

	private static final TestOrganisation ORGANISATION = TestOrganisation.SPORER;

	private final OrganisationStorageGateway organisationStorageGateway = mock();

	private final OrganisationService organisationService = new OrganisationService(
			organisationStorageGateway);

	@Nested
	class Create {

		@Test
		void success() {
			final OrganisationInfo organisation = new OrganisationInfo(ORGANISATION.getName());

			final OrganisationInfo created = ORGANISATION.info();
			when(organisationStorageGateway.create(organisation))
					.thenReturn(created);

			final OrganisationInfo result = organisationService.create(organisation);

			assertThat(created).isEqualTo(result);
		}

		@Test
		void organisationNull() {
			assertThatThrownBy(() -> organisationService.create(null))
					.isInstanceOf(NullPointerException.class);
		}
	}
}
