package com.cvesters.notula.organisation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
	class FindAllById {

		@Test
		void single() {
			final List<TestOrganisation> found = List
					.of(TestOrganisation.SPORER);

			final var ids = new ArrayList<Long>();
			final var daos = new ArrayList<OrganisationDao>();
			final var bdos = new ArrayList<OrganisationInfo>();
			for (final TestOrganisation org : found) {
				ids.add(org.getId());
				final OrganisationDao dao = mock();
				final OrganisationInfo bdo = mock();
				when(dao.toBdo()).thenReturn(bdo);

				daos.add(dao);
				bdos.add(bdo);
			}

			when(organisationRepository.findAllById(ids)).thenReturn(daos);

			final List<OrganisationInfo> result = gateway.findAllById(ids);

			assertThat(result).isEqualTo(bdos);
		}

		@Test
		void multiple() {
			final List<TestOrganisation> found = List
					.of(TestOrganisation.SPORER, TestOrganisation.GLOVER);

			final var ids = new ArrayList<Long>();
			final var daos = new ArrayList<OrganisationDao>();
			final var bdos = new ArrayList<OrganisationInfo>();
			for (final TestOrganisation org : found) {
				ids.add(org.getId());
				final OrganisationDao dao = mock();
				final OrganisationInfo bdo = mock();
				when(dao.toBdo()).thenReturn(bdo);

				daos.add(dao);
				bdos.add(bdo);
			}

			when(organisationRepository.findAllById(ids)).thenReturn(daos);

			final List<OrganisationInfo> result = gateway.findAllById(ids);

			assertThat(result).isEqualTo(bdos);
		}

		@Test
		void notFound() {
			final List<Long> ids = List.of(Long.MAX_VALUE);

			when(organisationRepository.findAllById(ids))
					.thenReturn(Collections.emptyList());

			final List<OrganisationInfo> result = gateway.findAllById(ids);

			assertThat(result).isEmpty();
		}

		@Test
		void someNotFound() {
			final List<TestOrganisation> found = List
					.of(TestOrganisation.SPORER, TestOrganisation.SPORER);

			final var ids = new ArrayList<Long>();
			final var daos = new ArrayList<OrganisationDao>();
			final var bdos = new ArrayList<OrganisationInfo>();
			for (final TestOrganisation org : found) {
				ids.add(org.getId());
				final OrganisationDao dao = mock();
				final OrganisationInfo bdo = mock();
				when(dao.toBdo()).thenReturn(bdo);

				daos.add(dao);
				bdos.add(bdo);
			}

			ids.add(Long.MAX_VALUE);

			when(organisationRepository.findAllById(ids)).thenReturn(daos);

			final List<OrganisationInfo> result = gateway.findAllById(ids);

			assertThat(result).isEqualTo(bdos);
		}

		@Test
		void idsEmpty() {
			final List<Long> ids = Collections.emptyList();

			when(organisationRepository.findAllById(ids))
					.thenReturn(Collections.emptyList());

			final List<OrganisationInfo> result = gateway.findAllById(ids);

			assertThat(result).isEmpty();
		}

		@Test
		void idsNull() {
			assertThatThrownBy(() -> gateway.findAllById(null))
					.isInstanceOf(NullPointerException.class);
		}
	}

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
