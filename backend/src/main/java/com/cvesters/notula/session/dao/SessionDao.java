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
import lombok.Setter;

import com.cvesters.notula.session.bdo.SessionCreate;
import com.cvesters.notula.session.bdo.SessionInfo;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Entity(name = "sessions")
public class SessionDao {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Setter(value = AccessLevel.PRIVATE)
	private Long id;

	@Setter(value = AccessLevel.PRIVATE)
	@Column(name = "user_id", nullable = false)
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

	public SessionInfo toBdo() {
		Validate.validState(id != null);

		return new SessionInfo(id, userId, organisationId, activeUntil);
	}
}
