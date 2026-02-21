package com.cvesters.notula.meeting;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

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
	class ExistsById {

		private static final TestSession SESSION = TestSession.EDUARDO_CHRISTIANSEN_SPORER;
		private static final Principal PRINCIPAL = SESSION.principal();

		private static final TestMeeting MEETING = TestMeeting.SPORER_Q2_PLANNING;

		@Test
		void found() {
			when(meetingStorageGateway.findById(MEETING.getId()))
					.thenReturn(Optional.of(MEETING.info()));

			assertThat(meetingService.existsById(PRINCIPAL, MEETING.getId()))
					.isTrue();
		}

		@Test
		void notFound() {
			when(meetingStorageGateway.findById(MEETING.getId()))
					.thenReturn(Optional.empty());

			assertThat(meetingService.existsById(PRINCIPAL, MEETING.getId()))
					.isFalse();
		}

		@Test
		void wrongOrganisation() {
			when(meetingStorageGateway.findById(MEETING.getId()))
					.thenReturn(Optional.of(MEETING.info()));

			final var principal = TestSession.ALISON_DACH_GLOVER.principal();

			assertThat(meetingService.existsById(principal, MEETING.getId()))
					.isFalse();
		}

		@Test
		void noOrganisation() {
			when(meetingStorageGateway.findById(MEETING.getId()))
					.thenReturn(Optional.of(MEETING.info()));

			final var principal = TestSession.EDUARDO_CHRISTIANSEN.principal();
			final long meetingId = MEETING.getId();

			assertThatThrownBy(
					() -> meetingService.existsById(principal, meetingId))
							.isInstanceOf(IllegalStateException.class);
		}

		@Test
		void principalNull() {
			final long meetingId = MEETING.getId();
			assertThatThrownBy(() -> meetingService.existsById(null, meetingId))
					.isInstanceOf(NullPointerException.class);
		}

	}

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

	@Nested
	class Create {

		private static final TestSession SESSION = TestSession.EDUARDO_CHRISTIANSEN_SPORER;
		private static final TestMeeting MEETING = TestMeeting.SPORER_PROJECT;

		@Test
		void success() {
			final Principal principal = SESSION.principal();
			final MeetingInfo meeting = mock();

			final MeetingInfo created = MEETING.info();
			when(meetingStorageGateway.create(meeting)).thenReturn(created);

			final MeetingInfo result = meetingService.create(principal,
					meeting);

			assertThat(created).isEqualTo(result);

			verifyNoInteractions(meeting);
		}

		@Test
		void organisationNull() {
			final Principal principal = SESSION.principal();
			final MeetingInfo organisation = null;

			assertThatThrownBy(
					() -> meetingService.create(principal, organisation))
							.isInstanceOf(NullPointerException.class);
		}

		@Test
		void principalNull() {
			final Principal principal = null;
			final var meeting = new MeetingInfo(
					MEETING.getOrganisation().getId(), MEETING.getName());

			assertThatThrownBy(() -> meetingService.create(principal, meeting))
					.isInstanceOf(NullPointerException.class);
		}
	}
}
