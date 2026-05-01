package com.cvesters.notula.topic;

import static org.assertj.core.api.Assertions.assertThat;

import com.cvesters.notula.test.Matcher;
import com.cvesters.notula.topic.bdo.TopicAction;

public final class TopicActionMatcher {

	private TopicActionMatcher() {
	}

	public static class Create extends Matcher<TopicAction.Create> {

		public Create(final TopicAction.Create expected) {
			super(expected, TopicAction.Create.class);
		}

		@Override
		public void assertEquals(final TopicAction.Create actual) {
			assertThat(actual.getName()).isEqualTo(expected.getName());
		}
	}

	public static class UpdateName extends Matcher<TopicAction.UpdateName> {

		public UpdateName(final TopicAction.UpdateName expected) {
			super(expected, TopicAction.UpdateName.class);
		}

		public void assertEquals(final TopicAction.UpdateName actual) {
			assertThat(actual.getPosition()).isEqualTo(expected.getPosition());
			assertThat(actual.getLength()).isEqualTo(expected.getLength());
			assertThat(actual.getValue()).isEqualTo(expected.getValue());
		}
	}
}
