package com.cvesters.notula.block.dao;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.lang.reflect.Field;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.cvesters.notula.block.TestBlock;
import com.cvesters.notula.block.bdo.BlockInfo;
import com.cvesters.notula.meeting.TestMeeting;
import com.cvesters.notula.organisation.TestOrganisation;
import com.cvesters.notula.topic.TestTopic;

class BlockDaoTest {

	private static final TestBlock BLOCK = TestBlock.SPORER_PROJECT_BLOCKERS_FIRST;
	private static final TestTopic TOPIC = BLOCK.getTopic();
	private static final TestMeeting MEETING = TOPIC.getMeeting();
	private static final TestOrganisation ORGANISATION = MEETING
			.getOrganisation();

	@Nested
	class Contructor {

		@Test
		void success() {
			final var dao = new BlockDao(BLOCK.info());

			assertThat(dao.getId()).isNull();
			assertThat(dao.getOrganisationId()).isEqualTo(ORGANISATION.getId());
			assertThat(dao.getTopicId()).isEqualTo(TOPIC.getId());
			assertThat(dao.getType()).isEqualTo(BLOCK.getTypeId());
			assertThat(dao.getSequenceId()).isEqualTo(BLOCK.getSequenceId());
		}

		@Test
		void bdoNull() {
			assertThatThrownBy(() -> new BlockDao(null))
					.isInstanceOf(NullPointerException.class);
		}
	}

	@Nested
	class Update {

		final BlockDao dao = new BlockDao(BLOCK.info());

		@Test
		void success() {
			final var updated = new BlockInfo(BLOCK.getId(), ORGANISATION.getId(),
					TOPIC.getId(), BLOCK.getType(), BLOCK.getSequenceId() + 1);

			dao.update(updated);

			assertThat(dao.getSequenceId()).isEqualTo(updated.getSequenceId());
		}

		@Test
		void bdoNull() {
			assertThatThrownBy(() -> dao.update(null))
					.isInstanceOf(NullPointerException.class);
		}
	}

	@Nested
	class ToBdo {

		private final BlockDao dao = new BlockDao(BLOCK.info());

		@Test
		void success() throws Exception {
			final Field idField = dao.getClass().getDeclaredField("id");
			idField.setAccessible(true);
			idField.set(dao, TOPIC.getId());

			final BlockInfo bdo = dao.toBdo();

			assertThat(bdo.getId()).isEqualTo(TOPIC.getId());
			assertThat(bdo.getOrganisationId()).isEqualTo(ORGANISATION.getId());
			assertThat(bdo.getTopicId()).isEqualTo(TOPIC.getId());
			assertThat(bdo.getType()).isEqualTo(BLOCK.getType());
			assertThat(bdo.getSequenceId()).isEqualTo(BLOCK.getSequenceId());
		}

		@Test
		void idNull() {
			assertThatThrownBy(dao::toBdo)
					.isInstanceOf(IllegalStateException.class);
		}

	}
}
