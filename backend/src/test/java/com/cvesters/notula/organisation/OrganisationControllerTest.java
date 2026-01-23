package com.cvesters.notula.organisation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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

@WebMvcTest(OrganisationController.class)
@WithSession(TestSession.EDUARDO_CHRISTIANSEN)
class OrganisationControllerTest extends ControllerTest {

	private static final String ENDPOINT = "/api/organisations";

	private static final TestSession SESSION = TestSession.EDUARDO_CHRISTIANSEN;
	private static final TestOrganisation ORGANISATION = TestOrganisation.SPORER;

	@MockitoBean
	private OrganisationService organisationService;

	@Nested
	class GetAll {

		@Test
		void single() throws Exception {
			final Principal principal = SESSION.principal();
			final List<TestOrganisation> organisations = List.of(ORGANISATION);
			final List<OrganisationInfo> info = organisations.stream()
					.map(TestOrganisation::info)
					.collect(Collectors.toList());

			when(organisationService.getAll(principal)).thenReturn(info);

			final String expectedResponse = getResponse(organisations);

			final var builder = get(ENDPOINT);

			mockMvc.perform(builder)
					.andExpect(status().isOk())
					.andExpect(content().json(expectedResponse));
		}

		@Test
		void multiple() throws Exception {
			final Principal principal = SESSION.principal();
			final List<TestOrganisation> organisations = List
					.of(TestOrganisation.SPORER, TestOrganisation.GLOVER);
			final List<OrganisationInfo> info = organisations.stream()
					.map(TestOrganisation::info)
					.collect(Collectors.toList());

			when(organisationService.getAll(principal)).thenReturn(info);

			final String expectedResponse = getResponse(organisations);

			final var builder = get(ENDPOINT);

			mockMvc.perform(builder)
					.andExpect(status().isOk())
					.andExpect(content().json(expectedResponse));
		}

		@Test
		void none() throws Exception {
			final Principal principal = SESSION.principal();
			final List<TestOrganisation> organisations = Collections
					.emptyList();
			final List<OrganisationInfo> info = organisations.stream()
					.map(TestOrganisation::info)
					.collect(Collectors.toList());

			when(organisationService.getAll(principal)).thenReturn(info);

			final var builder = get(ENDPOINT);

			mockMvc.perform(builder)
					.andExpect(status().isOk())
					.andExpect(content().json("[]"));
		}

		@Test
		@WithAnonymousUser
		void unauthorized() throws Exception {
			final var builder = get(ENDPOINT);

			mockMvc.perform(builder).andExpect(status().isUnauthorized());
		}

		private String getResponse(final List<TestOrganisation> organisations) {
			return organisations.stream()
					.map(organisation -> getResponse(organisation))
					.collect(Collectors.joining(",", "[", "]"));
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

	@Nested
	class Create {

		@Test
		void success() throws Exception {
			final Principal principal = SESSION.principal();
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
