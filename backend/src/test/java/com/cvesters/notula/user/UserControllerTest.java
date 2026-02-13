package com.cvesters.notula.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Optional;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.cvesters.notula.common.exception.DuplicateEntityException;
import com.cvesters.notula.test.ControllerTest;

@WebMvcTest(UserController.class)
class UserControllerTest extends ControllerTest {

	private static final String SERVER = "http://localhost";
	private static final String ENDPOINT = "/api/users";

	private static final TestUser USER = TestUser.EDUARDO_CHRISTIANSEN;

	@MockitoBean
	private UserService userService;

	@Nested
	class Create {

		@Test
		void success() throws Exception {
			when(userService.createUser(argThat(bdo -> {
				assertThat(bdo.getEmail()).isEqualTo(USER.getEmail());
				assertThat(bdo.getPassword()).isEqualTo(USER.getPassword());
				return true;
			}))).thenReturn(USER.info());

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
			when(userService.createUser(any()))
					.thenThrow(new RuntimeException());

			final String body = getBody(USER);

			final var builder = post(ENDPOINT).content(body)
					.contentType(MediaType.APPLICATION_JSON);

			mockMvc.perform(builder)
					.andExpect(status().isInternalServerError());
		}

		@Test
		void duplicateEntity() throws Exception {
			when(userService.createUser(any()))
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

		@Test
		void emailMissing() throws Exception {
			final String body = """
					{
						"password": "%s"
					}
					""".formatted(USER.getPassword().value());

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

		@Test
		void passwordMissing() throws Exception {
			final String body = """
					{
						"email": "%s"
					}
					""".formatted(USER.getEmail().value());

			final var builder = post(ENDPOINT).content(body)
					.contentType(MediaType.APPLICATION_JSON);

			mockMvc.perform(builder).andExpect(status().isBadRequest());
		}

		private String getBody(final TestUser user) {
			return getBody(user.getEmail().value(), user.getPassword().value());
		}

		private String getBody(final String email, final String password) {
			final String formattedEmail = Optional.ofNullable(email)
					.map(v -> String.format("\"%s\"", v))
					.orElse(null);
			final String formattedPassword = Optional.ofNullable(password)
					.map(v -> String.format("\"%s\"", v))
					.orElse(null);

			return """
					{
						"email": %s,
						"password": %s
					}
					""".formatted(formattedEmail, formattedPassword);
		}

		private String getResponse(final TestUser user) {
			final String formattedEmail = Optional
					.ofNullable(user.getEmail().value())
					.map(v -> String.format("\"%s\"", v))
					.orElse(null);

			return """
					{
						"id": %s,
						"email": %s
					}
					""".formatted(user.getId(), formattedEmail);
		}
	}
}
