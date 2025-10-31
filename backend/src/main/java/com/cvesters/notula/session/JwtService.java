package com.cvesters.notula.session;

import java.time.Duration;
import java.util.Objects;

import javax.crypto.SecretKey;

import org.springframework.stereotype.Service;

import com.cvesters.notula.session.bdo.SessionInfo;

// TODO: Rename to TokenService
@Service
public class JwtService {

	private static final Duration ACCESS_EXPIRATION = Duration.ofMinutes(30);
	private static final Duration REFRESH_EXPIRATION = Duration.ofDays(7);

	private final SecretKey jwtSecretKey;

	public JwtService(final SecretKey jwSecretKey) {
		this.jwtSecretKey = jwSecretKey;
	}
	
	public void create(final SessionInfo session) {
		Objects.requireNonNull(session);


	
	}
	// public String generateAccessToken(Authentication auth) {
	// 	final var now = Instant.now();
	// 	return Jwts.builder()
	// 			.subject(auth.getName())
	// 			.claim("roles",
	// 					auth.getAuthorities()
	// 							.stream()
	// 							.map(GrantedAuthority::getAuthority)
	// 							.toList())
	// 			.issuedAt(Date.from(now))
	// 			.expiration(Date.from(now.plus(ACCESS_EXPIRATION)))
	// 			.signWith(jwtSecretKey)
	// 			.compact();
	// }

	// public String generateRefreshToken(Authentication auth) {
	// 	final var random = new SecureRandom();
	// 	byte[] bytes = new byte[64];
    //     random.nextBytes(bytes);

    //     final String token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
	// }

	// public Map<String, String> refreshTokens(String refreshToken) {
	// 	try {
	// 		Claims claims = Jwts.parserBuilder()
	// 				.setSigningKey(SECRET.getBytes())
	// 				.build()
	// 				.parseClaimsJws(refreshToken)
	// 				.getBody();

	// 		if (!"refresh".equals(claims.get("type"))) {
	// 			throw new RuntimeException("Not a refresh token");
	// 		}

	// 		String username = claims.getSubject();
	// 		// optionally check if user still valid
	// 		Authentication auth = new UsernamePasswordAuthenticationToken(
	// 				username, null, List.of());

	// 		String newAccess = generateAccessToken(auth);
	// 		String newRefresh = generateRefreshToken(auth);

	// 		return Map.of("access_token", newAccess, "refresh_token",
	// 				newRefresh);
	// 	} catch (JwtException e) {
	// 		throw new RuntimeException("Invalid refresh token");
	// 	}
	// }
}
