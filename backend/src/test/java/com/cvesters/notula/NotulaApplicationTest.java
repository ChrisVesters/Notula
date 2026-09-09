package com.cvesters.notula;

import static org.mockito.Mockito.mockStatic;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.boot.SpringApplication;

class NotulaApplicationTest {

	@Test
	void main() {
		final String[] args = { "--server.port=0" };

		try (MockedStatic<SpringApplication> application = mockStatic(
				SpringApplication.class)) {
			NotulaApplication.main(args);

			application.verify(() -> SpringApplication
					.run(NotulaApplication.class, args));
		}
	}
}
