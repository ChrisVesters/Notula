package com.cvesters.notula.meeting;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.cvesters.notula.common.domain.Principal;
import com.cvesters.notula.meeting.bdo.MeetingInfo;
import com.cvesters.notula.session.TestSession;
import com.cvesters.notula.test.ControllerTest;
import com.cvesters.notula.test.WithSession;

@WebMvcTest(MeetingController.class)
@WithSession(TestSession.EDUARDO_CHRISTIANSEN_SPORER)
class MeetingControllerTest extends ControllerTest {

	private static final String ENDPOINT = "/api/meetings";

	private static final TestSession SESSION = TestSession.EDUARDO_CHRISTIANSEN_SPORER;

	@MockitoBean
	private MeetingService meetingService;

	@Nested
	class GetAll {

		@ParameterizedTest
		@MethodSource("cases")
		void success(final List<TestMeeting> meetings) throws Exception {
			final Principal principal = SESSION.principal();
			final List<MeetingInfo> info = meetings.stream()
					.map(TestMeeting::info)
					.toList();

			when(meetingService.getAll(principal)).thenReturn(info);

			final String expectedResponse = getResponse(meetings);

			final var builder = get(ENDPOINT);

			mockMvc.perform(builder)
					.andExpect(status().isOk())
					.andExpect(content().json(expectedResponse));
		}

		@Test
		@WithAnonymousUser
		void unauthenticated() throws Exception {
			final var builder = get(ENDPOINT);

			mockMvc.perform(builder).andExpect(status().isUnauthorized());
		}

		@Test
		@WithSession(TestSession.EDUARDO_CHRISTIANSEN)
		void withoutOrganisation() throws Exception {
			final var builder = get(ENDPOINT);

			mockMvc.perform(builder).andExpect(status().isForbidden());
		}

		private String getResponse(final List<TestMeeting> organisations) {
			return organisations.stream()
					.map(this::getResponse)
					.collect(Collectors.joining(",", "[", "]"));
		}

		private String getResponse(final TestMeeting meeting) {
			return """
					{
						"id": %s,
						"name": "%s"
					}
					""".formatted(meeting.getId(), meeting.getName());
		}

		public static List<List<TestMeeting>> cases() {
			return List.of(Collections.emptyList(),
					List.of(TestMeeting.SPORER_PROJECT),
					List.of(TestMeeting.SPORER_PROJECT,
							TestMeeting.SPORER_RETRO,
							TestMeeting.SPORER_Q2_PLANNING));
		}
	}

	// @Nested
	// class Create {

	// @Test
	// void success() throws Exception {
	// final Principal principal = SESSION.principal();
	// final OrganisationInfo info = ORGANISATION.info();

	// when(organisationService.create(eq(principal), argThat(org -> {
	// assertThat(org.getId()).isNull();
	// assertThat(org.getName()).isEqualTo(ORGANISATION.getName());
	// return true;
	// }))).thenReturn(info);

	// final String body = getBody(ORGANISATION);
	// final String expectedResponse = getResponse(ORGANISATION);

	// final var builder = post(ENDPOINT).content(body)
	// .contentType(MediaType.APPLICATION_JSON);

	// mockMvc.perform(builder)
	// .andExpect(status().isCreated())
	// .andExpect(header().string("location",
	// getUrl(ENDPOINT + "/" + ORGANISATION.getId())))
	// .andExpect(content().json(expectedResponse));
	// }

	// @Test
	// @WithAnonymousUser
	// void unauthorized() throws Exception {
	// final var builder = post(ENDPOINT).content(getBody(ORGANISATION))
	// .contentType(MediaType.APPLICATION_JSON);

	// mockMvc.perform(builder).andExpect(status().isUnauthorized());
	// }

	// @Test
	// void serverError() throws Exception {
	// when(organisationService.create(any(), any()))
	// .thenThrow(new RuntimeException());

	// final String body = getBody(ORGANISATION);

	// final var builder = post(ENDPOINT).content(body)
	// .contentType(MediaType.APPLICATION_JSON);

	// mockMvc.perform(builder)
	// .andExpect(status().isInternalServerError());
	// }

	// @ParameterizedTest
	// @NullAndEmptySource
	// @ValueSource(strings = { " " })
	// void nameInvalid(final String name) throws Exception {
	// final String body = getBody(name);

	// final var builder = post(ENDPOINT).content(body)
	// .contentType(MediaType.APPLICATION_JSON);

	// mockMvc.perform(builder).andExpect(status().isBadRequest());
	// }

	// private String getBody(final TestOrganisation organisation) {
	// return getBody(organisation.getName());
	// }

	// private String getBody(final String name) {
	// final String formattedName = Optional.ofNullable(name)
	// .map(n -> String.format("\"%s\"", n))
	// .orElse(null);

	// return """
	// {
	// "name": %s
	// }
	// """.formatted(formattedName);
	// }

	// private String getResponse(final TestOrganisation organisation) {
	// return """
	// {
	// "id": %s,
	// "name": "%s"
	// }
	// """.formatted(organisation.getId(), organisation.getName());
	// }
	// }
}
