package com.cvesters.notula.block.bdo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.cvesters.notula.block.TestBlock;
import com.cvesters.notula.meeting.TestMeeting;
import com.cvesters.notula.organisation.TestOrganisation;
import com.cvesters.notula.topic.TestTopic;

class BlockInfoTest {

	private static final TestBlock BLOCK = TestBlock.SPORER_PROJECT_BLOCKERS_FIRST;
	private static final TestTopic TOPIC = BLOCK.getTopic();
	private static final TestMeeting MEETING = TOPIC.getMeeting();
	private static final TestOrganisation ORGANISATION = MEETING
			.getOrganisation();

	@Nested
	class Constructor {

		@Test
		void withoutId() {
			final var result = new BlockInfo(ORGANISATION.getId(),
					TOPIC.getId(), BLOCK.getType(), BLOCK.getSequenceId());

			assertThatThrownBy(result::getId)
					.isInstanceOf(IllegalStateException.class);
			assertThat(result.getOrganisationId())
					.isEqualTo(ORGANISATION.getId());
			assertThat(result.getTopicId()).isEqualTo(TOPIC.getId());
			assertThat(result.getType()).isEqualTo(BLOCK.getType());
			assertThat(result.getSequenceId()).isEqualTo(BLOCK.getSequenceId());
		}

		@Test
		void withId() {
			final var result = new BlockInfo(BLOCK.getId(),
					ORGANISATION.getId(), TOPIC.getId(), BLOCK.getType(),
					BLOCK.getSequenceId());

			assertThat(result.getId()).isEqualTo(TOPIC.getId());
			assertThat(result.getOrganisationId())
					.isEqualTo(ORGANISATION.getId());
			assertThat(result.getTopicId()).isEqualTo(TOPIC.getId());
			assertThat(result.getType()).isEqualTo(BLOCK.getType());
			assertThat(result.getSequenceId()).isEqualTo(BLOCK.getSequenceId());
		}

		@Test
		void typeNull() {
			final long id = BLOCK.getId();
			final long organisationId = ORGANISATION.getId();
			final long topicId = TOPIC.getId();
			final BlockType type = null;
			final int sequenceId = BLOCK.getSequenceId();

			assertThatThrownBy(() -> new BlockInfo(id, organisationId, topicId,
					type, sequenceId)).isInstanceOf(NullPointerException.class);
		}

		@Test
		void sequenceIdNegative() {
			final long id = BLOCK.getId();
			final long organisationId = ORGANISATION.getId();
			final long topicId = TOPIC.getId();
			final BlockType type = BLOCK.getType();
			final int sequenceId = -1;

			assertThatThrownBy(() -> new BlockInfo(id, organisationId, topicId,
					type, sequenceId))
							.isInstanceOf(IllegalArgumentException.class);
		}
	}

	@Nested
	class MoveUp {

		@Test
		void success() {
			final TestBlock block = TestBlock.SPORER_PROJECT_BLOCKERS_SECOND;
			final var blockInfo = block.info();

			blockInfo.moveUp();

			assertThat(blockInfo.getSequenceId())
					.isEqualTo(block.getSequenceId() - 1);
		}

		@Test
		void first() {
			final TestBlock block = TestBlock.SPORER_PROJECT_BLOCKERS_FIRST;
			final var blockInfo = block.info();

			assertThatThrownBy(blockInfo::moveUp)
					.isInstanceOf(IllegalStateException.class);
		}
	}

	@Nested
	class MoveDown {

		@Test
		void success() {
			final TestBlock block = TestBlock.SPORER_PROJECT_BLOCKERS_FIRST;
			final var blockInfo = block.info();

			blockInfo.moveDown();

			assertThat(blockInfo.getSequenceId())
					.isEqualTo(block.getSequenceId() + 1);
		}

		@Test
		void overflow() {
			final var blockInfo = new BlockInfo(BLOCK.getId(),
					ORGANISATION.getId(), TOPIC.getId(), BLOCK.getType(),
					Integer.MAX_VALUE);

			assertThatThrownBy(blockInfo::moveDown)
					.isInstanceOf(IllegalStateException.class);
		}
	}
}
