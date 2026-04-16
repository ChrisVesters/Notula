package com.cvesters.notula.meeting.dao;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.lang.reflect.Field;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.cvesters.notula.meeting.TestMeeting;
import com.cvesters.notula.meeting.bdo.MeetingInfo;
import com.cvesters.notula.organisation.TestOrganisation;

class MeetingDaoTest {

	private static final TestMeeting MEETING = TestMeeting.SPORER_PROJECT;
	private static final TestOrganisation ORGANISATION = MEETING
			.getOrganisation();

	@Nested
	class Constructor {

		@Test
		void success() {
			final var dao = new MeetingDao(MEETING.info());

			assertThat(dao.getId()).isNull();
			assertThat(dao.getOrganisationId()).isEqualTo(ORGANISATION.getId());
			assertThat(dao.getName()).isEqualTo(MEETING.getName());
		}

		@Test
		void bdoNull() {
			assertThatThrownBy(() -> new MeetingDao(null))
					.isInstanceOf(NullPointerException.class);
		}
	}

	@Nested
	class Update {

		private final MeetingDao dao = new MeetingDao(MEETING.info());

		@Test
		void success() {
			final String name = "Updated";
			final MeetingInfo bdo = new MeetingInfo(ORGANISATION.getId(), name);
			dao.update(bdo);

			assertThat(dao.getId()).isNull();
			assertThat(dao.getOrganisationId()).isEqualTo(ORGANISATION.getId());
			assertThat(dao.getName()).isEqualTo(name);
		}

		@Test
		void bdoNull() {
			assertThatThrownBy(() -> dao.update(null))
					.isInstanceOf(NullPointerException.class);
		}
	}

	@Nested
	class ToBdo {

		private final MeetingDao dao = new MeetingDao(MEETING.info());

		@Test
		void success() throws Exception {
			final Field idField = dao.getClass().getDeclaredField("id");
			idField.setAccessible(true);
			idField.set(dao, MEETING.getId());

			final MeetingInfo bdo = dao.toBdo();

			assertThat(bdo.getId()).isEqualTo(MEETING.getId());
			assertThat(bdo.getOrganisationId()).isEqualTo(ORGANISATION.getId());
			assertThat(bdo.getName()).isEqualTo(MEETING.getName());
		}

		@Test
		void idNull() {
			assertThatThrownBy(dao::toBdo)
					.isInstanceOf(IllegalStateException.class);
		}
	}

}
