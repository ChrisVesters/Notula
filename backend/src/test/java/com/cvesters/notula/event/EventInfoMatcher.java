package com.cvesters.notula.event;

import static org.assertj.core.api.Assertions.assertThat;

import com.cvesters.notula.event.bdo.EventInfo;
import com.cvesters.notula.test.Matcher;

public class EventInfoMatcher extends Matcher<EventInfo> {

	public EventInfoMatcher(final EventInfo expected) {
		super(expected, EventInfo.class);
	}

	@Override
	protected void assertEquals(final EventInfo actual) {
		assertThat(actual.getMeetingId()).isEqualTo(expected.getMeetingId());
		assertThat(actual.getRevision()).isEqualTo(expected.getRevision());
		assertThat(actual.getUserId()).isEqualTo(expected.getUserId());
		assertThat(actual.getClientId()).isEqualTo(expected.getClientId());
		assertThat(actual.getMutation()).isEqualTo(expected.getMutation());
	}
}
