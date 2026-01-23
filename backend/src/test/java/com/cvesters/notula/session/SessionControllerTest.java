package com.cvesters.notula.session;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.cvesters.notula.common.exception.DuplicateEntityException;
import com.cvesters.notula.organisation.TestOrganisation;
import com.cvesters.notula.session.bdo.SessionTokens;
import com.cvesters.notula.test.ControllerTest;
import com.cvesters.notula.test.WithSession;
import com.cvesters.notula.user.TestUser;

@WebMvcTest(SessionController.class)
class SessionControllerTest extends ControllerTest {

	private static final String SERVER = "http://localhost";
	private static final String BASE_ENDPOINT = "/api/sessions";

	private static final String REFRESH_TOKEN_COOKIE = "refreshToken";

	private static final TestSession SESSION = TestSession.EDUARDO_CHRISTIANSEN;
	private static final TestUser USER = SESSION.getUser();
	private static final String ACCESS_TOKEN = "access";

	@MockitoBean
	private SessionService sessionService;

	@Nested
	class Create {

		@Test
		void success() throws Exception {
			final var tokens = new SessionTokens(SESSION.info(), ACCESS_TOKEN,
					SESSION.getRefreshToken());
			when(sessionService.create(argThat(login -> {
				assertThat(login.getEmail()).isEqualTo(USER.getEmail());
				assertThat(login.getPassword()).isEqualTo(USER.getPassword());
				return true;
			}))).thenReturn(tokens);

			final String body = getBody(USER);
			final String expectedResponse = getResponse(SESSION, ACCESS_TOKEN);

			final var builder = post(BASE_ENDPOINT).content(body)
					.contentType(MediaType.APPLICATION_JSON);

			mockMvc.perform(builder)
					.andExpect(status().isCreated())
					.andExpect(header().string("location",
							SERVER + BASE_ENDPOINT + "/" + SESSION.getId()))
					.andExpect(content().json(expectedResponse));
			// .andExpectAll(
			// cookie().value(REFRESH_TOKEN_COOKIE,
			// tokens.getRefreshToken()),
			// cookie().httpOnly(REFRESH_TOKEN_COOKIE, true),
			// cookie().secure(REFRESH_TOKEN_COOKIE, true),
			// cookie().path(REFRESH_TOKEN_COOKIE, "/refresh"),
			// cookie().maxAge(REFRESH_TOKEN_COOKIE,
			// (int) Duration.ofDays(7).toMillis()));
		}

		@Test
		void serverError() throws Exception {
			when(sessionService.create(any()))
					.thenThrow(new RuntimeException());

			final String body = getBody(USER);

			final var builder = post(BASE_ENDPOINT).content(body)
					.contentType(MediaType.APPLICATION_JSON);

			mockMvc.perform(builder)
					.andExpect(status().isInternalServerError());
		}

		@Test
		void duplicateEntity() throws Exception {
			when(sessionService.create(any()))
					.thenThrow(new DuplicateEntityException());

			final String body = getBody(USER);

			final var builder = post(BASE_ENDPOINT).content(body)
					.contentType(MediaType.APPLICATION_JSON);

			mockMvc.perform(builder).andExpect(status().isBadRequest());
		}

		@ParameterizedTest
		@NullAndEmptySource
		@ValueSource(strings = { "user.test", "@test", "user@", "     " })
		void emailInvalid(final String email) throws Exception {
			final String body = getBody(email, USER.getPassword().value());

			final var builder = post(BASE_ENDPOINT).content(body)
					.contentType(MediaType.APPLICATION_JSON);

			mockMvc.perform(builder).andExpect(status().isBadRequest());
		}

		@ParameterizedTest
		@NullAndEmptySource
		@ValueSource(strings = { "short" })
		void passwordInvalid(final String password) throws Exception {
			final String body = getBody(USER.getEmail().value(), password);

			final var builder = post(BASE_ENDPOINT).content(body)
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
	}

	@Nested
	@WithSession(TestSession.EDUARDO_CHRISTIANSEN)
	class Update {

		private static final String ENDPOINT = BASE_ENDPOINT + "/{id}";

		@Test
		void success() throws Exception {
			final TestOrganisation organisation = TestOrganisation.SPORER;
			final var tokens = new SessionTokens(SESSION.info(), ACCESS_TOKEN,
					SESSION.getRefreshToken());

			when(sessionService.update(eq(SESSION.principal()), argThat(update -> {
				assertThat(update.sessionId()).isEqualTo(SESSION.getId());
				assertThat(update.organisationId())
						.isEqualTo(organisation.getId());
				return true;
			}))).thenReturn(tokens);

			final String body = getBody(organisation);
			final String expectedResponse = getResponse(SESSION, ACCESS_TOKEN);

			final var builder = put(ENDPOINT, SESSION.getId()).content(body)
					.contentType(MediaType.APPLICATION_JSON);

			mockMvc.perform(builder)
					.andExpect(status().isOk())
					.andExpect(content().json(expectedResponse));
		}

		@Test
		@WithAnonymousUser
		void notAuthenticated() throws Exception {
			final var builder = put(ENDPOINT, SESSION.getId());

			mockMvc.perform(builder).andExpect(status().isUnauthorized());
		}

		private String getBody(final TestOrganisation organisation) {
			return """
					{
						"organisationId": %d
					}
					""".formatted(organisation.getId());
		}
	}

	private String getResponse(final TestSession session,
			final String accessToken) {
		return """
				{
					"id": %d,
					"accessToken": "%s",
					"activeUntil": "%s"
				}
				""".formatted(session.getId(), accessToken,
				session.getActiveUntil().toString());
	}
}
