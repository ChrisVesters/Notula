package com.cvesters.notula.meeting.dao;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.lang.reflect.Field;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.cvesters.notula.meeting.TestMeeting;

class MeetingDaoTest {

	private static final TestMeeting MEETING = TestMeeting.SPORER_PROJECT;

	@Nested
	class Constructor {

		@Test
		void success() {
			final var dao = new MeetingDao(MEETING.info());

			assertThat(dao.getId()).isNull();
			assertThat(dao.getOrganisationId())
					.isEqualTo(MEETING.getOrganisation().getId());
			assertThat(dao.getName()).isEqualTo(MEETING.getName());
		}

		@Test
		void nameNull() {
			assertThatThrownBy(() -> new MeetingDao(null))
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

			final var bdo = dao.toBdo();

			assertThat(bdo.getId()).isEqualTo(MEETING.getId());
			assertThat(bdo.getOrganisationId())
					.isEqualTo(MEETING.getOrganisation().getId());
			assertThat(bdo.getName()).isEqualTo(MEETING.getName());
		}

		@Test
		void idNull() {
			assertThatThrownBy(dao::toBdo)
					.isInstanceOf(IllegalStateException.class);
		}
	}

}
