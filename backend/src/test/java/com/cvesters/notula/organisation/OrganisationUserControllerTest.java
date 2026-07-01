package com.cvesters.notula.organisation;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.cvesters.notula.common.domain.Principal;
import com.cvesters.notula.organisation.bdo.OrganisationUserInfo;
import com.cvesters.notula.session.TestSession;
import com.cvesters.notula.test.ControllerTest;
import com.cvesters.notula.test.WithSession;

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
			final List<OrganisationUserInfo> info = users.stream()
					.map(TestOrganisationUser::info)
					.toList();

			when(organisationUserService.getAllForOrganisation(principal))
					.thenReturn(info);

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
			final List<OrganisationUserInfo> info = users.stream()
					.map(TestOrganisationUser::info)
					.toList();

			when(organisationUserService.getAllForOrganisation(principal))
					.thenReturn(info);

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
			final List<OrganisationUserInfo> info = users.stream()
					.map(TestOrganisationUser::info)
					.toList();

			when(organisationUserService.getAllForOrganisation(principal))
					.thenReturn(info);

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
					"userId": %s
				}
				""".formatted(orgUser.getId(),
				orgUser.getOrganisation().getId(), orgUser.getUser().getId());
	}
}
