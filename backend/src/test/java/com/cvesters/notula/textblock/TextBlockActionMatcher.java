package com.cvesters.notula.textblock;

import static org.assertj.core.api.Assertions.assertThat;

import com.cvesters.notula.test.Matcher;
import com.cvesters.notula.textblock.bdo.TextBlockAction;

public final class TextBlockActionMatcher {

	private TextBlockActionMatcher() {
	}

	public static class UpdateContent
			extends Matcher<TextBlockAction.UpdateContent> {

		public UpdateContent(final TextBlockAction.UpdateContent expected) {
			super(expected, TextBlockAction.UpdateContent.class);
		}

		@Override
		protected void assertEquals(
				final TextBlockAction.UpdateContent actual) {
			assertThat(actual.getPosition()).isEqualTo(expected.getPosition());
			assertThat(actual.getLength()).isEqualTo(expected.getLength());
			assertThat(actual.getValue()).isEqualTo(expected.getValue());
		}
	}
}
