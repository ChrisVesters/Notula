package com.cvesters.notula.common.controller;

import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.handler.annotation.support.MethodArgumentNotValidException;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.web.bind.annotation.ControllerAdvice;

import lombok.extern.slf4j.Slf4j;

import com.cvesters.notula.common.domain.ChangeId;
import com.cvesters.notula.common.dto.RejectedDto;
import com.cvesters.notula.common.exception.BusyEntityException;
import com.cvesters.notula.common.exception.InvalidActionException;
import com.cvesters.notula.common.exception.MissingEntityException;

@Slf4j
@ControllerAdvice
public class WebSocketExceptionHandler {

	public static final String DESTINATION = "/queue/rejections";

	@MessageExceptionHandler(MissingEntityException.class)
	@SendToUser(destinations = DESTINATION, broadcast = false)
	public RejectedDto handleMissing(final ChangeId change) {
		return RejectedDto.permanent(change, "It could not be found.");
	}

	@MessageExceptionHandler(BusyEntityException.class)
	@SendToUser(destinations = DESTINATION, broadcast = false)
	public RejectedDto handleBusy(final ChangeId change) {
		return RejectedDto.temporary(change,
				"Someone else was changing it. Try again.");
	}

	@MessageExceptionHandler({ InvalidActionException.class,
			IllegalArgumentException.class,
			MethodArgumentNotValidException.class })
	@SendToUser(destinations = DESTINATION, broadcast = false)
	public RejectedDto handleInvalid(final ChangeId change) {
		return RejectedDto.permanent(change, "It was not valid.");
	}

	@MessageExceptionHandler(Exception.class)
	@SendToUser(destinations = DESTINATION, broadcast = false)
	public RejectedDto handle(final ChangeId change, final Exception ex) {
		log.error("Unexpected exception", ex);

		return RejectedDto.permanent(change,
				"The request could not be handled.");
	}
}
