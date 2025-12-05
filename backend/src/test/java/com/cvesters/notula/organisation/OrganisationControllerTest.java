package com.cvesters.notula.organisation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
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
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.cvesters.notula.common.domain.Principal;
import com.cvesters.notula.organisation.bdo.OrganisationInfo;
import com.cvesters.notula.session.TestSession;
import com.cvesters.notula.test.ControllerTest;
import com.cvesters.notula.test.WithSession;
import com.cvesters.notula.user.TestUser;

@WebMvcTest(OrganisationController.class)
@WithSession(TestSession.EDUARDO_CHRISTIANSEN_DEKSTOP)
class OrganisationControllerTest extends ControllerTest {

	private static final String ENDPOINT = "/api/organisations";

	private static final TestSession SESSION = TestSession.EDUARDO_CHRISTIANSEN_DEKSTOP;
	// TODO: organisation to be taken from session.
	private static final TestOrganisation ORGANISATION = TestOrganisation.SPORER;
	private static final TestUser USER = SESSION.getUser();

	@MockitoBean
	private OrganisationService organisationService;

	@Nested
	class Create {

		@Test
		void success() throws Exception {
			final Principal principal = USER.principal();
			final OrganisationInfo info = ORGANISATION.info();

			when(organisationService.create(eq(principal), argThat(org -> {
				assertThat(org.getId()).isNull();
				assertThat(org.getName()).isEqualTo(ORGANISATION.getName());
				return true;
			}))).thenReturn(info);

			final String body = getBody(ORGANISATION);
			final String expectedResponse = getResponse(ORGANISATION);

			final var builder = post(ENDPOINT).content(body)
					.contentType(MediaType.APPLICATION_JSON);

			mockMvc.perform(builder)
					.andExpect(status().isCreated())
					.andExpect(header().string("location",
							getUrl(ENDPOINT + "/" + ORGANISATION.getId())))
					.andExpect(content().json(expectedResponse));
		}

		@Test
		@WithAnonymousUser
		void unauthorized() throws Exception {
			final var builder = post(ENDPOINT).content(getBody(ORGANISATION))
					.contentType(MediaType.APPLICATION_JSON);

			mockMvc.perform(builder).andExpect(status().isUnauthorized());
		}

		@Test
		void serverError() throws Exception {
			when(organisationService.create(any(), any()))
					.thenThrow(new RuntimeException());

			final String body = getBody(ORGANISATION);

			final var builder = post(ENDPOINT).content(body)
					.contentType(MediaType.APPLICATION_JSON);

			mockMvc.perform(builder)
					.andExpect(status().isInternalServerError());
		}

		@ParameterizedTest
		@NullAndEmptySource
		@ValueSource(strings = { "     " })
		void nameInvalid(final String name) throws Exception {
			final String body = getBody(name);

			final var builder = post(ENDPOINT).content(body)
					.contentType(MediaType.APPLICATION_JSON);

			mockMvc.perform(builder).andExpect(status().isBadRequest());
		}

		private String getBody(final TestOrganisation organisation) {
			return getBody(organisation.getName());
		}

		private String getBody(final String name) {
			final String formattedName = Optional.ofNullable(name)
					.map(n -> String.format("\"%s\"", n))
					.orElse(null);

			return """
					{
					    "name": %s
					}
					""".formatted(formattedName);
		}

		private String getResponse(final TestOrganisation organisation) {
			return """
					{
						"id": %s,
						"name": "%s"
					}
					""".formatted(organisation.getId(), organisation.getName());
		}
	}
}
