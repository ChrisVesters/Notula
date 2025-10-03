package com.cvesters.notula.common.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class EmailTest {

	@Nested
	class Constructor {

		@Test
		void valueNull() {
			assertThatThrownBy(() -> new Email(null))
					.isInstanceOf(NullPointerException.class);
		}

		@ParameterizedTest
		@EmptySource
		@ValueSource(strings = { " ", "user", "@test", "user@", "user@@test" })
		void invalid(final String value) {
			assertThatThrownBy(() -> new Email(value))
					.isInstanceOf(IllegalArgumentException.class);
		}

		@ParameterizedTest
		@ValueSource(strings = { "User@test", "a.b.c@test", "a_z1990@unit.test",
				"用户@例子.广告" })
		void valid(final String value) {
			final var email = new Email(value);

			assertThat(email).isNotNull();
		}
	}

	@Nested
	class Value {

		@ParameterizedTest
		@ValueSource(strings = { "john.doe@test", "user@unit.test", "用户@例子.广告",
				"مستخدم@مثال.شبكة", "ผู้ใช้@ตัวอย่าง.ไทย" })
		void noConversion(final String value) {
			final var email = new Email(value);

			assertThat(email.value()).isEqualTo(value);
		}

		@ParameterizedTest
		@CsvSource({ "John.Doe@test,john.doe@test",
				"USER@USER.TEST,user@user.test",
				"DÖRTE@SÖRENSEN.TEST,dörte@sörensen.test",
				"ПОЧТА@ПРИМЕР.РУС,почта@пример.рус",
				"ΘΣΕΡ@ΕΧΑΜΠΛΕ.ΨΟΜ,θσερ@εχαμπλε.ψομ", })
		void conversionToLowercase(final String input, final String expected) {
			final var email = new Email(input);

			assertThat(email.value()).isEqualTo(expected);
		}
	}
}
