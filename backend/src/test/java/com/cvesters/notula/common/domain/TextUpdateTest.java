package com.cvesters.notula.common.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import lombok.Getter;
import lombok.Setter;

class TextUpdateTest {

	@Nested
	class Constructor {

		@Test
		void success() {
			final var edit = new Splice(0, 0, "test");

			final var update = new MockUpdate(edit);

			assertThat(update.getEdit()).isEqualTo(edit);
		}

		@Test
		void editNull() {
			assertThatThrownBy(() -> new MockUpdate(null))
					.isInstanceOf(NullPointerException.class);
		}

		@Test
		void getterNull() {
			final var edit = new Splice(0, 0, "");

			assertThatThrownBy(() -> new TextUpdate<MockObject>(null,
					MockObject::setValue, edit) {
			}).isInstanceOf(NullPointerException.class);
		}

		@Test
		void setterNull() {
			final var edit = new Splice(0, 0, "");

			assertThatThrownBy(() -> new TextUpdate<MockObject>(
					MockObject::getValue, null, edit) {
			}).isInstanceOf(NullPointerException.class);
		}
	}

	@Nested
	class Apply {

		final MockObject object = new MockObject("test");

		@ParameterizedTest
		@CsvSource({ "0,atest", "2,teast", "4,testa" })
		void add(final int position, final String expected) {
			final var update = new MockUpdate(new Splice(position, 0, "a"));

			update.apply(object);

			assertThat(object.getValue()).isEqualTo(expected);
		}

		@ParameterizedTest
		@CsvSource({ "0,est", "2,tet", "3,tes" })
		void remove(final int position, final String expected) {
			final var update = new MockUpdate(new Splice(position, 1, ""));

			update.apply(object);

			assertThat(object.getValue()).isEqualTo(expected);
		}

		@ParameterizedTest
		@CsvSource({ "0,aest", "2,teat", "3,tesa" })
		void replace(final int position, final String expected) {
			final var update = new MockUpdate(new Splice(position, 1, "a"));

			update.apply(object);

			assertThat(object.getValue()).isEqualTo(expected);

		}

		@Test
		void objectNull() {
			final var update = new MockUpdate(new Splice(0, 0, "test"));

			assertThatThrownBy(() -> update.apply(null))
					.isInstanceOf(NullPointerException.class);
		}

		@ParameterizedTest
		@CsvSource({ "5,0,a", "4,1,''", "0,5,a" })
		void outOfBounds(final int position, final int length,
				final String value) {
			final var update = new MockUpdate(
					new Splice(position, length, value));

			assertThatThrownBy(() -> update.apply(object))
					.isInstanceOf(IllegalArgumentException.class);
		}

		@Test
		void valueNull() {
			final var object = new MockObject(null);
			final var update = new MockUpdate(new Splice(0, 0, "test"));

			assertThatThrownBy(() -> update.apply(object))
					.isInstanceOf(NullPointerException.class);
		}
	}

	@Getter
	@Setter
	private static class MockObject {
		private String value;

		public MockObject(final String value) {
			this.value = value;
		}
	}

	@Getter
	private static class MockUpdate extends TextUpdate<MockObject> {

		MockUpdate(final Splice edit) {
			super(MockObject::getValue, MockObject::setValue, edit);
		}
	}
}