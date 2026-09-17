package com.cvesters.notula.meeting;

import jakarta.validation.Valid;

import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;

import com.cvesters.notula.common.domain.ChangeId;
import com.cvesters.notula.common.domain.Origin;
import com.cvesters.notula.common.dto.AcknowledgedDto;
import com.cvesters.notula.meeting.bdo.MeetingScope;
import com.cvesters.notula.meeting.dto.ChangeDto;

@Controller
public class MeetingWebSocket {

	public static final String ACKNOWLEDGEMENTS = "/queue/acks";

	private final ChangeService changes;

	public MeetingWebSocket(final ChangeService changes) {
		this.changes = changes;
	}

	@MessageMapping("/meetings/{id}/changes")
	@SendToUser(destinations = ACKNOWLEDGEMENTS, broadcast = false)
	public AcknowledgedDto submit(final Origin origin, final ChangeId changeId,
			@DestinationVariable final long id,
			@Payload @Valid final ChangeDto change) {
		final MeetingScope scope = changes.apply(origin, id, change);

		return AcknowledgedDto.of(changeId, scope.revision());
	}
}
