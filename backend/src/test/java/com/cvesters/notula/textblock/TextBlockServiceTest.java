package com.cvesters.notula.textblock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.argThat;
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
import com.cvesters.notula.common.domain.Splice;
import com.cvesters.notula.common.exception.InvalidActionException;
import com.cvesters.notula.event.EventInfoMatcher;
import com.cvesters.notula.event.EventService;
import com.cvesters.notula.event.bdo.EventInfo;
import com.cvesters.notula.event.bdo.TextBlockMutation;
import com.cvesters.notula.meeting.bdo.MeetingScope;
import com.cvesters.notula.session.TestSession;
import com.cvesters.notula.textblock.bdo.TextBlockAction;
import com.cvesters.notula.textblock.bdo.TextBlockInfo;

class TextBlockServiceTest {

	private static final UUID CLIENT_ID = UUID
			.fromString("3f9c1a44-1d2e-4a51-8b0c-2c7e9b1d4a10");

	private final BlockService blockService = mock();

	private final TextBlockStorageGateway textBlockStorageGateway = mock();
	private final EventService eventService = mock();

	private final TextBlockService textBlockService = new TextBlockService(
			blockService, textBlockStorageGateway, eventService);

	@Nested
	class Update {

		private static final TestSession SESSION = TestSession.EDUARDO_CHRISTIANSEN_SPORER;
		private static final Origin ORIGIN = new Origin(SESSION.principal(),
				CLIENT_ID);
		private static final TestTextBlock TEXT_BLOCK = TestTextBlock.SPORER_PROJECT_BLOCKERS_FIRST;
		private static final TestBlock BLOCK = TestBlock.SPORER_PROJECT_BLOCKERS_FIRST;
		private static final long MEETING_ID = BLOCK.getTopic()
				.getMeeting()
				.getId();

		private static final long REVISION = BLOCK.getTopic()
				.getMeeting()
				.getRevision();
		private static final MeetingScope SCOPE = new MeetingScope(MEETING_ID,
				REVISION);

		@Test
		void success() {
			final Principal principal = ORIGIN.principal();
			final long blockId = BLOCK.getId();

			final BlockInfo blockInfo = BLOCK.info();
			when(blockService.getById(principal, MEETING_ID, blockId))
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

			final var action = new TextBlockAction.UpdateContent(
					new Splice(0, 0, "Project "));
			final TextBlockInfo result = textBlockService.update(ORIGIN, SCOPE,
					blockId, action);

			assertThat(result).isEqualTo(updated);

			final var event = new EventInfo(SCOPE, ORIGIN,
					new TextBlockMutation.Edit(blockId,
							new Splice(0, 0, "Project ")));
			final var matcher = new EventInfoMatcher(event);
			verify(eventService).publish(argThat(matcher::matches));
		}

		@Test
		void uninitialized() {
			final Principal principal = ORIGIN.principal();
			final long blockId = BLOCK.getId();

			final BlockInfo blockInfo = BLOCK.info();
			when(blockService.getById(principal, MEETING_ID, blockId))
					.thenReturn(blockInfo);

			when(textBlockStorageGateway.find(blockId))
					.thenReturn(Optional.empty());

			final TextBlockInfo updated = mock();
			when(textBlockStorageGateway.update(argThat(info -> {
				assertThat(info.getBlockId()).isEqualTo(blockId);
				assertThat(info.getContent()).isEqualTo("Project");
				return true;
			}))).thenReturn(updated);

			final var action = new TextBlockAction.UpdateContent(
					new Splice(0, 0, "Project"));
			final TextBlockInfo result = textBlockService.update(ORIGIN, SCOPE,
					blockId, action);

			assertThat(result).isEqualTo(updated);

			final var event = new EventInfo(SCOPE, ORIGIN,
					new TextBlockMutation.Edit(blockId,
							new Splice(0, 0, "Project")));
			final var matcher = new EventInfoMatcher(event);
			verify(eventService).publish(argThat(matcher::matches));
		}

		@Test
		void invalidType() {
			final Principal principal = ORIGIN.principal();
			final long blockId = BLOCK.getId();

			final BlockInfo blockInfo = mock();
			when(blockInfo.getType()).thenReturn(null);
			when(blockService.getById(principal, MEETING_ID, blockId))
					.thenReturn(blockInfo);

			final var action = new TextBlockAction.UpdateContent(
					new Splice(0, 0, "Project "));
			assertThatThrownBy(() -> textBlockService.update(ORIGIN, SCOPE,
					blockId, action))
							.isInstanceOf(InvalidActionException.class);

			verifyNoInteractions(textBlockStorageGateway);
			verifyNoInteractions(eventService);
		}

		@Test
		void originNull() {
			final long blockId = BLOCK.getId();

			final var action = new TextBlockAction.UpdateContent(
					new Splice(0, 0, "Project "));

			assertThatThrownBy(
					() -> textBlockService.update(null, SCOPE, blockId, action))
							.isInstanceOf(NullPointerException.class);
		}

		@Test
		void scopeNull() {
			final long blockId = BLOCK.getId();

			final var action = new TextBlockAction.UpdateContent(
					new Splice(0, 0, "Project "));

			assertThatThrownBy(() -> textBlockService.update(ORIGIN, null,
					blockId, action)).isInstanceOf(NullPointerException.class);
		}

		@Test
		void actionNull() {
			final long blockId = BLOCK.getId();

			assertThatThrownBy(
					() -> textBlockService.update(ORIGIN, SCOPE, blockId, null))
							.isInstanceOf(NullPointerException.class);
		}

	}
}
