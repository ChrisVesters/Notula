package com.cvesters.notula.meeting;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.cvesters.notula.common.domain.Rank;
import com.cvesters.notula.common.domain.Splice;
import com.cvesters.notula.common.exception.InvalidActionException;
import com.cvesters.notula.event.EventStorageGateway;
import com.cvesters.notula.event.bdo.EventInfo;
import com.cvesters.notula.event.bdo.MeetingMutation;
import com.cvesters.notula.event.bdo.TextBlockMutation;
import com.cvesters.notula.event.bdo.TopicMutation;
import com.cvesters.notula.meeting.bdo.MeetingScope;
import com.cvesters.notula.meeting.dto.MeetingChangeDto;
import com.cvesters.notula.meeting.dto.TextBlockChangeDto;
import com.cvesters.notula.meeting.dto.TextEditDto;
import com.cvesters.notula.meeting.dto.TopicChangeDto;

class TextHistoryTest {

	private static final long MEETING_ID = 1L;
	private static final long REVISION = 20L;
	private static final MeetingScope SCOPE = new MeetingScope(MEETING_ID,
			REVISION);
	private static final long BASE = REVISION - 3;
	private static final long USER_ID = 1L;

	private static final long TOPIC_ID = 2L;
	private static final long BLOCK_ID = 3L;

	private static final TextEditDto EDIT = new TextEditDto(5, 0, "X");
	private static final Splice OPENED = new Splice(0, 0, "We ");
	private static final Splice TRIMMED = new Splice(1, 2, "");

	private final EventStorageGateway eventStorage = mock();

	private final TextHistory history = new TextHistory(eventStorage);

	@Nested
	class Rebase {

		@Test
		void current() {
			final var change = new TextBlockChangeDto.Edit(BLOCK_ID,
					REVISION - 1, EDIT);
			when(eventStorage.findAllSince(MEETING_ID, REVISION - 1))
					.thenReturn(List.of());

			final Splice rebased = history.rebase(SCOPE, change);

			assertThat(rebased).isEqualTo(new Splice(5, 0, "X"));
		}

		@Test
		void textBlock() {
			final var change = new TextBlockChangeDto.Edit(BLOCK_ID, BASE,
					EDIT);
			when(eventStorage.findAllSince(MEETING_ID, BASE))
					.thenReturn(List.of(
							new EventInfo(7L, MEETING_ID, BASE + 1, USER_ID,
									null,
									new TextBlockMutation.Edit(BLOCK_ID,
											OPENED)),
							new EventInfo(8L, MEETING_ID, BASE + 2, USER_ID,
									null, new TextBlockMutation.Edit(
											BLOCK_ID, TRIMMED))));

			final Splice rebased = history.rebase(SCOPE, change);

			assertThat(rebased).isEqualTo(new Splice(6, 0, "X"));
		}

		@Test
		void otherTextBlock() {
			final var change = new TextBlockChangeDto.Edit(BLOCK_ID, BASE,
					EDIT);
			when(eventStorage.findAllSince(MEETING_ID, BASE))
					.thenReturn(List.of(new EventInfo(7L, MEETING_ID,
							BASE + 1, USER_ID, null,
							new TextBlockMutation.Edit(BLOCK_ID + 1,
									OPENED))));

			final Splice rebased = history.rebase(SCOPE, change);

			assertThat(rebased).isEqualTo(new Splice(5, 0, "X"));
		}

		@Test
		void topicName() {
			final var change = new TopicChangeDto.Rename(TOPIC_ID, BASE,
					EDIT);
			when(eventStorage.findAllSince(MEETING_ID, BASE))
					.thenReturn(List.of(
							new EventInfo(7L, MEETING_ID, BASE + 1, USER_ID,
									null, new TopicMutation.Rename(TOPIC_ID,
											OPENED)),
							new EventInfo(8L, MEETING_ID, BASE + 2, USER_ID,
									null, new TopicMutation.Describe(
											TOPIC_ID, TRIMMED)),
							new EventInfo(9L, MEETING_ID, BASE + 3, USER_ID,
									null, new TopicMutation.Rename(
											TOPIC_ID + 1, TRIMMED))));

			final Splice rebased = history.rebase(SCOPE, change);

			assertThat(rebased).isEqualTo(new Splice(8, 0, "X"));
		}

