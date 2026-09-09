package com.cvesters.notula.meeting;

import jakarta.validation.Valid;

import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;

import com.cvesters.notula.block.BlockService;
import com.cvesters.notula.common.domain.Origin;
import com.cvesters.notula.meeting.dto.BlockChangeDto;
import com.cvesters.notula.meeting.dto.ChangeDto;
import com.cvesters.notula.meeting.dto.MeetingChangeDto;
import com.cvesters.notula.meeting.dto.TextBlockChangeDto;
import com.cvesters.notula.meeting.dto.TopicChangeDto;
import com.cvesters.notula.textblock.TextBlockService;
import com.cvesters.notula.topic.TopicService;

@Controller
public class MeetingWebSocket {

	private final MeetingService meetings;
	private final TopicService topics;
	private final BlockService blocks;
	private final TextBlockService texts;

	public MeetingWebSocket(final MeetingService meetings,
			final TopicService topics, final BlockService blocks,
			final TextBlockService texts) {
		this.meetings = meetings;
		this.topics = topics;
		this.blocks = blocks;
		this.texts = texts;
	}

	@MessageMapping("/meetings/{id}/changes")
	public void submit(final Origin origin, @DestinationVariable final long id,
			@Payload @Valid final ChangeDto change) {
		switch (change) {
			case MeetingChangeDto c -> meetings.update(origin, id, c.toBdo());

			case TopicChangeDto.Add c -> topics.create(origin, id, c.toBdo());
			case TopicChangeDto.Move c -> topics.move(origin, id, c.topic(),
					c.toBdo());
			case TopicChangeDto.Update c -> topics.update(origin, id,
					c.topic(), c.toBdo());
			case TopicChangeDto.Remove c -> topics.delete(origin, id,
					c.topic());

			case BlockChangeDto.Add c -> blocks.create(origin, id, c.toBdo());
			case BlockChangeDto.Move c -> blocks.move(origin, id, c.block(),
					c.toBdo());
			case BlockChangeDto.Remove c -> blocks.delete(origin, id,
					c.block());

			case TextBlockChangeDto.Edit c -> texts.update(origin, id,
					c.block(), c.toBdo());
		}
	}
}
