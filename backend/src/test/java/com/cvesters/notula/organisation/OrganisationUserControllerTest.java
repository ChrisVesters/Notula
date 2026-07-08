package com.cvesters.notula.organisation;

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
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.cvesters.notula.common.domain.Principal;
import com.cvesters.notula.organisation.bdo.OrganisationUserAction;
import com.cvesters.notula.organisation.bdo.OrganisationUserInfo;
import com.cvesters.notula.organisation.bdo.OrganisationUserView;
import com.cvesters.notula.session.TestSession;
import com.cvesters.notula.test.ControllerTest;
import com.cvesters.notula.test.WithSession;
import com.cvesters.notula.user.TestUser;

@WebMvcTest(OrganisationUserController.class)
@WithSession(TestSession.EDUARDO_CHRISTIANSEN_SPORER)
class OrganisationUserControllerTest extends ControllerTest {

	private static final String BASE_ENDPOINT = "/api/organisation-users";

	private static final TestSession SESSION = TestSession.EDUARDO_CHRISTIANSEN_SPORER;

	@MockitoBean
	private OrganisationUserService organisationUserService;

	@Nested
	class GetAll {

		@Test
		void single() throws Exception {
			final Principal principal = SESSION.principal();
			final List<TestOrganisationUser> users = List
					.of(TestOrganisationUser.SPORER_EDUARDO_CHRISTIANSEN);
			final List<OrganisationUserView> view = users.stream()
					.map(TestOrganisationUser::view)
					.toList();

			when(organisationUserService.getAllForOrganisation(principal))
					.thenReturn(view);

			final String expectedResponse = getResponse(users);

			final var builder = get(BASE_ENDPOINT);

			mockMvc.perform(builder)
					.andExpect(status().isOk())
					.andExpect(content().json(expectedResponse));
		}

		@Test
		void multiple() throws Exception {
			final Principal principal = SESSION.principal();
			final List<TestOrganisationUser> users = TestOrganisationUser
					.ofOrganisation(TestOrganisation.SPORER);
			final List<OrganisationUserView> view = users.stream()
					.map(TestOrganisationUser::view)
					.toList();

			when(organisationUserService.getAllForOrganisation(principal))
					.thenReturn(view);

			final String expectedResponse = getResponse(users);

			final var builder = get(BASE_ENDPOINT);

			mockMvc.perform(builder)
					.andExpect(status().isOk())
					.andExpect(content().json(expectedResponse));
		}

		@Test
		void none() throws Exception {
			final Principal principal = SESSION.principal();
			final List<TestOrganisationUser> users = Collections.emptyList();
			final List<OrganisationUserView> view = users.stream()
					.map(TestOrganisationUser::view)
					.toList();

			when(organisationUserService.getAllForOrganisation(principal))
					.thenReturn(view);

			final var builder = get(BASE_ENDPOINT);

			mockMvc.perform(builder)
					.andExpect(status().isOk())
					.andExpect(content().json("[]"));
		}

		@Test
		@WithAnonymousUser
		void unauthorized() throws Exception {
			final var builder = get(BASE_ENDPOINT);

			mockMvc.perform(builder).andExpect(status().isUnauthorized());
		}
	}

	@Nested
	class Create {

		private static final TestOrganisationUser ORG_USER = TestOrganisationUser.SPORER_KRISTINA_THIEL;
		private static final TestUser USER = ORG_USER.getUser();

		@Test
		void success() throws Exception {
			final Principal principal = SESSION.principal();
			final OrganisationUserInfo info = ORG_USER.info();

			final var expected = new OrganisationUserAction.Create(
					USER.getEmail());
			final var matcher = new OrganisationUserActionMatcher.Create(
					expected);
			when(organisationUserService.create(eq(principal),
					argThat(matcher::matches))).thenReturn(info);

			final String body = getBody(ORG_USER);
			final String expectedResponse = getResponse(ORG_USER);

			final var builder = post(BASE_ENDPOINT).content(body)
					.contentType(MediaType.APPLICATION_JSON);

			mockMvc.perform(builder)
					.andExpect(status().isCreated())
					.andExpect(header().string("location",
							getUrl(BASE_ENDPOINT + "/" + ORG_USER.getId())))
					.andExpect(content().json(expectedResponse));
		}

		@Test
		@WithAnonymousUser
		void unauthorized() throws Exception {
			final var builder = post(BASE_ENDPOINT).content(getBody(ORG_USER))
					.contentType(MediaType.APPLICATION_JSON);

			mockMvc.perform(builder).andExpect(status().isUnauthorized());
		}

		@Test
		void serverError() throws Exception {
			when(organisationUserService.create(any(), any()))
					.thenThrow(new RuntimeException());

			final String body = getBody(ORG_USER);

			final var builder = post(BASE_ENDPOINT).content(body)
					.contentType(MediaType.APPLICATION_JSON);

			mockMvc.perform(builder)
					.andExpect(status().isInternalServerError());
		}

		private String getBody(final TestOrganisationUser user) {
			return getBody(user.getUser().getEmail().value());
		}

		private String getBody(final String email) {
			final String formattedEmail = Optional.ofNullable(email)
					.map(n -> String.format("\"%s\"", n))
					.orElse(null);

			return """
					{
						"email": %s
					}
					""".formatted(formattedEmail);
		}

		private String getResponse(final TestOrganisationUser orgUser) {
			return """
					{
						"id": %s,
						"organisationId": %s,
						"userId": %s
					}
					""".formatted(orgUser.getId(),
					orgUser.getOrganisation().getId(),
					orgUser.getUser().getId());
		}
	}

	private String getResponse(final List<TestOrganisationUser> orgUsers) {
		return orgUsers.stream()
				.map(this::getResponse)
				.collect(Collectors.joining(",", "[", "]"));
	}

	private String getResponse(final TestOrganisationUser orgUser) {
		return """
				{
					"id": %s,
					"organisationId": %s,
					"userId": %s,
					"email": "%s"
				}
				""".formatted(orgUser.getId(),
				orgUser.getOrganisation().getId(), orgUser.getUser().getId(),
				orgUser.getUser().getEmail().value());
	}
}
