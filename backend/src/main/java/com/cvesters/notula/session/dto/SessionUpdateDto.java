package com.cvesters.notula.session.dto;

import com.cvesters.notula.session.bdo.SessionUpdate;

public record SessionUpdateDto(long organisationId) {

	public SessionUpdate toBdo() {
		return new SessionUpdate(organisationId);
	}
}
