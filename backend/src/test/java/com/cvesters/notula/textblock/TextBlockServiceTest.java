package com.cvesters.notula.textblock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.cvesters.notula.block.BlockService;
import com.cvesters.notula.block.TestBlock;
import com.cvesters.notula.block.bdo.BlockInfo;
import com.cvesters.notula.common.domain.Origin;
import com.cvesters.notula.common.domain.Principal;
import com.cvesters.notula.common.exception.InvalidActionException;
import com.cvesters.notula.meeting.MeetingLock;
import com.cvesters.notula.meeting.TestMeetingLock;
import com.cvesters.notula.session.TestSession;
import com.cvesters.notula.textblock.bdo.TextBlockAction;
import com.cvesters.notula.textblock.bdo.TextBlockInfo;

class TextBlockServiceTest {

	private static final UUID CLIENT_ID = UUID
			.fromString("3f9c1a44-1d2e-4a51-8b0c-2c7e9b1d4a10");

	private final BlockService blockService = mock();
	private final MeetingLock meetingLock = TestMeetingLock.passThrough();

	private final TextBlockStorageGateway textBlockStorageGateway = mock();
	private final TextBlockPublisher textBlockPublisher = mock();

	private final TextBlockService textBlockService = new TextBlockService(
			blockService, meetingLock, textBlockStorageGateway,
			textBlockPublisher);

	@Nested
	class Update {

		private static final TestSession SESSION = TestSession.EDUARDO_CHRISTIANSEN_SPORER;
		private static final Origin ORIGIN = new Origin(SESSION.principal(),
				CLIENT_ID);
		private static final TestTextBlock TEXT_BLOCK = TestTextBlock.SPORER_PROJECT_BLOCKERS_FIRST;
		private static final TestBlock BLOCK = TestBlock.SPORER_PROJECT_BLOCKERS_FIRST;

		@Test
		void success() {
			final Principal principal = ORIGIN.principal();
			final long blockId = BLOCK.getId();

			final BlockInfo blockInfo = BLOCK.info();
			when(blockService.getById(principal, blockId))
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
			final TextBlockInfo result = textBlockService.update(ORIGIN,
					blockId, action);

			assertThat(result).isEqualTo(updated);

			verify(textBlockPublisher).publish(argThat(event -> {
				assertThat(event.origin()).isEqualTo(ORIGIN);
				assertThat(event.block()).isEqualTo(blockInfo);
				assertThat(event.action()).isEqualTo(action);
				return true;
			}));
		}

		@Test
		void uninitialized() {
			final Principal principal = ORIGIN.principal();
			final long blockId = BLOCK.getId();

			final BlockInfo blockInfo = BLOCK.info();
			when(blockService.getById(principal, blockId))
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
			final TextBlockInfo result = textBlockService.update(ORIGIN,
					blockId, action);

			assertThat(result).isEqualTo(updated);

			verify(textBlockPublisher).publish(argThat(event -> {
				assertThat(event.origin()).isEqualTo(ORIGIN);
				assertThat(event.block()).isEqualTo(blockInfo);
				assertThat(event.action()).isEqualTo(action);
				return true;
			}));
		}

		@Test
		void invalidType() {
			final Principal principal = ORIGIN.principal();
			final long blockId = BLOCK.getId();

			final BlockInfo blockInfo = mock();
			when(blockInfo.getType()).thenReturn(null);
			when(blockService.getById(principal, blockId))
					.thenReturn(blockInfo);

			final var action = new TextBlockAction.UpdateContent(0, 0,
					"Project ");
			assertThatThrownBy(
					() -> textBlockService.update(ORIGIN, blockId, action))
							.isInstanceOf(InvalidActionException.class);

			verifyNoInteractions(textBlockStorageGateway);
			verifyNoInteractions(textBlockPublisher);
		}

		@Test
		void originNull() {
			final long blockId = BLOCK.getId();

			final var action = new TextBlockAction.UpdateContent(0, 0,
					"Project ");

			assertThatThrownBy(
					() -> textBlockService.update(null, blockId, action))
							.isInstanceOf(NullPointerException.class);
		}

		@Test
		void actionNull() {
			final long blockId = BLOCK.getId();

			assertThatThrownBy(
					() -> textBlockService.update(ORIGIN, blockId, null))
							.isInstanceOf(NullPointerException.class);
		}

		@Test
		void serialised() {
			final long meetingId = 7L;

			when(blockService.getMeetingId(ORIGIN.principal(), BLOCK.getId()))
					.thenReturn(meetingId);

			TestMeetingLock.withhold(meetingLock);

			final var action = new TextBlockAction.UpdateContent(0, 0, "text");

			textBlockService.update(ORIGIN, BLOCK.getId(), action);

			verify(meetingLock).call(eq(meetingId), any());
			verifyNoInteractions(textBlockStorageGateway);
		}
	}
}
