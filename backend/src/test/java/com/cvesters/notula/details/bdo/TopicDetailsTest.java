package com.cvesters.notula.details.bdo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.cvesters.notula.block.TestBlock;
import com.cvesters.notula.topic.TestTopic;
import com.cvesters.notula.topic.bdo.TopicInfo;

class TopicDetailsTest {

	@Nested
	class Constructor {

		private static final TestTopic TOPICS = TestTopic.SPORER_PROJECT_BLOCKERS;
		private static final List<TestBlock> BLOCKS = TestBlock.ofTopic(TOPICS);

		@Test
		void success() {
			final TopicInfo topicInfo = TOPICS.info();
			final List<BlockDetails> blocksDetails = BLOCKS.stream()
					.map(TestBlock::info)
					.map(info -> new BlockDetails(info,
							new BlockContent.Text()))
					.toList();

			final var details = new TopicDetails(topicInfo, blocksDetails);

			assertThat(details.getId()).isEqualTo(TOPICS.getId());
			assertThat(details.getName()).isEqualTo(TOPICS.getName());
			assertThat(details.getBlocks()).isEqualTo(blocksDetails);
		}

		@Test
		void infoNull() {
			final TopicInfo topicInfo = null;
			final List<BlockDetails> blocksDetails = BLOCKS.stream()
					.map(TestBlock::info)
					.map(info -> new BlockDetails(info,
							new BlockContent.Text()))
					.toList();

			assertThatThrownBy(() -> new TopicDetails(topicInfo, blocksDetails))
					.isInstanceOf(NullPointerException.class);
		}

		@Test
		void blocksNull() {
			final TopicInfo topicInfo = TOPICS.info();
			final List<BlockDetails> blocksDetails = null;

			assertThatThrownBy(() -> new TopicDetails(topicInfo, blocksDetails))
					.isInstanceOf(NullPointerException.class);
		}
	}
}
