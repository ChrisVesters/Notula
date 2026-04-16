package com.cvesters.notula.meeting;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import com.cvesters.notula.common.domain.Principal;
import com.cvesters.notula.common.exception.MissingEntityException;
import com.cvesters.notula.meeting.bdo.MeetingAction;
import com.cvesters.notula.meeting.bdo.MeetingInfo;
import com.cvesters.notula.organisation.TestOrganisation;
import com.cvesters.notula.session.TestSession;

class MeetingServiceTest {

	private final MeetingStorageGateway meetingStorageGateway = mock();
	private final MeetingPublisher meetingPublisher = mock();

	private final MeetingService meetingService = new MeetingService(
			meetingStorageGateway, meetingPublisher);

	@Nested
	class GetById {

		private static final TestSession SESSION = TestSession.EDUARDO_CHRISTIANSEN_SPORER;
		private static final Principal PRINCIPAL = SESSION.principal();

		private static final TestMeeting MEETING = TestMeeting.SPORER_Q2_PLANNING;

		@Test
		void success() {
			final MeetingInfo meetingInfo = MEETING.info();
			when(meetingStorageGateway.findByOrganisationIdAndId(
					SESSION.getOrganisation().getId(), MEETING.getId()))
							.thenReturn(Optional.of(meetingInfo));

			final MeetingInfo result = meetingService.getById(PRINCIPAL,
					MEETING.getId());

			assertThat(result).isEqualTo(meetingInfo);
		}

		@Test
		void notFound() {
			when(meetingStorageGateway.findByOrganisationIdAndId(
					SESSION.getOrganisation().getId(), MEETING.getId()))
							.thenReturn(Optional.empty());

			assertThatThrownBy(
					() -> meetingService.getById(PRINCIPAL, MEETING.getId()))
							.isInstanceOf(MissingEntityException.class);
		}

		@Test
		void noOrganisation() {
			final var principal = TestSession.EDUARDO_CHRISTIANSEN.principal();
			final long meetingId = MEETING.getId();

			assertThatThrownBy(
					() -> meetingService.getById(principal, meetingId))
							.isInstanceOf(IllegalStateException.class);
		}

		@Test
		void principalNull() {
			final long meetingId = MEETING.getId();
			assertThatThrownBy(() -> meetingService.getById(null, meetingId))
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
			final MeetingInfo created = MEETING.info();
			when(meetingStorageGateway.create(argThat(meeting -> {
				assertThatThrownBy(() -> meeting.getId())
						.isInstanceOf(IllegalStateException.class);
				assertThat(meeting.getOrganisationId())
						.isEqualTo(SESSION.getOrganisation().getId());
				assertThat(meeting.getName()).isEqualTo(MEETING.getName());
				return true;
			}))).thenReturn(created);

			final var create = new MeetingAction.Create(MEETING.getName());
			final MeetingInfo result = meetingService
					.create(SESSION.principal(), create);

			assertThat(result).isEqualTo(created);
		}

		@Test
		void organisationNull() {
			final Principal principal = SESSION.principal();
			final MeetingAction.Create meeting = null;

			assertThatThrownBy(() -> meetingService.create(principal, meeting))
					.isInstanceOf(NullPointerException.class);
		}

		@Test
		void principalNull() {
			final Principal principal = null;
			final var create = new MeetingAction.Create(MEETING.getName());

			assertThatThrownBy(() -> meetingService.create(principal, create))
					.isInstanceOf(NullPointerException.class);
		}
	}

	@Nested
	class Update {

		private static final TestSession SESSION = TestSession.EDUARDO_CHRISTIANSEN_SPORER;
		private static final TestMeeting MEETING = TestMeeting.SPORER_PROJECT;

		@Test
		void success() {
			final Principal principal = SESSION.principal();
			final long meetingId = MEETING.getId();
			final MeetingInfo meetingInfo = MEETING.info();
			final MeetingAction.Update action = new MeetingAction.UpdateName(8,
					0, "Status ");

			when(meetingStorageGateway
					.findByOrganisationIdAndId(principal.userId(), meetingId))
							.thenReturn(Optional.of(meetingInfo));

			final MeetingInfo updated = mock();
			when(meetingStorageGateway.update(argThat(info -> {
				assertThat(info.getId()).isEqualTo(MEETING.getId());
				assertThat(info.getOrganisationId()).isEqualTo(MEETING.getOrganisation().getId());
				assertThat(info.getName()).isEqualTo("Project Status Meeting");
				return true;
			}))).thenReturn(updated);

			final MeetingInfo result = meetingService
					.update(SESSION.principal(), meetingId, action);

			assertThat(result).isEqualTo(updated);

			verify(meetingPublisher).publish(argThat(event -> {
				assertThat(event.meetingId()).isEqualTo(MEETING.getId());
				assertThat(event.action()).isEqualTo(action);
				return true;
			}));
		}

		@Test
		void meetingNotFound() {
			final Principal principal = SESSION.principal();
			final long meetingId = MEETING.getId();
			final MeetingAction.Update action = new MeetingAction.UpdateName(2,
					4, "27");

			when(meetingStorageGateway
					.findByOrganisationIdAndId(principal.userId(), meetingId))
							.thenReturn(Optional.empty());

			assertThatThrownBy(() -> meetingService.update(SESSION.principal(),
					meetingId, action))
							.isInstanceOf(MissingEntityException.class);
		}

		@Test
		void principalNull() {
			final long meetingId = MEETING.getId();
			final MeetingAction.Update action = new MeetingAction.UpdateName(2,
					4, "27");

			assertThatThrownBy(
					() -> meetingService.update(null, meetingId, action))
							.isInstanceOf(NullPointerException.class);
		}

		@Test
		void actionNull() {
			final Principal principal = SESSION.principal();
			final long meetingId = MEETING.getId();
			assertThatThrownBy(
					() -> meetingService.update(principal, meetingId, null))
							.isInstanceOf(NullPointerException.class);
		}
	}
}
