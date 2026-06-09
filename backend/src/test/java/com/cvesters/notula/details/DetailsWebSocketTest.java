package com.cvesters.notula.details;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.messaging.simp.stomp.ConnectionLostException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.cvesters.notula.common.domain.Principal;
import com.cvesters.notula.common.exception.MissingEntityException;
import com.cvesters.notula.details.bdo.MeetingDetails;
import com.cvesters.notula.details.bdo.TopicDetails;
import com.cvesters.notula.meeting.TestMeeting;
import com.cvesters.notula.session.TestSession;
import com.cvesters.notula.test.FrameHandler;
import com.cvesters.notula.test.WebSocketTest;
import com.cvesters.notula.topic.TestTopic;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

public class DetailsWebSocketTest extends WebSocketTest {

	private static final String DESTINATION_PREFIX = "/app/meetings/";

	private static final TestSession SESSION = TestSession.EDUARDO_CHRISTIANSEN_SPORER;
	private static final Principal PRINCIPAL = SESSION.principal();
	private static final TestMeeting MEETING = TestMeeting.SPORER_PROJECT;
	private static final List<TestTopic> TOPICS = TestTopic.ofMeeting(MEETING);

	private static final ObjectMapper MAPPER = new ObjectMapper();

	@MockitoBean
	private DetailsService detailsService;

	@Nested
	class Subscribe {

		@Test
		void success() throws Exception {
			final List<TopicDetails> topicDetails = TOPICS.stream()
					.map(TestTopic::info)
					.map(topicInfo -> new TopicDetails(topicInfo,
							Collections.emptyList()))
					.toList();

			final var details = new MeetingDetails(MEETING.info(),
					topicDetails);
			when(detailsService.get(PRINCIPAL, MEETING.getId()))
					.thenReturn(details);

			connect(SESSION);
			final FrameHandler frameHandler = subscribe(
					getDestination(MEETING.getId()));

			final String response = assertThat(frameHandler.getResponse())
					.succeedsWithin(WAIT_TIMEOUT.toSeconds(), TimeUnit.SECONDS)
					.isNotNull()
					.actual();

			final JsonNode responseJson = MAPPER.readTree(response);

			assertThat(responseJson.get("id").asLong())
					.isEqualTo(MEETING.getId());
			assertThat(responseJson.get("name").asString())
					.isEqualTo(MEETING.getName());
			assertThat(responseJson.get("description").asString())
					.isEqualTo(MEETING.getDescription());
			assertThat(responseJson.get("topics").size())
					.isEqualTo(TOPICS.size());

			for (int i = 0; i < TOPICS.size(); i++) {
				final JsonNode topicJson = responseJson.get("topics").get(i);
				assertThat(topicJson.get("id").asLong())
						.isEqualTo(TOPICS.get(i).getId());
				assertThat(topicJson.get("name").asString())
						.isEqualTo(TOPICS.get(i).getName());
				assertThat(topicJson.get("blocks").size()).isZero();
				// TODO: add blocks!
			}
		}

		@Test
		void notFound() throws Exception {
			when(detailsService.get(any(), anyLong()))
					.thenThrow(MissingEntityException.class);

			connect(SESSION);
			final FrameHandler errorFrameHandler = subscribeToErrors();
			subscribe(getDestination(MEETING.getId()));

			assertThat(errorFrameHandler.getResponse())
					.succeedsWithin(WAIT_TIMEOUT.toSeconds(), TimeUnit.SECONDS)
					.isNotNull()
					.isEqualTo("Error");
		}

		@Test
		void unauthenticated() throws Exception {
			connect();
			subscribe(getDestination(MEETING.getId()));

			assertThat(stompSessionHandler.getError())
					.succeedsWithin(WAIT_TIMEOUT.toSeconds(), TimeUnit.SECONDS)
					.isInstanceOf(ConnectionLostException.class);
		}

		private String getDestination(final long meetingId) {
			return DESTINATION_PREFIX + meetingId;
		}

	}
}
