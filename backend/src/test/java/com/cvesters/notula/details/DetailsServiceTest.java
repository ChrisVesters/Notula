package com.cvesters.notula.details;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.cvesters.notula.block.BlockStorageGateway;
import com.cvesters.notula.block.TestBlock;
import com.cvesters.notula.block.bdo.BlockInfo;
import com.cvesters.notula.common.domain.Principal;
import com.cvesters.notula.common.exception.MissingEntityException;
import com.cvesters.notula.details.bdo.MeetingDetails;
import com.cvesters.notula.meeting.MeetingStorageGateway;
import com.cvesters.notula.meeting.TestMeeting;
import com.cvesters.notula.meeting.bdo.MeetingInfo;
import com.cvesters.notula.session.TestSession;
import com.cvesters.notula.textblock.TextBlockStorageGateway;
import com.cvesters.notula.topic.TestTopic;
import com.cvesters.notula.topic.TopicStorageGateway;
import com.cvesters.notula.topic.bdo.TopicInfo;

class DetailsServiceTest {

	private final MeetingStorageGateway meetingStorage = mock();
	private final TopicStorageGateway topicStorage = mock();
	private final BlockStorageGateway blockStorage = mock();
	private final TextBlockStorageGateway textBlockStorage = mock();

	private final DetailsService meetingActionService = new DetailsService(
			meetingStorage, topicStorage, blockStorage, textBlockStorage);

	@Nested
	class Get {

		private static final TestSession SESSION = TestSession.EDUARDO_CHRISTIANSEN_SPORER;
		private static final Principal PRINCIPAL = SESSION.principal();
		private static final TestMeeting MEETING = TestMeeting.SPORER_PROJECT;
		private static final List<TestTopic> TOPICS = TestTopic
				.ofMeeting(MEETING);

		@Test
		void success() {
			final MeetingInfo info = MEETING.info();
			when(meetingStorage.findByOrganisationIdAndId(
					PRINCIPAL.organisationId(), MEETING.getId()))
							.thenReturn(Optional.of(info));

			final List<TopicInfo> topicsInfo = TOPICS.stream()
					.map(TestTopic::info)
					.toList();
			when(topicStorage.findAllByMeetingId(MEETING.getId()))
					.thenReturn(topicsInfo);

			for (final TestTopic topic : TOPICS) {
				final List<BlockInfo> blockInfo = TestBlock.ofTopic(topic)
						.stream()
						.map(TestBlock::info)
						.toList();

				when(blockStorage.findAllByTopicId(topic.getId()))
						.thenReturn(blockInfo);
			}

			final MeetingDetails result = meetingActionService.get(PRINCIPAL,
					MEETING.getId());

			assertThat(result).isNotNull();
			assertThat(result.getId()).isEqualTo(MEETING.getId());
			assertThat(result.getName()).isEqualTo(MEETING.getName());
			assertThat(result.getTopics()).hasSize(TOPICS.size());

			// TODO: extract to Matcher.
			TOPICS.forEach(topic -> {
				assertThat(result.getTopics()).anySatisfy(t -> {
					assertThat(t.getId()).isEqualTo(topic.getId());
					assertThat(t.getName()).isEqualTo(topic.getName());

					final List<TestBlock> blocks = TestBlock.ofTopic(topic);
					blocks.forEach(block -> {
						assertThat(t.getBlocks()).anySatisfy(b -> {
							assertThat(b.getId()).isEqualTo(block.getId());
							assertThat(b.getSequenceId())
									.isEqualTo(block.getSequenceId());
						});
					});
				});
			});
		}

		@Test
		void notFound() {
			final long meetingId = MEETING.getId();
			when(meetingStorage.findByOrganisationIdAndId(
					PRINCIPAL.organisationId(), MEETING.getId()))
							.thenReturn(Optional.empty());

			assertThatThrownBy(
					() -> meetingActionService.get(PRINCIPAL, meetingId))
							.isInstanceOf(MissingEntityException.class);
		}

		@Test
		void principalNull() {
			final long meetingId = MEETING.getId();

			assertThatThrownBy(() -> meetingActionService.get(null, meetingId))
					.isInstanceOf(NullPointerException.class);
		}

		@Test
		void principalWithoutOrganisation() {
			final long meetingId = MEETING.getId();
			final Principal principal = TestSession.ALISON_DACH.principal();

			assertThatThrownBy(
					() -> meetingActionService.get(principal, meetingId))
							.isInstanceOf(IllegalStateException.class);
		}
	}

}
