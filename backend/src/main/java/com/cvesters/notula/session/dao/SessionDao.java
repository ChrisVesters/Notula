package com.cvesters.notula.session.dao;

import java.time.OffsetDateTime;
import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import org.apache.commons.lang3.Validate;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import com.cvesters.notula.session.bdo.SessionCreate;
import com.cvesters.notula.session.bdo.SessionInfo;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Entity(name = "sessions")
public class SessionDao {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "user_id", nullable = false, updatable = false)
	private long userId;

	@Column(name = "organisation_id", nullable = true)
	private Long organisationId;

	@Column(name = "refresh_token", nullable = false)
	private String refreshToken;

	@Column(name = "active_until", nullable = false)
	private OffsetDateTime activeUntil;

	public SessionDao(final SessionCreate bdo, final String refreshToken) {
		Objects.requireNonNull(bdo);
		Objects.requireNonNull(refreshToken);

		this.userId = bdo.getUserId();
		this.refreshToken = refreshToken;
		this.activeUntil = bdo.getActiveUntil();
	}

	public void update(final SessionInfo info) {
		update(info, this.refreshToken);
	}

	public void update(final SessionInfo info, final String refreshToken) {
		Objects.requireNonNull(info);
		Objects.requireNonNull(refreshToken);
		Validate.isTrue(id == info.getId());

		this.organisationId = info.getOrganisationId().orElse(null);
		this.activeUntil = info.getActiveUntil();
		this.refreshToken = refreshToken;
	}

	public SessionInfo toBdo() {
		Validate.validState(id != null);

		return new SessionInfo(id, userId, organisationId, activeUntil);
	}
}
