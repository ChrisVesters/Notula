package com.cvesters.notula.details.dto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.cvesters.notula.block.TestBlock;
import com.cvesters.notula.details.bdo.BlockDetails;
import com.cvesters.notula.details.bdo.TopicDetails;
import com.cvesters.notula.topic.TestTopic;
import com.cvesters.notula.topic.bdo.TopicInfo;

class TopicDetailsDtoTest {

	@Nested
	class Constructor {

		@Test
		void success() {
			final TestTopic topic = TestTopic.SPORER_PROJECT_BLOCKERS;
			final TopicInfo topicInfo = topic.info();

			final List<TestBlock> blocks = TestBlock.ofTopic(topic);
			final List<BlockDetails> blocksDetails = blocks.stream()
					.map(TestBlock::info)
					.map(BlockDetails::new)
					.toList();
					
			final var details = new TopicDetails(topicInfo, blocksDetails);

			final TopicDetailsDto dto = new TopicDetailsDto(details);

			assertThat(dto.getId()).isEqualTo(details.getId());
			assertThat(dto.getName()).isEqualTo(details.getName());
			assertThat(dto.getBlocks()).hasSize(blocks.size());
			blocks.forEach(block -> {
				assertThat(dto.getBlocks()).anySatisfy(b -> {
					assertThat(b.getId()).isEqualTo(block.getId());
					assertThat(b.getType())
							.isEqualTo(block.getTypeString());
					assertThat(b.getSequenceId())
							.isEqualTo(block.getSequenceId());
				});
			});
		}

		@Test
		void detailsNull() {
			final TopicDetails details = null;

			assertThatThrownBy(() -> new TopicDetailsDto(details))
					.isInstanceOf(NullPointerException.class);
		}
	}
}
