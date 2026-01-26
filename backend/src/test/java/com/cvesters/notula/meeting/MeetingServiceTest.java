package com.cvesters.notula.meeting;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import com.cvesters.notula.common.domain.Principal;
import com.cvesters.notula.meeting.bdo.MeetingInfo;
import com.cvesters.notula.organisation.TestOrganisation;
import com.cvesters.notula.session.TestSession;

class MeetingServiceTest {

	private final MeetingStorageGateway meetingStorageGateway = mock();

	private final MeetingService meetingService = new MeetingService(
			meetingStorageGateway);

	@Nested
	class GetAll {

		@ParameterizedTest
		@MethodSource("cases")
		void success(final List<TestMeeting> meetings) {
			final TestSession session = TestSession.EDUARDO_CHRISTIANSEN_SPORER;
			final Principal principal = session.principal();
			final TestOrganisation organisation = session.getOrganisation();

			final List<MeetingInfo> info = meetings.stream()
					.map(TestMeeting::info)
					.toList();

			when(meetingStorageGateway
					.findAllByOrganisationId(organisation.getId()))
							.thenReturn(info);

			final List<MeetingInfo> result = meetingService.getAll(principal);

			assertThat(result).isEqualTo(info);

		}

		@Test
		void principalNull() {
			assertThatThrownBy(() -> meetingService.getAll(null))
					.isInstanceOf(NullPointerException.class);
		}

		@Test
		void principalWithoutOrganisation() {
			final Principal prinicpal = TestSession.ALISON_DACH.principal();
			assertThatThrownBy(() -> meetingService.getAll(prinicpal))
					.isInstanceOf(IllegalStateException.class);
		}

		private static List<List<TestMeeting>> cases() {
			return List.of(List.of(), List.of(TestMeeting.SPORER_PROJECT,
					TestMeeting.SPORER_RETRO));
		}
	}
}