		@Test
		void topicDescription() {
			final var change = new TopicChangeDto.Describe(TOPIC_ID, BASE,
					EDIT);
			when(eventStorage.findAllSince(MEETING_ID, BASE))
					.thenReturn(List.of(
							new EventInfo(7L, MEETING_ID, BASE + 1, USER_ID,
									null, new TopicMutation.Describe(
											TOPIC_ID, OPENED)),
							new EventInfo(8L, MEETING_ID, BASE + 2, USER_ID,
									null, new TopicMutation.Rename(TOPIC_ID,
											TRIMMED)),
							new EventInfo(9L, MEETING_ID, BASE + 3, USER_ID,
									null, new TopicMutation.Describe(
											TOPIC_ID + 1, TRIMMED))));

			final Splice rebased = history.rebase(SCOPE, change);

			assertThat(rebased).isEqualTo(new Splice(8, 0, "X"));
		}

		@Test
		void meetingName() {
			final var change = new MeetingChangeDto.Rename(BASE, EDIT);
			when(eventStorage.findAllSince(MEETING_ID, BASE))
					.thenReturn(List.of(
							new EventInfo(7L, MEETING_ID, BASE + 1, USER_ID,
									null, new MeetingMutation.Rename(OPENED)),
							new EventInfo(8L, MEETING_ID, BASE + 2, USER_ID,
									null,
									new MeetingMutation.Describe(TRIMMED))));

			final Splice rebased = history.rebase(SCOPE, change);

			assertThat(rebased).isEqualTo(new Splice(8, 0, "X"));
		}

		@Test
		void meetingDescription() {
			final var change = new MeetingChangeDto.Describe(BASE, EDIT);
			when(eventStorage.findAllSince(MEETING_ID, BASE))
					.thenReturn(List.of(
							new EventInfo(7L, MEETING_ID, BASE + 1, USER_ID,
									null,
									new MeetingMutation.Describe(OPENED)),
							new EventInfo(8L, MEETING_ID, BASE + 2, USER_ID,
									null, new MeetingMutation.Rename(TRIMMED))));

			final Splice rebased = history.rebase(SCOPE, change);

			assertThat(rebased).isEqualTo(new Splice(8, 0, "X"));
		}

		@Test
		void structural() {
			final var change = new TopicChangeDto.Rename(TOPIC_ID, BASE,
					EDIT);
			when(eventStorage.findAllSince(MEETING_ID, BASE))
					.thenReturn(List.of(new EventInfo(7L, MEETING_ID,
							BASE + 1, USER_ID, null,
							new TopicMutation.Move(TOPIC_ID, new Rank("V")))));

			final Splice rebased = history.rebase(SCOPE, change);

			assertThat(rebased).isEqualTo(new Splice(5, 0, "X"));
		}

		@Test
		void baseCurrent() {
			final var change = new TopicChangeDto.Rename(TOPIC_ID, REVISION,
					EDIT);

			assertThatThrownBy(() -> history.rebase(SCOPE, change))
					.isInstanceOf(InvalidActionException.class);

			verifyNoInteractions(eventStorage);
		}

		@Test
		void baseAhead() {
			final var change = new TopicChangeDto.Rename(TOPIC_ID,
					REVISION + 1, EDIT);

			assertThatThrownBy(() -> history.rebase(SCOPE, change))
					.isInstanceOf(InvalidActionException.class);

			verifyNoInteractions(eventStorage);
		}

		@Test
		void scopeNull() {
			final var change = new TopicChangeDto.Rename(TOPIC_ID, BASE,
					EDIT);

			assertThatThrownBy(() -> history.rebase(null, change))
					.isInstanceOf(NullPointerException.class);
		}

		@Test
		void changeNull() {
			assertThatThrownBy(() -> history.rebase(SCOPE, null))
					.isInstanceOf(NullPointerException.class);
		}
	}
}
