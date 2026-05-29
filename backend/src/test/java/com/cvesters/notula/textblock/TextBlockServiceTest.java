package com.cvesters.notula.textblock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.cvesters.notula.block.BlockService;
import com.cvesters.notula.block.TestBlock;
import com.cvesters.notula.block.bdo.BlockInfo;
import com.cvesters.notula.common.domain.Principal;
import com.cvesters.notula.common.exception.InvalidActionException;
import com.cvesters.notula.meeting.TestMeeting;
import com.cvesters.notula.session.TestSession;
import com.cvesters.notula.textblock.bdo.TextBlockAction;
import com.cvesters.notula.textblock.bdo.TextBlockInfo;
import com.cvesters.notula.topic.TestTopic;

class TextBlockServiceTest {

	private final BlockService blockService = mock();

	private final TextBlockStorageGateway textBlockStorageGateway = mock();
	private final TextBlockPublisher textBlockPublisher = mock();

	private final TextBlockService textBlockService = new TextBlockService(
			blockService, textBlockStorageGateway, textBlockPublisher);

	@Nested
	class Update {

		private static final TestSession SESSION = TestSession.EDUARDO_CHRISTIANSEN_SPORER;
		private static final TestTextBlock TEXT_BLOCK = TestTextBlock.SPORER_PROJECT_BLOCKERS_FIRST;
		private static final TestBlock BLOCK = TestBlock.SPORER_PROJECT_BLOCKERS_FIRST;
		private static final TestTopic TOPIC = BLOCK.getTopic();
		private static final TestMeeting MEETING = TOPIC.getMeeting();

		@Test
		void success() {
			final Principal principal = SESSION.principal();
			final long meetingId = MEETING.getId();
			final long topicId = TOPIC.getId();
			final long blockId = BLOCK.getId();

			final BlockInfo blockInfo = BLOCK.info();
			when(blockService.getById(principal, meetingId, topicId, blockId))
					.thenReturn(blockInfo);

			final TextBlockInfo textBlockInfo = TEXT_BLOCK.info();
			when(textBlockStorageGateway.find(blockId))
					.thenReturn(Optional.of(textBlockInfo));

			final TextBlockInfo updated = mock();
			when(textBlockStorageGateway.update(argThat(info -> {
				assertThat(info.getBlockId()).isEqualTo(blockId);
				assertThat(info.getContent()).isEqualTo("Project start");
				return true;
			}))).thenReturn(updated);

			final var action = new TextBlockAction.UpdateContent(0, 0,
					"Project ");
			final TextBlockInfo result = textBlockService.update(principal,
					meetingId, topicId, blockId, action);

			assertThat(result).isEqualTo(updated);

			verify(textBlockPublisher).publish(eq(meetingId), argThat(event -> {
				assertThat(event.topicId()).isEqualTo(topicId);
				assertThat(event.blockId()).isEqualTo(blockId);
				assertThat(event.action()).isEqualTo(action);
				return true;
			}));
		}

		@Test
		void uninitialized() {
			final Principal principal = SESSION.principal();
			final long meetingId = MEETING.getId();
			final long topicId = TOPIC.getId();
			final long blockId = BLOCK.getId();

			final BlockInfo blockInfo = BLOCK.info();
			when(blockService.getById(principal, meetingId, topicId, blockId))
					.thenReturn(blockInfo);

			when(textBlockStorageGateway.find(blockId))
					.thenReturn(Optional.empty());

			final TextBlockInfo updated = mock();
			when(textBlockStorageGateway.update(argThat(info -> {
				assertThat(info.getBlockId()).isEqualTo(blockId);
				assertThat(info.getContent()).isEqualTo("Project");
				return true;
			}))).thenReturn(updated);

			final var action = new TextBlockAction.UpdateContent(0, 0,
					"Project");
			final TextBlockInfo result = textBlockService.update(principal,
					meetingId, topicId, blockId, action);

			assertThat(result).isEqualTo(updated);

			verify(textBlockPublisher).publish(eq(meetingId), argThat(event -> {
				assertThat(event.topicId()).isEqualTo(topicId);
				assertThat(event.blockId()).isEqualTo(blockId);
				assertThat(event.action()).isEqualTo(action);
				return true;
			}));
		}

		@Test
		void invalidType() {
			final Principal principal = SESSION.principal();
			final long meetingId = MEETING.getId();
			final long topicId = TOPIC.getId();
			final long blockId = BLOCK.getId();

			final BlockInfo blockInfo = mock();
			when(blockInfo.getType()).thenReturn(null);
			when(blockService.getById(principal, meetingId, topicId, blockId))
					.thenReturn(blockInfo);

			final var action = new TextBlockAction.UpdateContent(0, 0,
					"Project ");
			assertThatThrownBy(() -> textBlockService.update(principal,
					meetingId, topicId, blockId, action))
							.isInstanceOf(InvalidActionException.class);

			verifyNoInteractions(textBlockStorageGateway);
			verifyNoInteractions(textBlockPublisher);
		}

		@Test
		void principalNull() {
			final long meetingId = MEETING.getId();
			final long topicId = TOPIC.getId();
			final long blockId = BLOCK.getId();

			final var action = new TextBlockAction.UpdateContent(0, 0,
					"Project ");

			assertThatThrownBy(() -> textBlockService.update(null, meetingId,
					topicId, blockId, action))
							.isInstanceOf(NullPointerException.class);
		}

		@Test
		void actionNull() {
			final Principal principal = SESSION.principal();
			final long meetingId = MEETING.getId();
			final long topicId = TOPIC.getId();
			final long blockId = BLOCK.getId();

			assertThatThrownBy(() -> textBlockService.update(principal,
					meetingId, topicId, blockId, null))
							.isInstanceOf(NullPointerException.class);
		}
	}
}
