package com.cvesters.notula.session.dao;

import java.time.OffsetDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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

	@Column(name = "refresh_token", unique = true, nullable = false)
	private String refreshToken;

	@Column(name = "active_until", nullable = false)
	private OffsetDateTime activeUntil;

	public SessionDao(final long userId, final String refreshToken,
			final OffsetDateTime activeUntil) {
		this.userId = userId;
		this.refreshToken = refreshToken;
		this.activeUntil = activeUntil;
	}

	public SessionInfo toBdo() {
		return new SessionInfo(id, userId, refreshToken, activeUntil);
	}
}
