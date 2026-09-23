package com.cvesters.notula.block.bdo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.cvesters.notula.block.TestBlock;
import com.cvesters.notula.common.domain.Rank;
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
					TOPIC.getId(), BLOCK.getType(), BLOCK.getRank());

			assertThatThrownBy(result::getId)
					.isInstanceOf(IllegalStateException.class);
			assertThat(result.getOrganisationId())
					.isEqualTo(ORGANISATION.getId());
			assertThat(result.getTopicId()).isEqualTo(TOPIC.getId());
			assertThat(result.getType()).isEqualTo(BLOCK.getType());
			assertThat(result.getRank()).isEqualTo(BLOCK.getRank());
		}

		@Test
		void withId() {
			final var result = new BlockInfo(BLOCK.getId(),
					ORGANISATION.getId(), TOPIC.getId(), BLOCK.getType(),
					BLOCK.getRank());

			assertThat(result.getId()).isEqualTo(TOPIC.getId());
			assertThat(result.getOrganisationId())
					.isEqualTo(ORGANISATION.getId());
			assertThat(result.getTopicId()).isEqualTo(TOPIC.getId());
			assertThat(result.getType()).isEqualTo(BLOCK.getType());
			assertThat(result.getRank()).isEqualTo(BLOCK.getRank());
		}

		@Test
		void typeNull() {
			final long id = BLOCK.getId();
			final long organisationId = ORGANISATION.getId();
			final long topicId = TOPIC.getId();
			final BlockType type = null;
			final Rank rank = BLOCK.getRank();

			assertThatThrownBy(() -> new BlockInfo(id, organisationId, topicId,
					type, rank)).isInstanceOf(NullPointerException.class);
		}

		@Test
		void rankNull() {
			final long id = BLOCK.getId();
			final long organisationId = ORGANISATION.getId();
			final long topicId = TOPIC.getId();
			final BlockType type = BLOCK.getType();

			assertThatThrownBy(() -> new BlockInfo(id, organisationId, topicId,
					type, null)).isInstanceOf(NullPointerException.class);
		}
	}

	@Nested
	class SetRank {

		@Test
		void success() {
			final var blockInfo = BLOCK.info();
			final var rank = new Rank("z");

			blockInfo.setRank(rank);

			assertThat(blockInfo.getRank()).isEqualTo(rank);
		}

		@Test
		void rankNull() {
			final var blockInfo = BLOCK.info();

			assertThatThrownBy(() -> blockInfo.setRank(null))
					.isInstanceOf(NullPointerException.class);
		}
	}
}
