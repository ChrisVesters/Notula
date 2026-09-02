package com.cvesters.notula.meeting;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.function.Supplier;

public final class TestMeetingLock {

	private TestMeetingLock() {
	}

	public static MeetingLock passThrough() {
		final MeetingLock meetingLock = mock();

		when(meetingLock.call(anyLong(), any())).thenAnswer(
				invocation -> invocation.getArgument(1, Supplier.class).get());
		doAnswer(invocation -> {
			invocation.getArgument(1, Runnable.class).run();

			return null;
		}).when(meetingLock).run(anyLong(), any());

		return meetingLock;
	}

	public static void withhold(final MeetingLock meetingLock) {
		doReturn(null).when(meetingLock).call(anyLong(), any());
		doNothing().when(meetingLock).run(anyLong(), any());
	}
}
