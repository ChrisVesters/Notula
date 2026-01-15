package com.cvesters.notula.session.dto;

import com.cvesters.notula.session.bdo.SessionUpdate;

public record SessionUpdateDto(long organisationId) {

	public SessionUpdate toBdo(final long sessionId) {
		return new SessionUpdate(sessionId, organisationId);
	}
}
