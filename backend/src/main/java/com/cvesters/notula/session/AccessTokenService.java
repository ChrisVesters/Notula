package com.cvesters.notula.session;

import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.Objects;

import javax.crypto.SecretKey;

import org.springframework.stereotype.Service;

import io.jsonwebtoken.Jwts;

import com.cvesters.notula.session.bdo.SessionInfo;

@Service
public class AccessTokenService {

	private static final Duration ACCESS_EXPIRATION = Duration.ofMinutes(30);

	private final SecretKey jwtSecretKey;

	public AccessTokenService(final SecretKey jwSecretKey) {
		this.jwtSecretKey = jwSecretKey;
	}

	public String create(final SessionInfo session) {
		Objects.requireNonNull(session);

		final var now = Instant.now();

		return Jwts.builder()
				.subject(String.valueOf(session.getUserId()))
				.issuedAt(Date.from(now))
				.expiration(Date.from(now.plus(ACCESS_EXPIRATION)))
				.signWith(jwtSecretKey)
				.compact();
	}
}
