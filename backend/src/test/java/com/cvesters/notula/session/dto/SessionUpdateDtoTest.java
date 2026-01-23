package com.cvesters.notula.session.dto;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import com.cvesters.notula.organisation.TestOrganisation;
import com.cvesters.notula.session.TestSession;
import com.cvesters.notula.session.bdo.SessionUpdate;

class SessionUpdateDtoTest {

	@Test
	void toBdo() {
		final TestSession session = TestSession.EDUARDO_CHRISTIANSEN_SPORER;
		final TestOrganisation organisation = TestOrganisation.SPORER;
		final var dto = new SessionUpdateDto(organisation.getId());

		final SessionUpdate bdo = dto.toBdo(session.getId());

		assertThat(bdo.sessionId()).isEqualTo(session.getId());
		assertThat(bdo.organisationId()).isEqualTo(organisation.getId());
	}
}
