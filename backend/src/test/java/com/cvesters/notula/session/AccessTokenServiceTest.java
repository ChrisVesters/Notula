package com.cvesters.notula.session;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;

import java.time.Duration;
import java.time.Instant;
import java.util.Base64;

import javax.crypto.Mac;
import javax.crypto.SecretKey;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import io.jsonwebtoken.security.Keys;

import com.cvesters.notula.session.bdo.SessionInfo;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.json.JsonMapper;

class AccessTokenServiceTest {

	private static final TestSession SESSION = TestSession.EDUARDO_CHRISTIANSEN_DEKSTOP;

	private final SecretKey secretKey = Keys
			.hmacShaKeyFor("SECRET".repeat(10).getBytes());

	private final AccessTokenService accessTokenService = new AccessTokenService(
			secretKey);

	@Nested
	class Create {

		@Test
		void success() throws Exception {
			final SessionInfo session = SESSION.info();
			
			final var decoder = Base64.getUrlDecoder();
			final var mapper = new JsonMapper();
			final var now = Instant.now();

			final String token = accessTokenService.create(session);

			assertThat(token).isNotNull();

			final String[] parts = token.split("\\.");
			assertThat(parts).hasSize(3);
			
			final String header = new String(decoder.decode(parts[0]));
			final JsonNode headerRootNode = mapper.readTree(header);
			assertThat(headerRootNode.get("alg").asText()).isEqualTo("HS384");
			
			final String payload = new String(decoder.decode(parts[1]));
			final JsonNode payloadRootNode = mapper.readTree(payload);
			assertThat(payloadRootNode.get("sub").asText())
			.isEqualTo(String.valueOf(SESSION.getUser().getId()));
			final long iat = payloadRootNode.get("iat").asLong();
			assertThat(Instant.ofEpochSecond(iat)).isCloseTo(now,
					within(Duration.ofSeconds(1)));
			final long exp = payloadRootNode.get("exp").asLong();
			assertThat(Instant.ofEpochSecond(exp)).isCloseTo(
					now.plus(Duration.ofMinutes(30)),
					within(Duration.ofSeconds(1)));

			final byte[] signature = decoder.decode(parts[2]);
			final var hashing = Mac.getInstance("HmacSHA384");
			hashing.init(secretKey);
			hashing.update((parts[0] + "." + parts[1]).getBytes());
			final byte[] expectedSignature = hashing.doFinal();
			assertThat(signature).isEqualTo(expectedSignature);
		}

		@Test
		void sessionNull() {
			assertThatThrownBy(() -> accessTokenService.create(null))
					.isInstanceOf(NullPointerException.class);
		}
	}
}
