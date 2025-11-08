package com.cvesters.notula.session;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.cvesters.notula.common.WebSecurityConfig;
import com.cvesters.notula.common.exception.DuplicateEntityException;
import com.cvesters.notula.user.TestUser;

@WebMvcTest(SessionController.class)
@Import(WebSecurityConfig.class)
class SessionControllerTest {

	private static final String SERVER = "http://localhost";
	private static final String ENDPOINT = "/api/sessions";

	private static final TestUser USER = TestUser.EDUARDO_CHRISTIANSEN;
	// private static final TestSession SESSION;

	@Autowired
	protected MockMvc mockMvc;

	@MockitoBean
	private SessionService sessionService;

	@Nested
	class Create {

		@Test
		void success() throws Exception {
			// TODO: check arguments
			when(sessionService.create(any())).thenReturn(null);

			final String body = getBody(USER);
			final String expectedResponse = getResponse(USER);

			final var builder = post(ENDPOINT).content(body)
					.contentType(MediaType.APPLICATION_JSON);

			mockMvc.perform(builder)
					.andExpect(status().isCreated())
					.andExpect(header().string("location",
							SERVER + ENDPOINT + "/" + USER.getId()))
					.andExpect(content().json(expectedResponse));
		}

		@Test
		void serverError() throws Exception {
			when(sessionService.create(any()))
					.thenThrow(new RuntimeException());

			final String body = getBody(USER);

			final var builder = post(ENDPOINT).content(body)
					.contentType(MediaType.APPLICATION_JSON);

			mockMvc.perform(builder)
					.andExpect(status().isInternalServerError());
		}

		@Test
		void duplicateEntity() throws Exception {
			when(sessionService.create(any()))
					.thenThrow(new DuplicateEntityException());

			final String body = getBody(USER);

			final var builder = post(ENDPOINT).content(body)
					.contentType(MediaType.APPLICATION_JSON);

			mockMvc.perform(builder).andExpect(status().isBadRequest());
		}

		@ParameterizedTest
		@NullAndEmptySource
		@ValueSource(strings = { "user.test", "@test", "user@", "     " })
		void emailInvalid(final String email) throws Exception {
			final String body = getBody(email, USER.getPassword().value());

			final var builder = post(ENDPOINT).content(body)
					.contentType(MediaType.APPLICATION_JSON);

			mockMvc.perform(builder).andExpect(status().isBadRequest());
		}

		@ParameterizedTest
		@NullAndEmptySource
		@ValueSource(strings = { "short" })
		void passwordInvalid(final String password) throws Exception {
			final String body = getBody(USER.getEmail().value(), password);

			final var builder = post(ENDPOINT).content(body)
					.contentType(MediaType.APPLICATION_JSON);

			mockMvc.perform(builder).andExpect(status().isBadRequest());
		}

		private String getBody(final TestUser user) {
			return getBody(user.getEmail().value(), user.getPassword().value());
		}

		private String getBody(final String email, final String password) {
			final String formattedEmail = email == null ? null
					: String.format("\"%s\"", email);
			final String formattedPassword = password == null ? null
					: String.format("\"%s\"", password);

			return """
					{
					    "email": %s,
					    "password": %s
					}
					""".formatted(formattedEmail, formattedPassword);
		}

		private String getResponse(final TestUser user) {
			return "{}";
			// return """
			// {
			// "id": "%s",
			// "email": "%s",
			// "name": "%s"
			// }
			// """.formatted(user.getId(), user.getEmail().value(),
			// user.getName());
		}
	}
}
