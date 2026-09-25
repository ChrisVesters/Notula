package com.cvesters.notula.meeting;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import com.cvesters.notula.meeting.bdo.MeetingScope;

public final class TestMeetingLock {

	private final MeetingLock meetingLock = mock();

	public MeetingLock lock() {
		return meetingLock;
	}

	public void passThrough(final long revision) {
		doAnswer(invocation -> {
			final long meetingId = invocation.getArgument(0, Long.class);
			final Function<MeetingScope, ?> action = invocation.getArgument(1);

			return action.apply(new MeetingScope(meetingId, revision));
		}).when(meetingLock).call(anyLong(), any());

		doAnswer(invocation -> {
			final long meetingId = invocation.getArgument(0, Long.class);
			final Consumer<MeetingScope> action = invocation.getArgument(1);

			action.accept(new MeetingScope(meetingId, revision));

			return null;
		}).when(meetingLock).run(anyLong(), any());

		doAnswer(invocation -> {
			final Supplier<?> action = invocation.getArgument(1);

			return action.get();
		}).when(meetingLock).hold(anyLong(), any());
	}

	public void withhold() {
		doReturn(null).when(meetingLock).call(anyLong(), any());
		doNothing().when(meetingLock).run(anyLong(), any());
		doReturn(null).when(meetingLock).hold(anyLong(), any());
	}
}
