package com.cvesters.notula.session.dto;

import java.util.Objects;

import com.cvesters.notula.session.bdo.SessionTokens;

public record SessionInfoDto(long id, String accessToken, String activeUntil) {
	
	public SessionInfoDto(final SessionTokens sessionTokens) {
		Objects.requireNonNull(sessionTokens);

		final long id = sessionTokens.getId();
		final String accessToken = sessionTokens.getAccessToken();
		final String activeUntil = sessionTokens.getActiveUntil().toString();
		this(id, accessToken, activeUntil);
	}
}
