package com.cvesters.notula.session;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import javax.crypto.SecretKey;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

public class JwtServiceTest {

	@MockitoBean
	private SecretKey secretKey;

	private final JwtService jwtService = new JwtService(secretKey);
	
	@Nested
	class Create {

		@Test
		void sessionNull() {
			assertThatThrownBy(() -> jwtService.create(null))
					.isInstanceOf(NullPointerException.class);
		}
	}

	@Nested
	class Refresh {

	}
}
