package com.cvesters.notula.credential.bdo;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

public class CredentialInfoTest {

	@Nested
	class Constructor {

		@Test
		void success() {
			final var result = new CredentialInfo(1L, 2L);

			assertThat(result.getId()).isEqualTo(1L);
			assertThat(result.getUserId()).isEqualTo(2L);
		}
	}
}
