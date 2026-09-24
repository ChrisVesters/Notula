package com.cvesters.notula.event.dao;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.cvesters.notula.meeting.TestMeeting;
import com.cvesters.notula.organisation.TestOrganisation;

class EventDaoTest {

	private static final TestMeeting MEETING = TestMeeting.SPORER_PROJECT;
	private static final TestOrganisation ORGANISATION = MEETING
			.getOrganisation();
	private static final long REVISION = 18L;
	private static final String PAYLOAD = """
			{
				"revision": 18,
				"mutation": {
					"type": "REMOVE_TOPIC",
					"topic": 3
				}
			}
			""";

	@Nested
	class Constructor {

		@Test
		void success() {
			final var dao = new EventDao(ORGANISATION.getId(), MEETING.getId(),
					REVISION, PAYLOAD);

			assertThat(dao.getId()).isNull();
			assertThat(dao.getOrganisationId()).isEqualTo(ORGANISATION.getId());
			assertThat(dao.getMeetingId()).isEqualTo(MEETING.getId());
			assertThat(dao.getRevision()).isEqualTo(REVISION);
			assertThat(dao.getPayload()).isEqualTo(PAYLOAD);
		}

		@Test
		void payloadNull() {
			final long organisationId = ORGANISATION.getId();
			final long meetingId = MEETING.getId();

			assertThatThrownBy(() -> new EventDao(organisationId, meetingId,
					REVISION, null)).isInstanceOf(NullPointerException.class);
		}
	}
}
