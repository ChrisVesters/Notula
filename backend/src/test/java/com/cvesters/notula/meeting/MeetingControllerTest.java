package com.cvesters.notula.meeting;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
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
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
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

		public static List<List<TestMeeting>> cases() {
			return List.of(Collections.emptyList(),
					List.of(TestMeeting.SPORER_PROJECT),
					List.of(TestMeeting.SPORER_PROJECT,
							TestMeeting.SPORER_RETRO,
							TestMeeting.SPORER_Q2_PLANNING));
		}
	}

	@Nested
	class Create {

		private static final TestMeeting MEETING = TestMeeting.SPORER_RETRO;

		@Test
		void success() throws Exception {
			final Principal principal = SESSION.principal();
			final MeetingInfo info = MEETING.info();

			when(meetingService.create(eq(principal), argThat(meeting -> {
				assertThatThrownBy(() -> meeting.getId())
						.isInstanceOf(IllegalStateException.class);
				assertThat(meeting.getOrganisationId())
						.isEqualTo(MEETING.getOrganisation().getId());
				assertThat(meeting.getName()).isEqualTo(MEETING.getName());
				return true;
			}))).thenReturn(info);

			final String body = getBody(MEETING);
			final String expectedResponse = getResponse(MEETING);

			final var builder = post(ENDPOINT).content(body)
					.contentType(MediaType.APPLICATION_JSON);

			mockMvc.perform(builder)
					.andExpect(status().isCreated())
					.andExpect(header().string("location",
							getUrl(ENDPOINT + "/" + MEETING.getId())))
					.andExpect(content().json(expectedResponse));
		}

		@Test
		@WithAnonymousUser
		void unauthorized() throws Exception {
			final var builder = post(ENDPOINT).content(getBody(MEETING))
					.contentType(MediaType.APPLICATION_JSON);

			mockMvc.perform(builder).andExpect(status().isUnauthorized());
		}

		@Test
		void serverError() throws Exception {
			when(meetingService.create(any(), any()))
					.thenThrow(new RuntimeException());

			final String body = getBody(MEETING);

			final var builder = post(ENDPOINT).content(body)
					.contentType(MediaType.APPLICATION_JSON);

			mockMvc.perform(builder)
					.andExpect(status().isInternalServerError());
		}

		@ParameterizedTest
		@NullAndEmptySource
		@ValueSource(strings = { " " })
		void nameInvalid(final String name) throws Exception {
			final String body = getBody(name);

			final var builder = post(ENDPOINT).content(body)
					.contentType(MediaType.APPLICATION_JSON);

			mockMvc.perform(builder).andExpect(status().isBadRequest());
		}

		private String getBody(final TestMeeting meeting) {
			return getBody(meeting.getName());
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
	}

	private static String getResponse(final List<TestMeeting> organisations) {
		return organisations.stream()
				.map(MeetingControllerTest::getResponse)
				.collect(Collectors.joining(",", "[", "]"));
	}

	private static String getResponse(final TestMeeting meeting) {
		return """
				{
					"id": %s,
					"name": "%s"
				}
				""".formatted(meeting.getId(), meeting.getName());
	}
}
